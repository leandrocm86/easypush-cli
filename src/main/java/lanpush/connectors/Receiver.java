package lanpush.connectors;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Receivers can connect to a given UDP port and listen for any messages arriving through it.
 */
public class Receiver {
	
	private DatagramSocket udpSocket;
    private boolean stopSignal = false;

	
	/**
	 * Starts listenning on the given port until there is a message to return or #stop() is invoked.
	 * It's recommended to call this method in a separated thread if the current thread cannot be block.
	 * The receiver will automatically disconnect itself upon an abrupt shutdown signal,
	 * and then it will execute the given shutdown procedure (if any).
	 * 
	 * @param udpPort - The UDP port number to listen to.
	 * @param shutdownProcedure - Procedure to be executed upon program termination.
	 * 
	 * @return the first received message, or null if #stop() was invoked.
	 */
    public String listen(int udpPort, Runnable shutdownProcedure) throws SocketException, IOException {
		this.stopSignal = false;
		this.setShutdownProcedure(shutdownProcedure);
		try {
			DatagramPacket packet = reconnect(udpPort);
			udpSocket.receive(packet);
			return new String(packet.getData(), 0, packet.getLength()).trim();
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

	private void setShutdownProcedure(Runnable shutdownProcedure) {
		Thread shutdown = new Thread(new Runnable() {
			public void run() {
				stop();
				if (shutdownProcedure != null)
					shutdownProcedure.run();
			}
		}, "Shutdown-thread");
		Runtime.getRuntime().addShutdownHook(shutdown);
	}
}