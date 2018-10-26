package kr.frostq.Networks.Protocols.ParallelFunctionCommunicationProtocol;

import java.util.Vector;

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
	private Vector<Position> pos = new Vector<Position>();
	public Line[] contained;
	
	public PointNode(Position position) {
		pos.setSize(1);
		pos.setElementAt(position, 0);
	}
	
	public void send(PointNode node, byte[] data) {
		
	}
	
	public abstract void receive(PointNode source, byte[] data);
}