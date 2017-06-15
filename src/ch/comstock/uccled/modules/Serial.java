package ch.comstock.uccled.modules;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

/**
 * 
 * This class deals with the communication to the Serial-DMX-Controller
 *
 */
public class Serial {

	SerialPort serPort;
	StringBuilder serInputBuf = new StringBuilder();
	static final Logger log = LogManager.getLogger(Serial.class);
	Mqtt mqtt;
	Fileproc fileproc;
	
	/**
	 * Constructor
	 */
	public Serial() {

	}
	
	/**
	 * Links to the other modules
	 * @param mqtt The running MQTT-module
	 * @param fileproc The running Fileprocessing-module
	 */
	public void setModules(Mqtt mqtt, Fileproc fileproc) {
		this.fileproc = fileproc;
		this.mqtt = mqtt;
	}

	/**
	 * Connects to the Serial-DMX-Controller
	 * @param port Portname (ex COM4, /dev/ttyUSB)
	 * @param str_baud Baudrate
	 * @return True if connected
	 */
	public boolean connect(String port, String str_baud){
		int baud = Integer.parseInt(str_baud);
		
		log.info("Connecting to DMX on port " + port + " at " + baud + " Baud");
		serPort = new SerialPort(port);
		try {
			serPort.openPort();
			serPort.setParams(baud,
		                         SerialPort.DATABITS_8,
		                         SerialPort.STOPBITS_1,
		                         SerialPort.PARITY_NONE);
			serPort.addEventListener(new PortReader(), SerialPort.MASK_RXCHAR);
			log.info("Port opened, registering shutdown hook");
			Runtime.getRuntime().addShutdownHook(new Thread() {
			    @Override
				public void run() {
			    	log.warn("Shutting down...");
			    	try {
			    		serPort.closePort();
			    	} catch (SerialPortException e) {
			    		// TODO Auto-generated catch block
			    	}
			    }
			 });
			return true;
		}catch (SerialPortException ex) {
			    log.warn("There are an error with port : " + ex);
			return false;
		}
	}
	
	
	/**
	 * Sends a String to the serialport
	 * @param data String to send
	 * @return True if success
	 */
	public boolean send(String data){
		try {
			serPort.writeString(data);	
			return true;
		} catch (SerialPortException e) {
			log.warn("tty not open");
			return false;
		}
	}
	
	/**
	 * 
	 * Callback from Serialport
	 * Reads input Bytewise and sets fileproc's isFading
	 *
	 */
	private class PortReader implements SerialPortEventListener{
		int pointer=0;
		@Override
	    public void serialEvent(SerialPortEvent event) {
			if(event.isRXCHAR() && event.getEventValue() > 0){
		        try {
		            byte buffer[] = serPort.readBytes();
		            for (byte b: buffer) {
		            	pointer++;
		            	log.trace(b);
		            	if(b==0x30 && pointer==1){ //Hex 0x30 = ANSII 0
		            		fileproc.setIsFading(false);
		            		log.debug("set isFading to false");
		            	}else if(b==0x31 && pointer==1){ //Hex 0x31 = ANSII 1
		            		fileproc.setIsFading(true);
		            		log.debug("set isFading to true");
		            	}else if(b == '\r' || b == '\n'){
		            		pointer=0;
		            	}
		            }                
		        }
		        catch (SerialPortException ex) {
		        	System.out.println("There are an error with port : " + ex);
		        }
		    }
		}
	}

}

