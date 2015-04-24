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
package se.inera.axel.riv.webconsole;

import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.ops4j.pax.wicket.api.PaxWicketMountPoint;
import se.inera.axel.riv.RivShsServiceMapping;
import se.inera.axel.riv.RivShsServiceMappingRepository;
import se.inera.axel.riv.webconsole.base.BasePage;
import se.inera.axel.riv.webconsole.base.ControlGroupContainer;
import se.inera.axel.shs.broker.product.ProductService;
import se.inera.axel.shs.xml.product.ShsProduct;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@PaxWicketMountPoint(mountPoint = "/riv-shs/mappings/edit")
public class RivShsServiceMappingEditPage extends BasePage {
	private static final long serialVersionUID = 1L;

    @Inject
	@Named("rivShsServiceMappingRepository")
    @SpringBean(name = "rivShsServiceMappingRepository")
	RivShsServiceMappingRepository mappingRepository;

    @Inject
	@Named("productService")
    @SpringBean(name = "productService")
	ProductService productService;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public RivShsServiceMappingEditPage(PageParameters params) {
		super(params);

		add(new FeedbackPanel("feedback"));

		RivShsServiceMapping mapping = null;
		String id = params.get("id").toString();
		if (id != null) {
			mapping = mappingRepository.findOne(id);
		}
		if (mapping == null) {
			mapping = new RivShsServiceMapping();
		}
		IModel<RivShsServiceMapping> mappingModel = new CompoundPropertyModel<RivShsServiceMapping>(
				mapping);
		Form<RivShsServiceMapping> form = new Form<RivShsServiceMapping>("form", mappingModel) {
			@Override
			protected void onSubmit() {
				super.onSubmit();
				RivShsServiceMapping rivShsMapping = getModelObject();
				
				mappingRepository.save(rivShsMapping);
				setResponsePage(RivShsServiceMappingsPage.class);
			}

			private static final long serialVersionUID = 1L;
		};
		form.add(new ControlGroupContainer(new TextField<String>("rivServiceNamespace").setRequired(true)));

		List<ShsProduct> products = productService.findAll();
		Collections.sort(products, ShsProductComparator.getComparator());
		IChoiceRenderer<String> productRenderer = new ShsProductChoiceRenderer(products);
		form.add(new ControlGroupContainer(new DropDownChoice("shsProductId", productsAsIdList(products), productRenderer)
				.setRequired(true)));

		form.add(new ControlGroupContainer(new TextField<String>("rivServiceEndpoint")));
        form.add(new ControlGroupContainer(new CheckBox("useAsynchronousShs")));
        form.add(new ControlGroupContainer(new TextArea<String>("asynchronousResponseSoapBody")));
		form.add(new SubmitLink("submit"));
		add(form);
	}

	private List<String> productsAsIdList(List<ShsProduct> products) {
		List<String> idList = new ArrayList<String>(products.size());
		
		for (ShsProduct product : products) {
			idList.add(product.getUuid());
		}
		
		return idList;
	}

}
