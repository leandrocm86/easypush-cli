package lanpush.connectors;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


/**
 * Encapsulates the logic for sending messages through an UDP port.
 */
public class Sender {
    
	/**
	 * Sends a given message to the given IP(s) and port (UDP).
	 * 
	 * @param hosts - An array of IPs for sending the message to.
	 * @param udpPort - The UDP port number for sending the message to.
	 * @param message - The message to be sent.
	 */
    public static void send(String[] hosts, int udpPort, String message) throws IOException {
    	for (String host : hosts) {
	        // Get the internet address of the specified host
	        InetAddress address = InetAddress.getByName(host.toString());
	
	        // Initialize a datagram packet with data and address
	        DatagramPacket packet = new DatagramPacket(message.getBytes(), message.getBytes().length, address, udpPort);
	
	        // Create a datagram socket, send the packet through it, close it.
	        DatagramSocket dsocket = new DatagramSocket();
	        dsocket.send(packet);
	        dsocket.close();
    	}
    }
}