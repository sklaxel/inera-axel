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
package se.inera.axel.shs.broker.agreement.mongo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dozer.DozerConverter;
import org.dozer.Mapper;
import org.dozer.MapperAware;
import org.dozer.MappingException;
import se.inera.axel.shs.broker.agreement.mongo.model.*;

public class ObjectListConverter extends DozerConverter<List<Object>, List<Object>> implements MapperAware {
	Mapper mapper;
	
	private static final Map<Class<?>, Class<?>> classMappings = createClassMappings();
	
	private static Map<Class<?>, Class<?>> createClassMappings() {
		Map<Class<?>, Class<?>> mappings = new HashMap<Class<?>, Class<?>>();
		
		// Billing.perExchangeOrPerVolumeOrPerPeriod
		mappings.put(PerExchange.class, se.inera.axel.shs.xml.agreement.PerExchange.class);
		mappings.put(se.inera.axel.shs.xml.agreement.PerExchange.class, PerExchange.class);
		
		mappings.put(PerVolume.class, se.inera.axel.shs.xml.agreement.PerVolume.class);
		mappings.put(se.inera.axel.shs.xml.agreement.PerVolume.class, PerVolume.class);
		
		mappings.put(PerPeriod.class, se.inera.axel.shs.xml.agreement.PerPeriod.class);
		mappings.put(se.inera.axel.shs.xml.agreement.PerPeriod.class, PerPeriod.class);
		
		// Open.starttimeOrStoptime
		mappings.put(Starttime.class, se.inera.axel.shs.xml.agreement.Starttime.class);
		mappings.put(se.inera.axel.shs.xml.agreement.Starttime.class, Starttime.class);
		
		mappings.put(Stoptime.class, se.inera.axel.shs.xml.agreement.Stoptime.class);
		mappings.put(se.inera.axel.shs.xml.agreement.Stoptime.class, Stoptime.class);
		
		return mappings;
	}
	
	@SuppressWarnings("unchecked")
	public ObjectListConverter() {
		super((Class<List<Object>>)(Class<?>)List.class, (Class<List<Object>>)(Class<?>)List.class);
	}

	@Override
	public List<Object> convertTo(List<Object> source, List<Object> destination) {
		return convert(source, destination);
	}
	
	private List<Object> convert(List<Object> source, List<Object> destination) {
		if (destination == null) {
			destination = new ArrayList<Object>();
		}
		
		for (Object srcElement : source) {
			Class<?> destClass = classMappings.get(srcElement.getClass());
			if (destClass == null) {
				throw new MappingException("Could not find destination class for class " + srcElement.getClass().getCanonicalName());
			}
			
			Object destElement = mapper.map(srcElement, destClass);
			
			destination.add(destElement);
		}
				
		return destination;
	}

	@Override
	public List<Object> convertFrom(List<Object> source,
			List<Object> destination) {
		
		return convert(source, destination);
	}

	@Override
	public void setMapper(Mapper mapper) {
		this.mapper = mapper;
	}

}
