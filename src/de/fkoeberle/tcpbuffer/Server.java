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

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
	private boolean hosting;
	private final List<HostingListener> hostingListener = new ArrayList<HostingListener>();
	private final List<EventListener> eventListener = new ArrayList<EventListener>();
	private final List<ServerStateListener> serverStateListener = new ArrayList<ServerStateListener>();
	private ServerSocket serverSocket;
	private final AtomicInteger periodInMS = new AtomicInteger(50);

	public boolean isHosting() {
		return hosting;
	}

	public synchronized void addHostingListener(HostingListener listener) {
		hostingListener.add(listener);
	}

	public synchronized void addEventListener(EventListener listener) {
		eventListener.add(listener);
	}

	private synchronized void fireEvent(String event) {
		for (EventListener listener : eventListener) {
			listener.handleEvent(event);
		}
	}

	private synchronized void fireServerStarted() {
		for (ServerStateListener listener : serverStateListener) {
			listener.handleServerStarted();
		}
	}

	private synchronized void fireServerStopped() {
		for (ServerStateListener listener : serverStateListener) {
			listener.handleServerStopped();
		}
	}

	private synchronized void startServer(final String targetAddress,
			final int targetPort, final int port) {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			fireEvent("Failed to start server: " + e.getMessage());
			return;
		}
		fireEvent(String.format("Started: Listening at port %d.", port));
		fireServerStarted();

		Thread thread;
		thread = new ConnectionAcceptingThread(targetPort, targetAddress,
				serverSocket, periodInMS, new ServerStateListener() {
					@Override
					public void handleServerStarted() {
						fireServerStarted();
					}

					@Override
					public void handleServerStopped() {
						fireServerStopped();
					}

				}, new EventListener() {

					@Override
					public void handleEvent(String event) {
						fireEvent(event);
					}
				});
		thread.start();
	}

	public synchronized void setHosting(boolean hosting) {
		this.hosting = hosting;
		for (HostingListener listener : hostingListener) {
			listener.handleHostingChanged();
		}
	}

	public synchronized void startServer(String target,
			String targetPortString, String portString) {
		int targetPort;
		try {
			targetPort = Integer.parseInt(targetPortString);
		} catch (NumberFormatException e) {
			fireEvent(String.format("The string '%s' is not a valid port",
					targetPortString));
			return;
		}
		int port;
		try {
			port = Integer.parseInt(portString);
		} catch (NumberFormatException e) {
			fireEvent(String.format("The string '%s' is not a valid port",
					portString));
			return;
		}
		startServer(target, targetPort, port);
	}

	public synchronized void addServerStateListener(ServerStateListener listener) {
		this.serverStateListener.add(listener);
	}

	public synchronized void stop() {
		if (serverSocket != null) {
			try {
				serverSocket.close();
				serverSocket = null;
			} catch (IOException e) {
				fireEvent("Could not stop server smothly: " + e.getMessage());
			}
		}
	}

	public synchronized void setPeriodInMS(String periodString) {
		if (!periodString.endsWith("ms")) {
			fireEvent(String
					.format("The specfied period duration of '%s' does not end with 'ms'",
							periodString));
			return;
		}
		final String periodStringWihtoutMS = periodString.substring(0,
				periodString.length() - 2);
		try {
			periodInMS.set(Integer.parseInt(periodStringWihtoutMS));
		} catch (NumberFormatException e) {
			fireEvent(String
					.format("The specfied period duration of '%s' is not an integer followed by 'ms'",
							periodString));
			return;
		}
		fireEvent(String
				.format("The period for package forwarding has been set to "
						+ periodString));

	}
}
