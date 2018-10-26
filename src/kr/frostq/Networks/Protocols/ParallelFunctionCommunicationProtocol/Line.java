package kr.frostq.Networks.Protocols.ParallelFunctionCommunicationProtocol;

/**
 * 
 * Line
 * 
 * @author FrostQ
 *
 */
public class Line {
	public static enum LineType {
		StraightLine,
		LineSegment,
		Ray,
		VOF, /** VOF: Value of function: y=f(x) */
		DomainedFigure
	}
	
	public LineType type;
}