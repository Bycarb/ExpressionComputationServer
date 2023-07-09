package Server;

import Requests.QuitRequest;
import Requests.Request;
import Requests.RequestBuilder;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.Semaphore;

public class ClientHandler extends Thread {
    private final Socket socket;
    private final Semaphore coresLimitSemaphore;
    private final ComputationServer server;

    public ClientHandler(Socket socket, Semaphore coresLimitSemaphore, ComputationServer server) {
        this.socket = socket;
        this.coresLimitSemaphore = coresLimitSemaphore;
        this.server = server;
    }

    public void run() {
        log("Thread created. Connecting with " + socket.getInetAddress() + ":" + socket.getPort());
        boolean semaphoreAcquired = false;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            while (true) {
                String line = br.readLine();
                long starTime = System.currentTimeMillis();

                coresLimitSemaphore.acquire();
                semaphoreAcquired = true;

                String response;
                try {
                    if (line == null || line.isEmpty() || line.isBlank()) {
                        throw new IllegalArgumentException("Provided empty line.");
                    }
                    Request request = (new RequestBuilder(line, server)).getRequest();
                    if (request instanceof QuitRequest) {
                        break;
                    }
                    String result = request.processRequest();
                    double elapsedTimeSeconds = ((double) (System.currentTimeMillis() - starTime)) / 1000;
                    String elapsedTimeString = String.format("%.3f;", elapsedTimeSeconds);
                    response = "OK;"
                            + elapsedTimeString
                            + result;
                    server.logNewServedRequest(elapsedTimeSeconds);
                } catch (ArithmeticException | IllegalArgumentException e) {
                    response = "ERR; " + e.getMessage();
                    log(response);
                }
                bw.write(response + System.lineSeparator());
                bw.flush();

                coresLimitSemaphore.release();
                semaphoreAcquired = false;
            }
        } catch (IOException | InterruptedException | OutOfMemoryError e) {
            log("Error: " + e.getMessage());
            e.printStackTrace();
            if (semaphoreAcquired) {
                coresLimitSemaphore.release();
            }
        } finally {
            try {
                socket.close();
                log("Thread closed. Disconnected from " + socket.getInetAddress() + ":" + socket.getPort());
            } catch (IOException e) {
                log(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void log(String string) {
        System.out.println("Thread " + getId() + ":  " +
                string);
    }
}