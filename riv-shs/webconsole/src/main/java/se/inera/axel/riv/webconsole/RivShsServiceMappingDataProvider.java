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

import java.util.Iterator;
import java.util.List;

import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import se.inera.axel.riv.RivShsServiceMapping;
import se.inera.axel.riv.RivShsServiceMappingRepository;

final class RivShsServiceMappingDataProvider implements IDataProvider<RivShsServiceMapping> {

	RivShsServiceMappingRepository mappingRepository;
	List<RivShsServiceMapping> mappings = null;

	public RivShsServiceMappingDataProvider(RivShsServiceMappingRepository mappingRepository) {
		super();
		this.mappingRepository = mappingRepository;
	}

	@Override
	public void detach() {
		mappings = null;
	}

	@Override
	public int size() {
		return (int) mappingRepository.count();
	}

	@Override
	public IModel<RivShsServiceMapping> model(RivShsServiceMapping mapping) {
		return new Model<RivShsServiceMapping>(mapping);
	}

	@Override
	public Iterator<? extends RivShsServiceMapping> iterator(int fromIndex, int count) {
		if (mappings == null) {
			int page = fromIndex % count;
			Page<RivShsServiceMapping> result = mappingRepository.findAll(new PageRequest(page,
					count));
			mappings = result.getContent();
		}
		return mappings.iterator();
	}

	private static final long serialVersionUID = 1L;
}