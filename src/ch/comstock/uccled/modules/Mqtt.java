/**
 * Package containing modules needed for ch.comstock.uccled
 */
package ch.comstock.uccled.modules;


/**
 * 
 * This class deals with the communication to the MQTT-Server
 *
 */
public class Mqtt {
	Fileproc fileproc;
	Serial serial;
	
	/**
	 * Constructor
	 */
	public Mqtt() {
		
	}
	
	/**
	 * Links to the other modules
	 * @param serial The running Serial-module
	 * @param fileproc The running Fileprocessing-module
	 */
	public void setModules(Fileproc fileproc, Serial serial){
		this.fileproc = fileproc;
		this.serial = serial;
	}

}
