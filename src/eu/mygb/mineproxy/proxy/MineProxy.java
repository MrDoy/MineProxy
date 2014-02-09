package eu.mygb.mineproxy.proxy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

public class MineProxy {
	private InetAddress remoteAddr;
	private int remotePort;
	private InetAddress localAddr;
	private int localPort;
	
	/**
	 * @param remoteAddr the remote IP of the server to proxy
	 * @param remotePort the remote port of the server to proxy
	 * @param localAddr the IP where the server will be bound
	 * @param localPort the port where the server will listen
	 */
	public MineProxy(InetAddress remoteServer, int remotePort,
			InetAddress localServer, int localPort) {
		super();
		this.remoteAddr = remoteServer;
		this.remotePort = remotePort;
		this.localAddr = localServer;
		this.localPort = localPort;
	}

	/**
	 * @param remoteAddr the remote IP of the server to proxy
	 * @param remotePort the remote port of the server to proxy
	 * @param localPort the port where the server will listen
	 */
	public MineProxy(InetAddress remoteServer, int remotePort, int localPort) {
		super();
		this.remoteAddr = remoteServer;
		this.remotePort = remotePort;
		this.localPort = localPort;
	}
	
	/**
	 * @param remoteAddr the remote IP of the server to proxy
	 * @param remotePort the remote port of the server to proxy
	 * @param localAddr the IP where the server will be bound
	 * @param localPort the port where the server will listen
	 * @throws UnknownHostException 
	 */
	public MineProxy(String remoteServer, int remotePort,
			String localServer, int localPort) throws UnknownHostException {
		super();
		this.remoteAddr = InetAddress.getByName(remoteServer);
		this.remotePort = remotePort;
		this.localAddr = InetAddress.getByName(localServer);
		this.localPort = localPort;
	}
	
	/**
	 * @param remoteAddr the remote IP of the server to proxy
	 * @param remotePort the remote port of the server to proxy
	 * @param localPort the port where the server will listen
	 * @throws UnknownHostException 
	 */
	public MineProxy(String remoteServer, int remotePort, int localPort) throws UnknownHostException {
		super();
		this.remotePort = remotePort;
		this.remoteAddr = InetAddress.getByName(remoteServer);
		this.localPort = localPort;
	}
	
	public InetAddress getRemoteServer() {
		return remoteAddr;
	}

	public void setRemoteServer(InetAddress remoteServer) {
		this.remoteAddr = remoteServer;
	}

	public int getRemotePort() {
		return remotePort;
	}

	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}

	public InetAddress getLocalServer() {
		return localAddr;
	}

	public void setLocalServer(InetAddress localServer) {
		this.localAddr = localServer;
	}

	public int getLocalPort() {
		return localPort;
	}

	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}
	
	private ArrayList<PacketListener> listenersList = new ArrayList<PacketListener>();
	public void addPacketListener(PacketListener po) {
		listenersList.add(po);
	}
	public List<PacketListener> getPacketListeners() {
		return listenersList;
	}

	public void listen() throws IOException {
		boolean running = true;
		
		ServerSocket serverSocket = new ServerSocket();
		
		InetSocketAddress inetsockaddr = new InetSocketAddress(this.localAddr, this.localPort);
		serverSocket.bind(inetsockaddr);
		
		ArrayList<Thread> sessions = new ArrayList<Thread>();
		
		while(running) {
			Socket connection = serverSocket.accept();
			System.out.println("Connection accepted from "+connection.toString());
			ClientSession newclient = new ClientSession(connection, this.remoteAddr, this.remotePort, this);
			newclient.initializeCredentials();
			Thread tmp = new Thread(newclient);
			tmp.start();
			sessions.add(tmp);
			
		}
		
		
		serverSocket.close();
		
	}

	
}
