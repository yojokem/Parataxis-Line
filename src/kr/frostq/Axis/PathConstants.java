package kr.frostq.Axis;

import java.nio.file.*;
import java.util.*;

public final class PathConstants {
	public static final Map<String, Path> consts = new HashMap<String, Path>();
	public static final List<String> dirs = new ArrayList<String>();
	
	static {
		
		//Directory
		
		addConstant("current", Paths.get(thisParent() +
				"/Parataxis Function Derivatives/"));
		dirs.add("current");
		
		addConstant("rcPkts", Paths.get(getConstant("current") +
				"/Received Packets/"));
		dirs.add("rcPkts");
		
		// File
		
	}
	
	public static final void addConstant(String id, Path value) {
		if(!consts.containsKey(id) && !consts.containsValue(value))
			consts.put(id, value);
	}
	
	public static final Path getConstant(String id) {
		return consts.get(id);
	}
	
	public static final void remConstant(String id) {
		if(consts.containsKey(id))
			consts.remove(id);
	}
	
	protected static Path thisParent() {
		return Paths.get(System.getProperty("user.dir") + "/").getParent();
	}
	
	public static final void resetInit() throws Exception {
		/*Field[] consts = PathConstants.class.getDeclaredFields();
		for(Field f : consts) {
			Path p = (Path) f.get(Paths.get(""));
			if(!Files.exists(p))
				if(p.toFile().isDirectory())
					Files.createDirectory(p);
				else Files.createFile(p);
		}*/
		
		for(String k : consts.keySet().toArray(new String[] {}))
			if(!Files.exists(consts.get(k)))
				if(dirs.contains(k))
					Files.createDirectory(consts.get(k));
				else Files.createFile(consts.get(k));
	}
}