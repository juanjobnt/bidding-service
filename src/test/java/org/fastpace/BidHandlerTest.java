package org.fastpace;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class BidHandlerTest {
	private static final int PORT = 0;
	private App app;

	@Before
	public void setUp() {
		app = new App(PORT);
		Long itemId = 1234L;
		for(long i=0; i<20; i++) {
			app.bid(itemId, Double.valueOf(i*1.0), Long.valueOf(i));
		}
	}

	@Test
	public void testTopBidList() {
		Long itemId = 1234L;
		String s = app.handler.getTopBidList(itemId, 0L);
		String expected = "[\r\n"
				+ "{\"19\": \"19.0\"},\r\n"
				+ "{\"18\": \"18.0\"},\r\n"
				+ "{\"17\": \"17.0\"},\r\n"
				+ "{\"16\": \"16.0\"},\r\n"
				+ "{\"15\": \"15.0\"},\r\n"
				+ "{\"14\": \"14.0\"},\r\n"
				+ "{\"13\": \"13.0\"},\r\n"
				+ "{\"12\": \"12.0\"},\r\n"
				+ "{\"11\": \"11.0\"},\r\n"
				+ "{\"10\": \"10.0\"},\r\n"
				+ "{\"9\": \"9.0\"},\r\n"
				+ "{\"8\": \"8.0\"},\r\n"
				+ "{\"7\": \"7.0\"},\r\n"
				+ "{\"6\": \"6.0\"},\r\n"
				+ "{\"5\": \"5.0\"}\r\n"
				+ "]";
		assertEquals(expected, s);
	}

}
