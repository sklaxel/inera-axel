package se.inera.axel.webconsole;

import org.apache.wicket.injection.Injector;
import org.ops4j.pax.wicket.api.InjectorHolder;
import org.ops4j.pax.wicket.api.PaxWicketInjector;
import org.ops4j.pax.wicket.internal.PaxWicketAppFactoryTracker;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class InjectorHelper {
    private InjectorHelper() {

    }

    public static void inject(Object object) {
        PaxWicketInjector paxWicketInjector = InjectorHolder.getInjector();

        if (paxWicketInjector != null) {
            paxWicketInjector.inject(object, object.getClass());
            return;
        }

        Injector injector = Injector.get();

        if (injector != null) {
            injector.inject(object);
        }
    }
}
