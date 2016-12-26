package org.SDS.Socket;


import com.google.common.base.MoreObjects;

public class SocketConfig {

	public static int bufferSize = 1000;
	public static String serverIP = "117.16.146.58";
	public static int serverPort = 55555;
	public static void showSocketConfigValue() {
		System.out.println("Buffer Size: " + bufferSize);
		System.out.println("Server IP: " + serverIP);
		System.out.println("Server Port: " + serverPort);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).
				add("Server IP", serverIP).add("Server Port", serverPort).toString();
	}
}
