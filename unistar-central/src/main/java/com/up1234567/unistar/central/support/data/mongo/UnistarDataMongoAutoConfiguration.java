package com.up1234567.unistar.central.support.data.mongo;


import com.up1234567.unistar.central.support.data.IUnistarDao;
import com.up1234567.unistar.central.support.data.UnistarDataProperties;
import com.up1234567.unistar.common.IUnistarConst;
import lombok.CustomLog;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;

@CustomLog(topic = IUnistarConst.DEFAULT_LOGGER_UNISTAR)
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = UnistarDataProperties.TYPE, havingValue = "mongodb", matchIfMissing = true)
@EnableConfigurationProperties(UnistarDataProperties.class)
public class UnistarDataMongoAutoConfiguration {

    @Bean
    public MongoDatabaseFactory mongoDbFactory(UnistarDataProperties unistarDataProperties) {
        return new SimpleMongoClientDatabaseFactory(unistarDataProperties.getUri());
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory databaseFactory, MappingMongoConverter mappingMongoConverter) {
        return new MongoTemplate(databaseFactory, mappingMongoConverter);
    }

    @Bean
    public IUnistarDao unistarDao(MongoTemplate mongoTemplate) {
        log.debug("use mongodb storage");
        MappingMongoConverter mongoTemplateConverter = ((MappingMongoConverter) mongoTemplate.getConverter());
        mongoTemplateConverter.setTypeMapper(new DefaultMongoTypeMapper(null));
        return new UnistarMongoDao(mongoTemplate);
    }

}
