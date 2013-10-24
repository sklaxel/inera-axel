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

import java.util.Iterator;
import java.util.List;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;

import org.apache.wicket.spring.injection.annot.SpringBean;
import se.inera.axel.shs.broker.directory.DirectoryAdminServiceRegistry;
import se.inera.axel.shs.broker.directory.Organization;
import se.inera.axel.shs.broker.directory.DirectoryAdminService;
import se.inera.axel.shs.broker.directory.ProductType;
import se.inera.axel.shs.broker.webconsole.common.DirectoryAdminServiceUtil;
import se.inera.axel.webconsole.InjectorHelper;

import javax.inject.Inject;
import javax.inject.Named;

public class ProductTypeDataProvider implements IDataProvider<ProductType> {

	private static final long serialVersionUID = 1L;

    @Inject
    @Named("directoryAdminServiceRegistry")
    @SpringBean(name = "directoryAdminServiceRegistry")
	private DirectoryAdminServiceRegistry directoryAdminServiceRegistry;
	private IModel<Organization> organizationModel;
	List<ProductType> products;

	public ProductTypeDataProvider(IModel<Organization> organizationModel) {
        InjectorHelper.inject(this);
		this.organizationModel = organizationModel;
	}

	@Override
	public void detach() {
		products = null;
	}

	@Override
	public Iterator<ProductType> iterator(long first, long count) {
		if (products == null) {
			products = getDirectoryAdminService().getProductTypes(organizationModel.getObject());
		}
		return products.subList((int) first, (int) (first + count)).iterator();
	}

	@Override
	public long size() {
		if (products == null) {
			products = getDirectoryAdminService().getProductTypes(organizationModel.getObject());
		}
		return products.size();
	}

	@Override
	public IModel<ProductType> model(ProductType product) {
		return new CompoundPropertyModel<>(product);
	}

    private DirectoryAdminService getDirectoryAdminService() {
        return DirectoryAdminServiceUtil.getSelectedDirectoryAdminService(directoryAdminServiceRegistry);
    }
}
