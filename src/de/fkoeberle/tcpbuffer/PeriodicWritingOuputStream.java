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

import static java.util.logging.Logger.getLogger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PeriodicWritingOuputStream implements Runnable {
	private static Logger LOG = getLogger(PeriodicWritingOuputStream.class
			.getCanonicalName());
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
					if (e instanceof RuntimeException) {
						LOG.log(Level.WARNING,
								"Cought runtime exception: " + e.getMessage(),
								e);
					} else {
						LOG.log(Level.INFO,
								"Exiting earlier: " + e.getMessage());
					}
				}
			}
		} finally {
			try {
				outputStream.close();
			} catch (IOException e) {
				LOG.log(Level.INFO,
						"Cought exception while closing output stream: "
								+ e.getMessage());
			}
		}
	}
}
