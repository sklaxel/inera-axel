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
package se.inera.axel.shs.messagestore.impl;

import com.mongodb.Mongo;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.RuntimeConfig;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.extract.UUIDTempNaming;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import se.inera.axel.shs.messagestore.MessageStoreService;

import java.io.IOException;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
@Configuration
public class MongoDBTestContextConfig implements DisposableBean {

    public @Bean MongodProcess mongodProcess() throws IOException {
        MongodProcess mongodProcess;

        RuntimeConfig config = new RuntimeConfig();
        config.setExecutableNaming(new UUIDTempNaming());

        MongodStarter starter = MongodStarter.getInstance(config);

        MongodExecutable mongoExecutable = starter.prepare(new MongodConfig(Version.V2_2_1));
        mongodProcess = mongoExecutable.start();

        return  mongodProcess;
    }

    public @Bean Mongo mongo() throws IOException {
        MongodProcess mongodProcess = mongodProcess();

        return new Mongo(mongodProcess.getConfig().getBindIp(), mongodProcess.getConfig().getPort());
    }

    public @Bean MongoDbFactory mongoDbFactory() throws Exception {
        return new SimpleMongoDbFactory(mongo(), "axel-test");
    }

    public @Bean MessageStoreService messageStoreService() throws Exception {
        return new MongoMessageStoreService(mongoDbFactory());
    }

    public @Bean MongoOperations mongoOperations() throws Exception {
        return new MongoTemplate(mongoDbFactory());
    }

    public @Bean MongoRepositoryFactory mongoRepositoryFactory() throws Exception {
        return new MongoRepositoryFactory(mongoOperations());
    }

    public @Bean
    MessageLogRepository messageLogRepository() throws Exception {
        return mongoRepositoryFactory().getRepository(MessageLogRepository.class);
    }

    @Override
    public void destroy() throws Exception {
        Mongo mongo = mongo();

        if (mongo != null)
            mongo.close();

        MongodProcess mongodProcess = mongodProcess();

        if (mongodProcess != null)
            mongodProcess.stop();
    }
}
