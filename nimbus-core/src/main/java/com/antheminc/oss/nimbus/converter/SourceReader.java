package com.antheminc.oss.nimbus.converter;

public interface SourceReader {

	public <T> void doOpen();
	
	public <T> void doClose();
	
	public <T> void doRead();
}
