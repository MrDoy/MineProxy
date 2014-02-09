package eu.mygb.mineproxy.packets;

import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class ChunkBulkDataPacket extends MinePacket {
	
	private int columnCount = -1;
	private int compressedSize = -1;
	private boolean skylight = false;
	private byte[] compressedData = null;
	// Meta
	private int xCoordinates = -1;
	private int yCoordinates = -1;
	private int primaryBitmap = -1;
	private int addBitmap = -1;

	
	
	public int getColumnCount() {
		return columnCount;
	}

	public boolean isSkylight() {
		return skylight;
	}

	public int getxCoordinates() {
		return xCoordinates;
	}

	public int getyCoordinates() {
		return yCoordinates;
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

	public ChunkBulkDataPacket() {
		super();
	}
	
	public ChunkBulkDataPacket(MinePacket packet) {
		super();
		this.setPacketdetails(packet.getPacketdetails());
	}

	@Override
	protected void finalizePacket() {
		super.finalizePacket();
		this.resetPos();
		this.nextVarInt();
		this.columnCount = this.nextShort();
		this.compressedSize = this.nextInt();
		this.skylight = this.nextBool();
		this.compressedData = this.nextByteArray(this.compressedSize);
		this.xCoordinates = this.nextInt();
		this.yCoordinates = this.nextInt();
		this.primaryBitmap = this.nextShort();
		this.addBitmap = this.nextShort();
	}

	@Override
	public String toString() {
		return "ChunkBulkDataPacket [columnCount=" + columnCount
				+ ", compressedSize=" + compressedSize + ", skylight="
				+ skylight + ", compressedData="
				+ Arrays.toString(compressedData) + ", xCoordinates="
				+ xCoordinates + ", yCoordinates=" + yCoordinates
				+ ", primaryBitmap=" + primaryBitmap + ", addBitmap="
				+ addBitmap + "]";
	}
	
	
}
