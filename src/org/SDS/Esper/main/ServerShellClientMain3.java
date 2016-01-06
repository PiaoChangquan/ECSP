package org.SDS.Esper.main;

import org.SDS.Esper.ClientSideUpdateListener;
import org.SDS.Esper.EPServiceProviderJMXMBean;
import org.SDS.Esper.ServerShellConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.InputStream;
import java.util.Properties;

public class ServerShellClientMain3
{
    private static Log log = LogFactory.getLog(ServerShellClientMain3.class);

    public static void main(String[] args) throws Exception
    {
    	new ServerShellClientMain3();
       
    }

    public ServerShellClientMain3() throws Exception
    {
//        log.info("Loading properties");
    	System.out.println("Loading properties");
        Properties properties = new Properties();
        InputStream propertiesIS = ServerShellClientMain3.class.getClassLoader().getResourceAsStream(ServerShellConstants.CONFIG_FILENAME);
        if (propertiesIS == null)
        {
            throw new RuntimeException("Properties file '" + ServerShellConstants.CONFIG_FILENAME + "' not found in classpath");
        }
        properties.load(propertiesIS);

        // Attached via JMX to running server
//        log.info("Attach to server via JMX");
        System.out.println("Attach to server via JMX");
        JMXServiceURL url = new JMXServiceURL(properties.getProperty(ServerShellConstants.MGMT_SERVICE_URL));
        JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
        MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
        ObjectName mBeanName = new ObjectName(ServerShellConstants.MGMT_MBEAN_NAME);
        EPServiceProviderJMXMBean proxy = (EPServiceProviderJMXMBean) MBeanServerInvocationHandler.newProxyInstance(
                     mbsc, mBeanName, EPServiceProviderJMXMBean.class, true);


        System.out.println("Creating a statement via Java Management Extensions (JMX) MBean Proxy");
        proxy.createEPL("select * from SensorEvent(value>20)","filter", new ClientSideUpdateListener());

        System.out.println("Destroing statement via Java Management Extensions (JMX) MBean Proxy");
        proxy.destroy("filterStatem");

        System.out.println("Exiting");
        System.exit(-1);
    }
}
