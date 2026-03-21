package com.transcol.busafe.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.MongoDatabaseFactory;

@Configuration
public class MongoConfig {

    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory databaseFactory, MappingMongoConverter converter) {
        // Essa linha remove o campo _class de todos os novos registros
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        return new MongoTemplate(databaseFactory, converter);
    }
}