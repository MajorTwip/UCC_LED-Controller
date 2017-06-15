/**
 * This app connects to an MQTT-Server as Client and subscribes to a channel to get commands.
 * These commands it then sends over serial to a serial-DMX converter.
 */

package ch.comstock.uccled;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import ch.comstock.uccled.modules.*;

/**
 * This Class instantiates and links the modules for MQTT, Serial and Fileprocessing. 
 * 
 * @author MajorTwip
 * @version 0.0.1
 * 
 *
 */

public class UCC_Led_Controller {
	
	static String confFile = "config.properties";
	static Properties conf = new Properties();
	static Logger log = LogManager.getLogger(UCC_Led_Controller.class);
	
	static Serial serial;
	static Mqtt mqtt;
	static Fileproc fileproc;
	

	/**
	 * Main class. Entrypoint for the app.
	 * @param args Only one argument is used, the location/name of the config-file
	 */	
	public static void main(String[] args) {
		System.out.println("Starting UCC_LED-Control");
		if(args.length>0){
			confFile = args[0];
		}
		getConfig();
		
		//Instanciate the modules
		fileproc = new Fileproc();
		serial = new Serial();
		mqtt = new Mqtt();
		
		//Link the modules together
		fileproc.setModules(mqtt, serial);
		serial.setModules(mqtt, fileproc);
		mqtt.setModules(fileproc, serial);
		
		//connect to DMX		
		serial.connect(conf.getProperty("ttyPort","/dev/ttyAMA0"), conf.getProperty("ttyBaud", "115200"));
		
		fileproc.playFile("test.txt");
		
		
	}

	/**
	 * Loads Conffile (config.properties if no argument is passed)
	 */
	private static void getConfig(){
		//get config, create sample if no file exists
		InputStream inputStream = null;
		try{
			inputStream = new FileInputStream(confFile);
			conf.load(inputStream);
			log.info("Config loaded");
		} catch (Exception e) {
			//log.error("Exception: " + e);
			log.warn("Probably no ConfFile existing, creating sample");
			createConfig();
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Creates sample of configfile if no file is found
	 */
	private static void createConfig(){
		OutputStream output = null;

		try {

			output = new FileOutputStream(confFile);

			// set the properties value
			conf.setProperty("ttyPort", "/dev/ttyAMA0");
			conf.setProperty("ttyBaud", "115200");
			conf.setProperty("mqttHost", "192.168.2.246");
			conf.setProperty("mqttTopicBase", "/PowerGov/");

			// save properties to project root folder
			conf.store(output, null);

		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (output != null) {
				try {
					output.close();
					log.warn("written, please modify " + confFile + " and restart");
					System.exit(0);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
