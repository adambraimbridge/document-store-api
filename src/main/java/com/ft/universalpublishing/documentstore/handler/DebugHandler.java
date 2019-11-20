package com.ft.universalpublishing.documentstore.handler;

import com.ft.universalpublishing.documentstore.model.read.Context;

import com.savoirtech.logging.slf4j.json.LoggerFactory;
import com.savoirtech.logging.slf4j.json.logger.JsonLogger;
import com.savoirtech.logging.slf4j.json.logger.Logger;

import java.time.Instant;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import static java.time.format.DateTimeFormatter.ISO_INSTANT;

public class DebugHandler implements Handler {

    private final Logger LOGGER = LoggerFactory.getLogger(DebugHandler.class);

    @Override
    public void handle(Context context) {

       final JsonLogger jsonLogger = LOGGER.info();

	Map<String, Object> data = context.getContentMap();
	Gson gson = new Gson(); 
	String json = gson.toJson(data); 
	jsonLogger.field("@time", ISO_INSTANT.format(Instant.now()))
		.field("event", "SaveDocStore")
		.field("collection", "UNKNOWN")
		.field("monitoring_event", "true")
		.field("service_name", "document-store-api")
		.field("content_type", "UNKNOWN")
		.field("uuid", context.getUuid())
		.message(json)
		.log();
    }
}
