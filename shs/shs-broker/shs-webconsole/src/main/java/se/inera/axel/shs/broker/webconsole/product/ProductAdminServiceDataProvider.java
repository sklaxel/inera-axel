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
package se.inera.axel.shs.broker.webconsole.product;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import se.inera.axel.shs.broker.product.ProductAdminService;
import se.inera.axel.shs.broker.webconsole.common.Util;
import se.inera.axel.shs.xml.product.Principal;
import se.inera.axel.shs.xml.product.ShsProduct;
import se.inera.axel.webconsole.InjectorHelper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class ProductAdminServiceDataProvider implements IDataProvider<ShsProduct> {

	private static final long serialVersionUID = 1L;

    @Inject
    @Named("productService")
    @SpringBean(name = "productAdminService")
	private ProductAdminService productAdminService;
	private List<ShsProduct> products;
	private String query;

	public ProductAdminServiceDataProvider(String query) {
		super();
        InjectorHelper.inject(this, getClass().getClassLoader());
		this.query = query;
	}

	@Override
	public void detach() {
		products = null;
	}

	@Override
	public Iterator<? extends ShsProduct> iterator(long first, long count) {
		if (products == null) {
			products = applyQuery(productAdminService.findAll());
			sortByProductCommonName();
		}
		return products.subList((int) first, (int) (first + count)).iterator();
	}

	protected List<ShsProduct> applyQuery(List<ShsProduct> originalList) {
		List<ShsProduct> filteredList = new ArrayList<>();
		if (StringUtils.isNotBlank(this.query)) {
			for (ShsProduct p : originalList) {
				if (queryMatchesProduct(query, p)) {
					filteredList.add(p);
				}
			}
		} else {
			filteredList = originalList;
		}
		return filteredList;
	}

	protected boolean queryMatchesProduct(String query, ShsProduct p) {
		List<String> args = new ArrayList<>();
		args.add(p.getCommonName());
		args.add(p.getDescription());
		args.add(p.getLabeledURI());
		args.add(p.getUuid());
		Principal pr = p.getPrincipal();
		if (pr != null) {
			args.add(pr.getCommonName());
			args.add(pr.getLabeledURI());
			args.add(pr.getValue());
		}
		return Util.stringsContainsQuery(query, args);
	}

	@Override
	public long size() {
		if (products == null) {
			products = applyQuery(productAdminService.findAll());
			sortByProductCommonName();
		}
		return products.size();
	}

	@Override
	public IModel<ShsProduct> model(ShsProduct product) {
		return new CompoundPropertyModel<>(product);
	}

	protected void sortByProductCommonName() {
		Collections.sort(products, new Comparator<ShsProduct>() {
			@Override
			public int compare(ShsProduct p1, ShsProduct p2) {
				if (p1 != null && p2 != null && p1.getCommonName() != null
						&& p2.getCommonName() != null) {
					return p1.getCommonName().compareTo(p2.getCommonName());
				} else {
					return 0;
				}
			}
		});
	}

}
