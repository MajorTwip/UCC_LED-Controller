package ch.comstock.uccled.modules;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * 
 * This class reads a given file and executes those orders
 *
 */
public class Fileproc {
	static final Logger log = LogManager.getLogger(Fileproc.class);
	private boolean isFading = true;
	BufferedReader fileBuf;
	Mqtt mqtt;
	Serial serial;
	
	
	/**
	 * Constructor
	 */
	public Fileproc() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Links to the other modules
	 * @param mqtt The running MQTT-module
	 * @param serial The running Serial-module
	 */
	public void setModules(Mqtt mqtt, Serial serial){
		this.mqtt = mqtt;
		this.serial = serial;
	}
	
	
	/**
	 *Normally set by Serial-DMX 
	 * @param isFading Indicates if the Leds are fading to the new Value (true=fading, false=finished)
	 */
	public synchronized void setIsFading(boolean isFading){
		this.isFading= isFading;
		notifyAll();
	}
	
	
	
	public void playFile(String path){
		try{
			fileBuf = new BufferedReader(new FileReader(path));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fileworker();
	}
	
	private synchronized void fileworker(){
		String line;
		while(fileBuf!=null){
			if(!isFading){
				try {
					line = fileBuf.readLine();
					if(line != null) {
						log.debug(line);
						serial.send(line);
						this.isFading=true;
					}
					else{
						fileBuf.close();
						fileBuf=null;
					}
				} catch (IOException e) {e.printStackTrace();}
			}
			try {
				wait();
			} catch (InterruptedException e1) {}
		}
	}
}
