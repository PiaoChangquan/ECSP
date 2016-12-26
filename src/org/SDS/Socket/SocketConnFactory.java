package org.SDS.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketConnFactory {
	private Logger logger = LoggerFactory.getLogger(SocketConnFactory.class);


	public EthernetSocketConnector getEthernetConnector() {
		logger.trace("EthernetSocketConnector will be created");
		return new EthernetSocketConnector();
	}
}
