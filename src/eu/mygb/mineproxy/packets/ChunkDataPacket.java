package eu.mygb.mineproxy.packets;

import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class ChunkDataPacket extends MinePacket {
	
	private int xCoordinate = -1;
	private int yCoordinate = -1;
	private boolean groundUp = false;
	private int primaryBitmap = -1;
	private int addBitmap = -1;
	private int compressedSize = -1;
	private byte[] compressedData = null;

	public int getxCoordinate() {
		return xCoordinate;
	}

	public int getyCoordinate() {
		return yCoordinate;
	}

	public boolean isGroundUp() {
		return groundUp;
	}

	public int getPrimaryBitmap() {
		return primaryBitmap;
	}

	public int getAddBitmap() {
		return addBitmap;
	}

	public int getCompressedSize() {
		return compressedSize;
	}

	public byte[] getCompressedData() {
		return compressedData;
	}
	
	private byte[] decompressData() throws DataFormatException {
		Inflater inflater = new Inflater();
		inflater.setInput(this.compressedData);
		byte[] ret = new byte[inflater.getTotalOut()];
		inflater.inflate(ret);
		return ret;
	}
	
	public byte[] getData() {
		byte[] ret = null;
		try {
			 this.decompressData();
		} catch (DataFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

	public ChunkDataPacket() {
		super();
	}
	
	public ChunkDataPacket(MinePacket packet) {
		super();
		this.setPacketdetails(packet.getPacketdetails());
	}

	@Override
	protected void finalizePacket() {
		super.finalizePacket();
		this.resetPos();
		this.nextVarInt();
		this.xCoordinate = this.nextInt();
		this.yCoordinate = this.nextInt();
		this.groundUp = this.nextBool();
		this.primaryBitmap = this.nextShort();
		this.addBitmap = this.nextShort();
		this.compressedSize = this.nextInt();
		this.compressedData = this.nextByteArray(this.compressedSize);
	}

	@Override
	public String toString() {
		return "ChunkDataPacket [xCoordinates=" + xCoordinate
				+ ", yCoordinates=" + yCoordinate + ", groundUp=" + groundUp
				+ ", primaryBitmap=" + primaryBitmap + ", addBitmap="
				+ addBitmap + ", compressedSize=" + compressedSize
				+ ", compressedData=" + Arrays.toString(compressedData) + "]";
	}
	
	
	
}
