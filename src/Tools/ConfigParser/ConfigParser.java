package Tools.ConfigParser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ConfigParser {
    private BufferedReader configFile;

    public ConfigParser(String configFile) {
        try {
            this.configFile = new BufferedReader(new FileReader(configFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String[] read() {
        String fileContent = "";
        String line;

        try {
            while ((line = configFile.readLine()) != null)
                fileContent += line;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileContent.split("\n");
    }
}
