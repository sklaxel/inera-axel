package se.inera.axel.shs.broker.webconsole;

import se.inera.axel.webconsole.NavigationProvider;
import se.inera.axel.webconsole.WebconsolePageTest;

import java.util.List;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public abstract class ShsWebconsolePageTest extends WebconsolePageTest {
    @Override
    protected void registerNavigationProviders(List<NavigationProvider> navigationProviderList) {
        navigationProviderList.add(new ShsAdminNavigationProvider());
    }
}
