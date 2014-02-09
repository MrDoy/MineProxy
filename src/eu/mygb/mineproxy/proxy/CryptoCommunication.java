package eu.mygb.mineproxy.proxy;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoCommunication implements Runnable {

	private byte[] sharedSecret = null;
	private Cipher cipher = null;
	
	private byte[] rawdata = null;
	private byte[] processeddata = null;
	private boolean continueThread = true;
	
	public synchronized byte[] getData() {
		synchronized(this) {
			while(this.processeddata == null) {
				try {
					this.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return this.processeddata;
		}
	}
	
	public synchronized void addData(byte[] data) {
		synchronized(this) {
			while(this.rawdata != null) {
				try {
					this.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			this.rawdata = data;
			this.processeddata = null;
			this.notifyAll();
		}
	}
	
	public void disableThread() {
		this.continueThread = false;
	}

	private void initCipherStream(int mode) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		Key key = new SecretKeySpec(this.sharedSecret, "AES");
		
		this.cipher = Cipher.getInstance("AES/CFB8/NoPadding");
		IvParameterSpec ivspec = new IvParameterSpec(this.sharedSecret);
		this.cipher.init(mode, key, ivspec);
	}
	
	public CryptoCommunication(byte[] sharedSecret, int mode) {
		this.sharedSecret = sharedSecret;
		try {
			this.initCipherStream(mode);
		} catch (InvalidKeyException | NoSuchAlgorithmException
				| NoSuchPaddingException | InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		while(this.continueThread) {
			synchronized(this) {
				while(this.rawdata == null) {
					try {
						this.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				this.processeddata = this.cipher.update(this.rawdata);
				this.rawdata = null;
				this.notifyAll();
			}
		}
	}

}
