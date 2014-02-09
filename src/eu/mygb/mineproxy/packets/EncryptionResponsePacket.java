package eu.mygb.mineproxy.packets;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class EncryptionResponsePacket extends MinePacket {
	private byte[] sharedsecret = new byte[0];
	private byte[] token = new byte[0];
	private PublicKey key;
	
	public void setSharedSecret(byte[] sharedsecret) {
		this.sharedsecret = sharedsecret;
	}
	
	public void setToken(byte[] token) {
		this.token = token;
	}
	
	public void setServerPublicKey(PublicKey key) {
		this.key = key;
	}
	
	private byte[] encode(byte[] encoding) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		Cipher ciph = Cipher.getInstance("RSA");
		ciph.init(Cipher.ENCRYPT_MODE, this.key);
		byte[] encoded = ciph.doFinal(encoding);
		return encoded;
	}
	
	public void assemblePacket() {
		
		byte[] tokenenc = new byte[0];
		byte[] sharedsecretenc = new byte[0];
			
		try {
			tokenenc = this.encode(this.token);
			sharedsecretenc = this.encode(this.sharedsecret);

		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		int packetLength = 1 + 2 + sharedsecretenc.length + 2 + tokenenc.length;

		int[] finalbytes = new int[packetLength];
				
		finalbytes[0] = 1; // Packet ID
		int offset = 1;
		finalbytes[offset] = (sharedsecretenc.length&0xFF00)>>8;
		offset++;
		finalbytes[offset] = (sharedsecretenc.length&0xFF);
		offset++;
		
		for(int i = 0 ; i < sharedsecretenc.length ; i++) {
			finalbytes[offset] = sharedsecretenc[i];
			offset++;
		}
		finalbytes[offset] = (tokenenc.length&0xFF00)>>8;
		offset++;
		finalbytes[offset] = (tokenenc.length&0xFF);
		offset++;
		
		for(int i = 0 ; i < tokenenc.length ; i++) {
			finalbytes[offset] = tokenenc[i];
			offset++;
		}
		
		this.setPacketdetails(finalbytes);
	}
	
}
