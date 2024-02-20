import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Utility {
    static Properties prop = new Properties();
    private static final Logger logger = Logger.getLogger(Client_FS.class.getName());

    static {
        try (FileInputStream inputStream = new FileInputStream("config.properties")) {
            prop.load(inputStream);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void logging_util(String classname, String filename) throws IOException {
        String logDirectory = prop.getProperty("logDestinationDirectory");
        Files.createDirectories(Paths.get(logDirectory));
        Path logFilePath = Paths.get(logDirectory, filename);
        // Check if the log file already exists
        if (Files.notExists(logFilePath)) {
            // Create a FileHandler to write to a log file
            FileHandler fileHandler = new FileHandler(logFilePath.toString(), true);
            fileHandler.setFormatter(new SimpleFormatter());
            Logger logger = Logger.getLogger(classname);
            logger.addHandler(fileHandler);

            // Log a message (example)
            logger.info("Log file created");
        }
//        FileHandler fileHandler = new FileHandler(logDorectory + File.separator + "fileclient.log", true);
//        fileHandler.setFormatter(new SimpleFormatter());
//        logger.addHandler(fileHandler);
    }
}
