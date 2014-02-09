package eu.mygb.mineproxy.packets;

import java.util.Arrays;

public class MinePacket {
	protected int length = -1;
	protected int[] packetdetails;
	private int offset;
	
		
	private int position = 0;
	public void resetPos() {
		this.position = 0;
	}
	
	public int nextVarInt() {
		this.varintHasNext = true;
		this.tmpvarint = 0;
		this.offsetvarint = 0;
		while(varintHasNext) {
			this.varintdecode(packetdetails[position]);
			this.position++;
		}
		return this.tmpvarint;
	}
	public byte[] nextByteArray(int length) {
		byte[] ba = new byte[length];
		for(int i = 0 ; i < length ; i++) {
			ba[i] = (byte) this.packetdetails[this.position];
			this.position++;
		}
		return ba;
	}
	
	public String nextString() {
		String str = "";
		int size = nextVarInt();
		for(int i = 0 ; i < size ; i++) {
			str += (char) this.packetdetails[this.position];
			this.position++;
		}
		return str;
	}
	
	public int[] createString(String str) {
		int[] strsize = this.varintencode(str.length());

		int[] ret = new int[strsize.length + str.length()];
		int offset = 0;
		for(int i = 0 ; i < strsize.length ; i++) {
			ret[offset] = strsize[i];
			offset++;
		}
		for(int i = 0 ; i < str.length() ; i++) {
			ret[offset] = str.charAt(i);
			offset++;
		}
		return ret;
	}
	
	public int nextShort() {
		int value = (int) this.getNextValue(2);
		return value;
	}
	
	public boolean nextBool() {
		int value = (int) this.getNextValue(1);
		return (value==1);
	}
	
	public int nextInt() {
		int value = (int) this.getNextValue(4);
		return value;
	}
	
	public long getNextValue(int bytenum) {
		long value = 0;
		for(int i = 0 ; i < bytenum ; i++) {
			value = value|((this.packetdetails[this.position]<<(8*(bytenum-1-i)))&0xFF);
			this.position++;
		}
		return value;
	}
	
	
	public int getPacketType() {
		return nextVarInt();
	}
	
	private boolean varintHasNext = true;
	private int tmpvarint = 0;
	private int offsetvarint = 0;
	private void varintdecode(int value) {
		
		if((value&128) != 0) {
			this.varintHasNext = true;
		} else {
			this.varintHasNext = false;
		}
		
		this.tmpvarint = this.tmpvarint | (value&127)<<(7*this.offsetvarint);
		
		if(this.varintHasNext) {
			this.offsetvarint++;
		}
	}
	
	private int[] varintencode(int value) {
		int count = 1;
		for(int i = 3 ; i >= 1 ; i--) {
			if(((value)&(127<<(7*i))) != 0) {
				count = i + 1;
				break;
			}
		}
		
		int[] ret = new int[count];
		
		for(int i = 0 ; i < count ; i++) {
			int hasNext = 0;
			if(i != count-1){
				hasNext = 1;
			}
			
			ret[i] = hasNext<<7 | ((value>>(7*i))&(127));
		}		
		
		return ret;
	}
	
	protected void finalizePacket() {
		
	}
	
	public void adddata(int data) {
		
		if(this.varintHasNext){
			varintdecode(data);
			if(!this.varintHasNext) {
				this.length = tmpvarint;
				this.packetdetails = new int[this.length];
				this.offset = 0;
			}
		} else {
			if(this.offset < this.length) {
				this.packetdetails[this.offset] = data; 
				this.offset++;
			} else {
				this.finalizePacket();
			}
		}
		
	}
	
	public boolean isComplete() {
		return (this.offset == this.length);
	}

	public int[] getPacketdetails() {
		return packetdetails;
	}
	
	public byte[] getCompletePacket() {
		int[] lengthvarint = varintencode(this.length);
		
		byte[] completepacket = new byte[lengthvarint.length +  this.length];
		
		for(int i = 0 ; i < lengthvarint.length ; i++) {
			completepacket[i] = (byte) lengthvarint[i];
		}
		for(int i = 0 ; i < this.length ; i++) {
			completepacket[i+lengthvarint.length] = (byte) this.packetdetails[i];
		}
		return completepacket;
	}
	
	public void setPacketdetails(int[] packetdetails) {
		this.packetdetails = packetdetails;
		this.length = this.packetdetails.length;
		this.finalizePacket();
	}

	public int getLength() {
		return length;
	}

	@Override
	public String toString() {
		return "MinePacket [length=" + length + ", packetdetails="
				+ Arrays.toString(packetdetails) + "]";
	}
	
	
}
