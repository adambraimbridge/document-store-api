package com.ft.universalpublishing.documentstore;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import com.ft.api.util.buildinfo.BuildInfoResource;
import com.ft.platform.dropwizard.AdvancedHealthCheckBundle;
import com.ft.universalpublishing.documentstore.health.HelloworldHealthCheck;
import com.ft.universalpublishing.documentstore.mongo.MongoContentReader;
import com.ft.universalpublishing.documentstore.mongo.MongoContentWriter;
import com.ft.universalpublishing.documentstore.resources.JsonApiResource;
import com.mongodb.DB;
import com.mongodb.MongoClient;

public class JsonApiApplication extends Application<JsonApiConfiguration> {

    public static void main(final String[] args) throws Exception {
        new JsonApiApplication().run(args);
    }

    @Override
    public void initialize(final Bootstrap<JsonApiConfiguration> bootstrap) {
        bootstrap.addBundle(new AdvancedHealthCheckBundle());
    }

    @Override
    public void run(final JsonApiConfiguration configuration, final Environment environment) throws Exception {
        environment.jersey().register(new BuildInfoResource());
        
        final MongoClient mongoClient = new MongoClient(configuration.getMongo().getHost(), configuration.getMongo().getPort());
        final DB db = mongoClient.getDB(configuration.getMongo().getDb());
        final MongoContentWriter contentWriter = new MongoContentWriter(db);
        final MongoContentReader contentReader = new MongoContentReader(db);

        environment.jersey().register(new JsonApiResource(contentWriter, contentReader));

        environment.healthChecks().register("My Health", new HelloworldHealthCheck("replace me"));

    }

}
