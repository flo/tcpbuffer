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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

final class ConnectionAcceptingThread extends Thread {
	private final int targetPort;
	private final String targetAddress;
	private final ServerSocket serverSocket;
	private final AtomicInteger periodInMS;
	private final ServerStateListener serverStateListener;
	private final EventListener eventListener;

	ConnectionAcceptingThread(int targetPort, String targetAddress,
			ServerSocket serverSocket, AtomicInteger periodInMS2,
			ServerStateListener serverStateListener, EventListener eventListener) {
		this.targetPort = targetPort;
		this.targetAddress = targetAddress;
		this.serverSocket = serverSocket;
		this.serverStateListener = serverStateListener;
		this.eventListener = eventListener;
		this.periodInMS = periodInMS2;
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
								.format("Could not connect to server: %s does not listen at %d!",
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

	public void delayedTransfer(Socket s0, Socket s1, AtomicInteger periodInMS2)
			throws IOException {
		final String s0Name = s0.getInetAddress().getHostAddress();
		final String s1Name = s1.getInetAddress().getHostAddress();
		eventListener.handleEvent(String.format(
				"Forwarding data between %s and %s", s0Name, s1Name));
		ConnectionEndListener connectionEndListener = new ConnectionEndListener(
				s0Name, s1Name, eventListener);
		pipeWithBuffer(s0.getInputStream(), s1.getOutputStream(), periodInMS2,
				connectionEndListener);
		pipeWithBuffer(s1.getInputStream(), s0.getOutputStream(), periodInMS2,
				connectionEndListener);
	}

	public void pipeWithBuffer(final InputStream inputStream,
			final OutputStream outputStream, final AtomicInteger periodInMS,
			final ConnectionEndListener connectionEndListener) {
		final PeriodicWritingOuputStream writePeriodicWritingOuputStream = new PeriodicWritingOuputStream(
				outputStream, periodInMS, connectionEndListener);
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
					connectionEndListener.handleConnectionEnds(e.getMessage());
				}
			}
		};
		reader.start();

		Thread writer = new Thread(writePeriodicWritingOuputStream);
		writer.start();
	}
}