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
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.*;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.extract.UUIDTempNaming;
import de.flapdoodle.embed.process.runtime.Network;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import se.inera.axel.shs.broker.messagestore.MessageLogAdminService;
import se.inera.axel.shs.broker.messagestore.MessageLogService;
import se.inera.axel.shs.broker.messagestore.MessageStoreService;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
@Configuration
public class MongoDBTestContextConfig implements DisposableBean {

    public @Bean(destroyMethod = "stop") MongodExecutable mongodExecutable() throws Exception {
        IMongodConfig mongodConfig = new MongodConfigBuilder()
                .version(Version.Main.V2_2)
                .net(new Net(Network.getFreeServerPort(), Network.localhostIsIPv6()))
                .build();

        IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder()
                .defaults(Command.MongoD)
                .artifactStore(new ArtifactStoreBuilder()
                        .defaults(Command.MongoD)
                        .executableNaming(new UUIDTempNaming())
                )
                .build();

        MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);

        return runtime.prepare(mongodConfig);
    }

    public @Bean(destroyMethod = "stop") MongodProcess mongodProcess() throws Exception {

        MongodProcess mongod = mongodExecutable().start();

        return  mongod;
    }

    public @Bean(destroyMethod = "close") Mongo mongo() throws Exception {
        MongodProcess mongodProcess = mongodProcess();

        return new Mongo(new ServerAddress(mongodProcess.getConfig().net().getServerAddress(), mongodProcess.getConfig().net().getPort()));
    }

    public @Bean MongoDbFactory mongoDbFactorySafe() throws Exception {
        SimpleMongoDbFactory simpleMongoDbFactory = new SimpleMongoDbFactory(mongo(), "axel-test");
        simpleMongoDbFactory.setWriteConcern(WriteConcern.SAFE);
        return simpleMongoDbFactory;
    }

    public @Bean MongoDbFactory mongoDbFactory() throws Exception {
        return new SimpleMongoDbFactory(mongo(), "axel-test");
    }

    public @Bean MessageStoreService messageStoreService() throws Exception {
        return new MongoMessageStoreService(mongoDbFactorySafe());
    }

    public @Bean MessageLogService messageLogService() throws Exception {
        return new MongoMessageLogService();
    }

    public @Bean
    MessageLogAdminService messageLogAdminService() throws Exception {
        return new MongoMessageLogAdminService();
    }

    public @Bean MongoOperations mongoOperations() throws Exception {
        return new MongoTemplate(mongoDbFactory());
    }

    public @Bean MongoRepositoryFactory mongoRepositoryFactory() throws Exception {
        return new MongoRepositoryFactory(mongoOperations());
    }

    public @Bean MessageLogRepository messageLogRepository() throws Exception {
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
