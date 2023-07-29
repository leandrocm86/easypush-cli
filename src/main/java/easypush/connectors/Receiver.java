package easypush.connectors;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

/**
 * Receivers can connect to a given UDP port and listen for any messages arriving through it.
 */
public class Receiver {
	
	private DatagramSocket udpSocket;
    private boolean stopSignal = false;

	
	/**
	 * Starts listenning on the given port until there is a message to return or #stop() is invoked.
	 * It's recommended to call this method in a separated thread if the current thread cannot be blocked.
	 * 
	 * @param udpPort - The UDP port number to listen to.
	 * 
	 * @return the first received message, or null if #stop() was invoked.
	 */
    public String listen(int udpPort) throws SocketException, IOException {
		this.stopSignal = false;
		try {
			DatagramPacket packet = reconnect(udpPort);
			udpSocket.receive(packet);
			return new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8).trim();
		} catch (Throwable t) {
			if (!stopSignal) {
				throw t;
			}
		} finally {
			closeConnection();
		}
		return null;
    }

	/**
	 * Stops the Receiver of listening for messages, closing the connection. 
	 */
    public void stop() {
    	this.stopSignal = true;
    	closeConnection();
    }

	private DatagramPacket reconnect(int udpPort) throws SocketException {
        if (udpSocket != null) {
            closeConnection();
        }
        udpSocket = new DatagramSocket(udpPort);
        byte[] message = new byte[8000];
        return new DatagramPacket(message, message.length);
    }

    private void closeConnection() {
        if (udpSocket != null) {
			if (!udpSocket.isClosed()) {
				udpSocket.close();
				udpSocket = null;
			}
		}
    }

}