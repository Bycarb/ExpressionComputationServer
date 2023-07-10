package requests;

import server.ComputationServer;

import static requests.StatRequest.*;

public class RequestBuilder {
    private final String request;
    private final ComputationServer server;

    public RequestBuilder(String request, ComputationServer server) {
        this.request = request;
        this.server = server;
    }

    public Request getRequest() {
        return switch (request) {
            case "BYE" -> new QuitRequest();
            case NUMBER_OF_REQUESTS, AVG_TIME, MAX_TIME -> new StatRequest(request, server);
            default -> new ComputationRequest(request);
        };
    }


}
