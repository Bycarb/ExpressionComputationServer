package Requests;

import Server.ComputationServer;

public class StatRequest implements Request {
    public static final String NUMBER_OF_REQUESTS = "STAT_REQS";
    public static final String AVG_TIME = "STAT_AVG_TIME";
    public static final String MAX_TIME = "STAT_MAX_TIME";
    private final String request;
    private final ComputationServer server;

    public StatRequest(String request, ComputationServer server) {
        this.request = request;
        this.server = server;
    }

    @Override
    public String call() {
        if (request.equals(NUMBER_OF_REQUESTS)) {
            return String.format("%d", server.getRequestsServed());
        }
        if (request.equals(AVG_TIME)) {
            return String.format("%.3f", server.getAVGRequestTime());
        }
        if (request.equals(MAX_TIME)) {
            return String.format("%.3f", server.getMAXRequestTime());
        }
        //should never reach here. If it does, there's a problem with the code
        throw new UnsupportedOperationException("The requested operation is not implemented.");
    }
}
