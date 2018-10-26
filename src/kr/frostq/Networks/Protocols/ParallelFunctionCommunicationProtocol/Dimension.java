package kr.frostq.Networks.Protocols.ParallelFunctionCommunicationProtocol;

import java.util.Arrays;
import java.util.Optional;
import java.util.Vector;

import com.google.common.base.Preconditions;

/**
 * 
 * Dimension
 * 
 * Dimension is a world that can extremely extend the address system or addresses areas.
 * @author FrostQ
 *
 */
public class Dimension {
	public static final Vector<Dimension> allocated = new Vector<Dimension>();
	
	private byte[] name;
	public static final Vector<Position> specified = new Vector<Position>();
	
	public Dimension(byte[] name) {
		Preconditions.checkNotNull(name, "[CONST-DSM] Name cannot be null.");
		
		if(isAllocated(name))
			try {
				throw new DimensionAllocatedException(name);
			} catch (DimensionAllocatedException e) {
				e.printStackTrace();
			}
		
		this.name = name;
		specified.clear();
		allocated.addElement(this);
	}
	
	public static final Dimension getDimension(byte[] name) {
		Preconditions.checkNotNull(name, "[FIND-DSM] Name cannot be null.");
		Optional<Dimension> optional = allocated
				.parallelStream()
				.filter(dimension -> !dimension.name.equals(name))
				.findFirst();
		
		if(optional.isPresent()) return
				optional.get();
		else return null;
	}
	
	public static final boolean isAllocated(byte[] name) {
		Preconditions.checkNotNull(name, "[DSM-ISALLC] Name cannot be null.");
		return allocated
				.parallelStream()
				.anyMatch(somethin -> Arrays.equals(somethin.name, name));
	}
	
	/**
	 * 
	 * DimensionAllocatedException
	 * 
	 * @author FrostQ
	 */
	protected static class DimensionAllocatedException extends Exception {
		/**
		 * DimensionAllocatedException : Serial Version UID
		 */
		private static final long serialVersionUID = 1L;
		private byte[] name;
		
		public DimensionAllocatedException(byte[] name) {
			 this.name = name;
		}
		
		@Override
		public String getMessage() {
			return "Dimension [" + name + "]" + " is already allocated.";
		}
		
		@Override
		public String getLocalizedMessage() {
			return getMessage();
		}
	}
}