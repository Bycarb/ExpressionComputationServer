import server.ComputationServer;

public class Main {
    private static final int DEFAULT_PORT = 10000;
    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number");
                System.exit(1);
            }
        }
        ComputationServer server = new ComputationServer(port);
        server.run();
    }
}