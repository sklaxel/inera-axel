package se.inera.axel.test.fitnesse.fixtures;

/**
 * Use this fetcher when the item to fetch is not expected
 * to be available immediately.
 *
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class AsynchFetcher {
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
    public static <T> T fetch(Fetcher<T> fetcher) throws Exception {
        T result = null;

        long startTime = System.currentTimeMillis();

        while ((result = fetcher.fetch()) == null) {
            if (System.currentTimeMillis() - startTime > 3000) {
                break;
            }
            Thread.sleep(100);
        }

        return result;
    }


    public interface Fetcher<T> {
        public T fetch() throws Exception;
    }
}
