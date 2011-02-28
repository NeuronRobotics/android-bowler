package com.neuronrobotics.sdk.pid;

import com.neuronrobotics.sdk.common.BowlerDatagram;
import com.neuronrobotics.sdk.common.ByteList;

public class PIDLimitEvent {
	private int channel;
	private int ticks; 
	private long timeStamp;
	private int limitIndex;
	public PIDLimitEvent(int chan,int tick,int index,long time){
		setGroup(chan);
		setLimitIndex(index);
		setValue(tick);
		setTimeStamp(time);
		
	}
	public PIDLimitEvent(BowlerDatagram data){
		if(!data.getRPC().contains("pidl"))
			throw new RuntimeException("Datagram is not a PID event");
		setGroup(data.getData().getByte(0));
		setLimitIndex(ByteList.convertToInt(data.getData().getBytes(1, 1),true));
		setValue(ByteList.convertToInt(data.getData().getBytes(2, 4),true));
		setTimeStamp(ByteList.convertToInt(data.getData().getBytes(6, 4),false));
		
	}
	public void setGroup(int channel) {
		this.channel = channel;
	}
	public int getGroup() {
		return channel;
	}
	public void setValue(int ticks) {
		this.ticks = ticks;
	}
	public int getValue() {
		return ticks;
	}
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}
	public long getTimeStamp() {
		return timeStamp;
	}
	@Override 
	public String toString(){
		return "PID Limit Event: chan="+channel+", value="+ticks+", time="+timeStamp;
	}
	public void setLimitIndex(int limitIndex) {
		this.limitIndex = limitIndex;
	}
	public int getLimitIndex() {
		return limitIndex;
	}
}