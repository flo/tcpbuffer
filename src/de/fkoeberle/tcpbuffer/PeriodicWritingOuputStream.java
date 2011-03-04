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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicInteger;

public class PeriodicWritingOuputStream implements Runnable {
	private final ByteArrayOutputStream buffer;
	private volatile boolean closed;
	private final OutputStream outputStream;
	private final AtomicInteger periodInMS;
	private final ConnectionEndListener connectionEndListener;

	public PeriodicWritingOuputStream(OutputStream outputStream,
			AtomicInteger periodInMS,
			ConnectionEndListener connectionEndListener) {
		this.buffer = new ByteArrayOutputStream(32 * 1024);
		this.closed = false;
		this.outputStream = outputStream;
		this.periodInMS = periodInMS;
		this.connectionEndListener = connectionEndListener;
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
					Thread.sleep(periodInMS.get());
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
				} catch (Throwable e) {
					connectionEndListener.handleConnectionEnds(e.getMessage());
				}
			}
		} finally {
			try {
				outputStream.close();
			} catch (IOException e) {
				// Exception no use to user:
				// Exception will not get handled anywhere,
				// but from IDEs watching for RuntimeExceptions
				throw new RuntimeException(e);
			}
		}
	}
}
