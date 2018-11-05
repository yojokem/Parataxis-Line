package kr.frostq.Networks.Protocols.ParallelFunctionCommunicationProtocol.Packet;

import java.time.Clock;
import java.util.Arrays;
import java.util.Date;

import com.google.common.base.Preconditions;

import kr.frostq.Networks.Protocols.ParallelFunctionCommunicationProtocol.BroadcastRadar;
import kr.frostq.Networks.Protocols.ParallelFunctionCommunicationProtocol.Position;
import kr.frostq.Utils.ByteUtils;

public class PFCPPacket {
	public static final int[] RESERVED = new int[] {
			0x00000000, // 
			0xAAAAAAAA, // ARF ID
			0xFFFFFFFF  // 
	};
	
	private int pktId;
	private Position src, dst;
	public static final byte
		ENCRYPTED = 0x04,
		DECRYPTED = 0x07,
		
		RAW = 0x00;
	private byte encrypted;
	private byte encoding;
	public static byte[] TYPE =
			new byte[] {
					
					0x01, // Exchange (UNICAST)
					0x7F  // Broadcast
					// And so on
					
			};
	private byte type;
	private byte[] payload;
	private int /*nextPacketIDAllow*/range = /*DEFAULTLY,*/100;
	private byte[] chk1, chk2;
	private int packet_size,
		srcPosSize = 0,
		srcPosIdSize = 0,
		srcPosNameSize = 0,
		dstPosSize = 0,
		dstPosIdSize = 0,
		dstPosNameSize = 0,
		
		payload_size = 0;
	
	private volatile boolean freeze = false;
	private static final java.util.Random rander = new java.util.Random();
	
	public byte[] createPacketBytes() {
		freeze = true;
		
		auto_checksum();
		byte[] result = new byte[this.packet_size];
		
		try {
			System.arraycopy(ByteUtils.intToBytes(this.getPktId()), 0, result, 0, 4);
			
			byte[] srcData = src.getData();
			System.arraycopy(srcData, 0, result, 4, srcData.length);
			
			byte[] dstData = dst.getData();
			System.arraycopy(dstData, 0, result, 4 + srcData.length, dstData.length);
			
			System.arraycopy(new byte[] { encrypted }, 0, result, 4 + srcData.length + dstData.length, 1);
			System.arraycopy(new byte[] { encoding }, 0, result, 4 + srcData.length + dstData.length + 1, 1);
			System.arraycopy(new byte[] { type }, 0, result, 4 + srcData.length + dstData.length + 1 + 1, 1);
			
			System.arraycopy(payload, 0, result, 4 + srcData.length + dstData.length + 1 + 1 + 1, payload.length);
			
			System.arraycopy(ByteUtils.intToBytes(range), 0, result, 4 + srcData.length + dstData.length + 1 + 1 + 1 + payload.length, 4);
			
			System.arraycopy(chk1, 0, result, 4 + srcData.length + dstData.length + 1 + 1 + 1 + payload.length + 4, 8);
			
			System.arraycopy(chk2, 0, result, 4 + srcData.length + dstData.length + 1 + 1 + 1 + payload.length + 4 + 8, 4);
			
			System.arraycopy(ByteUtils.intToBytes(this.packet_size), 0, result, 4 + srcData.length + dstData.length + 1 + 1 + 1 + payload.length + 4 + 8 + 4, 4);
			System.arraycopy(ByteUtils.intToBytes(srcPosSize), 0, result, 4 + srcData.length + dstData.length + 1 + 1 + 1 + payload.length + 4 + 8 + 4 + 4, 4);
			System.arraycopy(ByteUtils.intToBytes(srcPosIdSize), 0, result, 4 + srcData.length + dstData.length + 1 + 1 + 1 + payload.length + 4 + 8 + 4 + 4 + 4, 4);
			System.arraycopy(ByteUtils.intToBytes(srcPosNameSize), 0, result, 4 + srcData.length + dstData.length + 1 + 1 + 1 + payload.length + 4 + 8 + 4 + 4 + 4 + 4, 4);
			System.arraycopy(ByteUtils.intToBytes(dstPosSize), 0, result, 4 + srcData.length + dstData.length + 1 + 1 + 1 + payload.length + 4 + 8 + 4 + 4 + 4 + 4 + 4, 4);
			System.arraycopy(ByteUtils.intToBytes(dstPosIdSize), 0, result, 4 + srcData.length + dstData.length + 1 + 1 + 1 + payload.length + 4 + 8 + 4 + 4 + 4 + 4 + 4 + 4, 4);
			System.arraycopy(ByteUtils.intToBytes(dstPosNameSize), 0, result, 4 + srcData.length + dstData.length + 1 + 1 + 1 + payload.length + 4 + 8 + 4 + 4 + 4 + 4 + 4 + 4 + 4, 4);
			System.arraycopy(ByteUtils.intToBytes(payload_size), 0, result, 4 + srcData.length + dstData.length + 1 + 1 + 1 + payload.length + 4 + 8 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4, 4);
		} catch (Exception e) {
			System.err.println("Error !!! While creating the packet {" + getPktId() + "} .");
			e.printStackTrace();
		}
		
		byte[] checker = new byte[4];
		System.arraycopy(result, result.length - 4, checker, 0, 4);
		
		Preconditions.checkState(Arrays.equals(checker, ByteUtils.intToBytes(payload_size)), "[PFCPP-createPacket]1 The byte copying error was occurred. Cancel the creating packet {" + getPktId() + "} .");
		
		System.arraycopy(result, result.length - 4 * 8, checker, 0, 4);
		Preconditions.checkState(Arrays.equals(checker, ByteUtils.intToBytes(this.packet_size)), "[PFCPP-createPacket] The size of packet is not equals to checksummed packet size. (((())))");
		
		freeze = false;
		return result;
	}
	
	public PFCPPacket() {
		
	}
	
	public PFCPPacket(byte[] received, int off, int len) {
		byte[] data = new byte[len];
		System.arraycopy(received, off, data, 0, len);
		
		byte[] tempInt = new byte[4];
		byte[] tempLd = new byte[8];
		
		int pktId, range, packet_size,
		srcPosSize, srcPosIdSize, srcPosNameSize,
		dstPosSize, dstPosIdSize, dstPosNameSize, payload_size;
		Position src, dst;
		byte encrypted, encoding, type;
		byte[] payload, chk1, chk2;
		
		System.arraycopy(data, 0, tempInt, 0, 4);
		pktId = ByteUtils.bytesToInt(tempInt);
		
		System.arraycopy(data, data.length - 4 * 12, tempInt, 0, 4);
		range = ByteUtils.bytesToInt(tempInt);
		
		System.arraycopy(data, data.length - 4 * 11, tempLd, 0, 8);
		chk1 = tempLd;
		
		System.arraycopy(data, data.length - 4 * 9, tempInt, 0, 4);
		chk2 = tempInt;
		
		System.arraycopy(data, data.length - 4 * 8, tempInt, 0, 4);
		packet_size = ByteUtils.bytesToInt(tempInt);
		
		Preconditions.checkArgument(packet_size == data.length, "[CONST-PFCPP] Packet size does not match in the data of the packet, constructor - packet data compiliation error." + System.lineSeparator() +
				"Packet Size : " + packet_size + " | Packet bytes length : " + data.length + System.lineSeparator());
		
		// ByteUtils.printBytes16Bit(data);
		
		System.arraycopy(data, data.length - 4 * 7, tempInt, 0, 4);
		srcPosSize = ByteUtils.bytesToInt(tempInt);
		
		System.arraycopy(data, data.length - 4 * 6, tempInt, 0, 4);
		srcPosIdSize = ByteUtils.bytesToInt(tempInt);
		
		System.arraycopy(data, data.length - 4 * 5, tempInt, 0, 4);
		srcPosNameSize = ByteUtils.bytesToInt(tempInt);
		
		System.arraycopy(data, data.length - 4 * 4, tempInt, 0, 4);
		dstPosSize = ByteUtils.bytesToInt(tempInt);
		
		System.arraycopy(data, data.length - 4 * 3, tempInt, 0, 4);
		dstPosIdSize = ByteUtils.bytesToInt(tempInt);
		
		System.arraycopy(data, data.length - 4 * 2, tempInt, 0, 4);
		dstPosNameSize = ByteUtils.bytesToInt(tempInt);
		
		System.arraycopy(data, data.length - 4 * 1, tempInt, 0, 4);
		payload_size = ByteUtils.bytesToInt(tempInt);
		
		tempInt = new byte[srcPosSize];
		System.arraycopy(data, 4, tempInt, 0, srcPosSize);
		src = Position.getPosition(tempInt, srcPosIdSize, srcPosNameSize);
		Position.lesserPosition(src);
		
		tempInt = new byte[dstPosSize];
		System.arraycopy(data, 4 + srcPosSize, tempInt, 0, dstPosSize);
		dst = Position.getPosition(tempInt, dstPosIdSize, dstPosNameSize);
		Position.lesserPosition(dst);
		
		tempLd = new byte[1];
		System.arraycopy(data, 4 + srcPosSize + dstPosSize, tempLd, 0, 1);
		encrypted = tempLd[0];
		
		System.arraycopy(data, 4 + srcPosSize + dstPosSize + 1, tempLd, 0, 1);
		encoding = tempLd[0];
		
		System.arraycopy(data, 4 + srcPosSize + dstPosSize + 1 + 1, tempLd, 0, 1);
		type = tempLd[0];
		
		payload = new byte[payload_size];
		System.arraycopy(data, 4 + srcPosSize + dstPosSize + 1 + 1 + 1, payload, 0, payload_size);
		
		tempInt = tempLd = null;
		
		this.setPktId(pktId);
		this.setSrc(src);
		this.setDst(dst);
		this.setEncrypted(encrypted);
		this.setEncoding(encoding);
		this.setType(type);
		this.setPayload(payload);
		this.setRange(range);
		this.chk1 = chk1;
		this.chk2 = chk2;
		
		this.packet_size = packet_size;
		this.srcPosSize = srcPosSize;
		this.srcPosIdSize = srcPosIdSize;
		this.srcPosNameSize = srcPosNameSize;
		this.dstPosSize = dstPosSize;
		this.dstPosIdSize = dstPosIdSize;
		this.dstPosNameSize = dstPosNameSize;
		this.payload_size = payload_size;
	}
	
	public int getPktId() {
		return this.pktId;
	}
	
	public void setPktId(int pktId) {
		if(!freeze && (pktId > -1000000000 || pktId < 1000000000) && !BroadcastRadar.isReceived(pktId))
			this.pktId = pktId;
	}
	
	private final int randomed() {
		int rand = rander.nextInt(1000000000 + 1);
		int ye = rander.nextInt(2);
		
		if(ye == 1) rand *= -1;
		return rand;
	}
	
	public int randomPktId() {
		int rand = randomed();
		
		while(BroadcastRadar.isReceived(rand))
			rand = randomed();
		
		return rand;
	}
	
	public Position getSrc() {
		return src;
	}

	public PFCPPacket setSrc(Position src) {
		if(!freeze) {
			if(getDst() != null) {
				Preconditions.checkArgument(Arrays.equals(src.getDimensionName(), getDst().getDimensionName()), "[PFCPP-srcset] The dimension of source and destination has to be equivalent with each other.");
				Preconditions.checkState(src.getPos().length == dst.getPos().length, "[PFCPP-srcset] The length of each position has to be equivalent with each other.");
			}
			
			Position.lesserPosition(src);
			
			this.src = src;
			
			this.srcPosIdSize = this.src.getID().length;
			this.srcPosNameSize = this.src.getDimensionName().length;
			this.srcPosSize = this.src.getData().length;
			
			Position.lesserPosition(this.src);
			
			auto_checksum();
		}
		
		return this;
	}

	public Position getDst() {
		return dst;
	}

	public void setDst(Position dst) {
		if(!freeze) {
			if(getSrc() != null) {
				Preconditions.checkArgument(Arrays.equals(src.getDimensionName(), dst.getDimensionName()), "[PFCPP-srcset] The dimension of source and destination has to be equivalent with each other.");
				Preconditions.checkState(src.getPos().length == dst.getPos().length, "[PFCPP-srcset] The length of each position has to be equivalent with each other.");
			}
			
			Position.lesserPosition(dst);
			
			this.dst = dst;
			
			this.dstPosIdSize = this.dst.getID().length;
			this.dstPosNameSize = this.dst.getDimensionName().length;
			this.dstPosSize = this.dst.getData().length;
			
			Position.lesserPosition(this.dst);
			
			auto_checksum();
		}
	}

	public byte getEncrypted() {
		return encrypted;
	}

	public void setEncrypted(byte encrypted) {
		if(!freeze)
			this.encrypted = encrypted;
	}

	public byte getEncoding() {
		return encoding;
	}

	public void setEncoding(byte encoding) {
		if(!freeze)
			this.encoding = encoding;
	}

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		if(!freeze)
			this.type = type;
	}

	public byte[] getPayload() {
		return payload;
	}

	public void setPayload(byte[] payload) {
		if(!freeze) {
			this.payload = payload;
			
			this.payload_size = this.payload.length;
			
			auto_checksum();
		}
	}

	public int getRange() {
		return range;
	}

	public void setRange(int range) {
		if(!freeze)
			this.range = range;
	}

	public byte[] getChk1() {
		return chk1;
	}
	
	public void setChk1(long date) {
		if(!freeze)
			this.chk1 = ByteUtils.longToBytes(date);
	}
	
	public void setChk1(Date date) {
		if(!freeze)
			this.chk1 = ByteUtils.longToBytes(date.getTime());
	}

	public byte[] getChk2() {
		return chk2;
	}
	
	protected void setChk2(int alls) {
		this.chk2 = ByteUtils.intToBytes(alls);
	}

	public int getPacketSize() {
		return packet_size;
	}

	public void auto_checksum() {
		if(src != null && dst != null && payload != null) {
			this.packet_size = 4 +
					(src.getData().length) +
					(dst.getData().length) +
					1 +
					1 +
					1 +
					this.payload_size +
					4 +
					8 +
					4 * 9;
			
			setChk1(Clock.systemUTC().millis());
			int num = getPktId();
			double sum = 0;
			while(num != 0) {
				sum += num % 10;
				num /= 10;
			}
			
			sum -= (src.sum() + dst.sum());
			sum += type;
			
			setChk2((int) sum);
		}
	}
	
	public static final int MINIMUMSIZE() {
		return 4 + 10 + 10 + 1 + 1 + 1 + 0 + 4 + 8 + 4 * 9;
	}
	
	public final boolean isBroadcast() {
		return Arrays.stream(RESERVED).anyMatch(reserved -> reserved == getPktId());
	}
	
	public final boolean isExchange() {
		return !isBroadcast();
	}
	
	public String getIDByStr() {
		return String.format("%10d", this.pktId);
	}
}