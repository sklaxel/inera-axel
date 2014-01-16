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
package se.inera.axel.shs.broker.webconsole.agreement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;

import org.apache.wicket.spring.injection.annot.SpringBean;
import se.inera.axel.shs.broker.webconsole.common.Util;
import se.inera.axel.shs.broker.agreement.AgreementAdminService;
import se.inera.axel.shs.xml.agreement.Customer;
import se.inera.axel.shs.xml.agreement.Principal;
import se.inera.axel.shs.xml.agreement.Product;
import se.inera.axel.shs.xml.agreement.ShsAgreement;
import se.inera.axel.webconsole.InjectorHelper;

import javax.inject.Inject;
import javax.inject.Named;

public class AgreementAdminServiceDataProvider implements IDataProvider<ShsAgreement> {

	private static final long serialVersionUID = 1L;

    @Inject
    @Named("agreementService")
    @SpringBean(name = "agreementAdminService")
	private AgreementAdminService agreementAdminService;

	private List<ShsAgreement> agreements;
	private String query;

	public AgreementAdminServiceDataProvider(String query) {
		super();
        InjectorHelper.inject(this, getClass().getClassLoader());
		this.query = query;
	}

	@Override
	public void detach() {
		agreements = null;
	}

	@Override
	public Iterator<ShsAgreement> iterator(long first, long count) {
		if (agreements == null) {
			agreements = applyQuery(agreementAdminService.findAll());
			sortByProductId();
		}
		return agreements.subList((int) first, (int) (first + count)).iterator();
	}

	@Override
	public long size() {
		if (agreements == null) {
			agreements = applyQuery(agreementAdminService.findAll());
			sortByProductId();
		}
		return agreements.size();
	}

	@Override
	public IModel<ShsAgreement> model(ShsAgreement agreement) {
		return new CompoundPropertyModel<>(agreement);
	}

	protected List<ShsAgreement> applyQuery(List<ShsAgreement> originalList) {
		List<ShsAgreement> filteredList = new ArrayList<>();
		if (StringUtils.isNotBlank(this.query)) {
			for (ShsAgreement p : originalList) {
				if (queryMatchesProduct(query, p)) {
					filteredList.add(p);
				}
			}
		} else {
			filteredList = originalList;
		}
		return filteredList;
	}

	protected boolean queryMatchesProduct(String query, ShsAgreement a) {
		Principal pr = a.getShs().getPrincipal();
		Customer c = a.getShs().getCustomer();
		Product p = a.getShs().getProduct().get(0);
		List<String> args = new ArrayList<>();
		args.add(a.getContract());
		args.add(a.getTransferType());
		args.add(a.getUuid());
		if (pr != null) {
			args.add(pr.getCommonName());
			args.add(pr.getLabeledURI());
			args.add(pr.getValue());
		}
		if (c != null) {
			args.add(c.getLabeledURI());
			args.add(c.getValue());
		}
		if (p != null) {
			args.add(p.getCommonName());
			args.add(p.getLabeledURI());
			args.add(p.getValue());
		}
		if (a.getShs().getDirection() != null) {
			args.add(a.getShs().getDirection().getFlow());
		}
		if (a.getGeneral() != null) {
			args.add(a.getGeneral().getDescription());
		}
		return Util.stringsContainsQuery(query, args);
	}

	protected void sortByProductId() {
		Collections.sort(agreements, new Comparator<ShsAgreement>() {
			@Override
			public int compare(ShsAgreement a1, ShsAgreement a2) {
				if (a1.getShs() != null && !a1.getShs().getProduct().isEmpty()
						&& a2.getShs() != null && !a2.getShs().getProduct().isEmpty()) {
					return a1.getShs().getProduct().get(0).getValue()
							.compareTo(a2.getShs().getProduct().get(0).getValue());
				} else {
					return 0;
				}
			}
		});
	}
}
