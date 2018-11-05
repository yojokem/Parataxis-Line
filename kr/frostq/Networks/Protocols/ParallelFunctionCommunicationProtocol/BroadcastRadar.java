package kr.frostq.Networks.Protocols.ParallelFunctionCommunicationProtocol;

import java.io.FileOutputStream;
import java.nio.file.Paths;
import java.util.Vector;

import kr.frostq.Axis.PathConstants;
import kr.frostq.Axis.Synchronization;
import kr.frostq.Networks.Protocols.ParallelFunctionCommunicationProtocol.Packet.PFCPPacket;
import kr.frostq.Utils.ByteUtils;
import kr.frostq.Utils.PathStreamer;

public class BroadcastRadar {
	protected static final Vector<Integer> PKT_IDS = new Vector<Integer>();
	protected static final Vector<PointNode> Nodes = new Vector<PointNode>();
	
	public static void received(PFCPPacket pkt, boolean fs) {
		if(pkt.isExchange())
			PKT_IDS.addElement(pkt.getPktId());
		
		Synchronization.processSync(Synchronization.sync(() -> {
			if(pkt.isBroadcast()) transprocess(pkt);
			//if(fs) savePacket(pkt);
		}));
	}
	
	private static final void transprocess(PFCPPacket pkt) {
		
	}
	
	public static boolean isReceived(int pktId) {
		return PKT_IDS.contains(pktId);
	}
	
	public static void savePacket(PFCPPacket rcPkt) {
		if(rcPkt.isBroadcast()) return;
		
		PathStreamer streamer = new PathStreamer(Paths.get(PathConstants.getConstant("rcPkts") + "/Packet_" + rcPkt.getIDByStr() + ".pktDt"), PathStreamer.OUTPUT);
		try {
			FileOutputStream fos = (FileOutputStream) streamer.getResult();
			fos.write(rcPkt.createPacketBytes());
			fos.flush();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static class ARFPacket extends PFCPPacket {
		public static final byte[]
				prefix = ByteUtils.str2bytes("[ARF]"),
				suffix = ByteUtils.str2bytes("[/ARF]");
		
		public static final int CONNECT_FLAG = 0xFFFFE;
		public static final int DISCONNECT_FLAG = 0xFFFFF;
		
		@Deprecated
		public static final int WHOAREYOU = 0xABCDE;
		
		public static final int OKAY = 0x00;
		public static final int ACCEPT = 0x03;
		
		public static final int DECLINE = 0x01;
		
		public ARFPacket() {
			super.setPktId(0xAAAAAAAA);
			super.setEncoding(PFCPPacket.RAW);
			super.setEncrypted(PFCPPacket.DECRYPTED);
			super.setType((byte) 0x01);
			super.setRange(1);
		}
		
		public ARFPacket setFlag(int flag) {
			setPayload(ByteUtils.intToBytes(flag));
			return this;
		}
		
		public static int getFlag(byte[] flagdata) {
			return ByteUtils.bytesToInt(flagdata);
		}
		
		public void broadcast(final PointNode node) throws Throwable {
			super.setSrc(node.getPosition());
			node.broadcast(this);
		}
	}
}