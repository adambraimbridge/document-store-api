package com.ft.universalpublishing.documentstore.resources;

import com.codahale.metrics.annotation.Timed;
import com.ft.api.jaxrs.errors.ClientError;
import com.ft.universalpublishing.documentstore.handler.HandlerChain;
import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.model.read.Operation;

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import javafx.util.Pair;

@Path("/")
public class DocumentResource {

  protected static final String CHARSET_UTF_8 = ";charset=utf-8";

  private Map<Pair<String, Operation>, HandlerChain> collections;

  public DocumentResource(Map<Pair<String, Operation>, HandlerChain> collections) {
    this.collections = collections;
  }

  @GET
  @Timed
  @Path("/{collection}/{uuidString}")
  @Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
  public final Object getFromCollectionByUuid(
      @PathParam("uuidString") String uuidString, @PathParam("collection") String collection) {
    Context context = new Context();
    context.setUuids(uuidString);
    context.setCollection(collection);
    HandlerChain handlerChain = getHandlerChain(collection, Operation.GET_BY_ID);
    return handlerChain.execute(context);
  }

  @GET
  @Timed
  @Path("/{collection}")
  @Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
  public final Object getFromCollectionByUuids(
          @javax.ws.rs.core.Context HttpHeaders httpHeaders,
          @javax.ws.rs.core.Context UriInfo uriInfo, @PathParam("collection") String collection) {
    Context context = new Context();
    context.setUriInfo(uriInfo);
    context.setHttpHeaders(httpHeaders);
    context.setCollection(collection);
    HandlerChain handlerChain = getHandlerChain(collection, Operation.GET_FILTERED);
    return handlerChain.execute(context);
  }

  @PUT
  @Timed
  @Path("/{collection}/{uuidString}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Object writeInCollection(@PathParam("uuidString") String uuidString,
                                  Map<String, Object> contentMap, @javax.ws.rs.core.Context UriInfo uriInfo,
                                  @PathParam("collection") String collection) {

    Context context = new Context();
    context.setCollection(collection);
    context.setUuids(uuidString);
    context.setContentMap(contentMap);
    context.setUriInfo(uriInfo);
    HandlerChain handlerChain = getHandlerChain(collection, Operation.ADD);
    return handlerChain.execute(context);
  }


  @DELETE
  @Timed
  @Path("/{collection}/{uuidString}")
  public Object deleteFromCollection(@PathParam("uuidString") String uuidString,
                                     @javax.ws.rs.core.Context UriInfo uriInfo, @PathParam("collection") String collection) {
    Context context = new Context();
    context.setUuids(uuidString);
    context.setCollection(collection);
    context.setUriInfo(uriInfo);
    HandlerChain handlerChain = getHandlerChain(collection, Operation.REMOVE);
    return handlerChain.execute(context);
  }


  protected HandlerChain getHandlerChain(String collection, Operation Operation) {
    Pair<String, Operation> pair = new Pair<>(collection, Operation);
    if (!collections.containsKey(pair)) {
      throw ClientError.status(404).exception();
    }
    return collections.get(pair);
  }
}
