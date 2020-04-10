package com.ft.universalpublishing.documentstore.service;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;

import com.ft.universalpublishing.documentstore.clients.PublicConcordancesApiClient;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

public class PublicConcordancesApiServiceImplTest {
    private PublicConcordancesApiClient publicConcordancesApiClientMock = mock(PublicConcordancesApiClient.class);
    private PublicConcordancesApiServiceImpl publicConcordancesApiService = new PublicConcordancesApiServiceImpl(
            publicConcordancesApiClientMock);

    @BeforeEach
    public void setup() {
        reset(publicConcordancesApiClientMock);
    }

    @Test
    public void healthcheckIsOK() {
        String message = "{\"ok\": \"true\"}";

        final Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        when(response.readEntity(eq(String.class))).thenReturn(message);

        when(publicConcordancesApiClientMock.getHealthcheck()).thenReturn(response);
        boolean isHealthcheckOK = publicConcordancesApiService.isHealthcheckOK();
        assertTrue(isHealthcheckOK);
    }
}
