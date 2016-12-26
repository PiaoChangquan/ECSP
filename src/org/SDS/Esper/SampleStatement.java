package org.SDS.Esper;

import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.google.common.base.MoreObjects;

public class SampleStatement {
//	private static Log log = LogFactory.getLog(SampleStatement.class);
static String a = null;
	public static void createStatement(EPAdministrator admin) {
		
		EPStatement statement = admin.createEPL("select * from SensorEvent(value>30)", "sensor");
		
		statement.addListener(new UpdateListener() {
			@SuppressWarnings("static-access")
			
			public void update(EventBean[] newEvents, EventBean[] oldEvents) {
				
				if (newEvents == null) {
					
					return;
				}
				
				for (int i = 0; i < newEvents.length; i++) {
					
					if (newEvents != null) {
						System.out.println("!!!!!!!!!!!!!!!!!!! id: " + newEvents[i].get("id")
								+ " timestamp: " + newEvents[i].get("timestamp")+" Temp: "+newEvents[i].get("value")+" !!!!!!!!!!!!!!!!!!!!");
					a= "!!!!!!!!ID: "+newEvents[i].get("id")+" Timestamp: "+ newEvents[i].get("timestamp")+" Value: "+ newEvents[i].get("value")+"!!!!!!";
					}
				}
			}
		});
		
		
	}
	public static String getReurn() {
		String a2=a;
		a=null;
		return a2; 
		
	}
}

