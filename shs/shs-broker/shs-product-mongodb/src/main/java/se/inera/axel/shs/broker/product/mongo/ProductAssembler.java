/**
 * Copyright (C) 2013 Inera AB (http://www.inera.se)
 *
 * This file is part of Inera Axel (http://code.google.com/p/inera-axel).
 *
 * Inera Axel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Inera Axel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package se.inera.axel.shs.broker.product.mongo;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.dozer.DozerBeanMapper;
import org.dozer.loader.api.BeanMappingBuilder;

import se.inera.axel.shs.broker.product.mongo.model.MongoShsProduct;
import se.inera.axel.shs.xml.product.ShsProduct;

public class ProductAssembler {

	private DozerBeanMapper mapper;
	
	@PostConstruct
	public void configureMapper() {
		mapper = new DozerBeanMapper();
		mapper.addMapping(createBeanMappingBuilder());
	}
	
	private BeanMappingBuilder createBeanMappingBuilder() {
		BeanMappingBuilder builder = new BeanMappingBuilder() {
			protected void configure() {
				mapping(ShsProduct.class, MongoShsProduct.class)
				.fields(field("data").accessible(),
						field("data").accessible())
				.fields(field("replyData").accessible(),
						field("replyData").accessible());
			}
		};

		return builder;
	}
	
	public ShsProduct assembleShsProduct(MongoShsProduct src) {
		ShsProduct product = mapper.map(src, ShsProduct.class);
		
		return product; 
	}
	
	public MongoShsProduct assembleMongoShsProduct(ShsProduct src) {
		MongoShsProduct product = mapper.map(src, MongoShsProduct.class);
		
		return product;
	}

	/**
	 * Convert a list of MongoShsProducts to ShsProducts
	 * @param list
	 * @return
	 */
	public List<ShsProduct> assembleShsProductList(Iterable<MongoShsProduct> list) {
		List<ShsProduct> result = new ArrayList<ShsProduct>();
		for (MongoShsProduct product : list) {
			result.add(mapper.map(product, ShsProduct.class));
		}
		return result;
	}

}
