package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class PropertiesUtils {
    private static Logger logger = LogManager.getLogger(PropertiesUtils.class);

    public static void storeField(String field, String value, String path) {
        try {
            Properties AppProps = new Properties();
            AppProps.load(new FileInputStream(path));
            Path propertyFile = Paths.get(path);
            Writer PropWriter =
                    Files.newBufferedWriter(propertyFile);
            AppProps.setProperty(field, value);
            AppProps.store(PropWriter,
                    "Application Properties");
            PropWriter.close();
        } catch (IOException Ex) {
            logger.error("IO Exception :{}",
                    Ex.getMessage());
        }
    }

}