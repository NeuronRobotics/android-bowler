package com.neuronrobotics.sdk.addons.walker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.dyio.peripherals.ServoChannel;


public class BasicWalker {
	private ArrayList<Leg> legs=new ArrayList<Leg>();
	private double scale=0,inverse=0,linkLen = 0;
	private double x,y,theta;
	private int    llimit,ulimit,home,channel;
	private DyIO dyio;
	private boolean useHardware = true;
	public BasicWalker(File f,DyIO d){
		//useHardware = false;
		if(useHardware){
			dyio=d;
		}
		dyio.setCachedMode(true);
		/**
		 * sample code from
		 * http://www.mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/
		 */
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder dBuilder;
	    Document doc = null;
	    try {
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(f);
			doc.getDocumentElement().normalize();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		} catch (SAXException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		//System.out.println("Parsing File...");
		NodeList nList = doc.getElementsByTagName("leg");
		for (int temp = 0; temp < nList.getLength(); temp++) {
			System.out.println("Leg # "+temp);
		    Node nNode = nList.item(temp);	    
		    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
		    	Element eElement = (Element) nNode;
		    	x = Double.parseDouble(getTagValue("x",eElement));
		    	y = Double.parseDouble(getTagValue("y",eElement));
		    	theta = Double.parseDouble(getTagValue("theta",eElement));
		    	Leg tmpLeg = new Leg(x,y,theta);
		    	NodeList links = eElement.getElementsByTagName("link");
		    	for (int i = 0; i < links.getLength(); i++) {
		    		System.out.println("\tLink # "+i);
		    		Node lNode = links.item(i);
		    		if (lNode.getNodeType() == Node.ELEMENT_NODE) {
			    		Element lElement = (Element) lNode;
			    		llimit=Integer.parseInt(getTagValue("llimit",lElement));
			    		ulimit=Integer.parseInt(getTagValue("ulimit",lElement));
			    		home=Integer.parseInt(getTagValue("home",lElement));
			    		channel=Integer.parseInt(getTagValue("channel",lElement));
			    		inverse=Double.parseDouble(getTagValue("inverse",lElement));
			    		scale = Double.parseDouble(getTagValue("scale",lElement));
			    		linkLen = Double.parseDouble(getTagValue("linkLen",lElement));
			    		if(useHardware){
				    		ServoChannel srv = new ServoChannel(dyio.getChannel(channel));
				    		Link tmpLink = new Link(srv,home,llimit,ulimit,(scale*inverse),linkLen);
				    		tmpLeg.addLink(tmpLink,getTagValue("type",lElement));
			    		}
		    		}
		    	}
		    	if(!tmpLeg.legOk())
		    		throw new RuntimeException("Failed to get all leg elements for leg: "+temp);
		    	legs.add(tmpLeg);
		    }else{
		    	System.out.println("Not Element Node");
		    }
		}
		System.out.println("Populated Hexapod.");
		double time=.5;
		int leg=0;
		for (Leg l:legs){
			double hipStart=0.0-l.getThetaOffset();
			if(hipStart>90)
				hipStart-=180;
			if(hipStart<-90)
				hipStart+=180;	

			System.out.println("Leg : "+leg+" is set to : "+hipStart);
			l.setHip(hipStart, time);
			l.setKnee(0, time);
			l.setAnkle(-90, time);
			l.setStartPoint();
			leg++;
		}
		updateAllServos((float) .5);
		try {Thread.sleep(2000);} catch (InterruptedException e) {}
		for (Leg l:legs){
			l.save();
		}
		
	}
	public void Home() {
		for (Leg l:legs){
			l.Home();
		}
		dyio.flushCache(2);
	}
	public void save() {
		for (Leg l:legs){
			l.save();
		}
	}
	
	public void turnBody(double degrees,double time) {
		degrees*=-1;
		for (Leg l:legs){
			l.turn(degrees, time);
		}
		updateAllServos((float) time);
		fixAll(time);
	}
	
	public void incrementAllY(double inc,double time) {
		inc*=-1;
		for (Leg l:legs){
			l.incrementY(inc, time);
		}
		updateAllServos((float) time);
		fixAll(time);
	}
	public void incrementAllX(double inc,double time) {
		inc*=-1;
		for (Leg l:legs){
			l.incrementX(inc, time);
		}
		updateAllServos((float) time);
		fixAll(time);
	}

	public void incrementAllZ(double inc,double time) {
		inc*=-1;
		for (Leg l:legs){
			l.incrementZ(inc, time);
		}
		updateAllServos((float) time);
		fixAll(time);
	}
	public ArrayList<Leg> getLegs(){
		return legs;
	}
	
	public void fixAll(double time) {
		for (Leg l:legs){
			l.fix(time);
		}
	}
	public void updateAllServos(double time) {
		for (Leg l:legs){
			l.updateServos(time);
		}
		dyio.flushCache((float) time);
	}
	private static String getTagValue(String sTag, Element eElement){
	    NodeList nlList= eElement.getElementsByTagName(sTag).item(0).getChildNodes();
	    Node nValue = (Node) nlList.item(0); 
	    System.out.println("\t\t"+sTag+" = "+nValue.getNodeValue());
	    return nValue.getNodeValue();    
	}

}