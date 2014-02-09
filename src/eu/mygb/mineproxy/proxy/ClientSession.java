package eu.mygb.mineproxy.proxy;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import eu.mygb.mineproxy.packets.EncryptionRequestPacket;
import eu.mygb.mineproxy.packets.EncryptionResponsePacket;
import eu.mygb.mineproxy.packets.MinePacket;

import javax.json.*;

public class ClientSession implements Runnable, Observer {
	
	private Socket clientSocket;
	private Socket remoteSocket;
	
	
	public static int STATE_NONE = 0;
	public static int STATE_HANDSHAKE = 1;
	public static int STATE_PLAY = 2;
	public static int STATE_STATUS = 3;
	public static int STATE_LOGIN = 4;
	public static int STATE_UNKNOWN = 999;
	private int state = STATE_HANDSHAKE;
	
	public static int REQUEST_NONE = 0;
	public static int REQUEST_STATUS = 1;
	private int request = REQUEST_NONE;
	
	private boolean disableModifyAfterMitm = true;

	private MineProxy mineproxy;
	/**
	 * @param clientSocket the created socket
	 * @throws IOException 
	 */
	public ClientSession(Socket clientSocket, InetAddress remoteAddr, int remotePort, MineProxy mineproxy) throws IOException {
		super();
		this.clientSocket = clientSocket;
		this.clientSocket.setKeepAlive(true);
		this.remoteSocket = new Socket(remoteAddr, remotePort);
		this.remoteSocket.setKeepAlive(true);
		
		this.mineproxy = mineproxy;
		System.out.println("New connection opened to "+remoteSocket.toString());
	}
	
	
	private Thread cthread;
	private Thread rthread;
	private CommunicationGateway gatewayclientserver;
	private CommunicationGateway gatewayserverclient;
	
	
	private KeyPair key;
	
	public byte[] getPublicKey() {
		byte[] pubkey = this.key.getPublic().getEncoded();
		return pubkey;
	}
	
	public void generateKey(int size) throws NoSuchAlgorithmException {
		KeyPairGenerator kpgen = KeyPairGenerator.getInstance("RSA"); 
		kpgen.initialize(size);
		this.key = kpgen.generateKeyPair();
	}
	
	public String computeHash(String serverid, byte[] sharedSecret, byte[] serverPublicKey ) {
		String resulthash = "";
		
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA1");
			md.update(serverid.getBytes());
			md.update(sharedSecret);
			md.update(serverPublicKey);
						
			byte[] hash = md.digest();
			
			if(hash[0] < 0) {
				resulthash += "-";
				
				for(int i = 0 ; i < hash.length ; i++) {
					hash[i] = (byte) ((~hash[i])&0xFF);
				}
				hash[hash.length-1] += 1;
				
			}
			
			for(int i = 0 ; i < hash.length ; i++) {
				if((hash[i]&0xF0) == 0 && i != 0) {
					resulthash += "0";
				}
				resulthash += Integer.toHexString((int) (hash[i]&0xFF));
			}
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return resulthash;
	}
	
	private String path = System.getProperty("user.home")+"\\AppData\\Roaming\\.minecraft\\";
	private String accessfile = "launcher_profiles.json";
	private String playeruuid = "";
	private String accessToken = "";
	
	public void initializeCredentials() {
		initializeCredentials(null);
	}
	
	public void initializeCredentials(String filepath) {
		try {
			if(filepath == null) {
				filepath = this.path+this.accessfile;
			}
			
			FileInputStream fis = new FileInputStream(filepath);
			JsonReader jsr = Json.createReader(fis);
			JsonObject jsar = jsr.readObject();
			String selectedProfile = jsar.getJsonString("selectedProfile").getString();
			this.playeruuid = jsar.getJsonObject("profiles").getJsonObject(selectedProfile).getJsonString("playerUUID").getString();
			this.accessToken = jsar.getJsonObject("authenticationDatabase").getJsonObject(playeruuid).getJsonString("accessToken").getString();
			
			jsr.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getAccessToken() {
		return this.accessToken;
	}
	
	public String getPlayerUUID() {
		return this.playeruuid;
	}
	
	@Override
	public void run() {
		gatewayclientserver = new ServerGateway(this.clientSocket, this.remoteSocket);
		gatewayserverclient = new ClientGateway(this.remoteSocket, this.clientSocket);
		
		gatewayclientserver.addObserver(this);
		gatewayserverclient.addObserver(this);
		
		cthread = new Thread(gatewayclientserver);
		rthread = new Thread(gatewayserverclient);
		cthread.start();
		rthread.start();
		
	}
	
	private PublicKey remoteserverPublicKey;
	private String serverid;
	private byte[] sharedSecret;
	
	@Override
	public void update(Observable o, Object arg) {
		MinePacket mp = ((CommunicationGateway)  o).getLastPacket();
		if(this.state != STATE_PLAY) {
			System.out.println(o.toString()+" "+mp.toString());
		}
		mp.resetPos();
		int packettype = mp.getPacketType();
		
		// When the client sends a packet
		if(o instanceof ServerGateway) {
			if(this.state == STATE_HANDSHAKE) {
				if(packettype == 0) {
					this.state = STATE_STATUS;
					
					int protocol = mp.nextVarInt();
					String str = mp.nextString();
					int port = mp.nextShort();
					int nextState = mp.nextVarInt();
					System.out.println("proto : "+protocol+" - str : "+str+" - port : "+port+" - "+nextState);
					
					if(nextState == 2) {
						this.state = STATE_LOGIN;
						System.out.println("Switched to login");
					} else if(nextState == 1) {
						this.state = STATE_STATUS;
					} else {
						this.state = STATE_UNKNOWN;
					}
				}
				
			} else if(this.state == STATE_STATUS) {
				if(packettype == 0) {
					this.request = REQUEST_STATUS;
					System.out.println("Request");
				} else if(packettype == 1) {
					System.out.println("Client ping");
				}
				
			} else if(this.state == STATE_LOGIN) {
				if(packettype == 0) {
					String str = mp.nextString();
					System.out.println("str : "+str);
					
				} else if(packettype == 1) {
					int lengthshared = mp.nextShort();
					byte[] sharedsecret = mp.nextByteArray(lengthshared);
					int lengthtoken = mp.nextShort();
					byte[] token = mp.nextByteArray(lengthtoken);
					
					System.out.println("Encryption response : ");
					System.out.println("Secret : length : "+lengthshared+"\n"+Arrays.toString(sharedsecret));
					System.out.println("Token : length : "+lengthtoken+"\n"+Arrays.toString(token));

					try {
						Cipher ciph = Cipher.getInstance("RSA");
						ciph.init(Cipher.DECRYPT_MODE, this.key.getPrivate());
						byte[] decodedtoken = ciph.doFinal(token);
						byte[] decodedsecret = ciph.doFinal(sharedsecret);
						
						this.sharedSecret = decodedsecret;
						
						System.out.println("Decoded Token : length : "+decodedtoken.length+"\n"+Arrays.toString(decodedtoken));
						System.out.println("Decoded Secret : length : "+decodedsecret.length+"\n"+Arrays.toString(decodedsecret));
						
						String serverHash = this.computeHash(serverid, decodedsecret, this.remoteserverPublicKey.getEncoded());

						try {
							URL url = new URL("https://sessionserver.mojang.com/session/minecraft/join");
							HttpURLConnection http = (HttpURLConnection) url.openConnection();
						 	http.setRequestMethod("POST");
						 	http.setRequestProperty("Content-Type", "application/json");
						 	
						 	String payload = "{\"accessToken\": \""+this.getAccessToken()+"\","
						 			+ "\"selectedProfile\": \""+this.getPlayerUUID()+"\","
						 			+ "\"serverId\": \""+serverHash+"\"}";
						 	
						 	System.out.println("Payload : "+payload);
						 	
						 	http.setUseCaches (false);
						 	http.setDoOutput(true);
						 	http.setDoInput(true);
						 	OutputStream httpos = http.getOutputStream();
						 	httpos.write(payload.getBytes());
						 	httpos.flush();
						 	httpos.close();
						 	System.out.println("Server returned : "+http.getResponseCode());

						 	
						} catch (MalformedURLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						
						EncryptionResponsePacket ers = new EncryptionResponsePacket();
						
						ers.setServerPublicKey(this.remoteserverPublicKey);
						ers.setSharedSecret(decodedsecret);
						ers.setToken(decodedtoken);
						ers.assemblePacket();
						this.gatewayclientserver.setCurrentPacket(ers);
						System.out.println("MODIF "+o.toString()+" "+ers.toString());
						
						
					} catch (NoSuchAlgorithmException e1) {
						e1.printStackTrace();
					} catch (NoSuchPaddingException e1) {
						e1.printStackTrace();
					} catch (InvalidKeyException e) {
						e.printStackTrace();
					} catch (IllegalBlockSizeException e) {
						e.printStackTrace();
					} catch (BadPaddingException e) {
						e.printStackTrace();
					}
					
					
					this.gatewayclientserver.setSharedSecret(this.sharedSecret, false);
					this.gatewayserverclient.setSharedSecret(this.sharedSecret, true);
					
					if(this.disableModifyAfterMitm) {
						this.gatewayclientserver.setModifyPackets(false);
						this.gatewayserverclient.setModifyPackets(false);
					}
				}
			}
		// When the server sends a packet
		} else if(o instanceof ClientGateway) {
			if(this.state == STATE_STATUS) {
				if(packettype == 0) {
					if(this.request == REQUEST_STATUS) {	
						String json = mp.nextString();
						System.out.println("json : "+json);
						this.request = REQUEST_NONE;
					}
				} else if(packettype == 1) {
					System.out.println("Server ping");

				}

			} else if(this.state == STATE_LOGIN) {
				if(packettype == 0) {
					String json = mp.nextString();
					System.out.println("disconnect : "+json);
					
				} else if(packettype == 1) {
					String serverid = mp.nextString();
					this.serverid = serverid;
					int lengthpkey = mp.nextShort();
					byte[] pkey = mp.nextByteArray(lengthpkey);
					int lengthtoken = mp.nextShort();
					byte[] token = mp.nextByteArray(lengthtoken);
					
					try {
						KeyFactory kf = KeyFactory.getInstance("RSA");
						KeySpec keySpec = new X509EncodedKeySpec(pkey);
						this.remoteserverPublicKey = kf.generatePublic(keySpec);
					} catch (NoSuchAlgorithmException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvalidKeySpecException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					System.out.println("serverid : \""+serverid+"\"");
					System.out.println("Key : length : "+lengthpkey+"\n"+Arrays.toString(pkey));
					System.out.println("Token : length : "+lengthtoken+"\n"+Arrays.toString(token));
					
					try {
						this.generateKey(1024);
						byte[] publicKey = this.getPublicKey();
						EncryptionRequestPacket erp = new EncryptionRequestPacket();
						erp.setServerId(serverid);
						erp.setPublicKey(publicKey);
						erp.setVerifyToken(token);
						erp.assemblePacket();
						
						System.out.println("MODIF "+o.toString()+" "+erp.toString());
						
						this.gatewayserverclient.setCurrentPacket(erp);
						
					} catch (NoSuchAlgorithmException e1) {
						e1.printStackTrace();
					}
					
					
				} else if(packettype == 2) {
					System.out.println("Login success");
					
					this.state = STATE_PLAY;
										
				} else if(packettype == 64) {
					
					String reason = mp.nextString();
							
					System.out.println("Disconnect "+reason);
					
					try {
						this.clientSocket.close();
						this.remoteSocket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		}
		mp.resetPos();
		PacketNotification pn = new PacketNotification(mp, (CommunicationGateway) o, state);
		for(PacketListener pl : this.mineproxy.getPacketListeners()) {
			pl.receivePacket(pn);
		}
	}
	
}
