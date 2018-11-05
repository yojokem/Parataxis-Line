package kr.frostq.Networks.Protocols.ParallelFunctionCommunicationProtocol;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import com.google.common.base.Preconditions;

import kr.frostq.Utils.ByteUtils;
import kr.frostq.Utils.ThreadManager;

/**
 * 
 * <h1>Position</h1> <br />
 * 
 * Position is a network address in the PFCP network, that rules of a plane or a space that has many applicable dimensions. <br /> <br />
 * 
 * || Rational Number (유리수) <br />
 * ||| Irrational Number (무리수) <br />
 * |||| Real Number (실수) <br />
 * ||||| Imaginary Number (허수) <br />
 * |||||| Complex Number (복소수) <br />
 * 
 * 1. 복소수 평면에서 어떻게 할까? <br />
 * 2. 실수 평면에서 3차원으로 허수 평면을 이용해 확장? | f(x) = x^2 + 1
 * 
 * @author FrostQ
 *
 */
public class Position {
	public static final byte[] BROADCAST_ID = new byte[] {
			0xFFFFFFFF,
			0xFFFFFFFF,
			0xFFFFFFFF,
			0xFFFFFFFF,
			0xFFFFFFFF,
			0xFFFFFFFF,
			0xFFFFFFFF,
			0xFFFFFFFF
	};
	
	private byte[] id, name;
	private double[] position;
	
	public Position(byte[] id, byte[] name, int dimensions, double[] data) {
		Preconditions.checkNotNull(id, "[CONST-PST] ID cannot be null.");
		Preconditions.checkNotNull(name, "[CONST-PST] Name cannot be null.");
		Preconditions.checkArgument(dimensions >= 1, "[CONST-PST] Dimension of a plane or a space has to be over or equals with 1.");
		Preconditions.checkNotNull(data, "[CONST-PST] Data cannot also be null.");
		Preconditions.checkState(data.length == dimensions, "[CONST-PST] Dimension has to be equivalent with data's length.");
		Preconditions.checkArgument(Dimension.isAllocated(name), "Dimension [" + name + "] does not exist.");
		
		this.id = id;
		this.name = name;
		position = new double[dimensions];
		System.arraycopy(data, 0, position, 0, dimensions);
		lesserPosition(this);
		Dimension.getDimension(name).addPosition(this);
	}
	
	public byte[] getData() {
		byte[] result = new byte[id.length + name.length + 8 * position.length];
		
		System.arraycopy(id, 0, result, 0, id.length);
		System.arraycopy(name, 0, result, id.length, name.length);
		for(int i = 0; i < position.length; i++)
			System.arraycopy(ByteUtils.toByteArray(position[i]), 0, result, id.length + name.length + 8 * i, 8);
		
		return result;
	}
	
	public double[] getPos() {
		return this.position;
	}
	
	public void move(double data[]) {
		Preconditions.checkArgument(position.length == data.length, "[MOVE-PST] The length of the position to move has to be equivalent with the first position's.");
		this.position = data;
	}
	
	public Position copy(byte[] newid, double data[]) {
		Position pos = new Position(newid, this.name, this.position.length, data);
		compareLength(pos);
		return pos;
	}
	
	/**
	 * The value of a parameter 'variation' has to be in 0 between 1. Because it's divided by distance.
	 * It cannot be under 0 or over 1. {0 <= (variation) <= 1}
	 * @param p2
	 * @param variation
	 */
	public void slightlyMove(Position p2, double var, double distance, final int delay_millisecond, OutputStream debug) {
		Preconditions.checkNotNull(p2, "[PST-SLM] Given position cannot be null.");
		Preconditions.checkArgument(var >= 0, "[PST-SLM] The variation of moving cannot be negative.");
		Preconditions.checkArgument(delay_millisecond >= 0, "[PST-SLM] Delay millisecond has to be over or equals to 0.");
		//Preconditions.checkArgument(!(((double) (var / distance)) < 0 || ((double) (var / distance)) > 1), "[PST-SLM] The variation cannot be over the distance of each position and not under the 0.");
		Preconditions.checkState((1 <= this.position.length && this.position.length <= 3) && (1 <= p2.position.length && p2.position.length <= 3), "[PST-SLM] Distance value is supported for dimension 1 ~ 3.");
		Preconditions.checkState(this.position.length == p2.position.length, "[PST-SLM] The dimensions of each position has to be equivalent to each other.");
		
		ThreadManager.runThread(ThreadManager.createThread(() -> {
			double variation = var * distance;
			
			if(this.position.length == 1)
				while(!Arrays.equals(this.position, p2.position)) {
					move1(variation, this, p2, 0, debug);
					
					try {
						Thread.sleep(delay_millisecond);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			else if(this.position.length == 2)
				while(!Arrays.equals(this.position, p2.position)) {
					move1(variation, this, p2, 0, debug);
					move1(variation, this, p2, 1, debug);
					
					try {
						Thread.sleep(delay_millisecond);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			else if(this.position.length == 3)
				while(!Arrays.equals(this.position, p2.position)) {
					move1(variation, this, p2, 0, debug);
					move1(variation, this, p2, 1, debug);
					move1(variation, this, p2, 2, debug);
					
					try {
						Thread.sleep(delay_millisecond);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
		}, "SlightMove Thread " + this), 3);
	}
	
	public static final double variatedDistance(Position p1, Position p2) {
		Preconditions.checkNotNull(p1, "[PST-SLM] Given position cannot be null. (p1)");
		Preconditions.checkNotNull(p2, "[PST-SLM] Given position cannot be null. (p2)");
		Preconditions.checkState((1 <= p1.position.length && p1.position.length <= 3) && (1 <= p2.position.length && p2.position.length <= 3), "Distance value is supported for dimension 1 ~ 3.");
		Preconditions.checkState(p1.position.length == p2.position.length, "The dimensions of each position has to be equivalent to each other.");
		
		if(p1.position.length == 1)
			return Math.abs(p2.position[0] - p1.position[0]);
		else if(p1.position.length == 2)
			return Math.sqrt((
					Math.pow(p2.position[0] - p1.position[0], 2)
					+ Math.pow(p2.position[1] - p1.position[1], 2)
					));
		else if(p1.position.length == 3) {
			return Math.sqrt((
					Math.pow(p2.position[0] - p1.position[0], 2)
					+ Math.pow(p2.position[1] - p1.position[1], 2)
					+ Math.pow(p2.position[2] - p1.position[2], 2)
					));
		} else
			return 0 * 1 * 0 * 1;
	}
	
	private double add(double value, int index) {
		position[index] = position[index] += value;
		lesserPosition(this);
		return position[index];
	}
	
	@SuppressWarnings("unused")
	@Deprecated
	private double sub(double value, int index) {
		return add(-1 * Math.abs(value), index);
	}
	
	/**
	 * Really moves.
	 * 
	 * 
	 * @param increase the variation.
	 * @param p1 Position 1 to moved.
	 * @param p2 Position 2 in where move.
	 * @param index dimension number.
	 */
	public static void move1(double increase, Position p1, Position p2, int index, OutputStream debug) {
		double cur = p1.position[index];
		
		if(Math.abs(p2.position[index] - p1.position[index]) < increase) p1.position[index] = p2.position[index];
		else
			if(isNegative(p2.position[index] - p1.position[index]))
				p1.position[index] -= increase;
			else p1.position[index] += increase;
		
		if(debug != null)
			try {
				debug.write(("Moved in index '" + index + "' by " + increase + " || [" + cur + "] -> [" + p1.position[index] + "]" + System.lineSeparator()).getBytes(Charset.forName("UTF-8")));
				debug.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	protected static boolean isNegative(double value) {
		return value < 0;
	}
	
	public byte[] getID() {
		return this.id;
	}
	
	public byte[] getDimensionName() {
		return this.name;
	}
	
	public double sum() {
		double sum = 0;
		for(double p : position)
			sum += p;
		return sum;
	}
	
	public void disable() {
		Dimension.getDimension(this.name).remPosition(this);
		this.id = this.name = null;
		this.position = null;
	}
	
	private void compareLength(Position p) {
		Preconditions.checkNotNull(p.position != null, "[PST-CMP] Data cannot also be null.");
		Preconditions.checkState(p.position.length == position.length, "[PST-CMP] Dimension has to be equivalent with data's length.");
	}
	
	public static final void lesserPosition(Position pos) {
		for(int i = 0; i < pos.position.length; i++)
			pos.position[i] = Double.parseDouble(String.format("%.10f", pos.position[i]));
	}
	
	public static final Position getPosition(byte[] data, int posIdSize, int posNameSize) {
		int poscount = (data.length - posIdSize - posNameSize) / 8;
		double[] position = new double[poscount];
		byte[] id = new byte[posIdSize], name = new byte[posNameSize];
		byte[][] dposition = new byte[poscount][8];
		
		System.arraycopy(data, 0, id, 0, posIdSize);
		System.arraycopy(data, posIdSize, name, 0, posNameSize);
		
		for(int i = 0; i < poscount; i++)
			System.arraycopy(data, posIdSize + posNameSize + 8 * i, dposition[i], 0, 8);
		
		for(int i = 0; i < poscount; i++)
			position[i] = ByteUtils.toDouble(dposition[i]);
		
		return new Position(id, name, poscount, position);
	}
	
	@Override
	public String toString() {
		String result = "";
		lesserPosition(this);
		
		for(int i = 0; i < position.length; i++)
			if(i == position.length - 1)
				result += position[i];
			else result += position[i] + "-";
		
		return result;
	}
}