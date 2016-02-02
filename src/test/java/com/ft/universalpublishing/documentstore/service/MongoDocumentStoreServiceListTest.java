package com.ft.universalpublishing.documentstore.service;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import org.bson.Document;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.ft.universalpublishing.documentstore.exception.DocumentNotFoundException;
import com.ft.universalpublishing.documentstore.model.ListItem;
import com.ft.universalpublishing.documentstore.model.ContentList;
import com.ft.universalpublishing.documentstore.write.DocumentWritten;
import com.ft.universalpublishing.documentstore.write.DocumentWritten.Mode;
import com.google.common.collect.ImmutableList;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;


public class MongoDocumentStoreServiceListTest {
    @ClassRule
    public static final EmbeddedMongoRule MONGO = new EmbeddedMongoRule(12032);
    
    private static final String DB_NAME = "upp-store";
    private static final String DB_COLLECTION = "lists";
    private static final String WEBURL = "http://www.bbc.co.uk/";

    private MongoDocumentStoreService mongoDocumentStoreService;

    private UUID uuid;
    private String contentUuid1;
    private MongoCollection<Document> collection;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() {
        MongoDatabase db = MONGO.client().getDatabase(DB_NAME);
        db.getCollection(DB_COLLECTION).drop();
        
        mongoDocumentStoreService = new MongoDocumentStoreService(db);
        collection = db.getCollection("lists");
        uuid = UUID.randomUUID();
        contentUuid1 = UUID.randomUUID().toString();
    }

    private List<ListItem> mockInboundListItems() {
        ListItem contentItem1 = new ListItem();
        contentItem1.setUuid(contentUuid1);
        ListItem contentItem2 = new ListItem();
        contentItem2.setWebUrl(WEBURL);
        return ImmutableList.of(contentItem1, contentItem2);
    }

    private List<ListItem> mockOutboundListItems() {
        ListItem outboundContentItem1 = new ListItem();
        outboundContentItem1.setUuid(contentUuid1);
        ListItem outboundContentItem2 = new ListItem();
        outboundContentItem2.setWebUrl(WEBURL);
        return ImmutableList.of(outboundContentItem1, outboundContentItem2);
    }

    @Test
    public void contentListInStoreShouldBeRetrievedSuccessfully() {
        BasicDBList items = new BasicDBList();
        items.add(new BasicDBObject().append("uuid", contentUuid1));
        items.add(new BasicDBObject().append("webUrl", WEBURL));
        final Document toInsert = new Document()
                .append("uuid", uuid.toString())
                .append("items", items);
        collection.insertOne(toInsert);

        ContentList expectedList = new ContentList.Builder()
            .withUuid(uuid)
            .withItems(mockOutboundListItems())
            .build();

        Map<String,Object> contentMap = mongoDocumentStoreService.findByUuid("lists", uuid);
        ContentList retrievedContentList = new ObjectMapper().convertValue(contentMap, ContentList.class);

        assertThat(retrievedContentList, is(expectedList));
    }

    @Test
    public void contentListNotInStoreShouldNotBeReturned() {
        Map<String, Object> contentMap = mongoDocumentStoreService.findByUuid("lists", uuid);
        assertThat(contentMap, nullValue());
    }

    @Test
    public void contentListShouldBePersistedOnWrite() {
        ContentList list = new ContentList.Builder()
            .withUuid(uuid)
            .withItems(mockInboundListItems())
            .build();

        DocumentWritten result = mongoDocumentStoreService.write("lists", new ObjectMapper().convertValue(list, Map.class));
        assertThat(result.getMode(), is(Mode.Created));

        Document findOne = collection.find().filter(Filters.eq("uuid", uuid.toString())).first();
        assertThat(findOne , notNullValue());
    }

    @Test
    public void thatLayoutHintIsPersisted() {
        String hint = "junit-layout";
        ContentList list = new ContentList.Builder()
            .withUuid(uuid)
            .withItems(mockInboundListItems())
            .withLayoutHint(hint)
            .build();

        DocumentWritten result = mongoDocumentStoreService.write("lists", new ObjectMapper().convertValue(list, Map.class));
        assertThat(result.getMode(), is(Mode.Created));

        ContentList actual = new ObjectMapper().convertValue(result.getDocument(), ContentList.class);
        assertThat("list uuid", actual.getUuid(), is(uuid.toString()));
        assertThat("layout hint", actual.getLayoutHint(), is(hint));
    }

    @Test
    public void thatLayoutHintIsRetrieved() {
        String hint = "junit-layout";

        BasicDBList items = new BasicDBList();
        items.add(new BasicDBObject().append("uuid", contentUuid1));
        items.add(new BasicDBObject().append("webUrl", WEBURL));

        final Document toInsert = new Document()
                .append("uuid", uuid.toString())
                .append("layoutHint", hint)
                .append("items", items);

        collection.insertOne(toInsert);

        ContentList expectedList = new ContentList.Builder()
            .withUuid(uuid)
            .withItems(mockOutboundListItems())
            .withLayoutHint(hint)
            .build();

        Map<String, Object> contentMap = mongoDocumentStoreService.findByUuid("lists", uuid);
        ContentList retrievedContentList = new ObjectMapper().convertValue(contentMap, ContentList.class);
        assertThat(retrievedContentList, is(expectedList));
    }

    @Test
    public void thatPublishReferenceIsPersisted() {
        String publishReference = "tid_zxcv7531";
        ContentList list = new ContentList.Builder()
                .withUuid(uuid)
                .withItems(mockInboundListItems())
                .withPublishReference(publishReference)
                .build();

        DocumentWritten result = mongoDocumentStoreService.write("lists", new ObjectMapper().convertValue(list, Map.class));
        assertThat(result.getMode(), is(Mode.Created));

        ContentList actual = new ObjectMapper().convertValue(result.getDocument(), ContentList.class);
        assertThat("list uuid", actual.getUuid(), is(uuid.toString()));
        assertThat("publish reference", actual.getPublishReference(), is(publishReference));
    }

    @Test
    public void thatPublishReferenceIsRetrieved() {
        String publishReference = "tid_zxcv7531";

        BasicDBList items = new BasicDBList();
        items.add(new BasicDBObject().append("uuid", contentUuid1));
        items.add(new BasicDBObject().append("webUrl", WEBURL));

        final Document toInsert = new Document()
                .append("uuid", uuid.toString())
                .append("publishReference", publishReference)
                .append("items", items);

        collection.insertOne(toInsert);

        ContentList expectedList = new ContentList.Builder()
                .withUuid(uuid)
                .withItems(mockOutboundListItems())
                .withPublishReference(publishReference)
                .build();

        Map<String, Object> contentMap = mongoDocumentStoreService.findByUuid("lists", uuid);
        ContentList retrievedContentList = new ObjectMapper().convertValue(contentMap, ContentList.class);
        assertThat(retrievedContentList, is(expectedList));
    }

    @Test
    public void contentListShouldBeDeletedOnRemove() {
        ContentList list = new ContentList.Builder()
            .withUuid(uuid)
            .withItems(mockInboundListItems())
            .build();

        DocumentWritten result = mongoDocumentStoreService.write("lists", new ObjectMapper().convertValue(list, Map.class));
        assertThat(result.getMode(), is(Mode.Created));
        Document findOne = collection.find().filter(Filters.eq("uuid", uuid.toString())).first();
        assertThat(findOne , notNullValue());

        mongoDocumentStoreService.delete("lists", uuid);
        assertThat(collection.find().filter(Filters.eq("uuid", uuid.toString())).first(), nullValue());
    }

    @Test
    public void deleteForContentListNotInStoreThrowsContentNotFoundException() {
        exception.expect(DocumentNotFoundException.class);
        exception.expectMessage(String.format("Document with uuid : %s not found!", uuid));

        mongoDocumentStoreService.delete("lists", uuid);
    }
}