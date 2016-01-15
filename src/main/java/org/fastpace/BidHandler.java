package org.fastpace;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fastpace.App.SessionInfo;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

class BidHandler implements HttpHandler {

	private static final String SESSION_KEY = "sessionkey";
	public static final Pattern PATTERN_LOGIN = Pattern.compile("/(.+)/login");
	public static final Pattern PATTERN_ITEMBID = Pattern.compile("/(.+)/bid");
	public static final Pattern PATTERN_TOPBID = Pattern.compile("/(.+)/topBidList");

	private App app;

	public BidHandler(App app) {
		this.app = app;
	}

	public void doGet(HttpExchange xchg, String path) throws IOException {
		String msg = "";
		String query = xchg.getRequestURI().getQuery();
		Map<String, String> params = getParams(query);
		String sessionKey = params.get(SESSION_KEY);
		Matcher loginMatcher = PATTERN_LOGIN.matcher(path);
		Matcher topBidMatcher = PATTERN_TOPBID.matcher(path);
		if (loginMatcher.find()) {
			Long userId = Long.parseLong(loginMatcher.group(1));
			sessionKey = app.login(userId);
			msg = sessionKey + "\r\n";
			textResponse(xchg, msg);
		} else if (topBidMatcher.find()) {
			Long itemId = Long.parseLong(topBidMatcher.group(1));
			topBidList(itemId, sessionKey, xchg);
		} else {
			notFound(xchg);
		}
	}

	public void topBidList(Long itemId, String sessionKey, HttpExchange xchg) throws IOException {
		// TODO Auto-generated method stub
		SessionInfo session = app.getSession(sessionKey);
		if (session == null) {
			forbidden(xchg);
		} else {
			Long userId = session.getUserId();
			String bidList = getTopBidList(itemId, userId);
			if (bidList == null) {
				notFound(xchg);
			} else {
				textResponse(xchg, bidList);
			}
		}
	}

	public void doPost(HttpExchange xchg, String path) throws IOException {
		String msg = "";
		String query = xchg.getRequestURI().getQuery();
		Map<String, String> params = getParams(query);
		String sessionKey = params.get(SESSION_KEY);
		if (sessionKey == null) {
			forbidden(xchg);
			return;
		}
		Matcher loginMatcher = PATTERN_LOGIN.matcher(path);
		Matcher bidMatcher = PATTERN_ITEMBID.matcher(path);
		Matcher topBidMatcher = PATTERN_TOPBID.matcher(path);
		if (loginMatcher.find()) {
			forbidden(xchg);
		} else if (bidMatcher.find()) {
			Long itemId = Long.parseLong(bidMatcher.group(1));
			SessionInfo session = app.getSession(sessionKey);
			if (session == null) {
				forbidden(xchg);
			} else {
				bid(xchg, session);
				textResponse(xchg, HttpURLConnection.HTTP_NOT_IMPLEMENTED, "TBD: bid into an item\r\n");
			}
		} else {
			msg = "Not found";
			xchg.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, msg.length());
			xchg.getResponseBody().write(msg.getBytes());
		}
	}

	public void bid(HttpExchange xchg, SessionInfo session) throws IOException {
		Vector<String> requestPaths = App.split(xchg.getRequestURI().getPath());
		Long itemId = Long.parseLong(requestPaths.get(0));
		double amount = getBidAmount(xchg.getRequestBody());
		app.bid(itemId, amount, session.getUserId());
	}

	private void forbidden(HttpExchange xchg) throws IOException {
		int statusCode = HttpURLConnection.HTTP_FORBIDDEN;
		emptyResponse(xchg, statusCode);
	}

	private void notFound(HttpExchange xchg) throws IOException {
		int statusCode = HttpURLConnection.HTTP_NOT_FOUND;
		emptyResponse(xchg, statusCode);
	}

	public void emptyResponse(HttpExchange xchg, int statusCode) throws IOException {
		textResponse(xchg, statusCode, null);
	}

	public void textResponse(HttpExchange xchg, String body) throws IOException {
		textResponse(xchg, HttpURLConnection.HTTP_OK, body);
	}

	public void textResponse(HttpExchange xchg, int statusCode, String body) throws IOException {
		int length = 0;
		if (body != null) {
			length = body.length();
		}
		xchg.sendResponseHeaders(statusCode, length);
		if (body != null) {
			xchg.getResponseBody().write(body.getBytes());
		}
		xchg.getResponseBody().close();
	}

	private Map<String, String> getParams(String query) {
		Map<String, String> result = new TreeMap<String, String>();
		if (query != null) {
			for (String pair : query.split("&")) {
				String[] a = pair.split("=");
				String key = a[0];
				String value = a[1];
				result.put(key, value);
			}
		}
		return result;
	}

	public void handle(HttpExchange xchg) throws IOException {
		try {
			if (xchg.getRequestMethod().equals("GET")) {
				doGet(xchg, xchg.getRequestURI().getPath());
			} else if (xchg.getRequestMethod().equals("POST")) {
				doPost(xchg, xchg.getRequestURI().getPath());
			} else {
				// Not implemented
				xchg.sendResponseHeaders(HttpURLConnection.HTTP_NOT_IMPLEMENTED, 0);
				xchg.getResponseBody().close();
			}
		} catch (Exception e) {
			textResponse(xchg, HttpURLConnection.HTTP_INTERNAL_ERROR, dumpException(e));
		}
		OutputStream os = xchg.getResponseBody();
		os.close();
	}

	public String dumpException(Exception e) {
		e.getStackTrace();
		StringBuffer sb = new StringBuffer();
		sb.append("Internal Error: ");
		sb.append(e.getMessage());
		sb.append("\r\n");
		for (StackTraceElement i : e.getStackTrace()) {
			sb.append(i.getFileName() + ":" + i.getLineNumber() + "\r\n");
		}
		return sb.toString();
	}

	public double getBidAmount(InputStream requestBody) throws IOException {
		// TODO Auto-generated method stub
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[65536];
		for (int l = requestBody.read(buffer); l > 0; l = requestBody.read(buffer)) {
			baos.write(buffer, 0, l);
		}
		requestBody.close();
		String value = new String(baos.toByteArray());
		return Double.valueOf(value);
	}

	public String getTopBidList(Long itemId, Long userId) {
		List<Entry<Long, Double>> bidList = app.topBidList(itemId, userId);
		StringBuffer sb = new StringBuffer();
		if (bidList != null && !bidList.isEmpty()) {
			sb.append("[\r\n");
			Vector<String> items = new Vector<String>();
			for (Entry<Long, Double> i : bidList) {
				String item = "{\"" + i.getKey() + "\": \"" + i.getValue() + "\"}";
				items.add(item);
			}
			sb.append(String.join(",\r\n", items));
			sb.append("\r\n");
			sb.append("]");
		}
		return sb.toString();
	}
}