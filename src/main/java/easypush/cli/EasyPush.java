package easypush.cli;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import easypush.connectors.Receiver;
import easypush.connectors.Sender;

/**
 * EasyPush's main class for CLI usage.
 * It parses through the command line parameters received and calls Sender/Receiver accordingly.
 * It also handles the outputs, printing messages received, potential errors and the app's help.
 */
public final class EasyPush {

    private static Receiver receiver;
    private static Thread shutdownThread;
   
    public static void main(String[] args) {
        List<String> sysArgs = new ArrayList<>(Arrays.asList(args));
        if (sysArgs.isEmpty() || sysArgs.contains("-h") || sysArgs.contains("--help")) {
            printHelp();
        }
        else if (sysArgs.contains("-l") || sysArgs.contains("--listen")) {
            listenerMode(sysArgs);
        }
        else if (sysArgs.size() == 2 && sysArgs.get(0).contains("@"))  {
            senderMode(sysArgs);
        }
        else {
            System.err.println("Parameters not recognized.");
            printHelp();
        }
        System.exit(0);
    }

    private static void printHelp() {
        String helpMsg = 
                  "----------------------------------------------------------------------------------------------\n"
                + "                                           EASYPUSH                                           \n"
                + "----------------------------------------------------------------------------------------------\n"
                + "Usage for listener mode: easypush --listen <UDP_PORT> [--wait <milliseconds>] \n"
                + "Example for listening on UDP port 1050: easypush -l 1050\n"
                + "That way, easypush will keep listening continuously for all messages sent to port 1050.\n"
                + "Example for listening on UDP port 1050 and waiting for 1 sec: easypush -l 1050 -w 1000\n"
                + "When the optional 'wait' parameter is used, easypush waits for a message up to a maximum time.\n"
                + "That way, it will exit after the first message is received or the given time expires.\n"
                + "----------------------------------------------------------------------------------------------\n"
                + "Usage for sending message: easypush <IPs>@<PORT> <MESSAGE>\n"
                + "Multiple IPs can be used separated by comma.\n"
                + "Example for sending: easypush 192.168.0.255,192.168.0.1@1050 \"Hello, world!\"\n"
                + "----------------------------------------------------------------------------------------------\n"
                + "For more information about this app or its siblings (desktop or android),\n"
                + "check its github repository: https://github.com/leandrocm86/easypush-cli\n"
                + "----------------------------------------------------------------------------------------------\n";
        System.out.println(helpMsg);
    }

    private static void listenerMode(List<String> sysArgs) {
        int waitParameterIndex = sysArgs.contains("-w") ? sysArgs.indexOf("-w") : sysArgs.indexOf("--wait");
        String possibleErrorMessage = "Received 'wait' parameter, but specified time is absent or invalid";
        int timeout = waitParameterIndex > -1 ? convertIntParameter(sysArgs.get(waitParameterIndex + 1), possibleErrorMessage) : 0;

        // Consumes all parameters until only the port number is left
        if (waitParameterIndex > -1) {
            sysArgs.remove(waitParameterIndex + 1);
            sysArgs.remove(waitParameterIndex);
        }
        sysArgs.remove("--listen");
        sysArgs.remove("-l");

        if (sysArgs.size() > 1) {
            System.err.println("[EASYPUSH ERROR] Received parameter for listener mode, but excessive parameters were given.\n"
                + "For listener mode, only port number and wait time are allowed. Try '-h' or '--help' for instructions.");
        }
        else if (sysArgs.isEmpty()) {
            System.err.println("[EASYPUSH ERROR] Received parameter for listener mode, but no port was specified.\n"
                + "For listener mode, a port number must me given. Try '-h' or '--help' for instructions.");
        }
        else {
            possibleErrorMessage = "Received parameter for listener mode, but specified port is invalid.\n"
                    + "For listener mode, a port number must be given";
            int portNumber = convertIntParameter(sysArgs.get(0), possibleErrorMessage);
            System.out.println("[EASYPUSH] Starting to listen for messages on port " + portNumber);

            receiver = new Receiver();
            shutdownThread = setShutdownProcedure();

            if (timeout > 0)
                waitForMessage(portNumber, timeout);
            else
                keepListening(portNumber);
        }
    }

    private static Thread setShutdownProcedure() {
        Thread shutdownThread = new Thread(() -> {
            receiver.stop();
            System.out.println("[EASYPUSH] Received shutdown signal. Listener was closed.");
        }, "Shutdown-thread");
        Runtime.getRuntime().addShutdownHook(shutdownThread);
        return shutdownThread;
	}

    private static void waitForMessage(int portNumber, int timeout) {
        Thread listeningThread = new Thread(() -> {
            listen(portNumber);
            Runtime.getRuntime().removeShutdownHook(shutdownThread);
        });
        listeningThread.start();
        try {
            listeningThread.join(timeout);
        } catch (InterruptedException e) {
            receiver.stop();
        }
        if (listeningThread.isAlive()) {
            listeningThread.interrupt();
            receiver.stop();
            System.out.printf("[EASYPUSH] Listener timed out waiting for message on port %d.\n", portNumber);
            Runtime.getRuntime().removeShutdownHook(shutdownThread);
        }
    }

    private static void keepListening(int portNumber) {
        for (String receivedMessage = ""; receivedMessage != null; receivedMessage = listen(portNumber));
    }

    private static String listen(int portNumber) {
        try {
            String receivedMessage = receiver.listen(portNumber);
            if (receivedMessage != null)
                System.out.println("[EASYPUSH] Message received: " + receivedMessage);
            return receivedMessage;
        } catch (Throwable t) {
            System.err.printf("[EASYPUSH ERROR] Couldn't keep connection on port %d. Listener will shutdown.\n", portNumber);
            return null;
        }
    }

    private static int convertIntParameter(String parameter, String errorMessage) {
        try {
            return Integer.parseInt(parameter);
        }
        catch (NumberFormatException e) {
            System.err.printf("[EASYPUSH ERROR] %s. Try '-h' or '--help' for instructions.\n", errorMessage);
            System.exit(1);
            return 1;
        }
    }

    private static void senderMode(List<String> sysArgs) {

        String[] ips = sysArgs.get(0).split("@")[0].split(",");
        String port = sysArgs.get(0).split("@")[1];
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

/*
MMMMMMMMMWXOo:'.        .';oOXWMMMMMMMMM
MMMMMMMW0l' .':ldkOOOOkdl:'. 'l0WMMMMMMM
MMMMMW0c. 'lONWMWWNNNNWWMWNOl' .c0WMMMMM
MMMMWx. .oXWMNOo:,'..',:oONMWXo. .xWMMMM
MMMWx. ,0WMNd,  ':cllc:'  ,dNMW0, .xWMMM
MMMO' '0MMK: .:kNWWWWWWNk:. :KMM0' 'OMMM
MMNl .dWMNc .oNMKo;,,;oKMNo. cNMWd. lNMM
MMX: .OMM0' ,KMX:      :XMK, '0MMO. :XMM
MMX: .kMM0' '0MNl      lNM0' '0MMk. :XMM
MMWo  oWMNo  :KMK,    ,KMX:  oNMWl  oWMM
MMM0, .kWMXo. ;Kk.    .kK; .oXMWk. ,0MMM
MMMWO' .xNMW0dkKc .''. cKkd0WMNx. 'OWMMM
MMMMW0; .cKMMMMk. .::. .kMMMMKc. ;0WMMMM
MMMMMMXd;:0MMMNc  '..   cNMMM0:;dXMMMMMM
MMMMMMMMWWMMMMO.  'cdo' .OMMMMWWMMMMMMMM
MMMMMMMMMMMMMNl  .,cl:.  lNMMMMMMMMMMMMM
MMMMMMMMMMMMM0' 'kOc,,,. 'OMMMMMMMMMMMMM
MMMMMMMMMMMMWo  :d:cdOXo  oWMMMMMMMMMMMM
MMMMMMMMMMMM0'  . .;:::'  '0MMMMMMMMMMMM
MMMMMMMMMMMMx.            .xMMMMMMMMMMMM
 */
