package org.SDS.mgnt;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

import java.io.InputStream;
import java.rmi.registry.LocateRegistry;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.SDS.Esper.EPServiceProviderJMX;
import org.SDS.Esper.SampleStatement;
import org.SDS.Esper.ServerShellConstants;
import org.SDS.Loader.ConfigLoader;
import org.SDS.Sensor.Sensor;
//import org.SDS.Scoket.test.DataStreamConfig;
//import org.SDS.Scoket.test.DataStreamControllerUnit;
import org.SDS.Sensor.SensorControllerUnit;
import org.SDS.Sensor.SensorData;
import org.SDS.Socket.SocketConfig;
//import org.SDS.Socket.SocketControllerUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Uninterruptibles;

//import test.ConfigLoader;

public class SDSManager {
	private static Logger logger = LoggerFactory.getLogger(SDSManager.class);
//	日志生成
//	private static final String CONF_PATH = "./CSNPod_conf.json";
	private static final String CONF_PATH = "./SDS_conf.json";
// 读取json文件
	private SensorControllerUnit sensorCtrlUnit;
// 声明 私有变量	
	public static void main(String[] args) {
		logger.info("Start CSN Pod ...");
//mian函数
		SDSManager manager = new SDSManager();
//创建新的方法
		
		ConfigLoader.loadAllConfig(CONF_PATH);
//从载入 config提取信息
		logger.info("Init CSN Pod ...");
		int ret = manager.initSystem();
//		调用manager中的initSystem的函数
		if (ret != 0) {
			logger.error("System Can't initialize");
			return;
		}

		logger.info("Run CSN Pod ...");
		try {
			manager.EsperUnit();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		manager.startSystem();
//		调用开始 函数

		Uninterruptibles.sleepUninterruptibly(180, TimeUnit.SECONDS);
		logger.info("Stop CSN Pod ...");
		manager.stopUnits();
//  	调用结束函数
		logger.info("Closed CSN Pod System");
	}

		public void EsperUnit() throws Exception{
		//	log.info("Loading properties");
	        System.out.println("Loading properties");
	        Properties properties = new Properties();
	        InputStream propertiesIS = SDSManager.class.getClassLoader().getResourceAsStream(ServerShellConstants.CONFIG_FILENAME);
	        if (propertiesIS == null)
	        {
	            throw new RuntimeException("Properties file '" + ServerShellConstants.CONFIG_FILENAME + "' not found in classpath");
	        }
	        properties.load(propertiesIS);

	        // Start RMI registry
//	        log.info("Starting RMI registry");
	        System.out.print("Starting RMI registry");
	        int port = Integer.parseInt(properties.getProperty(ServerShellConstants.MGMT_RMI_PORT));
	        LocateRegistry.createRegistry(port);
	        System.out.println("-finish!");

	        // Obtain MBean server
//	        log.info("Obtaining JMX server and connector");
	        System.out.print("Obtaining JMX server and connector");
	        MBeanServer mbs = MBeanServerFactory.createMBeanServer();
	        String jmxServiceURL = properties.getProperty(ServerShellConstants.MGMT_SERVICE_URL);
	        JMXServiceURL jmxURL = new JMXServiceURL(jmxServiceURL);
	        JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer(jmxURL, null, mbs);
	        cs.start();
	        System.out.println("-finish!");
	        
	        // Initialize engine
//	        log.info("Getting Esper engine instance");
	        System.out.print(" Getting Esper engine instance......");
	        Configuration configuration = new Configuration();
	        configuration.addEventType("SensorEvent", SensorData.class.getName());
	        EPServiceProvider engine = EPServiceProviderManager.getDefaultProvider(configuration);
	        System.out.println("-finish!");
	    
//	        log.info("Creating sample statement");
	        System.out.print("Creating sample statement.......");
	        SampleStatement.createStatement(engine.getEPAdministrator());
	        Sensor.runTime = engine.getEPRuntime();
	        System.out.println("-finish!");
	        
	        // Register MBean
//	        log.info("Registering MBean");
	        System.out.print("Registering MBean");
	        ObjectName name = new ObjectName(ServerShellConstants.MGMT_MBEAN_NAME);
	        EPServiceProviderJMX mbean = new EPServiceProviderJMX(engine);
	        mbs.registerMBean(mbean, name);     
	        System.out.println("-finish!");

	    
		}
		public int initSystem() {
			logger.info("Initialzie CSN Pod Units...");
			BlockingQueue<SensorData> queue = new ArrayBlockingQueue<SensorData>(SocketConfig.bufferSize);
			sensorCtrlUnit = new SensorControllerUnit(queue);
			return 0;
		}
	

	public void startSystem() {
		logger.trace("Start startSystem method");
		
		sensorCtrlUnit.start();
		
//		传感器 控制单元
		logger.trace("End startSystem method");
	}

	private void stopUnits() {
		logger.trace("Start stopUnits method");

		logger.trace("Waiting for the stop of Sensor Controller Unit thread");
		sensorCtrlUnit.abort();
//		传感器控制单元中的 停止··
		try {
			sensorCtrlUnit.join();
//			传感器控制单元的 join（）
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		logger.trace("Finish to stop of Sensor Controller Unit thread");
		logger.trace("End stopUnits method");
	}
}
