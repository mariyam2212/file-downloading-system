import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Client_FS {
    private String serverIP;
    private int serverPort;
    private int maxDownloadAttempts;
    private ExecutorService downloadExecutor;

    private final int numThreads;
    private final String fileSize;

    private static String downloadLocation;
    private final int numExperiments;
    private static final Logger logger = Logger.getLogger(Client_FS.class.getName());
    static Properties prop = new Properties();
    private final SocketConnectionPool connectionPool;


    static {
        System.out.println("Client created!");
        try (FileInputStream inputStream = new FileInputStream("config.properties")) {
            prop.load(inputStream);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try {
            Utility.logging_util(Client_FS.class.getName(), "fileclient.log");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error occur in FileHandler.", e);
        }
        getDownloadLocation();
    }

    public Client_FS(String serverIP, int serverPort, int maxDownloadAttempts, int numThreads, String fileSize, int numExperiments, int poolSize) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.maxDownloadAttempts = maxDownloadAttempts;
        this.numThreads = numThreads;
        this.fileSize = fileSize;
        this.numExperiments = numExperiments;
        this.connectionPool = new SocketConnectionPool(serverIP, serverPort, poolSize);
        this.downloadExecutor = Executors.newCachedThreadPool();
    }

    public void start() throws InterruptedException {
        ArrayList<Double> experiment_durations = new ArrayList<>();
        for (int i = 0; i < numExperiments; i++) {
            long startTime = System.nanoTime(); // Start the timer

            // Create a thread pool with a fixed number of threads
            ExecutorService executor = Executors.newFixedThreadPool(numThreads);

            // Submit download tasks to the executor
            for (int t = 0; t < numThreads; t++) {
                executor.execute(() -> downloadMechanism(Collections.singletonList(fileSize), true));
            }
            // Initiates an orderly shutdown in which previously submitted tasks are executed, but no new tasks will be accepted
            executor.shutdown();

            // Wait for all submitted tasks to complete
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

            long endTime = System.nanoTime(); // End the timer
            double durationInMicroseconds = (endTime - startTime) / 1_000.0; // Convert ns to µs
            experiment_durations.add(durationInMicroseconds);
            // Calculate and print the time taken for the current experiment
            // System.out.println("\nEXPERIMENT " + (i + 1) + ": Time taken = " + durationInMilliseconds + " µs\n");
        }
        String durationsString = experiment_durations.stream()
                .map(duration -> String.format("%.2f", duration))
                .collect(Collectors.joining(", "));

        System.out.println("Experiment durations:" + durationsString);
    }


    public void downloadMechanism(List<String> fileNames, boolean isParallel) {

        if (isParallel) {
            List<Future<?>> futures = new ArrayList<>();

            for (String fileName : fileNames) {
                Future<?> future = downloadExecutor.submit(() -> downloadFile(fileName));
                futures.add(future);
            }

            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    // Consider additional error handling here
                }
            }


        } else {
            long startTime = System.nanoTime(); // Start timing for serial execution

            for (String fileName : fileNames) {
                downloadFile(fileName);
            }

            long endTime = System.nanoTime();
            double durationInMicroseconds = (endTime - startTime) / 1_000.0; // Convert ns to µs
            System.out.println("\nTotal serial download duration: " + durationInMicroseconds + "µs");
        }

        // If the executor is local to this method, shut it down here
        // downloadExecutor.shutdown();
    }


    //file download logic
    private void downloadFile(String fileName) {
        for (int attempt = 0; attempt < maxDownloadAttempts; attempt++) {
            long startTime = System.nanoTime(); // Start timing
            Socket socket = null;
            try {
                socket = connectionPool.borrowSocket();
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
                FileOutputStream fos = new FileOutputStream(downloadLocation + fileName);
                out.println("download " + fileName);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
                //TODO: MD5 checksum verification can be added here
                //   logger.info("Successfully downloaded file: " + fileName + " in " + duration + " µs");
                return;
            } catch (IOException e) {
                System.out.println("Attempt " + (attempt + 1) + " failed for file: " + fileName);
                logger.log(Level.SEVERE, "Failed to download file: " + fileName + "after " + (attempt + 1) + "attempts", e);
            } finally {
                if (socket != null) {
                    connectionPool.returnSocket(socket);
                    long endTime = System.nanoTime(); // end timing
                    double durationInMilliseconds = (endTime - startTime) / 1_000.0; // Convert ns to µs
                    //  System.out.println("Downloaded: " + fileName + " in : " + durationInMilliseconds + "µs");
                }
            }
        }
        System.out.println("Failed to download file after " + maxDownloadAttempts + " attempts: " + fileName);
    }

    public List<String> getFileList() {
        List<String> fileList = new ArrayList<>();
        Socket socket = null;
        try {
            socket = connectionPool.borrowSocket();
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println("get_files_list");
            String fileName;
            while ((fileName = in.readLine()) != null && !fileName.isEmpty()) {
                fileList.add(fileName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                connectionPool.returnSocket(socket);
            }
        }
        return fileList;
    }


    private static boolean getDownloadLocation() {
        downloadLocation = prop.getProperty("downloadDestinationDirectory") + File.separator;
        File directory = new File(downloadLocation);
        if (!directory.exists()) {
            boolean isDirectoryCreated = directory.mkdirs();
            if (!isDirectoryCreated) {
                System.err.println("Failed to create directory: " + downloadLocation);
                logger.log(Level.SEVERE, "Failed to create directory: " + downloadLocation);
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String serverIP = prop.getProperty("serverIP");
        int serverPort = Integer.parseInt(prop.getProperty("serverPort"));
        int maxDownloadAttempts = Integer.parseInt(prop.getProperty("maxDownloadAttempts"));
        String filesize = "";
        int numExperiments = Integer.parseInt(prop.getProperty("numExperiments"));
        ;
        // Check if filesize to be downloaded given in the argument
        if (args.length >= 1) {
            filesize = args[0];
        }
        Client_FS client = new Client_FS(serverIP, serverPort, maxDownloadAttempts, 10, filesize, numExperiments, 16);
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
            if (fileList.size() > 1) {  //multiple file download
                System.out.println("1: Serial Download\n2: Parallel Download");
                int a = sc.nextInt();
                // For parallel download, set the second argument to true
                logger.info("downloadFiles request");
                long startTime = System.nanoTime();
                client.downloadMechanism(fileList, a == 2);
                long endTime = System.nanoTime();
                double durationInMicroseconds = (endTime - startTime) / 1_000.0; // Convert ns to µs
                System.out.println("\nTotal download duration: " + String.format("%.2f", durationInMicroseconds) + "µs");
            } else {    // single file download
                long startTime = System.nanoTime();
                client.downloadMechanism(fileList, false);
                long endTime = System.nanoTime();
                double durationInMicroseconds = (endTime - startTime) / 1_000.0; // Convert ns to µs
                System.out.println("\nTotal download duration: " + String.format("%.2f", durationInMicroseconds) + "µs");
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

