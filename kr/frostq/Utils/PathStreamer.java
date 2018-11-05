package kr.frostq.Utils;

import java.io.Closeable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class PathStreamer {
	public static final int INPUT = 0x00;
	public static final int OUTPUT = 0x01;
	public static final int STREAM = 0xFF;
	
	private Path path;
	private int mode;
	
	public PathStreamer(Path path, int mode) {
		this.path = path;
		
		if(mode == INPUT || mode == OUTPUT || mode == STREAM)
			this.mode = mode;
		else this.mode = STREAM;
	}
	
	public Closeable getResult() throws Exception {
		if(!Files.exists(this.path))
			Files.createFile(this.path);
		
		if(mode == (INPUT | STREAM))
			return Files.newInputStream(this.path, StandardOpenOption.READ);
		else if(mode == OUTPUT)
			return Files.newOutputStream(this.path, StandardOpenOption.WRITE);
		else return null;
	}
}