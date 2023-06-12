package lanpush.cli;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lanpush.connectors.Receiver;
import lanpush.connectors.Sender;

/**
 * Lanpush's main class for CLI usage.
 * It parses through the command line parameters received and calls Sender/Receiver accordingly.
 * It also handles the outputs, printing messages received, potential errors and the app's help.
 */
public final class Lanpush {
   
    public static void main(String[] args) {
        List<String> sysArgs = new ArrayList<>(Arrays.asList(args));
        if (sysArgs.isEmpty() || sysArgs.contains("-h") || sysArgs.contains("--help")) {
            printHelp();
        }
        else if (sysArgs.contains("-l") || sysArgs.contains("--listen")) {
            listenerMode(sysArgs);
        }
        else if (sysArgs.size() == 2 && sysArgs.get(0).contains(":"))  {
           senderMode(sysArgs);
        }
        else {
            System.err.println("Parameters not recognized.");
            printHelp();
        }
    }

    private static void printHelp() {
        String helpMsg = "-------------------------------------------------------------------------\n"
                + "                                 LANPUSH                                 \n"
                + "-------------------------------------------------------------------------\n"
                + "Usage for listener mode: lanpush [-l | --listen] <PORT>\n"
                + "Example for listening on UDP port 1050: lanpush -l 1050\n"
                + "-------------------------------------------------------------------------\n"
                + "Usage for sending message: lanpush <IPs>:<PORT> <MESSAGE>\n"
                + "Multiple IPs can be used separated by comma\n"
                + "Example for sending: lanpush 192.168.0.255,192.168.0.1:1050 \"Hello, world!\"\n"
                + "-------------------------------------------------------------------------\n";
        System.out.println(helpMsg);
    }

    private static void listenerMode(List<String> sysArgs) {
        sysArgs.remove("--listen"); sysArgs.remove("-l");
        if (sysArgs.size() > 1) {
            System.err.println("[LANPUSH ERROR] Received parameter for listener mode, but excessive parameters were given.\n"
                + "For listener mode, only port number is allowed. Try '-h' or '--help' for instructions.");
        }
        else if (sysArgs.isEmpty()) {
            System.err.println("[LANPUSH ERROR] Received parameter for listener mode, but no port was specified.\n"
                + "For listener mode, a port number must me given. Try '-h' or '--help' for instructions.");
        }
        else {
            int portNumber;
            try {
                portNumber = Integer.parseInt(sysArgs.get(0));
            }
            catch(NumberFormatException e) {
                System.err.println("[LANPUSH ERROR] Received parameter for listener mode, but specified port is invalid.\n"
                + "For listener mode, a port number must me given. Try '-h' or '--help' for instructions.");
                return;
            }
            System.out.println("[LANPUSH] Starting to listen for messages on port " + portNumber);
            
            Receiver receiver = new Receiver();
            setShutdownProcedure(receiver);
            String receivedMessage;
            try {
                while((receivedMessage = receiver.listen(portNumber)) != null) {
                    System.out.println("[LANPUSH] Message received: " + receivedMessage);
                }
            } catch (Throwable t) {
                System.err.printf("[LANPUSH ERROR] Couldn't keep connection on port %d. Listener will shutdown.\n", portNumber);
            }
        }
    }

    private static void setShutdownProcedure(Receiver receiver) {
		Thread shutdown = new Thread(new Runnable() {
			public void run() {
                receiver.stop();
				System.out.println("[LANPUSH] Received shutdown signal. Listener was closed.");
			}
		}, "Shutdown-thread");
		Runtime.getRuntime().addShutdownHook(shutdown);
	}

    private static void senderMode(List<String> sysArgs) {

        String[] ips = sysArgs.get(0).split(":")[0].split(",");
        String port = sysArgs.get(0).split(":")[1];
        String msg = sysArgs.get(1);

        try {
            Sender.send(ips, Integer.parseInt(port), msg);
        } catch (NumberFormatException e) {
            System.err.printf("Couldn't parse port number '%s'. It must be an integer.\n", port);
        } catch (IOException e) {
            System.err.println("Error while trying to send message: " + e.getMessage());
        }
    }
}
