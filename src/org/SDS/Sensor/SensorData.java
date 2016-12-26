package org.SDS.Sensor;

import com.google.common.base.MoreObjects;

public class SensorData {

	private String id;
	private String timestamp;
	private float value;

	public SensorData(String id, String Timestamp, float Value) {
		this.id = id;
		this.timestamp = Timestamp;
		this.value = Value;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public float getValue() {
		return value;
	}

	public void setValue(float value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("ID", id)
				.add("Timestamp", timestamp).add("Value", value).toString();
	}

}
