package com.ft.universalpublishing.documentstore.service;

import com.ft.universalpublishing.documentstore.exception.DocumentNotFoundException;
import com.ft.universalpublishing.documentstore.exception.ExternalSystemInternalServerException;
import com.ft.universalpublishing.documentstore.exception.ExternalSystemUnavailableException;
import com.ft.universalpublishing.documentstore.exception.QueryResultNotUniqueException;
import com.ft.universalpublishing.documentstore.write.DocumentWritten;
import com.mongodb.MongoException;
import com.mongodb.MongoSocketException;
import com.mongodb.MongoTimeoutException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

public class MongoDocumentStoreService implements DocumentStoreService {
    private static final Logger LOG = LoggerFactory.getLogger(MongoDocumentStoreService.class);
    
    private static final String IDENT_AUTHORITY = "identifiers.authority";
    private static final String IDENT_VALUE = "identifiers.identifierValue";
    
    private final MongoDatabase db;

    public MongoDocumentStoreService(final MongoDatabase db) {
        this.db = db;
    }

    @Override
    public Map<String, Object> findByUuid(String resourceType, UUID uuid) {
        try {
            MongoCollection<Document> dbCollection = db.getCollection(resourceType);
            Document foundDocument = dbCollection.find().filter(Filters.eq("uuid", uuid.toString())).first();
            if (foundDocument!= null) {
                foundDocument.remove("_id");
            }
            return foundDocument;
        } catch (MongoSocketException | MongoTimeoutException e) {
            throw new ExternalSystemUnavailableException("cannot communicate with mongo", e);
        } catch (MongoException e) {
            throw new ExternalSystemInternalServerException(e);
        }
    }
    
    @Override
    public Map<String,Object> findByIdentifier(String resourceType, String authority, String identifierValue) {
        Bson filter = Filters.and(
            Filters.eq("identifiers.authority", authority),
            Filters.eq("identifiers.identifierValue", identifierValue)
            );
        
        try {
            MongoCollection<Document> dbCollection = db.getCollection(resourceType);
            Document found = null;
            
            for (Document doc : dbCollection.find(filter).limit(2)) {
                if (found == null) {
                    found = doc;
                    found.remove("_id");
                }
                else {
                    LOG.warn("found too many results for collection {} identifier {}:{}: at least {} and {}",
                            resourceType, authority, identifierValue, found, doc);
                    throw new QueryResultNotUniqueException();
                }
            }
            
            return found;
        }
        catch (MongoException e) {
            throw new ExternalSystemInternalServerException(e);
        }
    }
    

    @Override
    public Map<String, Object> findByConceptAndType(String resourceType, String conceptId, String typeId) {
        Bson filter = Filters.and(
                Filters.eq("concept.tmeIdentifier", conceptId),
                Filters.eq("type.id", typeId)
                );
            
            try {
                MongoCollection<Document> dbCollection = db.getCollection(resourceType);
                Document found = null;
                
                for (Document doc : dbCollection.find(filter).limit(2)) {
                    if (found == null) {
                        found = doc;
                        found.remove("_id");
                    }
                    else {
                        LOG.warn("found too many results for collection {} identifier {}:{}: at least {} and {}",
                                resourceType, conceptId, typeId, found, doc);
                        throw new QueryResultNotUniqueException();
                    }
                }
                
                return found;
            }
            catch (MongoException e) {
                throw new ExternalSystemInternalServerException(e);
            }
    }

    @Override
    public void delete(String resourceType, UUID uuid) {
        try {

            MongoCollection<Document> dbCollection = db.getCollection(resourceType);
            DeleteResult deleteResult = dbCollection.deleteOne(Filters.eq("uuid", uuid.toString()));

            if (deleteResult.getDeletedCount() == 0) {
                throw new DocumentNotFoundException(uuid);
            }

        } catch (MongoSocketException | MongoTimeoutException e) {
            throw new ExternalSystemUnavailableException("cannot communicate with mongo", e);
        } catch (MongoException e) {
            throw new ExternalSystemInternalServerException(e);
        }
    }

    @Override
    public DocumentWritten write(String resourceType, Map<String, Object> content) {
        try {
            MongoCollection<Document> dbCollection = db.getCollection(resourceType);
            final String uuid = (String) content.get("uuid");
            Document document = new Document(content);
            UpdateResult updateResult = dbCollection.replaceOne(Filters.eq("uuid", uuid), document, new UpdateOptions().upsert(true));
            if (updateResult.getUpsertedId() == null) {
                return DocumentWritten.updated(document);
            }
            return DocumentWritten.created(document);
        } catch (MongoSocketException | MongoTimeoutException e) {
            throw new ExternalSystemUnavailableException("cannot communicate with mongo", e);
        } catch (MongoException e) {
            throw new ExternalSystemInternalServerException(e);
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void applyIndexes() {
      MongoCollection content = db.getCollection(CONTENT_COLLECTION);
      createUuidIndex(content);
      createIdentifierIndex(content);
      
      MongoCollection lists = db.getCollection(LISTS_COLLECTION);
      createUuidIndex(lists);
    }
    
    private void createUuidIndex(MongoCollection<?> collection) {
      collection.createIndex(new Document("uuid", 1));
    }
    
    private void createIdentifierIndex(MongoCollection<?> collection) {
      Document queryByIdentifierIndex = new Document();
      queryByIdentifierIndex.put(IDENT_AUTHORITY, 1);
      queryByIdentifierIndex.put(IDENT_VALUE, 1);
      collection.createIndex(queryByIdentifierIndex);
    }

}
