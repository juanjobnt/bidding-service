package org.fastpace;

import java.util.Map;

public class Bid implements Map.Entry<Long, Double> {
	Long key;
	Double value;

	public Bid() {

	}

	public Bid(Long key, Double value) {
		this.key = key;
		this.value = value;
	}

	public Double getValue() {
		return value;
	}

	public Double setValue(Double value) {
		Double oldValue = this.value;
		this.value = value;
		return oldValue;
	}

	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}
}