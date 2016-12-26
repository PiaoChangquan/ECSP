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
//		穿件 名为mapper （映射）的对象
		try {
			rootNode = mapper.readTree(new File(filePath));
//			root 节点定义为 mapper readTree  json 的用法
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
	
//		加载csnPOD  sensor DataStream communication 信息函数
		logger.trace("End loadAllConfig Method");
	}


	private static void loadSensorConfig() {
		logger.trace("Start loadSensorConfig Method");

		JsonNode sensorUnitNode = rootNode.path("sensor_unit");
//		在json 文件中 提取 sensor 单元的 信息 
		SensorUnitConfig.snsrCount = sensorUnitNode.path("count").intValue();
//		提取 传感器数量
		if (SensorUnitConfig.snsrCount > 0) {
			JsonNode sensorListNode = sensorUnitNode.path("elements");
			List<PhysicalSensorInformation> physicalSensorConf = new ArrayList<PhysicalSensorInformation>();
//			如果 传感器数量大于0 的提取 element 中数据
			for (JsonNode sensorNode : sensorListNode) {
//				sensorNode 小于 sensorlist Node 
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
//					取得物理传感器的信息  并赋值
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
//				定义错误
			}

			SensorUnitConfig.physicalSnsrInfo = physicalSensorConf;
//			讲物理传感器信息 传给 传感器控制 中的物理信息 
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
