package mflix.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Configuration
@Service
public class MongoDBConfiguration {

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public MongoClient mongoClient(@Value("${spring.mongodb.uri}") String connectionString) {

        ConnectionString connString = new ConnectionString(connectionString); 
        //TODO> Ticket: Handling Timeouts - configure the expected
        // WriteConcern `wtimeout` and `connectTimeoutMS` values
        
        /*
         * Either to set properties file with spring.mongodb.uri=mongodb+srv://m220user:m220password@<YOUR_CLUSTER_HOSTS>/sample_mflix?maxPoolSize=50&wtimeout=2500&connectTimeoutMS=2000
         * Or
	        WriteConcern wc = WriteConcern.MAJORITY.withWTimeout(2500,
	              TimeUnit.MILLISECONDS);
	      MongoClientSettings settings =
	              MongoClientSettings.builder()
	                      .applyConnectionString(this.connectionString)
	                      .writeConcern(wc)
	                      .build();
	      mongoClient = MongoClients.create(settings);
	      */
	      
        MongoClient mongoClient = MongoClients.create(connectionString);
         
        return mongoClient;
    }
}
