package kr.frostq.Axis.Tests;

import kr.frostq.Networks.Protocols.ParallelFunctionCommunicationProtocol.Dimension;
import kr.frostq.Networks.Protocols.ParallelFunctionCommunicationProtocol.Position;

public class LauncherTest {
	public static void main(String[] args) {
		Dimension d = new Dimension(new byte[] {1});
		Position p1 = new Position(new byte[] {1}, 2, new double[] {0.5, 0});
		Position p2 = new Position(new byte[] {1}, 2, new double[] {312.5555, 0});
		
		double distance = Position.variatedDistance(p1, p2);
		System.out.println("°Å¸®: " + distance);
		p1.slightlyMove(p2, 0.0277775 / distance, distance, 0, System.out);
	}
}