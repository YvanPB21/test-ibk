package org.example.demo;

import io.r2dbc.spi.ConnectionFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.web.reactive.config.EnableWebFlux;

@org.springframework.boot.autoconfigure.SpringBootApplication
@EnableWebFlux
@EnableR2dbcRepositories(basePackages = "org.example.demo.repository")
public class SpringBootApplication {

  public static void main(String[] args) {
    SpringApplication.run(SpringBootApplication.class, args);
  }

  @Bean
  ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {
    ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
    initializer.setConnectionFactory(connectionFactory);
    ResourceDatabasePopulator schemaPopulator = new ResourceDatabasePopulator(new ClassPathResource("schema.sql"));
    schemaPopulator.setContinueOnError(false);
    schemaPopulator.setSeparator(";");

    initializer.setDatabasePopulator(schemaPopulator);
    return initializer;
  }

}
