package de.fkoeberle.tcpbuffer;

public interface ServerStateListener {
	void handleServerStarted();

	void handleServerStopped();
}
