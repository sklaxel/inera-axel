package se.inera.axel.test.fitnesse.fixtures;

/**
 * Use this fetcher when the item to fetch is not expected
 * to be available immediately.
 *
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public final class AsynchFetcher {
    private AsynchFetcher() {

    }

    /**
     * Repeatedly tries to fetch the item with the given fetcher.
     *
     * @param fetcher the fetcher to used to do the fetch
     *
     * @return the fetched value if it could be retrieved, <code>null</code>
     * if the timeout was reached.
     *
     * @throws Exception
     */
    public static <T> T fetch(int maxWaitInSeconds, Fetcher<T> fetcher) throws Throwable {
        T result;

        long startTime = System.currentTimeMillis();

        while ((result = fetcher.fetch()) == null) {
            if (System.currentTimeMillis() - startTime > (maxWaitInSeconds * 1000)) {
                break;
            }
            System.out.println("No result found: sleeping");
            Thread.sleep(200);
        }

        return result;
    }


    public interface Fetcher<T> {
        T fetch() throws Throwable;
    }
}
