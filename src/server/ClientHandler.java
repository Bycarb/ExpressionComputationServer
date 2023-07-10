package server;

import requests.ComputationRequest;
import requests.QuitRequest;
import requests.Request;
import requests.RequestBuilder;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

public class ClientHandler extends Thread {
    private final Socket socket;
    private final ExecutorService executorService;
    private final ComputationServer server;

    public ClientHandler(Socket socket, ExecutorService executorService, ComputationServer server) {
        this.socket = socket;
        this.executorService = executorService;
        this.server = server;
    }

    public void run() {
        log("Thread created. Connecting with " + socket.getInetAddress() + ":" + socket.getPort());
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            while (true) {
                String line = br.readLine();
                long starTime = System.currentTimeMillis();

                String response;
                try {
                    if (line == null || line.isEmpty() || line.isBlank()) {
                        throw new IllegalArgumentException("Provided empty line.");
                    }
                    Request request = (new RequestBuilder(line, server)).getRequest();

                    String result;
                    if (request instanceof QuitRequest) {
                        break;
                    } else  if (request instanceof ComputationRequest) {
                        result = executorService.submit(request).get();
                    } else{
                        result = request.call();
                    }

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
            }
        } catch (IOException | InterruptedException | OutOfMemoryError | ExecutionException e) {
            log("Error: " + e.getMessage());
            e.printStackTrace();
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