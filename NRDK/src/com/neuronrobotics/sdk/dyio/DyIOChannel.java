/*******************************************************************************
 * Copyright 2010 Neuron Robotics, LLC
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.neuronrobotics.sdk.dyio;

import java.util.ArrayList;
import java.util.Collection;

import com.neuronrobotics.sdk.commands.bcs.io.AsyncMode;
import com.neuronrobotics.sdk.commands.bcs.io.AsyncThreshholdEdgeType;
import com.neuronrobotics.sdk.commands.bcs.io.ConfigAsyncCommand;
import com.neuronrobotics.sdk.commands.bcs.io.GetChannelModeCommand;
import com.neuronrobotics.sdk.commands.bcs.io.GetValueCommand;
import com.neuronrobotics.sdk.commands.bcs.io.SetChannelValueCommand;
import com.neuronrobotics.sdk.commands.bcs.io.setmode.SetChannelModeCommand;
import com.neuronrobotics.sdk.common.BowlerAbstractCommand;
import com.neuronrobotics.sdk.common.BowlerDatagram;
import com.neuronrobotics.sdk.common.ByteList;
import com.neuronrobotics.sdk.common.ISendable;
import com.neuronrobotics.sdk.common.InvalidResponseException;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.dyio.peripherals.DyIOAbstractPeripheral;
/**
 * A DyIO channel. This represents a single DyIO pin.
 * @author rbreznak
 *
 */
public class DyIOChannel implements IDyIOChannel {
	private int MAXATTEMPTS = 3;
	private float cachedTime=0;
	private DyIO device;
	private int number;
	private boolean editable;
	private DyIOChannelMode current=null;
	private boolean isAsync=true;
	private ArrayList<IChannelEventListener> listeners = new ArrayList<IChannelEventListener>();
	
	private ArrayList< IDyIOChannelModeChangeListener> modeListeners = new ArrayList< IDyIOChannelModeChangeListener>();
	
	protected int cachedValue = 0;
	private boolean cachedMode=false;
	private DyIOAbstractPeripheral dap=null;
	private int previousValue = 1;
	private boolean haveSetMode = false;
	/**
	 * Construct a channel object.
	 * @param dyio			The DyIO that the channel belongs on
	 * @param channel		The Channel on the DyIO that this object corresponds to.
	 * @param mode			The Type of channel.
	 * @param isEditable	Lock the channel parameters after creation.
	 */
	public DyIOChannel(DyIO dyio, int channel, DyIOChannelMode mode, boolean isEditable) {
		update(dyio, channel, mode,  isEditable);
	}
	
	/**
	 * 
	 * 
	 * @param dyio
	 * @param channel
	 * @param mode
	 * @param isEditable
	 */
	public synchronized void update(DyIO dyio, int channel, DyIOChannelMode mode, boolean isEditable) {
		setDevice(dyio);
		number = channel;
		editable = isEditable;
		if(getCurrentMode() == null){
			setMode(mode);
		}else{
			setMode(mode,isAsync);
		}
		fireModeChangeEvent(mode);
		if(getCurrentMode() == DyIOChannelMode.NO_CHANGE) {
			System.err.println("Failed to update channel: "+ channel);
			throw new RuntimeException("Failed to update channel: "+ channel);
		}
	}
	
	private boolean isDefaultAsync(DyIOChannelMode m) {
		return false;
	}
	
	@SuppressWarnings("incomplete-switch")
	private boolean isOutputMode( DyIOChannelMode m) {
		switch(m) {
		case SERVO_OUT:
		case ANALOG_OUT:
		case DC_MOTOR_DIR:
		case DC_MOTOR_VEL:
		case PWM_OUT:
		case SPI_CLOCK:
		case SPI_MISO:
		case SPI_MOSI:
		case SPI_SELECT:
		case USART_TX:
			return true;
		}
		return false;
	}

	/**
	 * Get the channel's number.
	 * @return The Channel on the corresponding DyIO that this pin belongs to.
	 */
	public int getChannelNumber() {
		return number;
	}
	
	/**
	 * Can the parameters of this channel be edited?.
	 * 
	 * @return True if the parameters can be changed
	 */
	public boolean isEditable() {
		return editable;
	}
	
	/**
	 * Send a command to the Channel's DyIO.
	 * @param command The command to send.
	 */
	public void send(BowlerAbstractCommand command) {
		getDevice().send(command);
	}
	
	
	/**
	 * Clear list of objects that have subscribed to channel updates.
	 */
	public void removeAllChannelModeChangeListener() {
		modeListeners.clear();
	}
	
	/**
	 * Remove a particular subscription.
	 * 
	 * @param l
	 *            The object that has subscribed to updates
	 */
	public void removeChannelModeChangeListener(IDyIOChannelModeChangeListener l) {
		if(!modeListeners.contains(l)) {
			return;
		}
		
		modeListeners.remove(l);
	}
	
	/**
	 * Add an object that wishes to receive channel updates.
	 * 
	 * @param l
	 *            The object that wishes to receive updates.
	 */
	public void addChannelModeChangeListener(IDyIOChannelModeChangeListener l) {
		if(modeListeners.contains(l)) {
			return;
		}
		modeListeners.add(l);
	}
	
	/**
	 * Clear list of objects that have subscribed to channel updates.
	 */
	public void removeAllChannelEventListeners() {
		listeners.clear();
	}
	
	/**
	 * Remove a particular subscription.
	 * 
	 * @param l
	 *            The object that has subscribed to updates
	 */
	public void removeChannelEventListener(IChannelEventListener l) {
		if(!listeners.contains(l)) {
			return;
		}
		
		listeners.remove(l);
	}
	
	/**
	 * Add an object that wishes to receive channel updates.
	 * 
	 * @param l
	 *            The object that wishes to receive updates.
	 */
	public void addChannelEventListener(IChannelEventListener l) {
		if(listeners.contains(l)) {
			return;
		}
		
		listeners.add(l);
	}
	
	/**
	 * Set the mode of the DyIO Channel.
	 * 
	 * @param mode
	 *            The desired channel mode.
	 * @return True if successful
	 */
	public boolean setMode(DyIOChannelMode mode) {
		return setMode(mode, isDefaultAsync(mode));
	}
	
	/**
	 * Gets the mode of the channel. If resync is true, then the channel will do
	 * a live query to the device for its current mode and cache it to the
	 * channel for future use.
	 * 
	 * Identical to calling <code>resync(false)</code> before
	 * <code>getMode()</code>
	 * 
	 * @param resync
	 * @return
	 */
	public DyIOChannelMode getMode(boolean resync) {
		resync(false);
		return getMode();
	}
	
	private void setDevice(DyIO device) {
		this.device = device;
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public DyIO getDevice() {
		return device;
	}
	
	/**
	 * Live query the device for its mode and cache it.
	 * 
	 * @param all - should all channels be refreshed.
	 */
	public void resync(boolean all) {
		
		if(all) {
			getDevice().resync();
			return;
		}
		BowlerDatagram bd = getDevice().send(new GetChannelModeCommand(number));
		//System.out.println(bd);
		setCurrentMode(DyIOChannelMode.get(bd.getData().getByte(1)));
		throw new RuntimeException();
	}
	
	public boolean canBeMode(DyIOChannelMode m) {
		Collection<DyIOChannelMode> modes = getAvailableModes();
		for(DyIOChannelMode mo:modes) {
			if(mo == m)
				return true;
		}
		return false;
	}
	
	public boolean hasAsync(){
		if(getMode() == null)
			return false;
		switch(getMode()){
		case ANALOG_IN:
		case COUNT_IN_INT:
		case COUNT_OUT_INT:
		case DIGITAL_IN:
			return true;
		default:
			return false;	
		}
	}
	
	public Collection<DyIOChannelMode> getAvailableModes() {
		Collection<DyIOChannelMode> modes = new ArrayList<DyIOChannelMode>();
		modes.add(DyIOChannelMode.DIGITAL_IN);
		modes.add(DyIOChannelMode.DIGITAL_OUT);
		
		if(number < 12 && device.getBankAState() != DyIOPowerState.REGULATED) {
			modes.add(DyIOChannelMode.SERVO_OUT);	
		}

		if(number > 11 && device.getBankBState() != DyIOPowerState.REGULATED) {
			modes.add(DyIOChannelMode.SERVO_OUT);	
		}
		
		
		switch(number) {
		case 0:
			modes.add(DyIOChannelMode.SPI_CLOCK);
			break;
		case 1:
			modes.add(DyIOChannelMode.SPI_MISO);
			break;
		case 2:
			modes.add(DyIOChannelMode.SPI_MOSI);
			break;

		}
		switch(number) {
		case 0:
		case 1:
		case 2:
		case 3:
			modes.add(DyIOChannelMode.COUNT_IN_HOME);
			modes.add(DyIOChannelMode.COUNT_OUT_HOME);
			break;
		case 16:
		case 18:
		case 20:
		case 22:
			modes.add(DyIOChannelMode.COUNT_IN_DIR);
			modes.add(DyIOChannelMode.COUNT_OUT_DIR);
			break;
		case 17:
		case 19:
		case 21:
		case 23:
			modes.add(DyIOChannelMode.COUNT_IN_INT);
			modes.add(DyIOChannelMode.COUNT_OUT_INT);
			break;
		}
		switch(number) {
		case 8:
		case 9:
		case 10:
		case 11:
		case 12:
		case 13:
		case 14:
		case 15:
			modes.add(DyIOChannelMode.ANALOG_IN);
			break;
		}
		switch(number) {
		case 4:
		case 5:
		case 6:
		case 7:
			modes.add(DyIOChannelMode.PWM_OUT);
			modes.add(DyIOChannelMode.DC_MOTOR_VEL);
			break;
		case 8:
		case 9:
		case 10:
		case 11:
			modes.add(DyIOChannelMode.DC_MOTOR_DIR);
			break;
		}
		switch(number) {
		case 16:
			modes.add(DyIOChannelMode.USART_TX);
			break;
		case 17:
			modes.add(DyIOChannelMode.USART_RX);
			break;
		}
		if(number == 23) {
			modes.add(DyIOChannelMode.PPM_IN);
		}
		return modes;
	}
	private boolean isStreamChannel(){
		return ((getMode() == DyIOChannelMode.PPM_IN) ||(getMode() == DyIOChannelMode.USART_RX)||(getMode() == DyIOChannelMode.USART_TX));
	}
	public int parseDyIOChannelEvent(DyIOChannelEvent e){
		if(isStreamChannel())
			return 0;
		return ByteList.convertToInt(e.getData().getBytes());
	}
	
	/**
	 * 
	 * 
	 * @param e
	 */
	protected void fireChannelEvent(DyIOChannelEvent e) {
		if((getPreviousValue() == parseDyIOChannelEvent(e)) && !isStreamChannel() ){
			Log.debug("Value is the same");
			//return;
		}
		setPreviousValue(parseDyIOChannelEvent(e));
		for(IChannelEventListener l : listeners) {
			l.onChannelEvent(e);
		}
	}
	
	/**
	 * 
	 * 
	 * @param e
	 */
	protected void fireModeChangeEvent(DyIOChannelMode e) {
		boolean ok = false;
		for (DyIOChannelMode md :getAvailableModes()){
			if(md == e)
				ok=true;
		}
		if(! ok){
			Log.error("Mode is invalid!!!");
			e = DyIOChannelMode.DIGITAL_IN;
		}
		if(e==getMode()) {
			//Log.info("Mode not changed: "+getChannelNumber()+" mode: "+getMode());
		}
		setCurrentMode(e);
		for(IDyIOChannelModeChangeListener l : modeListeners) {
			l.onModeChange(e);
		}
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	 
	public String toString() {
		return String.format("(%02d) - %-20s", getChannelNumber(), getMode());
	}
	
	/* (non-Javadoc)
	 * @see com.neuronrobotics.sdk.dyio.IDyIOChannel#getChannel()
	 */
	 
	public DyIOChannel getChannel() {
		return this;
	}
	
	/* (non-Javadoc)
	 * @see com.neuronrobotics.sdk.dyio.IDyIOChannel#getMode()
	 */
	 
	public DyIOChannelMode getMode() {		
		return getCurrentMode();
	}
	
	/* (non-Javadoc)
	 * @see com.neuronrobotics.sdk.dyio.IDyIOChannel#getValue()
	 */
	 
	public int getValue() {
		BowlerDatagram response=null;
		int val=0;
		try {
			response = getDevice().send(new GetValueCommand(number));
		} catch (InvalidResponseException e) {
			response = getDevice().send(new GetValueCommand(number));
		}
		switch(getCurrentMode()) {
		case ANALOG_IN:
		case DIGITAL_IN:
		case DIGITAL_OUT:
		case SERVO_OUT: 
		case PWM_OUT:
		case DC_MOTOR_DIR:
		case DC_MOTOR_VEL:
			val=ByteList.convertToInt(response.getData().getBytes(1), false);
			break;
		case COUNT_IN_INT:
		case COUNT_IN_DIR:
		case COUNT_IN_HOME:
		case COUNT_OUT_INT:
		case COUNT_OUT_DIR:
		case COUNT_OUT_HOME:
			byte [] b=response.getData().getBytes(1);
			val=  ByteList.convertToInt(b, true);
			break;
		default:
			Log.error(this.getClass()+"Invalid mode for get: "+getCurrentMode());
			return 0;
		} 
		setCachedValue(val);
		setPreviousValue(val);
		return val;
	}
	
	/* (non-Javadoc)
	 * @see com.neuronrobotics.sdk.dyio.IDyIOChannel#setMode(com.neuronrobotics.sdk.dyio.DyIOChannelMode, boolean)
	 */
	 
	public synchronized boolean setMode(DyIOChannelMode mode, boolean async) {
		//resyncIfNotSynced();
		if(mode == null) {
			return true;
		}
		if(getMode()  == null) {
			Log.debug(this.getClass()+" First time setting mode.");
			setCurrentMode(mode);
			isAsync = isDefaultAsync(mode);
			return true;
		}
		if ((getMode() == mode) && (async == isAsync) && haveSetMode) {
			Log.debug(this.getClass()+"Channel: "+getChannelNumber()+" Mode and Async is the same, ignoring...");
			return true;
		}
		
		if(!canBeMode(mode)){
			
			throw new RuntimeException("\nChannel: "+getChannelNumber()+" can not be mode '"+mode+"' in current configuration. \nCheck the power switch settings and availible modes.");
		}
		for(int i = 0; i < MAXATTEMPTS; i++) {
			try {
				isAsync = async;
				setCurrentMode(mode);
				getDevice().send(new SetChannelModeCommand(number, mode, async));
				haveSetMode=true;
				if(!getDevice().isMuteResyncOnModeChange()){
					try {
						getDevice().resync();
					}catch(RuntimeException e) {
						//e.printStackTrace();
						getDevice().setMuteResyncOnModeChange(true);
					}
				}else{
					Log.debug("Not resyncing from channel: "+getChannelNumber());
				}
				return true;
			} catch (InvalidResponseException e) {
				Log.error(e.getMessage());
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
					return false;
				}
			}
		}
		return false;
	}
	private boolean synced = false;
	private void resyncIfNotSynced() {
		if(synced==false) {
			synced = true;
			resync(false);
		}
	}

	/* (non-Javadoc)
	 * @see com.neuronrobotics.sdk.dyio.IDyIOChannel#setValue(int)
	 */
	 
	public boolean setValue(int value) {
		Log.debug("Setting channel: "+number+" to value: "+value);
		setCachedValue(value);
		setCachedTime(0);
		if(cachedMode)
			return true;
		if(dap!=null)
			return dap.flush();
		return flush();
	}
	
	public boolean flush() {
//		if(getCachedMode())
//			throw new RuntimeException("In chached mode and flushing from channel");
		//Log.enableDebugPrint(true);
		Log.debug("Flushing channel: "+number);
		
		ByteList b = new ByteList();
		switch(getMode()){
		case COUNT_IN_INT:
		case COUNT_IN_DIR:
		case COUNT_IN_HOME:
		case COUNT_OUT_INT:
		case COUNT_OUT_DIR:
		case COUNT_OUT_HOME:
			b.addAs32(getCachedValue());
			b.addAs32((int)(getCachedTime()*1000));
			break;
		case SERVO_OUT:
			b.add(getCachedValue());
			b.addAs16((int)(getCachedTime()*1000));
			break;
		default:
			b.add(getCachedValue());
		}
		Log.info("Setting channel: "+getChannelNumber()+" to value: "+b);
		boolean back = setValue(b);
		//Log.enableDebugPrint(false);
		return back;
	}

	/* (non-Javadoc)
	 * @see com.neuronrobotics.sdk.dyio.IDyIOChannel#setValue(com.neuronrobotics.sdk.common.ISendable)
	 */
	 
	public boolean setValue(ByteList data) {
		int attempts = MAXATTEMPTS;
		if(getMode() == DyIOChannelMode.USART_RX ||getMode() == DyIOChannelMode.USART_TX )
			attempts=1;
		for(int i = 0; i < attempts; i++) {
			try {
				getDevice().send(new SetChannelValueCommand(number, data));
				return true;
			} catch (InvalidResponseException e) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
					return false;
				}
			}
		}
		
		return false;
	}

	public void setCachedValue(int cachedValue) {
		this.cachedValue = cachedValue;
	}

	public int getCachedValue() {
		return cachedValue;
	}
	
	public boolean getCachedMode() {
		return cachedMode;
	}

	public void setCachedMode(boolean mode) {
		cachedMode=mode;
	}

	public void setDap(DyIOAbstractPeripheral dap) {
		this.dap = dap;
	}

	public DyIOAbstractPeripheral getDap() {
		return dap;
	}

	public void setPreviousValue(int previousValue) {
		this.previousValue = previousValue;
	}

	public int getPreviousValue() {
		return previousValue;
	}	
	
	public boolean configAdvancedAsyncNotEqual(){
		return configAdvancedAsyncNotEqual(100);
	}
	public boolean configAdvancedAsyncDeadBand(int deadbandSize){
		return  configAdvancedAsyncDeadBand(100,deadbandSize);
	}
	public boolean configAdvancedAsyncTreshhold(int threshholdValue, AsyncThreshholdEdgeType edgeType){
		return  configAdvancedAsyncTreshhold(100, threshholdValue, edgeType);
	}
	public boolean configAdvancedAsyncAutoSample(){
		return  configAdvancedAsyncAutoSample(100);
	}
	
	public boolean configAdvancedAsyncNotEqual(int msTime){
		isAsync=true;
		return  getDevice().configAdvancedAsyncNotEqual(getChannelNumber(),msTime);
	}
	public boolean configAdvancedAsyncDeadBand(int msTime,int deadbandSize){
		isAsync=true;
		return  getDevice().configAdvancedAsyncDeadBand(getChannelNumber(),msTime,deadbandSize);
	}
	public boolean configAdvancedAsyncTreshhold(int msTime,int threshholdValue, AsyncThreshholdEdgeType edgeType){
		isAsync=true;
		return  getDevice().configAdvancedAsyncTreshhold(getChannelNumber(),msTime, threshholdValue, edgeType);
	}
	public boolean configAdvancedAsyncAutoSample(int msTime){
		isAsync=true;
		return  getDevice().configAdvancedAsyncAutoSample(getChannelNumber(),msTime);
	}

	public void setCurrentMode(DyIOChannelMode mode) {
		try {
			if(!canBeMode(mode)) {
				String message = this.getClass()+" Can not set channel: "+getChannelNumber()+" to mode: "+mode;
				System.err.println(message);
				//throw new RuntimeException(message);
			}
		}catch(RuntimeException ex) {
			ex.printStackTrace();
			throw ex;
		}
		this.current = mode;
	}

	public DyIOChannelMode getCurrentMode() {
		return current;
	}

	public void setCachedTime(float cachedTime) {
		this.cachedTime = cachedTime;
	}

	public float getCachedTime() {
		return cachedTime;
	}
}
