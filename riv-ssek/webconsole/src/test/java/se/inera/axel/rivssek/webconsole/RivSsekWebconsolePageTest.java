package se.inera.axel.rivssek.webconsole;

import se.inera.axel.webconsole.NavigationProvider;
import se.inera.axel.webconsole.WebconsolePageTest;

import java.util.List;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public abstract class RivSsekWebconsolePageTest extends WebconsolePageTest {
    @Override
    protected void registerNavigationProviders(List<NavigationProvider> navigationProviderList) {
        navigationProviderList.add(new SsekRivAdminNavigationProvider());
    }
}
