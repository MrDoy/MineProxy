package eu.mygb.mineproxy.test;

import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

import eu.mygb.mineproxy.packets.ChunkBulkDataPacket;
import eu.mygb.mineproxy.packets.ChunkDataPacket;
import eu.mygb.mineproxy.packets.MinePacket;
import eu.mygb.mineproxy.proxy.ClientGateway;
import eu.mygb.mineproxy.proxy.ClientSession;
import eu.mygb.mineproxy.proxy.CommunicationGateway;
import eu.mygb.mineproxy.proxy.PacketAdapter;
import eu.mygb.mineproxy.proxy.PacketNotification;
import eu.mygb.mineproxy.proxy.ServerGateway;



public class MyCustomPacketListener extends PacketAdapter {

	public void receivePacket(PacketNotification pn) {
		int state = pn.getState();
		MinePacket mp = pn.getPacket();
		int packettype = mp.getPacketType();
		CommunicationGateway o = pn.getGateway();
		
		if(o instanceof ServerGateway) {
			if (state == ClientSession.STATE_PLAY) {
				if(packettype == 1) {
					
					String text = mp.nextString();
					System.out.println("Chat message sent : "+text);
				} else if(packettype == 21) {
					
					System.out.println("Client settings");
					System.out.println(Arrays.toString(mp.getPacketdetails()));
				}
			}
		// When the server sends a packet
		} else if(o instanceof ClientGateway) {
			if(state == ClientSession.STATE_PLAY) {
				if(packettype == 1) {
					
					System.out.println("Join game");

				} else if(packettype == 2) {
					
					String json = mp.nextString();
					System.out.println("Chat : "+json);

				} else if(packettype == 33) {
					
					System.out.println("Chunk data");
					ChunkDataPacket cdp = new ChunkDataPacket(mp);
					
				} else if(packettype == 38) {
					
					System.out.println("Chunk bulk data");
					ChunkBulkDataPacket cdp = new ChunkBulkDataPacket(mp);

				}  else if(packettype == 56) {
					
					System.out.println("Player List");
					String user = mp.nextString();
					System.out.println(user);

				}
			}
		}
	}

}
