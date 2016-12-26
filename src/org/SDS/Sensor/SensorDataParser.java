package org.SDS.Sensor;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SensorDataParser {
	

	private String parsingRegex;
	private Map<String, Object> targetPosMap;

	public SensorDataParser(String parsingRegex,
			Map<String, Object> targetPosMap) {
		super();
		this.parsingRegex = parsingRegex;
		this.targetPosMap = targetPosMap;
	}

	public Map<String, Object> parseData(String rawData) {
		Pattern pattern = Pattern.compile(parsingRegex);
		Matcher matcher = pattern.matcher(rawData);
		Map<String, Object> parsedResult = null;

		if (matcher.matches()) {
			parsedResult = new HashMap<String, Object>();
			for (String targetKey : targetPosMap.keySet()) {
				int targetPos = Integer.parseInt((String) targetPosMap.get(targetKey));

				String value = matcher.group(targetPos);
				parsedResult.put(targetKey, value);
			}
		}

		return parsedResult;
	}
}
