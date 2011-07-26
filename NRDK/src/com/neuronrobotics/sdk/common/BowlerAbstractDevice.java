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
/**
 *
 * Copyright 2009 Neuron Robotics, LLC
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.neuronrobotics.sdk.common;

import java.util.ArrayList;

import com.neuronrobotics.sdk.commands.bcs.core.NamespaceCommand;
import com.neuronrobotics.sdk.commands.bcs.core.PingCommand;
import com.neuronrobotics.sdk.commands.neuronrobotics.dyio.InfoFirmwareRevisionCommand;
import com.neuronrobotics.sdk.util.ThreadUtil;

// TODO: Auto-generated Javadoc
/**
 * AbstractDevices are used to model devices that are connected to the Bowler network. AbstractDevice
 * implementations should encapsulate command generation and provide higher-level actions to users.  
 * 
 * @author rbreznak
 *
 */
public abstract class BowlerAbstractDevice implements IBowlerDatagramListener {
	
	private long heartBeatTime=1000;
	private long lastPacketTime=0;
	private HeartBeat beater;
	/** The connection. */
	private BowlerAbstractConnection connection=null;
	
	/** The address. */
	private MACAddress address = new MACAddress(MACAddress.BROADCAST);
	
	/**
	 * Determines if the device is available.
	 *
	 * @return true if the device is avaiable, false if it is not
	 * @throws InvalidConnectionException the invalid connection exception
	 */
	public boolean isAvailable() throws InvalidConnectionException{
		return getConnection().isConnected();
	}

	
	/**
	 * Set the connection to use when communicating commands with a device.
	 *
	 * @param connection the new connection
	 */
	private static ArrayList<IConnectionEventListener> disconnectListeners = new ArrayList<IConnectionEventListener> ();
	
	public void addConnectionEventListener(IConnectionEventListener l ) {
		if(!disconnectListeners.contains(l)) {
			disconnectListeners.add(l);
		}
	}
	public void removeConnectionEventListener(IConnectionEventListener l ) {
		if(disconnectListeners.contains(l)) {
			disconnectListeners.remove(l);
		}
	}
	public void setConnection(BowlerAbstractConnection connection) {
		if(connection == null) {
			throw new NullPointerException("Can not use a NULL connection.");
		}
		for(IConnectionEventListener i:disconnectListeners) {
			connection.addConnectionEventListener(i);
		}
		this.connection = connection;
		connection.addDatagramListener(this);
	}
	
	/**
	 * Connect.
	 *
	 * @return true, if successful
	 * @throws InvalidConnectionException the invalid connection exception
	 */
	public boolean connect() throws InvalidConnectionException {
		
		if (connection == null) {
			throw new InvalidConnectionException("Null Connection");
		}
		if(!connection.isConnected()) {
			if(!connection.connect()) {
				startHeartBeat();
				return false;
			}
		}
		return this.isAvailable();
	}
	
	/**
	 * Disconnect.
	 */
	public void disconnect() {
		
		if (connection != null) {
			if(connection.isConnected()){
				Log.info("Disconnecting Bowler Device");
				connection.disconnect();
			}
		}
	}
	
	/**
	 * Get the connection that is used for communicating with the device.
	 *
	 * @return the device's connection
	 */
	public BowlerAbstractConnection getConnection() {
		return connection;
	}
	
	/**
	 * Set the MAC Address of the device. Every device should have a unique MAC address.
	 *
	 * @param address the new address
	 */
	public void setAddress(MACAddress address) {
		if(!address.isValid()) {
			throw new InvalidMACAddressException();
		}
		this.address = address;
	}
	
	/**
	 * Get the device's mac address.
	 *
	 * @return  the device's address
	 */
	public MACAddress getAddress() {
		//System.out.println();
		return address;
	}
	
	/**
	 * Send a sendable to the connection.
	 *
	 * @param sendable the sendable
	 * @return the syncronous response
	 */
	public BowlerDatagram send(ISendable sendable) {
		if((new BowlerDatagram(new ByteList(sendable.getBytes())).getRPC().toLowerCase().contains("_png")))
			Log.debug("sending ping");
		else
			Log.debug("TX>>\n"+sendable.toString());
		BowlerDatagram b =connection.send(sendable);
		if(b != null) {
			if(b.getRPC().toLowerCase().contains("_png"))
				Log.debug("ping ok!");
			else
				Log.debug("RX<<\n"+b);
		}else {
			Log.debug("RX<<: No response");
		}
		lastPacketTime = System.currentTimeMillis();
		return b;
	}
	

	/**
	 * Send a command to the connection.
	 *
	 * @param command the command
	 * @return the syncronous response
	 * @throws NoConnectionAvailableException the no connection available exception
	 * @throws InvalidResponseException the invalid response exception
	 */
	public BowlerDatagram send(BowlerAbstractCommand command) throws NoConnectionAvailableException, InvalidResponseException {	
		if(!connection.isConnected()) {
			throw new NoConnectionAvailableException();
		}
		
		return command.validate(send(BowlerDatagramFactory.build(getAddress(), command,0)));
	}
		
	/**
	 * Implementation of the Bowler ping ("_png") command
	 * Sends a ping to the device returns the device's MAC address.
	 *
	 * @return the device's address
	 */
	public BowlerDatagram ping() {
		try {
			BowlerDatagram bd = send(new PingCommand());
			//System.out.println("Ping success " + bd.getAddress());
			setAddress(bd.getAddress());
			return bd;
		} catch (InvalidResponseException e) {
			Log.error("Invalid response from Ping");
			return null;
		} catch (NoConnectionAvailableException e) {
			Log.error("No connection is available.");
			return null;
		}
	}
	
	/**
	 * Gets the revisions.
	 *
	 * @return the revisions
	 */
	public ArrayList<ByteList> getRevisions(){
		ArrayList<ByteList> list = new ArrayList<ByteList>();
		try {
			BowlerDatagram b = send(new InfoFirmwareRevisionCommand());
			System.out.println("FW info:\n"+b);
			for(int i=0;i<(b.getData().size()/3);i++){
				list.add(new ByteList(b.getData().getBytes((i*3),3)));
			}
		} catch (InvalidResponseException e) {
			Log.error("Invalid response from Firmware rev request");
			
		} catch (NoConnectionAvailableException e) {
			Log.error("No connection is available.");
		}
		return list;
	}
	
	/**
	 * Get all the namespaces.
	 *
	 * @return the namespaces
	 */
	public ArrayList<String>  getNamespaces(){
		ArrayList<String> list = new ArrayList<String>();
		try {
			BowlerDatagram b = send(new NamespaceCommand());
			int num = b.getData().getByte(0);
			Log.debug("There are "+num+" namespaces on this device");
			for (int i=0;i<num;i++){
				b = send(new NamespaceCommand(i));
				String space = b.getData().asString();
				Log.debug(space);
				list.add(space);
			}
		} catch (InvalidResponseException e) {
			Log.error("Invalid response from Namespace");
			
		} catch (NoConnectionAvailableException e) {
			Log.error("No connection is available.");
		}
		return list;
	}
	
	public void startHeartBeat(){
		lastPacketTime=System.currentTimeMillis();
		beater = new HeartBeat();
		beater.start();
	}
	public void startHeartBeat(long msHeartBeatTime){
		if (msHeartBeatTime<10)
			msHeartBeatTime = 10;
		heartBeatTime= msHeartBeatTime;
		startHeartBeat();
	}
	public void stopHeartBeat(){
		beater=null;
	}
	private BowlerAbstractDevice getInstance(){
		return this;
	}
	private class HeartBeat extends Thread{
		public void run(){
			while (connection.isConnected()){
				if((connection.msSinceLastSend())>heartBeatTime){
					try{
						if(ping()==null)
							connection.disconnect();
					}catch(Exception e){
						connection.disconnect();
					}
				}
				ThreadUtil.wait(10);
				if(getInstance() == null){
					connection.disconnect();
				}
			}
		}
	}
}
