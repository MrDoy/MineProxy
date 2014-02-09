package eu.mygb.mineproxy.packets;

import java.util.Arrays;

public class EncryptionRequestPacket extends MinePacket {
	
	private String serverid = ""; 
	private byte[] publicKey = new byte[0];
	private byte[] token = new byte[0];
	
	public void setServerId(String serverid) {
		this.serverid = serverid;
	}
	
	public void setPublicKey(byte[] publicKey) {
		this.publicKey = publicKey;
	}
	
	public void setVerifyToken(byte[] token) {
		this.token = token;
	}
	
	public void assemblePacket() {
		
		int[] serveridbytes = this.createString(this.serverid);
		int packetLength = 1 + serveridbytes.length + 2 + publicKey.length + 2 + token.length;
		int[] finalbytes = new int[packetLength];
				
		finalbytes[0] = 1; // Packet ID
		int offset = 1;
		for(int i = 0 ; i < serveridbytes.length ; i++) {
			finalbytes[offset] = serveridbytes[i];
			offset++;
		}
		finalbytes[offset] = (publicKey.length&0xFF00)>>8;
		offset++;
		finalbytes[offset] = (publicKey.length&0xFF);
		offset++;
		
		for(int i = 0 ; i < publicKey.length ; i++) {
			finalbytes[offset] = publicKey[i];
			offset++;
		}
		finalbytes[offset] = (token.length&0xFF00)>>8;
		offset++;
		finalbytes[offset] = (token.length&0xFF);
		offset++;
		
		for(int i = 0 ; i < token.length ; i++) {
			finalbytes[offset] = token[i];
			offset++;
		}
		
		this.setPacketdetails(finalbytes);
	}
	
}
