package kr.frostq.Networks.Protocols.ParallelFunctionCommunicationProtocol;

import java.util.*;

public abstract class GhostNode {
	protected final Map<PointNode, Boolean> pos = new LinkedHashMap<PointNode, Boolean>();
	
	/* public GhostNode() throws Exception {
		super(InetAddress.getByName("0.0.0.0"), position, false);
	} */
	
	public void send(int srcIndex, PointNode dst, byte[] data) {
		if(pos.size() > srcIndex && srcIndex >= 0) {
			
		}
	}
}