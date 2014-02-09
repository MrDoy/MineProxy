package eu.mygb.mineproxy.proxy;

import eu.mygb.mineproxy.packets.MinePacket;

public class PacketNotification {
	private MinePacket packet;
	private CommunicationGateway gateway;
	private int state;
	
	/**
	 * @param packet
	 * @param gateway
	 * @param state
	 */
	public PacketNotification(MinePacket packet, CommunicationGateway gateway,
			int state) {
		super();
		this.packet = packet;
		this.gateway = gateway;
		this.state = state;
	}
	
	public MinePacket getPacket() {
		return packet;
	}
	public void setPacket(MinePacket packet) {
		this.packet = packet;
	}
	public CommunicationGateway getGateway() {
		return gateway;
	}
	public void setGateway(CommunicationGateway gateway) {
		this.gateway = gateway;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
}
