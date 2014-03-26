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
package se.inera.axel.shs.broker.messagestore.internal;

import com.mongodb.Mongo;
import com.mongodb.WriteConcern;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import se.inera.axel.shs.broker.messagestore.MessageLogAdminService;
import se.inera.axel.shs.broker.messagestore.MessageLogService;

import java.io.IOException;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
@Configuration
@Import(MockConfig.class)
public class MongoDBTestContextConfig {

    @Bean(destroyMethod = "shutdown")
    public MongodForTestsFactory mongodForTestsFactory() throws IOException {
        return MongodForTestsFactory.with(Version.Main.V2_4);
    }

    @Bean
    public Mongo mongo() throws Exception {
        return mongodForTestsFactory().newMongo();
    }

    @Bean
    public MongoDbFactory mongoDbFactorySafe() throws Exception {
        SimpleMongoDbFactory simpleMongoDbFactory = new SimpleMongoDbFactory(mongo(), "axel-test");
        simpleMongoDbFactory.setWriteConcern(WriteConcern.SAFE);
        return simpleMongoDbFactory;
    }

    @Bean
    public MongoDbFactory mongoDbFactory() throws Exception {
        SimpleMongoDbFactory simpleMongoDbFactory = new SimpleMongoDbFactory(mongo(), "axel-test");
        simpleMongoDbFactory.setWriteConcern(WriteConcern.SAFE);
        return simpleMongoDbFactory;
    }

    @Bean
    public MessageLogService messageLogService() throws Exception {
        return new MongoMessageLogService();
    }

    @Bean
    public MessageLogAdminService messageLogAdminService() throws Exception {
        return new MongoMessageLogAdminService();
    }

    @Bean
    public MongoOperations mongoOperations() throws Exception {
        return new MongoTemplate(mongoDbFactory());
    }

    @Bean
    public MongoRepositoryFactory mongoRepositoryFactory() throws Exception {
        return new MongoRepositoryFactory(mongoOperations());
    }

    @Bean
    public MessageLogRepository messageLogRepository() throws Exception {
        return mongoRepositoryFactory().getRepository(MessageLogRepository.class);
    }
}
