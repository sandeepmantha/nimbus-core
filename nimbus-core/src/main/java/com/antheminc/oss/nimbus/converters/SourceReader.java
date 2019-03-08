package com.antheminc.oss.nimbus.converters;

public interface SourceReader {

	public <T> void doOpen();
	
	public <T> void doClose();
	
	public <T> void doRead();
}
