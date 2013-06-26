package se.inera.axel.shs.cmdline;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.component.http.HttpOperationFailedException;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class ShsCmdlineExceptionHandler {
    public static void handleException(CamelExecutionException e) throws Throwable {
        Throwable cause = e.getCause();
        if (cause instanceof HttpOperationFailedException) {
            HttpOperationFailedException httpException = (HttpOperationFailedException)cause;

            ShsHttpException shsHttpException =
                    new ShsHttpException(httpException.getResponseBody(),
                            httpException.getStatusText(),
                            httpException.getResponseHeaders(),
                            httpException.getStatusCode());

            throw shsHttpException;
        }

        throw e.getCause();
    }
}
