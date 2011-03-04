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

import java.util.concurrent.atomic.AtomicBoolean;

public class ConnectionEndListener {
	private final String ip0;
	private final String ip1;
	private final EventListener listener;
	private final AtomicBoolean eventSent = new AtomicBoolean(false);

	public ConnectionEndListener(String ip0, String ip1, EventListener listener) {
		this.ip0 = ip0;
		this.ip1 = ip1;
		this.listener = listener;
	}

	public void handleConnectionEnds(String reason) {
		boolean sent = eventSent.getAndSet(true);
		if (!sent) {
			listener.handleEvent(String.format(
					"No longer forwarding data between %s and %s: %s", ip0,
					ip1, reason));
		}
	}
}
