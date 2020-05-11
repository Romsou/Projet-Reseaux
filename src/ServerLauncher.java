import Serveur.ChatAmuCentral.ChatamuCentral;
import Tools.ConfigParser.ConfigParser;

public class ServerLauncher {
    private static final String DEFAULT_CONFIG_FILE = "src//Config/pairs.cfg";


    public static void main(String[] args) {
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
