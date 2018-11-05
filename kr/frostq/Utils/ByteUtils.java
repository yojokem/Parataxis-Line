package kr.frostq.Utils;

import java.nio.*;
import java.nio.charset.Charset;

public class ByteUtils {
	public static final void printBytes16Bit(byte[] bit) {
		String r = "";
		for(byte b : bit)
			r += String.format("%02x", b) + " ";
		System.out.println(r);
	}
	
	public static byte[] str2bytes(String str) {
		return str.getBytes(Charset.forName("UTF-8"));
	}
	
	public static String bytes2str(byte[] data) {
		return new String(data, Charset.forName("UTF-8"));
	}
	
	public static byte[] intToBytes(final int i) {
		ByteBuffer buffer = ByteBuffer.allocate(Integer.SIZE / 8);
		buffer.putInt(i);
		buffer.order(ByteOrder.BIG_ENDIAN);
		return buffer.array();
	}
	
	public static int bytesToInt(byte[] b) {
		ByteBuffer buffer = ByteBuffer.allocate(4);
		final byte[] newBytes = new byte[4];
		for(int i = 0; i < 4; i++) {
			if(i + b.length < 4)
				newBytes[i] = (byte) 0x00;
			else
				newBytes[i] = b[i + b.length - 4];
		}
		
		buffer = ByteBuffer.wrap(newBytes);
		buffer.order(ByteOrder.BIG_ENDIAN);
		return buffer.getInt();
	}
	
	public static byte[] floatToBytes(float x) {
		return ByteBuffer.allocate(8).putFloat(x).array();
	}
	
	public static float bytesToFloat(byte[] b) {
		return ByteBuffer.wrap(b, 0, 4).order(ByteOrder.BIG_ENDIAN).getFloat();
	}
	
	public static byte[] longToBytes(long x) {
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.putLong(x);
		return buffer.array();
	}
	
	public static long bytesToLong(byte[] bits) {
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.put(bits, 0, bits.length);
		buffer.flip();
		return buffer.getLong();
	}
	
	public static byte[] toByteArray(double value) {
	    byte[] bytes = new byte[8];
	    ByteBuffer.wrap(bytes).putDouble(value);
	    return bytes;
	}

	public static double toDouble(byte[] bytes) {
	    return ByteBuffer.wrap(bytes).getDouble();
	}
}