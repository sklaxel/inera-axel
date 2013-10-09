package se.inera.axel.shs.camel;

import org.apache.camel.ExchangeException;
import se.inera.axel.shs.exception.ShsException;

/**
 * Use this class in an onException(ShsException.class) to set the nested
 * ShsException as the body.
 *
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class SetShsExceptionAsBody {
    public ShsException echo(@ExchangeException ShsException e) {
        return e;
    }
}
