package org.SDS.Sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.SDS.Loader.PhysicalSensorInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SensorControllerUnit extends Thread {
	private Logger logger = LoggerFactory.getLogger(SensorControllerUnit.class);

	protected BlockingQueue<SensorData> sensorDataQueue;
	private List<Sensor> sensorThreadList;
	private boolean abort = false; // Flag in case of abort
	
	public SensorControllerUnit(BlockingQueue<SensorData> queue) {
//	这个是干啥用的
		super("Sensor Controller Unit");
		this.sensorDataQueue = queue;
	}
	
	@Override
	public void run() {
		logger.trace("Start run Method");
//		运行线程
		
		logger.info("Init Sensors");
		initSensorThreads();
//		运行初始化传感器线程函数
		logger.info("{}s Sensor has been created", sensorThreadList.size());

		while (true) {
			synchronized (this) {
				if (abort) {
					logger.info("Abort Sensor Controller Unit");
					stopSensorThreads();
//					直到abort 为true的时候运行 停止传感器线程函数
					logger.info("Finish stopping the Sensor");
					break;
				}
			}
		}
		
		logger.trace("End run Method");
	}

	private void initSensorThreads() {
		logger.trace("Start initSensorThreads Method");
		
		int i = 0;
		sensorThreadList = new ArrayList<Sensor>();
		List<String> comPorts = new SerialConnFactory().findComPort("ACM");
//	usb ACM端口
		logger.debug("{} ports are found", comPorts.size());
		logger.debug("Port Data: {} ", comPorts);
		
		if(comPorts.size() != SensorUnitConfig.snsrCount)// != 0 ?
			logger.error("Can't match the sensor port and sensor");
//		
		logger.trace("Creating Sensor");
		for (PhysicalSensorInformation metadata : SensorUnitConfig.physicalSnsrInfo) {
			
//			有多少sensor 创建多少个sensor线程
			logger.debug("Create \"{}\" Sensor", metadata.getName());
			logger.debug("Select Port \"{}\"", comPorts.get(i));
			
			Sensor sensorThread = new Sensor(metadata.getName(),
					comPorts.get(i++), 9600, sensorDataQueue, metadata);
//			定义 Sensor线程 
			logger.trace("Start \"{}\"", sensorThread.getName());
			sensorThread.start();
//			运行 sensor线程
			sensorThreadList.add(sensorThread);
			logger.debug("Created Sensor: {}", sensorThreadList);
			logger.debug("Total Sensor number: {}", sensorThreadList.size());
		}
		
		logger.trace("End initSensorThreads Method");
	}

	private void stopSensorThreads() {
		logger.trace("Start stopSensorThreads Method");
		
		if (sensorThreadList != null) {
//	为什么是    ！=null
			logger.trace("Kill Each Sensor");
			
			for (Sensor sensor : sensorThreadList) {
//				如果 sensor 小于sensor线程数
				String sensorThreadName = sensor.getName();
				logger.debug("\"{}\" Sensor will be killed", sensorThreadName);
				sensor.abort();
//				关闭 传感器线程
				
				try {
					sensor.join();
// 这是干啥的
					logger.trace("Finish to Kill \"{}\"", sensorThreadName);
				} catch (InterruptedException e) {
					logger.error("Can't kill the \"{}\" Sensor", sensorThreadName);
					logger.error("Error: {}", e.toString());
				}
				
			}
		}
		
		logger.trace("End stopSensorThreads Method");
	}

	public void abort() {
		this.abort = true;
	}
}
