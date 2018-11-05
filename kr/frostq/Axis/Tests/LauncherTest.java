package kr.frostq.Axis.Tests;

import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Clock;

import kr.frostq.Networks.Protocols.ParallelFunctionCommunicationProtocol.BroadcastRadar.ARFPacket;
import kr.frostq.Networks.Protocols.ParallelFunctionCommunicationProtocol.Dimension;
import kr.frostq.Networks.Protocols.ParallelFunctionCommunicationProtocol.PointNode;
import kr.frostq.Networks.Protocols.ParallelFunctionCommunicationProtocol.Position;
import kr.frostq.Networks.Protocols.ParallelFunctionCommunicationProtocol.Packet.PFCPPacket;
import kr.frostq.Utils.ThreadManager;

public class LauncherTest {
	public static void main(String[] args) throws SocketException {
		Dimension d = new Dimension(new byte[] {1});
		Position p1 = new Position("A".getBytes(), new byte[] {1}, 2, new double[] {0.5, 0});
		Position p2 = new Position("B".getBytes(), new byte[] {1}, 2, new double[] {312.5555, 0});
		
		double distance = Position.variatedDistance(p1, p2);
		/*System.out.println("거리: " + distance);
		System.out.println(d.getById("A".getBytes()));
		System.out.println(d.getById("B".getBytes()));*/
		
		//p1.slightlyMove(p2, 0.0277775 / distance, distance, 0, System.out);
		
		long now1 = Clock.systemUTC().millis();
		
		PFCPPacket p = new PFCPPacket();
		p.setPktId(p.randomPktId());
		p.setSrc(p1).setDst(p2);
		p.setEncoding(PFCPPacket.RAW);
		p.setEncrypted(PFCPPacket.DECRYPTED);
		p.setType((byte) 0x01);
		try {
			p.setPayload(Files.readAllBytes(Paths.get("C:\\Users\\Stockholm\\Desktop\\1000%.txt")));
		} catch (IOException e) {
			e.printStackTrace(System.err);
			p.setPayload(new byte[] {1,2,3,4});
		}
		p.setPayload(new byte[]{12,23,14, 2, 3, 32, 15, 3, 0});
		
		byte[] cc = p.createPacketBytes();
		
		PFCPPacket pp = new PFCPPacket(cc, 0, cc.length);
		//System.out.println(Arrays.equals(p.getPayload(), pp.getPayload()));
		
		long now2 = Clock.systemUTC().millis();
		
		//System.out.println((now2 - now1));
		
		PointNode a = new PointNode(p1, false) {
			@Override
			public void receive(PFCPPacket packet) {
				System.out.println("A received: " + packet.getIDByStr());
			}
		};
		
		PointNode b = new PointNode(p2, false) {
			@Override
			public void receive(PFCPPacket packet) {
				System.out.println("B received: " + packet.getIDByStr());
			}
		};
		
		b.startOwnReceiver(null);
		
		ThreadManager.runThread(ThreadManager.createThread(() -> {
			try {
				Thread.sleep(2000);
				a.broadcast(p);
				Thread.sleep(2000);
				ARFPacket arf = new ARFPacket();
				arf.setFlag(ARFPacket.OKAY);
				arf.broadcast(a);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}, "Sender"), 2);
	}
	
	public static final void testUDP() throws Exception {
		new Thread(() -> {
			DatagramSocket server;
			try {
				server = new DatagramSocket(3213);
				byte[] b = new byte[21];
				DatagramPacket p = new DatagramPacket(b, b.length);
				
				while(true) {
					server.receive(p);
					System.out.println(p.getAddress());
					System.out.println(p.getPort());
					System.out.println(new String(p.getData(), Charset.forName("UTF-8")));
					server.close();
					System.exit(0);
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}).start();
		
		DatagramSocket s = new DatagramSocket();
		
		String s2 = "안녕하세요";
		s.send(new DatagramPacket(s2.getBytes(Charset.forName("UTF-8")), s2.getBytes(Charset.forName("UTF-8")).length, InetAddress.getLocalHost(), 3213));
		s.close();
	}
}