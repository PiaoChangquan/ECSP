package org.SDS.Sensor;

import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.SDS.Esper.ClientSideUpdateListener;
import org.SDS.Esper.SampleStatement;
import org.SDS.Loader.LogicalSensorMetadata;
import org.SDS.Loader.PhysicalSensorInformation;
import org.SDS.Socket.SocketConfig;
import org.SDS.Socket.SocketConnFactory;
import org.SDS.Socket.SocketConnector;
import org.csn.agent.exception.SerialReadException;
//import org.csnpod.comm.socket.SocketConnector;
//import org.csn.agent.exception.SerialReadException;
//import org.csnpod.exception.SerialReadException;
//import org.csnpod.sensor.data.LogicalSensorMetadata;
//import org.csnpod.sensor.data.SensorData;
//import org.csnpod.sensor.data.PhysicalSensorInformation;
//import org.csnpod.sensor.parser.SensorDataParser;
//import org.csnpod.sensor.sampling.SensorStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.espertech.esper.client.EPRuntime;
import com.google.common.util.concurrent.Uninterruptibles;

public class Sensor extends Thread {
	private Logger logger = LoggerFactory.getLogger(Sensor.class);
	protected BlockingQueue<SensorData> sensorDataQueue;//
	private SensorStream stream;
	private PhysicalSensorInformation physicalInfo;
	private SensorDataParser parser;
	private SocketConnector socket;
	private boolean abort = false; // Flag in case of abort
	public static EPRuntime runTime;
	public Sensor(String name, String comPort, int baudRate, BlockingQueue<SensorData> sensorDataQueue,
			PhysicalSensorInformation metadata) {

		super(name);
		stream = new SensorStream(comPort, baudRate);
		this.sensorDataQueue = sensorDataQueue;
		this.physicalInfo = metadata;
		parser = new SensorDataParser(physicalInfo.getParsingRegex(), physicalInfo.getTargetInfo());
		socket = new SocketConnFactory().getEthernetConnector();
	}

	@Override
	public void run() {
		logger.trace("Start run Method");
		
//		Socket socket = new Socket(SocketConfig.serverIP,SocketConfig.serverPort);
		socket.connect(SocketConfig.serverIP,SocketConfig.serverPort);
		
		logger.trace("Start new Socket \"{}\"",SocketConfig.serverIP,SocketConfig.serverPort);

		logger.info("Start \"{}\"", physicalInfo.getName());

		logger.info("Connect to Sensor Stream");
		stream.connectSensorStream();
		// 连接数据stream（） 其实是丢掉4次垃圾值

		while (true) {
			logger.info("Wait {}s for the next sampling time", physicalInfo.getPeriod());
			Uninterruptibles.sleepUninterruptibly(physicalInfo.getPeriod(), TimeUnit.SECONDS);
			// 正是这个值 传输数据变了 json中定义为 *0
			synchronized (this) {
				if (abort) {
					logger.info("Close \"{}\"", physicalInfo.getName());

					break;
				}
			}

			logger.trace("Reading Data from Sensor...");
			try {
				String rawData = stream.readLine();
				logger.info("Data from sensor: {}", rawData);

				logger.trace("Parsing Sensor Data");
				Map<String, Object> parseResult = parser.parseData(rawData);
				
				if (parseResult == null) {
					logger.warn("Sensor Data Error");
					continue;
				}

				for (Object localIdKey : parseResult.keySet()) {
					logger.debug("Parsed Result: {}", parseResult.get(localIdKey));

					LogicalSensorMetadata sensorMeta = null;
					for (LogicalSensorMetadata tempSensorMeta : physicalInfo.getSensors()) {
						if (localIdKey.equals(tempSensorMeta.getLocalId())) {
							sensorMeta = tempSensorMeta;
						}
					}

					Date date = Calendar.getInstance().getTime();
					String currentTimestamp = new SimpleDateFormat(sensorMeta.getTimeFormat()).format(date);
					logger.debug("Making Timestamp: {}", currentTimestamp);

					SensorData sensorData = new SensorData(sensorMeta.getCsnId(), currentTimestamp,
							Float.valueOf((String) parseResult.get(localIdKey)));
					logger.info("Created Data: {}", sensorData);

					logger.trace("Send Data to Data Stream Controller");
					sensorDataQueue.put(sensorData);

					System.out.println("================Send Event to Esper ==================");
					runTime.sendEvent(sensorData); 
					
					
					
					int ret = socket.connect(SocketConfig.serverIP,SocketConfig.serverPort);

					Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);

					if (ret != 0)
						ret = socket.connect(SocketConfig.serverIP,	SocketConfig.serverPort);
					logger.debug("Socket Connect Return: {}", ret);

					Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);

					if (ret != 0)
						ret = socket.connect(SocketConfig.serverIP,	SocketConfig.serverPort);
					logger.debug("Socket Connect Return: {}", ret);
					
					socket.write(sensorData.toString());
					System.out.println("++++++++++++End send sensorData to server++++++++++");
					socket.write(SampleStatement.getReurn());
					socket.write(ClientSideUpdateListener.getReurn());
					
					System.out.println("++++++++++++End Send EVENT to server    +++++++++++");
					
					socket.close();
					logger.info("Finish to Send Data to Server");
					
				}

			} catch (SerialReadException e) {
				logger.error("Can't read stream from sensor");
				logger.error("Error: {}", e.toString());
			} catch (InterruptedException e) {
				logger.error("Can't send Sensor Data to Data stream controller unit");
				logger.error("Error: {}", e.toString());
			}

		}

		stream.closeSensorStream();
		logger.info("\"{}\" will be Stopped", physicalInfo.getName());

		logger.trace("End run Method");
	}

	public void abort() {
		this.abort = true;
	}

}
