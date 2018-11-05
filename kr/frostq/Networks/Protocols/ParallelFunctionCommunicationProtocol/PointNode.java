package kr.frostq.Networks.Protocols.ParallelFunctionCommunicationProtocol;

import java.io.IOException;
import java.net.*;

import com.google.common.base.Preconditions;

import kr.frostq.Networks.Protocols.ParallelFunctionCommunicationProtocol.Packet.PFCPPacket;

/**
 * 
 * PointNode
 * 
 * It is same to a client, works independently.
 * Address is so flexible.
 * 
 * Asynchronous sending and receiving datas.
 * @author FrostQ
 *
 */
public abstract class PointNode {
	public static InetAddress MULTICAST_ADDR1;
	public static final int PORT = 3215;
	public static ParagramSocket ALS;
	static {
		try {
			MULTICAST_ADDR1 = InetAddress.getByName("232.45.145.141");
			ALS = new ParagramSocket(InetAddress.getLocalHost(), PORT);
		} catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	//public Line[] contained;
	
	private Position pos;
	
	private ParagramSocket LLS;
	private InetAddress local;
	private boolean isTarget = false;
	
	public PointNode(Position position, boolean isTarget) {
		this.pos = position;
		try {
			this.local = InetAddress.getLocalHost();
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		this.isTarget = isTarget;
		
		if(!isTarget) {
			try {
				LLS = ALS;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public PointNode(InetAddress inet, Position position, boolean isTarget) {
		this.pos = position;
		this.local = inet;
		this.isTarget = isTarget;
		
		if(!isTarget) {
			try {
				LLS = new ParagramSocket(this.local, PORT);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	protected void connect() {
		
	}
	
	public byte[] getNodeID() {
		byte[] result = new byte[pos.getDimensionName().length + pos.getID().length];
		
		System.arraycopy(pos.getDimensionName(), 0, result, 0, pos.getDimensionName().length);
		System.arraycopy(pos.getID(), 0, result, pos.getDimensionName().length, pos.getID().length);
		
		return result;
	}
	
	public Position getPosition() {
		return pos;
	}
	
	public static final PointNode getBroadcastNode(byte[] dimensionName, int dimensions) {
		double[] data = new double[dimensions];
		for(int i = 0; i < dimensions; i++)
			data[i] = Double.MAX_VALUE;
		
		return new PointNode(new Position(Position.BROADCAST_ID, dimensionName, dimensions, data), true) {
			@Override
			public void receive(PFCPPacket packet) {}
		};
	}
	
	public void broadcast(PFCPPacket packet) throws Throwable {
		exchange(getBroadcastNode(packet.getSrc().getDimensionName(), packet.getSrc().getPos().length), packet);
	}
	
	public void exchange(PointNode dst, PFCPPacket packet) throws Throwable {
		Preconditions.checkArgument(!isTarget, "PointNode {" + getPosition() + "} is targetable only.");
		if(LLS == null) {
			pos.disable();
			Preconditions.checkNotNull(LLS, "PointNode {" + getPosition() + "} 's ParagramSocket is null!");
		}
		transmitPacket(this, dst, packet);
	}
	
	public static final void transmitPacket(PointNode src, PointNode dst, PFCPPacket packet) throws Throwable {
		Preconditions.checkArgument(!src.isTarget, "PointNode {" + src.getPosition() + "} is targetable only.");
		if(src.LLS == null) {
			src.pos.disable();
			Preconditions.checkNotNull(src.LLS, "PointNode {" + src.getPosition() + "} 's ParagramSocket is null!");
		}
		// packet.type = 0xFF;
		
		packet.setSrc(src.getPosition()).setDst(dst.getPosition());
		
		byte[] bytes = packet.createPacketBytes();
		src.LLS.send1(MULTICAST_ADDR1, PORT, bytes, 0, bytes.length);
	}
	
	public abstract void receive(PFCPPacket packet);
	
	public void startOwnReceiver(Runnable run) {
		this.LLS.startOwnReceiver(this, run);
	}
	
	public static class ParagramSocket extends MulticastSocket {
		private Thread recvThr;
		
		public ParagramSocket(int port) throws IOException {
			super(port);
			join();
		}
		
		public ParagramSocket(InetAddress srcAddr, int srcport) throws IOException {
			super(new InetSocketAddress(srcAddr, srcport));
			join();
		}
		
		private void join() {
			try {
				this.joinGroup(MULTICAST_ADDR1);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		private void leave() {
			try {
				this.leaveGroup(MULTICAST_ADDR1);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void send0(InetAddress inet, int port, byte[] data, int offset, int length) throws IOException {
			Preconditions.checkNotNull(inet, "[PTND-PGSKT-sendtarget] Target InetAddress is null.");
			Preconditions.checkNotNull(data, "[PTND-PGSKT-sendtarget] Data is null!");
			this.send(new DatagramPacket(data, offset, length, inet, port));
		}
		
		public ParagramSocket send1(InetAddress inet, int port, byte[] data, int offset, int length) throws IOException {
			send0(inet, port, data, offset, length);
			return this;
		}
		
		public void startOwnReceiver(final PointNode node, Runnable runnable) {
			Preconditions.checkNotNull(node, "[PTND-PGSKT-startreceiver] Given PointNode is null! What is hell of you su**?");
			
			// node.receive(packet);
			
			if(runnable == null)
				runnable = () -> {
					try {
						byte[] buf = new byte[1024];
						DatagramPacket pac = new DatagramPacket(buf, buf.length);
						
						while(true) {
							this.receive(pac);
							PFCPPacket p = null;
							try {
								p = new PFCPPacket(pac.getData(), pac.getOffset(), pac.getLength());
							} catch (Exception e) {
								e.printStackTrace();
							}
							
							Preconditions.checkNotNull(p, "[PTND-PGSKT-receive] Packet is null. Was there any error?");
							
							if(!node.isTarget) node.receive(p);
							BroadcastRadar.received(p, true);
							/*ThreadManager.runThread(ThreadManager.createThread(() -> {}, "[PTND-PGSKT-receive-handle-" + new String(node.getNodeID()) + "] Default Receiver PGSKT#" + this.hashCode()), 5);*/
						}
					} catch(Exception e) {
						System.err.println(e.getMessage());
						// Error handler
					}
				};
			
			try {
				recvThr = new Thread(runnable, "PointNode " + node.getPosition().getID() + "{" + node.getPosition() + "} UDP Receiver Thread");
				recvThr.setPriority(3);
				recvThr.start();
			} catch (Exception e) {
				System.err.println(e.getMessage());
				return;
			}
		}
		
		@Override
		public void close() {
			leave();
			super.close();
		}
	}
}