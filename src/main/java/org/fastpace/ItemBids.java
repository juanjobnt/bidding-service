package org.fastpace;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

public class ItemBids {
	Long itemId;
	Vector<Map.Entry<Long, Double>> userBids;
	Set<Long> bidders;

	public ItemBids(Long itemId2) {
		this.userBids = new Vector<Map.Entry<Long, Double>>();
		this.bidders = new TreeSet<Long>();
	}

	public void bid(Long userId, double value) {
		if ((userBids.size() == 0) || (value > userBids.get(0).getValue())) {
			userBids.add(0, new Bid(userId, value));
			// Remove any other bids by this user
			if (bidders.contains(userId)) {
				for (int i=1; i<userBids.size(); i++) {
					if (userBids.get(i).getKey().equals(userId)) {
						userBids.remove(i);
					}
				}
			}
			// TODO: Implement!
			// Keep only the 15 highest bids
			while (userBids.size() > 15) {
				userBids.remove(15);
			}
			// TODO: Implement
			this.bidders.add(userId);
		}
	}

	public List<Entry<Long, Double>> getBidsForUser(Long userId) {
		// TODO Auto-generated method stub
		ArrayList<Entry<Long, Double>> result = new ArrayList<Entry<Long, Double>>();
		if (bidders.contains(userId)) {
			// Get bids
			result.addAll(this.userBids);
		}
		return result;
	}
}
