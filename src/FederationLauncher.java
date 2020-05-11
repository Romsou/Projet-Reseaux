import Serveur.ChatAmuCentral.ChatamuCentral;
import Serveur.Federation.MasterServer;
import Tools.ConfigParser.ConfigParser;

import java.io.IOException;

public class FederationLauncher {
    private static final String DEFAULT_CONFIG_FILE = "src//Config/pairs.cfg";


    public static void main(String[] args) throws IOException, InterruptedException {
        startSlaveServers();
        Thread masterThread = new Thread(new MasterServer());
        masterThread.start();
    }


    public static void startSlaveServers() {
        ConfigParser configParser = new ConfigParser(DEFAULT_CONFIG_FILE);
        String[] fileContent = configParser.read();

        for (String line : fileContent) {
            String[] lineParts = line.split(" ");

            if (lineParts[0].equals("pairs")) {
                int port = Integer.parseInt(lineParts[3]);
                new Thread(new ChatamuCentral(port)).start();
            }
        }
    }
}


