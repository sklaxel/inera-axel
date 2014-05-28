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


import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import se.inera.axel.riv2ssek.RivSsekServiceMapping;
import se.inera.axel.riv2ssek.RivSsekServiceMappingRepository;
import se.inera.axel.webconsole.InjectorHelper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Iterator;
import java.util.List;

final class RivSsekServiceMappingDataProvider implements IDataProvider<RivSsekServiceMapping> {

    @Inject
    @Named("rivSsekServiceMappingRepository")
    @SpringBean(name = "rivSsekServiceMappingRepository")
    private RivSsekServiceMappingRepository mappingRepository;
    
    List<RivSsekServiceMapping> mappings = null;

    public RivSsekServiceMappingDataProvider() {
        super();

        InjectorHelper.inject(this, getClass().getClassLoader());
    }

    @Override
    public void detach() {
        mappings = null;
    }

    @Override
    public long size() {
        return (int) mappingRepository.count();
    }

    @Override
    public IModel<RivSsekServiceMapping> model(RivSsekServiceMapping mapping) {
        return new Model<>(mapping);
    }

    @Override
    public Iterator<? extends RivSsekServiceMapping> iterator(long fromIndex, long count) {
        if (mappings == null) {
            int page = (int) (fromIndex % count);
            Page<RivSsekServiceMapping> result = mappingRepository.findAll(new PageRequest(page,
                    (int) count));
            mappings = result.getContent();
        }
        return mappings.iterator();
    }

    private static final long serialVersionUID = 1L;
}