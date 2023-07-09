package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ComputationServer {
    private final int port;
    private final List<Double> requestsTimeRecord;
    private final ExecutorService executorService;

    public ComputationServer(int port) {
        this.port = port;
        requestsTimeRecord = new LinkedList<>();
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port "
                    + port + ". "
                    + new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime()));
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    ClientHandler clientHandler = new ClientHandler(socket, executorService, this);
                    clientHandler.start();
                } catch (IOException e) {
                    System.out.println("ERROR: error establishing new connection.\n" + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("FATAL ERROR: impossible to create server socket." +
                    e.getMessage() +
                    Arrays.toString(e.getStackTrace()));
            System.exit(1);
        }
    }

    public synchronized int getRequestsServed() {
        return requestsTimeRecord.size();
    }

    public synchronized double getAVGRequestTime() {
        return requestsTimeRecord.stream()
                .mapToDouble(Double::doubleValue)
                .average().orElse(0);
    }

    public synchronized double getMAXRequestTime() {
        return requestsTimeRecord.stream()
                .mapToDouble(Double::doubleValue)
                .max().orElse(0);
    }

    public synchronized void logNewServedRequest(double processingTime) {
        requestsTimeRecord.add(processingTime);
    }
}