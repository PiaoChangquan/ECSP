/**************************************************************************************
 * Copyright (C) 2006-2015 EsperTech Inc. All rights reserved.                        *
 * http://www.espertech.com/esper                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.SDS.Esper;


import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

public class ClientSideUpdateListener implements Serializable, UpdateListener
{
	static String a = null;
    private static Log log = LogFactory.getLog(ClientSideUpdateListener.class);

	public void update(EventBean[] newEvents, EventBean[] oldEvents)
    {	
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
	public static String getReurn() {
		String a2=a;
		a=null;
		return a2; 
		
	}
}
