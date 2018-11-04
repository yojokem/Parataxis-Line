package kr.frostq.Networks.Protocols.ParallelFunctionCommunicationProtocol;

import java.net.*;
import java.util.*;

public abstract class GhostNode extends PointNode {
	protected final Map<ParagramSocket, Position> pos = new LinkedHashMap<ParagramSocket, Position>();
	
	public GhostNode(Position position) throws Exception {
		super(InetAddress.getByName("0.0.0.0"), position, false);
	}
	
	public void send(PointNode dst, byte[] data) {
		
	}
}