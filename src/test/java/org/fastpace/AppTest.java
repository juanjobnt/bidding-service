package org.fastpace;

import static org.junit.Assert.assertNotEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.regex.Matcher;

import org.junit.Before;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {

	private static final int PORT = 0;
	private App app;

	@Before
	public void setUp() {
		app = new App(PORT);
	}

	/**
	 * Create the test case
	 *
	 * @param testName
	 *            name of the test case
	 */
	public AppTest(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(AppTest.class);
	}

	/**
	 * Rigourous Test :-)
	 */
	public void testApp() {
		assertTrue(true);
	}

	public void testSplit() {
		Vector<String> result = new Vector<String>();
		result.add("foo");
		result.add("bar");
		result.add("baz");
		assertEquals(result, App.split("/foo/bar/baz"));
	}

	public void testLogin() {
		Long userId = 1L;
		String sessionKey = app.login(userId);
		Long userId2 = 2L;
		String sessionKey2 = app.login(userId2);
		assertNotEquals(sessionKey, sessionKey2);
	}

	public void testGetBidAmount() throws IOException {
		double amount = Math.PI;
		InputStream bais = new ByteArrayInputStream(String.valueOf(amount).getBytes("UTF-8"));
		assertEquals(amount, app.handler.getBidAmount(bais));
	}

	public void testLoginPattern() {
		String userId = "1234";
		String input = "/" + userId + "/login";
		Matcher m = BidHandler.PATTERN_LOGIN.matcher(input);
		assertTrue(m.find());
		assertEquals(userId, m.group(1));
	}

	public void testItemBidPattern() {
		String itemId = "1234";
		String input = "/" + itemId + "/bid";
		Matcher m = BidHandler.PATTERN_ITEMBID.matcher(input);
		assertTrue(m.find());
		assertEquals(itemId, m.group(1));
	}

	public void testTopBidPattern() {
		String itemId = "1234";
		String input = "/" + itemId + "/topBidList";
		Matcher m = BidHandler.PATTERN_TOPBID.matcher(input);
		assertTrue(m.find());
		assertEquals(itemId, m.group(1));
	}

	public void testTopBidListSingleBid() {
		Long userId = 1234L;
		Double value = 30.5;
		Long itemId = 5678L;
		ItemBids bidsForItem = new ItemBids(itemId);
		bidsForItem.userBids.add(new Bid(userId, value));
		bidsForItem.bidders.add(userId);
		app.bids.put(itemId, bidsForItem);
		List<Entry<Long, Double>> topBids = app.topBidList(itemId, userId);
		assertEquals(1, topBids.size());
	}
	
	public void testTopBidListTwoBidsSameUser() {
		Long userId = 1234L;
		Long itemId = 5678L;
		Double value1 = 1d;
		Double value2 = 2d;
		app.bid(itemId, value1, userId);
		app.bid(itemId, value2, userId);
		List<Entry<Long, Double>> topBids = app.topBidList(itemId, userId);
		assertEquals(1, topBids.size());
	}

	public void testGetSessionNull() {
		assertNull(app.getSession(null));
	}

	public void testSingleBid() {
		assertTrue(app.bids.isEmpty());
		Long itemId = 1234L;
		Long userId = 5678L;
		Double value = 10.0;
		app.bid(itemId, value, userId);
		assertEquals(1, app.bids.size());
		ItemBids bids = app.bids.get(itemId);
		assertEquals(1, bids.userBids.size());
		assertTrue(bids.userBids.get(0).getKey().equals(userId));
		Entry<Long, Double> bid = bids.userBids.get(0);
		assertEquals(value, bid.getValue());
	}

	public void testTwoBidsSameUser() {
		assertTrue(app.bids.isEmpty());
		Long itemId = 1234L;
		Long userId = 5678L;
		Double value = 10.0;
		Double outBid = 11.0;
		app.bid(itemId, value, userId);
		app.bid(itemId, outBid, userId);
		assertEquals(1, app.bids.size());
		ItemBids itemBids = app.bids.get(itemId);
		assertEquals(1, itemBids.userBids.size());
		assertTrue(itemBids.userBids.get(0).getKey().equals(userId));
		Map.Entry<Long, Double> bid = itemBids.userBids.get(0);
		assertEquals(outBid, bid.getValue());
	}

	public void testOneBidTwoUsers() {
		assertTrue(app.bids.isEmpty());
		Long itemId = 1234L;
		Long userId = 5678L;
		Long userId2 = 9000L;
		Double value = 10.0;
		Double outBid = 11.0;
		app.bid(itemId, value, userId);
		app.bid(itemId, outBid, userId2);
		assertEquals(1, app.bids.size());
		ItemBids bids = app.bids.get(itemId);
		assertEquals(2, bids.userBids.size());
		assertTrue(bids.userBids.get(0).getKey().equals(userId2));
		Map.Entry<Long, Double> bid = bids.userBids.get(0);
		assertEquals(outBid, bid.getValue());
		assertEquals(userId2, bid.getKey());
		assertTrue(bids.userBids.get(1).getKey().equals(userId));
		Map.Entry<Long, Double> bid2 = bids.userBids.get(1);
		assertEquals(value, bid2.getValue());
		assertEquals(userId, bid2.getKey());
	}
	
	public void testTopBidListMoreThanFifteenBids() {
		Long itemId = 7890L;
		for (long i=0; i<20L; i++) {
			app.bid(itemId, i, i);
		}
		for(long i=1; i<=15; i++) {
			List<Entry<Long, Double>> topBidList = app.topBidList(itemId, i);
			assertEquals(15, topBidList.size());
			assertEquals(Long.valueOf(19L), topBidList.get(0).getKey());
			assertEquals(Double.valueOf(19d), topBidList.get(0).getValue());
		}
	}
}
