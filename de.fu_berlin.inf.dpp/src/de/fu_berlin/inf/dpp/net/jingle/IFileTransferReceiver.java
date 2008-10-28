package de.fu_berlin.inf.dpp.net.jingle;

import java.net.InetAddress;

public interface IFileTransferReceiver extends IJingleFileTransferConnection{

	public InetAddress getLocalHost();

    public InetAddress getRemoteHost() ;

    public int getLocalPort();

    public int getRemotePort() ;

    public void stop();
}
