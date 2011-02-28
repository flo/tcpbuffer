package de.fkoeberle.tcpbuffer;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class Server {
	private boolean hosting;
	private final List<HostingListener> hostingListener = new ArrayList<HostingListener>();
	private final List<EventListener> eventListener = new ArrayList<EventListener>();
	private final List<ServerStateListener> serverStateListener = new ArrayList<ServerStateListener>();
	private ServerSocket serverSocket;

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
		fireEvent(String.format("Started: Listening at port %d.%n", port));
		fireServerStarted();

		Thread thread;
		thread = new ConnectionAcceptingThread(targetPort, targetAddress,
				serverSocket, new ServerStateListener() {
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
}
