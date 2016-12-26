package org.SDS.Loader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
//
//import org.SDS.Scoket.test.DataStreamConfig;
import org.SDS.Sensor.SensorUnitConfig;
import org.SDS.Socket.SocketConfig;
//import org.csnpod.util.DataStreamConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConfigLoader {

	private static Logger logger = LoggerFactory.getLogger(ConfigLoader.class);

	private static ObjectMapper mapper;
	private static JsonNode rootNode;

	public static void setLoader(String filePath) {
		logger.trace("Start setLoader Method");

		logger.info("Load the \"{}\" configuration file", filePath);
		mapper = new ObjectMapper();
//		���� ��Ϊmapper ��ӳ�䣩�Ķ���
		try {
			rootNode = mapper.readTree(new File(filePath));
//			root �ڵ㶨��Ϊ mapper readTree  json ���÷�
		} catch (IOException e) {
			logger.error("Can't load \"{}\" file", filePath);
			logger.error("Error: {}", e.toString());
		}

		logger.trace("End setLoader Method");
	}

	public static void loadAllConfig(String filePath) {
		logger.trace("Start loadAllConfig Method");
		logger.debug("Load Configuration from {}", filePath);

		setLoader(filePath);

		loadSensorConfig();
		loadSocketConfig();
	
//		����csnPOD  sensor DataStream communication ��Ϣ����
		logger.trace("End loadAllConfig Method");
	}


	private static void loadSensorConfig() {
		logger.trace("Start loadSensorConfig Method");

		JsonNode sensorUnitNode = rootNode.path("sensor_unit");
//		��json �ļ��� ��ȡ sensor ��Ԫ�� ��Ϣ 
		SensorUnitConfig.snsrCount = sensorUnitNode.path("count").intValue();
//		��ȡ ����������
		if (SensorUnitConfig.snsrCount > 0) {
			JsonNode sensorListNode = sensorUnitNode.path("elements");
			List<PhysicalSensorInformation> physicalSensorConf = new ArrayList<PhysicalSensorInformation>();
//			��� ��������������0 ����ȡ element ������
			for (JsonNode sensorNode : sensorListNode) {
//				sensorNode С�� sensorlist Node 
				try {
					List<Map<String, String>> tempSensors = mapper.readValue(
									sensorNode.path("sensors").toString(),
									new TypeReference<LinkedList<HashMap<String, String>>>() {
									});
					List<LogicalSensorMetadata> sensors = new LinkedList<LogicalSensorMetadata>();
					for (Map<String, String> logicalSnsrMap : tempSensors) {
						LogicalSensorMetadata tempSensor = new LogicalSensorMetadata(
								logicalSnsrMap.get("local_id"),
								logicalSnsrMap.get("csn_id"),
								logicalSnsrMap.get("time_format"),
								logicalSnsrMap.get("value_type"),
								logicalSnsrMap.get("description"));
						sensors.add(tempSensor);
					}

					HashMap<String, Object> target = mapper.readValue(
							sensorNode.path("parse_target").toString(),
							new TypeReference<HashMap<String, Object>>() {});

					PhysicalSensorInformation sensor = new PhysicalSensorInformation(
							sensorNode.path("name").textValue(), sensorNode.path("type").textValue(), sensors,
							sensorNode.path("parse_regex").textValue(), target,
							sensorNode.path("sampling_period").intValue());
//					ȡ��������������Ϣ  ����ֵ
					physicalSensorConf.add(sensor);
					logger.debug("Added Physical Sensor's number: {}",
							physicalSensorConf.size());
					logger.debug("Added Sensor: {}", physicalSensorConf);

				} catch (JsonParseException e) {
					logger.error("Can't Parse the configuration file");
					logger.error("Error: {}", e.toString());
				} catch (JsonMappingException e) {
					logger.error("Can't map the configuration file");
					logger.error("Error: {}", e.toString());
				} catch (IOException e) {
					logger.error("Can't open the configurationfile");
					logger.error("Error: {}", e.toString());
				}
//				�������
			}

			SensorUnitConfig.physicalSnsrInfo = physicalSensorConf;
//			������������Ϣ ���� ���������� �е�������Ϣ 
			logger.info("Loaded Sensor Config: {}", new SensorUnitConfig());
		}

		logger.trace("End loadSensorConfig Method");
	}

	private static void loadSocketConfig() {
		logger.trace("Start loadDataStreamConfig Method");

		JsonNode SocketUnitNode = rootNode.path("socket_unit");
	
		SocketConfig.serverIP = SocketUnitNode.path("server_ip").textValue();
		SocketConfig.serverPort = SocketUnitNode.path("server_port").intValue();
		logger.info("Loaded Socket Config: {}", new SocketConfig());

		logger.trace("End loadDataStreamConfig Method");
	}

}
