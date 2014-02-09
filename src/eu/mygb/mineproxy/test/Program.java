package eu.mygb.mineproxy.test;

import java.io.IOException;
import java.net.UnknownHostException;


import eu.mygb.mineproxy.proxy.MineProxy;

public class Program {
	
	public static void main(String[] args) {
		try {			
			
			MineProxy mp = new MineProxy("127.0.0.1",25565,25566);
			
			MyCustomPacketListener po = new MyCustomPacketListener();
			mp.addPacketListener(po);
			mp.listen();
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
