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
package se.inera.axel.shs.broker.webconsole.directory;

import org.apache.wicket.Session;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.ops4j.pax.wicket.api.PaxWicketBean;
import org.ops4j.pax.wicket.api.PaxWicketMountPoint;
import se.inera.axel.shs.broker.directory.DirectoryAdminServiceRegistry;
import se.inera.axel.shs.broker.directory.Organization;
import se.inera.axel.shs.broker.webconsole.WicketApplication;
import se.inera.axel.shs.broker.webconsole.base.BasePage;
import se.inera.axel.shs.broker.webconsole.common.DirectoryAdminServiceUtil;

/**
 * List LDAP Directory
 */
@PaxWicketMountPoint(mountPoint = "/shs/directory/actor/view")
public class ActorPage extends BasePage {
	private static final long serialVersionUID = 1L;

    @PaxWicketBean(name = "directoryAdminServiceRegistry")
    @SpringBean(name = "directoryAdminServiceRegistry")
    protected DirectoryAdminServiceRegistry directoryAdminServiceRegistry;

	public ActorPage(final PageParameters parameters) {
		super(parameters);

        String organizationNumber = parameters.get("orgNumber").toString();
        OrganizationModel organizationModel = new OrganizationModel(organizationNumber, directoryAdminServiceRegistry);
		add(new ActorViewPanel("organization", organizationModel));
		add(new ProductListPanel("productlist", organizationModel));
		add(new AddressListPanel("addresslist", organizationModel));
		add(new AgreementListPanel("agreementlist", organizationModel));
	}

    public static class OrganizationModel extends LoadableDetachableModel<Organization> {
        private DirectoryAdminServiceRegistry directoryAdminServiceRegistry;
        private String organizationNumber;

        public OrganizationModel(String organizationNumber, DirectoryAdminServiceRegistry directoryAdminServiceRegistry) {
            this.directoryAdminServiceRegistry = directoryAdminServiceRegistry;
            this.organizationNumber = organizationNumber;
        }

        @Override
        protected Organization load() {
            return DirectoryAdminServiceUtil.getSelectedDirectoryAdminService(directoryAdminServiceRegistry)
                    .getOrganization(organizationNumber);
        }
    }
}
