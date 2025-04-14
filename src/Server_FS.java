import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Server_FS {
    private ServerSocket serverSocket;
    private int clientNumber;
    private static final AtomicInteger clientCount = new AtomicInteger(0);
    private File directory;

    private static final Logger logger = Logger.getLogger(Server_FS.class.getName());

    static Properties prop = new Properties();

    static {
        try (FileInputStream inputStream = new FileInputStream("config.properties")) {
            prop.load(inputStream);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try {
            String logDorectory = prop.getProperty("logDestinationDirectory");
            Files.createDirectories(Paths.get(logDorectory));
            FileHandler fileHandler = new FileHandler(logDorectory + File.separator + "fileserver.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error occur in FileHandler.", e);
        }
    }

    public Server_FS(int port, String directoryPath) throws IOException {
        serverSocket = new ServerSocket(port);
        directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            throw new IllegalArgumentException("Invalid directory path");
        }
    }

    public void start() {
        //TODO: FileWatcher
        System.out.println("Server started. Listening for connections...");
        logger.info("Server started. Listening for connections...");
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
                //handleClient(clientSocket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String request = in.readLine();
            if ("get_files_list".equals(request)) {
                sendFilesList(out);
            } else if (request.startsWith("download")) {
                String fileName = request.split(" ")[1];
                sendFile(fileName, clientSocket);
            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Exception in handleClient", e);
            e.printStackTrace();
        }
    }

    private void sendFilesList(PrintWriter out) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                out.println(file.getName());
            }
        }
    }

    private void sendFile(String fileName, Socket clientSocket) {
        File file = new File(directory, fileName);
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file);
                 BufferedOutputStream bos = new BufferedOutputStream(clientSocket.getOutputStream())) {
                byte[] buffer = new byte[4096];
                int count;
                while ((count = fis.read(buffer)) > 0) {
                    bos.write(buffer, 0, count);
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Exception in sendFile", e);
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(System.getProperty("user.dir"));
        int port = Integer.parseInt(prop.getProperty("port"));
        String directoryPath = prop.getProperty("fileDirectory");
        try {
            Server_FS server = new Server_FS(port, directoryPath);
            server.start();
        } catch (IOException e) {
            System.err.println("Server could not be started: " + e.getMessage());
            logger.log(Level.SEVERE, "Server could not be started:", e);
        }
    }
}


