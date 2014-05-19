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

import se.inera.axel.test.wicket.AbstractPageTest;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public abstract class WebconsolePageTest extends AbstractPageTest {
    abstract protected void registerNavigationProviders(List<NavigationProvider> navigationProviderList);

    /**
     * Override this method in sub classes to perform sub class specific initialization.
     */
    protected void beforeMethodSetup() {
        ArrayList<NavigationProvider> navigationProviders = new ArrayList<>();
        registerNavigationProviders(navigationProviders);
        injector.registerBean("navigationProviders", navigationProviders);

        NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.setOrganizationNumber("0000000000");
        nodeInfo.setNodeId("axel");
        nodeInfo.setGroupId("se.inera.axel");
        nodeInfo.setArtifactId("axel-webconsole");
        injector.registerBean("nodeInfo", nodeInfo);
    }
}
