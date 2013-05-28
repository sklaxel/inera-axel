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
package se.inera.axel.shs.camel;

import com.mongodb.Mongo;
import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spring.javaconfig.SingleRouteCamelConfiguration;
import org.apache.camel.spring.javaconfig.test.JavaConfigContextLoader;
import org.apache.camel.testng.AbstractCamelTestNGSpringContextTests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactoryBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import se.inera.axel.shs.messagestore.*;
import se.inera.axel.shs.messagestore.impl.MessageLogRepository;
import se.inera.axel.shs.messagestore.impl.MongoMessageLogService;
import se.inera.axel.shs.messagestore.impl.MongoMessageLogEntry;
import se.inera.axel.shs.protocol.ShsMessage;

import java.io.IOException;
import java.util.List;

import static org.testng.Assert.assertNotNull;

@ContextConfiguration(
        locations = {"se.inera.axel.shs.camel.MessageStoreCamelLoggingIT$ContextConfig",
        		"se.inera.axel.shs.camel.MessageStoreCamelLoggingIT$MongoContextConfig"},
        loader = JavaConfigContextLoader.class,
        inheritLocations = true)
public class MessageStoreCamelLoggingIT extends AbstractCamelTestNGSpringContextTests {
	
	Resource testMimeMessage = new ClassPathResource("se/r2m/axel/shs/camel/mimeMessage.txt");
	
	static final MessageLogService messageLog = new MongoMessageLogService();
	
	@Autowired
    MessageLogRepository repository;
	
	@BeforeClass
	public void beforeClass() {
		Assert.assertNotNull(repository);
		ReflectionTestUtils.setField(messageLog, "repository", repository);
		
	}
	
	@EndpointInject(uri = "mock:result")
    protected MockEndpoint resultEndpoint;
	
	@Produce(uri = "direct:start")
    protected ProducerTemplate template;
	
	@Test(enabled = false)
	public void whenRoutingAMessageALogEntryShouldBeCreatedInTheMessageLog() throws CamelExecutionException, IOException, InterruptedException, InvalidPayloadException {
		Assert.assertNotNull(testMimeMessage);
		
		resultEndpoint.expectedMessageCount(1);
		template.sendBody(testMimeMessage.getInputStream());
		
		resultEndpoint.assertIsSatisfied();
		List<Exchange> exchanges = resultEndpoint.getExchanges();
		exchanges.get(0);
		ShsMessageEntry entry = exchanges.get(0).getIn().getMandatoryBody(ShsMessageEntry.class);
		assertNotNull(repository.findOne(entry.getId()));
	}

	@Configuration
    public static class ContextConfig extends SingleRouteCamelConfiguration {
		@Autowired
		MongoOperations mongoOperations;
		
        @Bean
        public RouteBuilder route() {
            return new RouteBuilder() {
                public void configure() {
                    from("direct:start")
                    .unmarshal(new ShsMessageDataFormat())
                    .process(new Processor() {
						
						@Override
						public void process(Exchange exchange) throws Exception {
							Message in = exchange.getIn();
							ShsMessage message = in.getMandatoryBody(ShsMessage.class);
							ShsMessageEntry entry = messageLog.createEntry(message);
							in.setBody(entry);
						}
					})
                    .to("mock:result");
                }
            };
        }
        
        @Bean
        public MessageLogRepository messageStoreRepository() {
        	MongoRepositoryFactoryBean<MessageLogRepository, MongoMessageLogEntry, String> factory = new MongoRepositoryFactoryBean<MessageLogRepository, MongoMessageLogEntry, String>();
        	factory.setMongoOperations(mongoOperations);
        	factory.setRepositoryInterface(MessageLogRepository.class);
        	factory.afterPropertiesSet();
        	
        	return factory.getObject();
        }
    }
	
	@Configuration
	public static class MongoContextConfig extends AbstractMongoConfiguration {

		@Override
		public String getDatabaseName() {
			return "axel-test";
		}

		@Override
		@Bean
		public Mongo mongo() throws Exception {
			return new Mongo("localhost");
		}
		
	}
}
