package Application;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ApplicationConfigLoader {

    public void load(String configPath) {
        Properties props = new Properties();
        try {props.load(new FileInputStream("src/config.properties"));}
        catch (IOException e) {throw new RuntimeException(e);}

        String appName = props.getProperty("app.name");
        String dbUrl   = props.getProperty("db.url");

    }

    public static void main(String[] args) throws IOException {



    }
}
