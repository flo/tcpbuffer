package de.fkoeberle.tcpbuffer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class PeriodicWritingOuputStream implements Runnable {
	private final ByteArrayOutputStream buffer;
	private volatile boolean closed;
	private final OutputStream outputStream;
	private final int periodInMS;

	public PeriodicWritingOuputStream(OutputStream outputStream, int periodInMS) {
		this.buffer = new ByteArrayOutputStream(32 * 1024);
		this.closed = false;
		this.outputStream = outputStream;
		this.periodInMS = periodInMS;
	}

	public void write(byte[] data, int offset, int length) {
		synchronized (buffer) {
			buffer.write(data, offset, length);
		}
	}

	public void sheduleClose() {
		closed = true;
	}

	@Override
	public void run() {
		try {
			boolean done = false;
			while (!done) {
				try {
					Thread.sleep(periodInMS);
					byte[] dataToWrite;
					synchronized (buffer) {
						dataToWrite = buffer.toByteArray();
						buffer.reset();
						done = closed && (dataToWrite.length == 0);
					}
					if (dataToWrite.length > 0) {
						outputStream.write(dataToWrite);
						outputStream.flush();
					}
				} catch (Exception e) {
					System.err.println("Exiting earlier: " + e.getMessage());
					if (e instanceof RuntimeException) {
						e.printStackTrace();
					}
				}
			}
		} finally {
			try {
				outputStream.close();
			} catch (IOException e) {
				System.err
						.println("Cought exception while closing output stream: "
								+ e.getMessage());
			}
		}
	}
}
