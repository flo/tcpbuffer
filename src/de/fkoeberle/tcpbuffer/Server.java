package de.fkoeberle.tcpbuffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	private static int BUFFER_SIZE = 32 * 1024;

	public static void main(String[] args) throws IOException {
		if (args.length != 4) {
			System.err
					.println("Require 4 args: realServer realServerPort newPort bufferSize");
			System.exit(1);
			return;
		}
		String targetAddress = args[0];
		int targetPort = Integer.parseInt(args[1]);
		int port = Integer.parseInt(args[2]);
		int periodInMS = Integer.parseInt(args[3]);
		ServerSocket serverSocket = new ServerSocket(port);
		while (true) {
			Socket socket = serverSocket.accept();
			Socket secondSocket = null;
			try {
				secondSocket = new Socket(targetAddress, targetPort);
			} finally {
				if (secondSocket == null) {
					socket.close();
					System.err.printf("%s does not listen at %d!%n",
							targetAddress, targetPort);
					continue;
				}
			}
			delayedTransfer(socket, secondSocket, periodInMS);
		}
	}

	public static void delayedTransfer(Socket s0, Socket s1, int periodInMS)
			throws IOException {
		pipeWithBuffer(s0.getInputStream(), s1.getOutputStream(), periodInMS,
				"->");
		pipeWithBuffer(s1.getInputStream(), s0.getOutputStream(), periodInMS,
				"<-");
	}

	public static void pipeWithBuffer(final InputStream inputStream,
			final OutputStream outputStream, final int periodInMS,
			final String name) {
		final PeriodicWritingOuputStream writePeriodicWritingOuputStream = new PeriodicWritingOuputStream(
				outputStream, periodInMS);
		Thread reader = new Thread() {
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
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		reader.start();

		Thread writer = new Thread(writePeriodicWritingOuputStream);
		writer.start();
	}
}
