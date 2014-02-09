package eu.mygb.mineproxy.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Observable;
import javax.crypto.Cipher;

import eu.mygb.mineproxy.packets.MinePacket;

public class CommunicationGateway extends Observable implements Runnable {
	protected Socket in;
	protected Socket out;
	
	MinePacket currentPacket;
	
	private boolean cipherstarted = false;
	private boolean ordercipher = false;
	
	private boolean lastpacket = false;
	private boolean listeningEnabled = true;
	private boolean modifyPackets = true;
	
	public boolean isListeningEnabled() {
		return listeningEnabled;
	}

	public void setListeningEnabled(boolean listeningEnabled) {
		this.listeningEnabled = listeningEnabled;
	}

	public boolean isModifyPackets() {
		return modifyPackets;
	}

	public void setModifyPackets(boolean modifyPackets) {
		if(!modifyPackets) {
			this.lastpacket = true;
		}
		this.modifyPackets = modifyPackets;
	}

	private CryptoCommunication cce = null;
	private CryptoCommunication ccd = null;
	
	public void setSharedSecret(byte[] sharedSecret, boolean start) {
		
		if(sharedSecret != null) {
			
			ccd = new CryptoCommunication(sharedSecret, Cipher.DECRYPT_MODE);
			Thread thccd = new Thread(ccd);
			thccd.start();
			
			cce = new CryptoCommunication(sharedSecret, Cipher.ENCRYPT_MODE);
			Thread thcce = new Thread(cce);
			thcce.start();
			
			this.ordercipher = false;
			this.cipherstarted = start;
			if(!start) {
				this.ordercipher = true;
			}
		}
	}

	public void setCurrentPacket(MinePacket minepacket) {
		this.currentPacket = minepacket;
	}
	
	public MinePacket getLastPacket() {
		return this.currentPacket;
	}
	
	protected void finalizePacket() throws IOException {		
		this.setChanged();
		this.notifyObservers();
	}
	
	/**
	 * @param in where to receive the packets
	 * @param out where to forward the packets
	 */
	public CommunicationGateway(Socket in, Socket out) {
		super();
		this.in = in;
		this.out = out;
	}
	
	public void processData(byte[] data) throws IOException {
		
		if(!this.listeningEnabled || (this.cipherstarted && !this.modifyPackets && !this.lastpacket)) {
			os.write(data);
		}
		
		if(this.listeningEnabled) {
			if(this.cipherstarted) {
				byte[] data_orig = data.clone();
				
				ccd.addData(data_orig);
				data = ccd.getData();
			}
			
			for(byte b : data) {
				if(this.currentPacket == null) {
					this.currentPacket = new MinePacket();
				}
				
				this.currentPacket.adddata(b);
				
				if(this.currentPacket.isComplete()) {
					this.finalizePacket();
					if(this.modifyPackets || this.lastpacket) {
						if(this.cipherstarted) {
							
							cce.addData(this.currentPacket.getCompletePacket());
							os.write(cce.getData());
							
						} else {
							
							os.write(this.currentPacket.getCompletePacket());
							if(this.ordercipher) {
								this.cipherstarted = true;
								this.ordercipher = false;
							}
						}
						this.lastpacket = false;
					} 
					
					this.currentPacket = null;
					
				}
			}
		}
			
		
	}
	

	private InputStream is = null;
	private OutputStream os = null;
	
	
	@Override
	public void run() {
		boolean running = true;
		this.cipherstarted = false;
				
		try {
			this.is = in.getInputStream();
			this.os = out.getOutputStream();
			
			
			while(running){
				
				byte[] bitread = new byte[is.available()];
				is.read(bitread);
				if(bitread.length != 0) {
					this.processData(bitread);
				}
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
