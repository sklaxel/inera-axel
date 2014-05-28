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
package se.inera.axel.rivssek.webconsole;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.ops4j.pax.wicket.api.PaxWicketMountPoint;
import se.inera.axel.riv2ssek.RivSsekServiceMappingRepository;
import se.inera.axel.riv2ssek.SsekServiceInfo;
import se.inera.axel.rivssek.webconsole.base.BasePage;
import se.inera.axel.rivssek.webconsole.base.ControlGroupContainer;

import javax.inject.Inject;
import javax.inject.Named;

@PaxWicketMountPoint(mountPoint = "/riv-ssek/mappings/edit")
public class SsekServiceInfoEditPage extends BasePage {
	private static final long serialVersionUID = 1L;

    @Inject
	@Named("rivSsekServiceMappingRepository")
    @SpringBean(name = "rivSsekServiceMappingRepository")
    RivSsekServiceMappingRepository mappingRepository;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SsekServiceInfoEditPage(PageParameters params) {
		super(params);

		add(new FeedbackPanel("feedback"));

		SsekServiceInfo mapping = null;
		String id = params.get("id").toString();
		if (id != null) {
			mapping = mappingRepository.findOne(id);
		}
		if (mapping == null) {
			mapping = new SsekServiceInfo.Builder().build();
		}
		IModel<SsekServiceInfo> mappingModel = new CompoundPropertyModel<>(
				mapping);
		Form<SsekServiceInfo> form = new Form<SsekServiceInfo>("form", mappingModel) {
			@Override
			protected void onSubmit() {
				super.onSubmit();
				SsekServiceInfo rivShsMapping = getModelObject();
				
				mappingRepository.save(rivShsMapping);
				setResponsePage(SsekServiceInfoPage.class);
			}

			private static final long serialVersionUID = 1L;
		};

		form.add(new ControlGroupContainer(new TextField<String>("rivServiceNamespace").setRequired(true)));
		form.add(new ControlGroupContainer(new TextField<String>("address").setRequired(true)));
		form.add(new ControlGroupContainer(new TextField<String>("receiver")));
		form.add(new SubmitLink("submit"));
		add(form);
	}
}
