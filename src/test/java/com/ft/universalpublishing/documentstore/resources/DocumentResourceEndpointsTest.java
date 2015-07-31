package com.ft.universalpublishing.documentstore.resources;

import com.ft.api.jaxrs.errors.ErrorEntity;
import com.ft.universalpublishing.documentstore.exception.DocumentNotFoundException;
import com.ft.universalpublishing.documentstore.exception.ExternalSystemUnavailableException;
import com.ft.universalpublishing.documentstore.exception.ValidationException;
import com.ft.universalpublishing.documentstore.model.ContentMapper;
import com.ft.universalpublishing.documentstore.model.IdentifierMapper;
import com.ft.universalpublishing.documentstore.model.TypeResolver;
import com.ft.universalpublishing.documentstore.service.DocumentStoreService;
import com.ft.universalpublishing.documentstore.validators.ContentListDocumentValidator;
import com.ft.universalpublishing.documentstore.validators.UuidValidator;
import com.ft.universalpublishing.documentstore.write.DocumentWritten;
import com.sun.jersey.api.client.ClientResponse;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.bson.Document;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import javax.ws.rs.core.MediaType;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class DocumentResourceEndpointsTest {

    private String uuid;
    private String resourceType;
    private Document document;
    private String writePath;

    private final static DocumentStoreService documentStoreService = mock(DocumentStoreService.class);
    private final static ContentListDocumentValidator contentListDocumentValidator = mock(ContentListDocumentValidator.class);
    private final static UuidValidator uuidValidator = mock(UuidValidator.class);
    private static final String API_URL_PREFIX_CONTENT = "localhost";

    public DocumentResourceEndpointsTest(String resourceType, Document document,
            String uuid) {
        this.resourceType = resourceType;
        this.document = document;
        this.uuid = uuid;
        this.writePath = "/" + resourceType + "/" + uuid;
    }

    @Parameters
    public static Collection<Object[]> documents() {
        String uuid1 = UUID.randomUUID().toString();
        String uuid2 = UUID.randomUUID().toString();
        return Arrays.asList(new Object[][]{{"content", getContent(uuid1), uuid1}
//                ,{"lists", getContentList(uuid2), uuid2}
        });

    }

    private static Document getContent(String uuid) {
        Date lastPublicationDate = new Date();
        Map<String, Object> content = new HashMap<>();
        content.put("uuid", uuid);
        content.put("title", "Here's the news");
        content.put("bodyXML", "xmlBody");
        content.put("publishedDate", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(lastPublicationDate));
        return new Document(content);
    }

//    private static Document getContentList(String uuid) {
//        String contentUuid1 = UUID.randomUUID().toString();
//        String contentUuid2 = UUID.randomUUID().toString();
//        ListItem contentItem1 = new ListItem();
//        contentItem1.setUuid(contentUuid1);
//        ListItem contentItem2 = new ListItem();
//        contentItem2.setUuid(contentUuid2);
//        List<ListItem> content = ImmutableList.of(contentItem1, contentItem2);
//
//        return new Document(new ObjectMapper().convertValue(new ContentList.Builder()
//                .withId("http://api.ft.com/thing/" + uuid)
//                .withApiUrl("http://localhost/lists/" + uuid)
//                .withUuid(UUID.fromString(uuid))
//                .withItems(content)
//                .build(), Map.class));
//    }

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new DocumentResource(documentStoreService, contentListDocumentValidator, uuidValidator, API_URL_PREFIX_CONTENT,
                    new ContentMapper(new IdentifierMapper(), new TypeResolver())))
            .build();

    @Before
    public void setup() {
        reset(documentStoreService);
        reset(contentListDocumentValidator);
        reset(uuidValidator);
        when(documentStoreService.write(eq(resourceType), any(Map.class))).thenReturn(DocumentWritten.created(document));
    }

    //WRITE

    @Test
    public void shouldReturn201ForNewDocument() {
        ClientResponse clientResponse = writeDocument(writePath, document);
        assertThat("response", clientResponse, hasProperty("status", equalTo(201)));
        verify(documentStoreService).write(eq(resourceType), any(Map.class));
    }

    @Test
    public void shouldReturn200ForUpdatedContent() {
        when(documentStoreService.write(eq(resourceType), any(Map.class))).thenReturn(DocumentWritten.updated(document));

        ClientResponse clientResponse = writeDocument(writePath, document);
        assertThat("response", clientResponse, hasProperty("status", equalTo(200)));
    }

    @Test
    public void shouldReturn400OnWriteWhenUuidNotValid() {
        doThrow(new ValidationException("Invalid Uuid")).when(uuidValidator).validate(anyString());
        ClientResponse clientResponse = writeDocument(writePath, document);

        assertThat("response", clientResponse, hasProperty("status", equalTo(400)));
        validateErrorMessage("Invalid Uuid", clientResponse);
    }

    @Test
    public void shouldReturn503WhenCannotAccessExternalSystem() {
        when(documentStoreService.write(eq(resourceType), any())).thenThrow(new ExternalSystemUnavailableException("Cannot connect to Mongo"));

        ClientResponse clientResponse = writeDocument(writePath, document);

        assertThat("", clientResponse, hasProperty("status", equalTo(503)));

    }

    //DELETE

    @Test
    public void shouldReturn204WhenDeletedSuccessfully() {
        ClientResponse clientResponse = resources.client().resource(writePath)
                .delete(ClientResponse.class);

        assertThat("response", clientResponse, hasProperty("status", equalTo(204)));
    }

    @Test
    public void shouldReturn404WhenDeletingNonExistentContentList() {
        doThrow(new DocumentNotFoundException(UUID.fromString(uuid))).when(documentStoreService).delete(eq(resourceType), any(UUID.class));

        ClientResponse clientResponse = resources.client().resource(writePath)
                .delete(ClientResponse.class);

        assertThat("response", clientResponse, hasProperty("status", equalTo(404)));
    }

    @Test
    public void shouldReturn400OnDeleteWhenUuidNotValid() {
        doThrow(new ValidationException("Invalid Uuid")).when(uuidValidator).validate(anyString());
        ClientResponse clientResponse = resources.client().resource(writePath)
                .delete(ClientResponse.class);

        assertThat("response", clientResponse, hasProperty("status", equalTo(400)));
        validateErrorMessage("Invalid Uuid", clientResponse);
    }

    @Test
    public void shouldReturn503OnDeleteWhenMongoIsntReachable() {
        doThrow(new ExternalSystemUnavailableException("Cannot connect to Mongo")).when(documentStoreService).delete(eq(resourceType), any(UUID.class));

        ClientResponse clientResponse = resources.client().resource(writePath)
                .delete(ClientResponse.class);

        assertThat("response", clientResponse, hasProperty("status", equalTo(503)));
    }

    //READ
    @Test
    public void shouldReturn200WhenReadSuccessfully() {
        when(documentStoreService.findByUuid(eq(resourceType), any(UUID.class))).thenReturn(document);
        ClientResponse clientResponse = resources.client().resource(writePath)
                .get(ClientResponse.class);

        assertThat("response", clientResponse, hasProperty("status", equalTo(200)));
        final Document retrievedDocument = clientResponse.getEntity(Document.class);
        assertThat("document", retrievedDocument, equalTo(document));
    }

    @Test
    public void shouldReturn404WhenContentNotFound() {
        when(documentStoreService.findByUuid(eq(resourceType), any(UUID.class))).thenReturn(null);
        ClientResponse clientResponse = resources.client().resource(writePath)
                .get(ClientResponse.class);

        assertThat("response", clientResponse, hasProperty("status", equalTo(404)));
        validateErrorMessage("Requested item does not exist", clientResponse);
    }

    @Test
    public void shouldReturn400OnReadWhenUuidNotValid() {
        doThrow(new ValidationException("Invalid Uuid")).when(uuidValidator).validate(anyString());
        ClientResponse clientResponse = resources.client().resource(writePath)
                .get(ClientResponse.class);

        assertThat("response", clientResponse, hasProperty("status", equalTo(400)));
        validateErrorMessage("Invalid Uuid", clientResponse);
    }

    @Test
    public void shouldReturn503OnReadWhenMongoIsntReachable() {
        doThrow(new ExternalSystemUnavailableException("Cannot connect to Mongo")).when(documentStoreService).findByUuid(eq(resourceType), any(UUID.class));

        ClientResponse clientResponse = resources.client().resource(writePath)
                .get(ClientResponse.class);

        assertThat("response", clientResponse, hasProperty("status", equalTo(503)));
    }

    //OTHER
    @Test
    public void shouldReturn405ForPost() {
        ClientResponse clientResponse = resources.client().resource(writePath)
                .post(ClientResponse.class);

        assertThat("response", clientResponse, hasProperty("status", equalTo(405)));
    }


    private ClientResponse writeDocument(String writePath, Document document) {
        return resources.client()
                .resource(writePath)
                .entity(document, MediaType.APPLICATION_JSON)
                .put(ClientResponse.class);
    }

    private void validateErrorMessage(String expectedErrorMessage, ClientResponse clientResponse) {
        final ErrorEntity responseBodyMessage = clientResponse.getEntity(ErrorEntity.class);
        assertThat("message", responseBodyMessage, hasProperty("message", equalTo(expectedErrorMessage)));
    }

}
