package Requests;

import java.util.concurrent.Callable;

public interface Request extends Callable<String> {
    String call();
}
