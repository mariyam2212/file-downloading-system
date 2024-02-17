import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;

public class Client_FS {
    private String serverIP;
    private int serverPort;
    private int maxDownloadAttempts;
    private ExecutorService downloadExecutor;

    private final int numThreads;
    private final String fileSize;
    private final int numExperiments;
    private static final Logger logger = Logger.getLogger(Client_FS.class.getName());
    static Properties prop = new Properties();

    static {
        try {
            FileHandler fileHandler = new FileHandler("fileclient.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error occur in FileHandler.", e);
        }
        try (FileInputStream inputStream = new FileInputStream("config.properties")) {
            prop.load(inputStream);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public Client_FS(String serverIP, int serverPort, int maxDownloadAttempts, int numThreads, String fileSize, int numExperiments) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.maxDownloadAttempts = maxDownloadAttempts;
        this.numThreads = numThreads;
        this.fileSize = fileSize;
        this.numExperiments = numExperiments;
        this.downloadExecutor = Executors.newCachedThreadPool();
    }

    public List<String> getFileList() {
        List<String> fileList = new ArrayList<>();
        try (Socket socket = new Socket(serverIP, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("get_files_list");
            String fileName;
            while ((fileName = in.readLine()) != null && !fileName.isEmpty()) {
                fileList.add(fileName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileList;
    }

    public void start() throws InterruptedException {
        for (int i = 0; i < numExperiments; i++) {
            long startTime = System.currentTimeMillis();

            ExecutorService executor = Executors.newFixedThreadPool(numThreads);
            for (int t = 0; t < numThreads; t++) {
                executor.execute(this::downloadFileSized);
            }
            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

            long endTime = System.currentTimeMillis();
            System.out.println("\n\nExperiment " + (i + 1) + ": Time taken = " + (endTime - startTime) + " ms");
        }
    }

    public void downloadFileSized() {
        long startTime = System.currentTimeMillis(); // Start timing
        downloadExecutor.submit(() -> downloadFile(fileSize));
        long endTime = System.currentTimeMillis(); // End timing
        System.out.println("Total download duration: " + (endTime - startTime) + "ms");
       // logger.info("Total download time: " + (endTime - startTime) + " ms");
    }

    public void downloadFiles(List<String> fileNames, boolean isParallel) {
        long startTime = System.currentTimeMillis(); // Start timing
        if (isParallel) {
            for (String fileName : fileNames) {
                downloadExecutor.submit(() -> downloadFile(fileName));
            }
        } else {
            for (String fileName : fileNames) {
                downloadFile(fileName);
            }
        }
        long endTime = System.currentTimeMillis(); // End timing
        System.out.println("Total download duration: " + (endTime - startTime) + "ms");
     //   logger.info("Total download time: " + (endTime - startTime) + " ms");
    }

    private void downloadFile(String fileName) {
        for (int attempt = 0; attempt < maxDownloadAttempts; attempt++) {
            long startTime = System.currentTimeMillis(); // Start timing
            String downloadLocation = prop.getProperty("downloadDestinationDirectory") + File.separator;
            File directory = new File(downloadLocation);
            if (!directory.exists()) {
                boolean isDirectoryCreated = directory.mkdirs();
                if (!isDirectoryCreated) {
                    System.err.println("Failed to create directory: " + downloadLocation);
                    logger.log(Level.SEVERE, "Failed to create directory: " + downloadLocation);
                    return;
                }
            }

            try (Socket socket = new Socket(serverIP, serverPort);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
                 FileOutputStream fos = new FileOutputStream(downloadLocation + fileName)) {

                out.println("download " + fileName);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
                //TODO: MD5 checksum verification can be added here
                long endTime = System.currentTimeMillis(); // end timing
                long duration = endTime - startTime;
                System.out.println("Downloaded: " + fileName + " in : " + duration + "ms");
             //   logger.info("Successfully downloaded file: " + fileName + " in " + duration + " ms");
                return;
            } catch (IOException e) {
                System.out.println("Attempt " + (attempt + 1) + " failed for file: " + fileName);
                logger.log(Level.SEVERE, "Failed to download file: " + fileName + "after " + (attempt + 1) + "attempts", e);
            }
        }
        System.out.println("Failed to download file after " + maxDownloadAttempts + " attempts: " + fileName);
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String serverIP = prop.getProperty("serverIP");
        int serverPort = Integer.parseInt(prop.getProperty("serverPort"));
        int maxDownloadAttempts = Integer.parseInt(prop.getProperty("maxDownloadAttempts"));
        String filesize = "";
        // Check if server IP and port are provided as arguments
        if (args.length >= 1) {
            filesize = args[0];
        }
        Client_FS client = new Client_FS(serverIP, serverPort, maxDownloadAttempts, 10, filesize, 5);
        //  while (true) {
        if (filesize.equals("")) {
            System.out.println("get_files_list");
            logger.info("get_files_list request");
            List<String> fileList = client.getFileList();
            System.out.println("Files available for download: " + fileList);
            System.out.println("Enter comma-separated filenames to be downloaded:");
            String inputLine = sc.nextLine(); // Read the whole line of input

            String[] fileNamesArray = inputLine.split(","); // Split by comma
            fileList = new ArrayList<>(Arrays.asList(fileNamesArray));

            fileList = fileList.stream().map(String::trim).collect(Collectors.toList());
            if (fileList.size() > 1) {
                System.out.println("1: Serial Download\n2: Parallel Download");
                int a = sc.nextInt();
                // For parallel download, set the second argument to true
                logger.info("downloadFiles request");
                client.downloadFiles(fileList, a == 2);
            } else {
                client.downloadFiles(fileList, false);
            }
        } else {
            try {
                client.start();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }


        // Shutdown the executor service
        client.downloadExecutor.shutdown();
        //  }
    }
}

