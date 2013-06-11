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

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.ops4j.pax.wicket.api.PaxWicketBean;
import org.ops4j.pax.wicket.api.PaxWicketMountPoint;
import se.inera.axel.riv.RivShsServiceMapping;
import se.inera.axel.riv.RivShsServiceMappingRepository;
import se.inera.axel.riv.webconsole.base.BasePage;

@PaxWicketMountPoint(mountPoint = "/riv-shs/mappings")
public class RivShsServiceMappingsPage extends BasePage {
	private static final long serialVersionUID = 1L;

	@PaxWicketBean(name = "mappingRepository")
	RivShsServiceMappingRepository mappingRepository;

	IDataProvider<RivShsServiceMapping> mappingData;

	public RivShsServiceMappingsPage(final PageParameters parameters) {
		super(parameters);

		mappingData = new RivShsServiceMappingDataProvider(mappingRepository);

		DataView<RivShsServiceMapping> dataView = new DataView<RivShsServiceMapping>("mappings",
				mappingData) {
			private static final long serialVersionUID = 1L;

			@SuppressWarnings("serial")
			protected void populateItem(final Item<RivShsServiceMapping> item) {
				item.setModel(new CompoundPropertyModel<RivShsServiceMapping>(item.getModel()));
				String id = item.getModelObject().getId();
				item.add(labelWithLink("rivServiceNamespace", id));
				item.add(labelWithLink("shsProductId", id));
//				item.add(labelWithLink("rivServiceEndpoint", id));
				item.add(new Link<String>("delete") {
					@Override
					public void onClick() {
						mappingRepository.delete(item.getModelObject());
						setResponsePage(RivShsServiceMappingsPage.class);
					}
				});
			}
		};
		add(dataView);

		dataView.setItemsPerPage(10000);
//		add(new PagingNavigator("navigator", dataView));

		add(new BookmarkablePageLink<RivShsServiceMappingEditPage>("add",
				RivShsServiceMappingEditPage.class, new PageParameters()));

	}

	protected Component labelWithLink(String labelId, String id) {
		PageParameters params = new PageParameters();
		params.add("id", id);
		Link<String> link = new BookmarkablePageLink<String>(labelId + ".link",
				RivShsServiceMappingEditPage.class, params);
		link.add(new Label(labelId));
		return link;
	}
}
