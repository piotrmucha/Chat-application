package server;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.PropertiesUtils;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.Properties;

public class ServerApp {

    private static String PROPERTIES_PATH = "config.properties";
    private static String PROPERTIES_ABSOLUTE_PATH = "src/main/resources/config.properties";
    private static String PORT = "port";
    private static String REMOTE_PORT = "remotePort";
    private static String REMOTE_ADDRESS = "remoteAddress";
    private static String HOST_ADDRESS = "hostAddress";

    private static Logger logger = LogManager.getLogger(ServerApp.class);


    public static void main(String[] args) {
        Properties properties = getProperties();
        if (properties == null) return;
        int port = getIntProperty(properties, PORT);
        int remotePort = getIntProperty(properties, REMOTE_PORT);
        String remoteAddress = properties.getProperty(REMOTE_ADDRESS);
        String hostAddress = setProperHostAddress(remotePort, remoteAddress);
        if (hostAddress.isEmpty()) return;
        Server server = Server.getInstance(port, hostAddress);
        runServerInstance(server);
    }

    private static String setProperHostAddress(int remotePort, String remoteAddress) {
        String hostAddress = getHostAddress(remoteAddress, remotePort);
        if (hostAddress.isEmpty()) return hostAddress;
        PropertiesUtils.storeField(HOST_ADDRESS, hostAddress, PROPERTIES_ABSOLUTE_PATH);
        return hostAddress;
    }

    private static void runServerInstance(Server server) {
        try {
            server.run();
        } catch (IOException exc) {
            logger.error("error while processing server session");
            exc.printStackTrace();
        }
    }

    private static Properties getProperties() {
        try {
            Properties properties = new Properties();
            properties.load(Objects.requireNonNull(ServerApp.class.getClassLoader().getResourceAsStream(PROPERTIES_PATH)));
            return properties;
        } catch (IOException e) {
            logger.error("cannot bind properties file with properties object");
            return null;
        }
    }

    private static String getHostAddress(String remoteAddress, int remotePort) {
        String hostAddress;
        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName(remoteAddress), remotePort);
            hostAddress = socket.getLocalAddress().getHostAddress();
            return hostAddress;
        } catch (SocketException | UnknownHostException e) {
            logger.error("cannot retrieve host address");
        }
        return "";
    }

    private static int getIntProperty(Properties properties, String field) {
        String property = properties.getProperty(field);
        return Integer.parseInt(property);
    }

}

