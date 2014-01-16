/**
 * Copyright (C) 2013 Inera AB (http://www.inera.se)
 *
 * This file is part of Inera Axel (http://code.google.com/p/inera-axel).
 *
 * Inera Axel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Inera Axel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package se.inera.axel.webconsole;

import org.apache.wicket.injection.Injector;
import org.ops4j.pax.wicket.api.InjectorHolder;
import org.ops4j.pax.wicket.api.PaxWicketInjector;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class InjectorHelper {
    private InjectorHelper() {

    }

    public static void inject(Object object, ClassLoader classLoader) {
        if (isPaxWicketAvailable(classLoader)) {
            PaxWicketInjector paxWicketInjector = InjectorHolder.getInjector();

            paxWicketInjector.inject(object, object.getClass());
            return;
        }

        Injector injector = Injector.get();

        if (injector != null) {
            injector.inject(object);
        }
    }

    private static boolean isPaxWicketAvailable(ClassLoader classLoader) {
        try {
            Class.forName("org.ops4j.pax.wicket.api.PaxWicketInjector", false, classLoader);
            return true;
        } catch (Throwable T) {
            return false;
        }
    }
}
