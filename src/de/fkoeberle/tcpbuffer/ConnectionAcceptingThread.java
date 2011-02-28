package de.fkoeberle.tcpbuffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

final class ConnectionAcceptingThread extends Thread {
	private final int targetPort;
	private final String targetAddress;
	private final ServerSocket serverSocket;
	private final int periodInMS = 50;
	private final ServerStateListener serverStateListener;
	private final EventListener eventListener;

	ConnectionAcceptingThread(int targetPort, String targetAddress,
			ServerSocket serverSocket, ServerStateListener serverStateListener,
			EventListener eventListener) {
		this.targetPort = targetPort;
		this.targetAddress = targetAddress;
		this.serverSocket = serverSocket;
		this.serverStateListener = serverStateListener;
		this.eventListener = eventListener;
	}

	@Override
	public void run() {
		try {
			while (true) {
				Socket socket = serverSocket.accept();
				Socket secondSocket = null;
				try {
					secondSocket = new Socket(targetAddress, targetPort);
				} finally {
					if (secondSocket == null) {
						socket.close();
						final String message = String
								.format("Could not connect to server: %s does not listen at %d!%n",
										targetAddress, targetPort);
						eventListener.handleEvent(message);
						continue;
					}
				}
				delayedTransfer(socket, secondSocket, periodInMS);
			}
		} catch (Throwable e) {
			serverStateListener.handleServerStopped();
			if (e instanceof InterruptedException) {
				eventListener.handleEvent("Server stopped as requested");
			} else {
				eventListener.handleEvent("Server stopped: " + e.getMessage());
			}
		}
	}

	public void delayedTransfer(Socket s0, Socket s1, int periodInMS)
			throws IOException {
		pipeWithBuffer(s0.getInetAddress().toString(), s1.getInetAddress()
				.toString(), s0.getInputStream(), s1.getOutputStream(),
				periodInMS);
		pipeWithBuffer(s1.getInetAddress().toString(), s0.getInetAddress()
				.toString(), s1.getInputStream(), s0.getOutputStream(),
				periodInMS);
	}

	public void pipeWithBuffer(final String inputName, final String outputName,
			final InputStream inputStream, final OutputStream outputStream,
			final int periodInMS) {
		final PeriodicWritingOuputStream writePeriodicWritingOuputStream = new PeriodicWritingOuputStream(
				outputStream, periodInMS);
		Thread reader = new Thread() {
			private static final int BUFFER_SIZE = 32 * 1024;
			final byte[] buffer = new byte[BUFFER_SIZE];

			@Override
			public void run() {
				try {
					try {
						int readedBytes;
						do {
							readedBytes = inputStream.read(buffer);
							if (readedBytes != -1) {
								writePeriodicWritingOuputStream.write(buffer,
										0, readedBytes);
							}
						} while (readedBytes != -1);
					} finally {
						writePeriodicWritingOuputStream.sheduleClose();
					}
				} catch (Throwable e) {
					eventListener.handleEvent(String.format(
							"No longer forwarding data from %s to %s: %s",
							inputName, outputName, e.getMessage()));
				}
			}
		};
		reader.start();

		Thread writer = new Thread(writePeriodicWritingOuputStream);
		writer.start();
	}
}