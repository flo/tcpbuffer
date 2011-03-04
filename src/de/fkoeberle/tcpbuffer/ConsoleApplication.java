/*
 * This file is made available under the Creative Commons CC0 1.0 Universal Public Domain Dedication:
 * 
 * http://creativecommons.org/publicdomain/zero/1.0/deed.en
 * 
 * The person who associated a work with this deed has dedicated the work to the public domain 
 * by waiving all of his or her rights to the work worldwide under copyright law, including all
 * related and neighboring rights, to the extent allowed by law. You can copy, modify, distribute 
 * and perform the work, even for commercial purposes, all without asking permission. 
 * 
 */
package de.fkoeberle.tcpbuffer;

public class ConsoleApplication {
	public static void main(String[] args) {
		String targetAddress = getProperty("target.address", "localhost");
		String targetPortString = getProperty("target.port",
				Constants.MINECRAFT_DEFAULT_PORT_STRING);
		String portString = getProperty("port", Constants.DEFAULT_PORT_STRING);
		String periodString = getProperty(Constants.PERIOD_PROPERTY,
				Constants.PERIOID_DEFAULT_VALUE);
		Server server = new Server();
		server.addEventListener(new EventListener() {

			@Override
			public void handleEvent(String event) {
				System.out.println(event);
			}
		});
		if (!periodString.equals(Constants.PERIOID_DEFAULT_VALUE)) {
			server.setPeriodInMS(periodString);
		}
		server.startServer(targetAddress, targetPortString, portString);
	}

	public static String getProperty(String property, String defaultValue) {
		String value = System.getProperty(property);
		if (value == null) {
			System.out.printf(
					"Property \"%s\" was not set: Using default \"%s\"%n",
					property, defaultValue);
			value = defaultValue;
		}
		return value;
	}
}
