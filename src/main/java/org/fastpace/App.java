package org.fastpace;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;
import java.util.Vector;

import com.sun.net.httpserver.HttpServer;

/**
 * Hello world!
 *
 */
public class App {
	private static final int SESSION_TIMEOUT_MINUTES = 10;

	private HttpServer server;
	private int port;
	public BidHandler handler;
	Map<String, SessionInfo> sessions;
	private Random random;
	Map<Long, ItemBids> bids;

	public class SessionInfo {
		Long userId;
		String sessionKey;
		Date timeout;

		public Date getTimeout() {
			return timeout;
		}

		public void setTimeout(Date timeout) {
			this.timeout = timeout;
		}

		public String getSessionKey() {
			return sessionKey;
		}

		public void setSessionKey(String sessionKey) {
			this.sessionKey = sessionKey;
		}

		public Long getUserId() {
			return userId;
		}

		public void setUserId(Long userId) {
			this.userId = userId;
		}

		public SessionInfo(Long userId, String sessionKey, Date timeout) {
			this.userId = userId;
			this.sessionKey = sessionKey;
			this.timeout = timeout;
		}
	}

	public App(int port) {
		this.port = port;
		this.handler = new BidHandler(this);
		this.sessions = new TreeMap<String, App.SessionInfo>();
		this.bids = new TreeMap<Long, ItemBids>();
		this.random = new Random(System.currentTimeMillis());

	}

	public void start() throws IOException {
		server = HttpServer.create();
		server.createContext("/", handler);
		InetSocketAddress addr = new InetSocketAddress(port);
		server.bind(addr, port);
		server.start();
	}

	public void stop() {
		server.stop(port);
	}

	public static void main(String[] args) throws Exception {
		System.out.println("Hello World!");
		App app = new App(8080);
		app.start();
	}

	public String login(Long userId) {
		// TODO Auto-generated method stub
		String sessionKey = null;
		Calendar now = Calendar.getInstance();
		now.roll(Calendar.MINUTE, SESSION_TIMEOUT_MINUTES);
		sessionKey = createSessionKey();
		Date timeout = now.getTime();
		SessionInfo sessionInfo = new SessionInfo(userId, sessionKey, timeout);
		sessions.put(sessionKey, sessionInfo);
		return sessionKey;
	}

	private String createSessionKey() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 10; i++) {
			sb.append((char) ('A' + random.nextInt(26)));
		}
		String sessionKey = sb.toString();
		return sessionKey;
	}

	public void bid(Long itemId, double value, Long userId) {
		synchronized (this.bids) {
			ItemBids itemBids = null;
			if (this.bids.containsKey(itemId)) {
				itemBids = this.bids.get(itemId);
			} else {
				itemBids = new ItemBids(itemId);
				this.bids.put(itemId, itemBids);
			}
			itemBids.bid(userId, value);
		}
	}

	public static Vector<String> split(String realPath) {
		String[] elems = realPath.split("/");
		Vector<String> result = new Vector<String>();
		for (String e : elems) {
			if (e != null && !e.isEmpty()) {
				result.add(e);
			}
		}
		return result;
	}

	public SessionInfo getSession(String sessionKey) {
		if (sessionKey == null) {
			return null;
		}
		SessionInfo session = sessions.get(sessionKey);
		if (session != null && session.timeout.before(Calendar.getInstance().getTime())) {
			// Out of date!
			sessions.remove(sessionKey);
		}
		return session;
	}

	public List<Entry<Long, Double>> topBidList(Long itemId, Long userId) {
		// Only highest bid per user
		// If user NOT in list of bidders, empty result
		List<Entry<Long, Double>> result = new ArrayList<Entry<Long, Double>>();
		if (bids.containsKey(itemId)) {
			return bids.get(itemId).getBidsForUser(userId);
		}
		return result;
	}

}
