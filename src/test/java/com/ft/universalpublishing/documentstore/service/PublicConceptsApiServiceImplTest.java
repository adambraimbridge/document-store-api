package com.ft.universalpublishing.documentstore.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.universalpublishing.documentstore.clients.PublicConceptsApiClient;
import com.ft.universalpublishing.documentstore.model.read.Concept;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

public class PublicConceptsApiServiceImplTest {
    private PublicConceptsApiClient publicConceptsApiClientMock = mock(PublicConceptsApiClient.class);
    private PublicConceptsApiServiceImpl publicConceptApiService = new PublicConceptsApiServiceImpl(
            publicConceptsApiClientMock);

    @BeforeEach
    public void setup() {
        reset(publicConceptsApiClientMock);
    }

    @Test
    public void healthcheckIsOK() {
        String message = "{\"ok\": true}";

        final Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        when(response.readEntity(eq(String.class))).thenReturn(message);

        when(publicConceptsApiClientMock.getHealthcheck()).thenReturn(response);
        boolean isHealthcheckOK = publicConceptApiService.isHealthcheckOK();
        assertTrue(isHealthcheckOK);
    }

    @Test
    public void healthcheckIsNotOK() {
        String message = "{\"ok\": false}";

        final Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        when(response.readEntity(eq(String.class))).thenReturn(message);

        when(publicConceptsApiClientMock.getHealthcheck()).thenReturn(response);
        boolean isHealthcheckOK = publicConceptApiService.isHealthcheckOK();
        assertFalse(isHealthcheckOK);
    }

    @Test
    public void getUpToDateConceptShouldIvnokedWithNull() throws JsonMappingException, JsonProcessingException {
        Concept concept = publicConceptApiService.getUpToDateConcept(null);
        assertNull(concept);
    }

    @Test
    public void getUpToDateConceptShouldReturnInvokedWithEmptyString()
            throws JsonMappingException, JsonProcessingException {
        Concept concept = publicConceptApiService.getUpToDateConcept("");
        assertNull(concept);
    }

    @Test
    public void getUpToDateConcept() throws JsonProcessingException {
        Concept concept = new Concept(UUID.randomUUID(), "somePrefLabel");
        ObjectMapper objectMapper = new ObjectMapper();
        String message = objectMapper.writeValueAsString(concept);

        final Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        when(response.readEntity(eq(String.class))).thenReturn(message);

        String conceptUuid = concept.getUuid().toString();
        when(publicConceptsApiClientMock.getConcept(eq(conceptUuid))).thenReturn(response);
        Concept result = publicConceptApiService.getUpToDateConcept(conceptUuid);
        verify(publicConceptsApiClientMock).getConcept(eq(conceptUuid));

        assertEquals(concept, result, "Expected concepts to be equal");
    }

    // TODO: negative tests
}
