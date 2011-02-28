package de.fkoeberle.tcpbuffer;

import static java.util.logging.Logger.getLogger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
	private static int BUFFER_SIZE = 32 * 1024;
	private static Logger LOG = getLogger(Server.class.getCanonicalName());

	public static void main(String[] args) throws IOException {
		if (args.length != 4) {
			System.err
					.println("Require 4 args: realServer realServerPort newPort bufferSize");
			System.err.println("If you are host this could like this:");
			System.err.println("localhost 25565 4000 50");
			System.err.println("If you are client this could look like this:");
			System.err.println("localhost 4000 25565 50");
			System.err
					.println("The host has in that example to make sure that he can be reached on port 4000");
			System.err
					.println("and both host and client would connect to localhost in minecraft");
			System.exit(1);
			return;
		}
		String targetAddress = args[0];
		int targetPort = Integer.parseInt(args[1]);
		int port = Integer.parseInt(args[2]);
		int periodInMS = Integer.parseInt(args[3]);
		ServerSocket serverSocket = new ServerSocket(port);
		System.out.println("Running:");
		System.out.printf("Listening at port %d.%n", port);
		System.out.printf(
				"Forwarding data from and to %s:%d every %d milliseconds%n",
				targetAddress, targetPort, periodInMS);
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
		pipeWithBuffer(s0.getInputStream(), s1.getOutputStream(), periodInMS);
		pipeWithBuffer(s1.getInputStream(), s0.getOutputStream(), periodInMS);
	}

	public static void pipeWithBuffer(final InputStream inputStream,
			final OutputStream outputStream, final int periodInMS) {
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
					LOG.log(Level.INFO,
							"Exception while reading: " + e.getMessage(), e);
				}
			}
		};
		reader.start();

		Thread writer = new Thread(writePeriodicWritingOuputStream);
		writer.start();
	}
}
