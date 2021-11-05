package com.workshops.eips;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.CsvDataFormat;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * A simple Camel route that triggers from a timer and calls a bean and prints to system out.
 * <p/>
 * Use <tt>@Component</tt> to make Camel auto detect this route when starting.
 */
@Component
public class MySpringBootRouter extends RouteBuilder {

    @Bean
    public CsvDataFormat csvDataFormat() {
        return new CsvDataFormat("|");
    }


    @Override
    public void configure() {
        getContext().setStreamCaching(true);

        from("file:data/inbox/animals").routeId("zooRoute")
                .log("Reading file ${headers.CamelFileName}")
                //TODO: Use Split EIP and Aggregator EIP to generate multiple JSON files grouped by Country
                //TODO: Use as filename zoo-{COUNTRY}.json output notation
                //TODO: the output directory should be data/outbox/animals
                //TODO: The JSON output must be pretty print.
                .end();


        from("file:data/inbox/json").routeId("JsonTransformerRoute")
                .log("Reading file ${headers.CamelFileName}")
                .log("File Details: ${file:name} - ${file:path} - ${file:size} - ${file:modified} - ${file:ext}")
                .setHeader("factoryName", jsonpath("$.factory.name"))
                .log("Processing factory ${headers.factoryName}")
                //TODO: Use Split EIP for apply a discount to all used cars by 25%
                // then return must have the same JSON content with the prices updated
                // The output directory should be data/outbox/cars, and the filename the same cars.json
                .end();

    }

}
