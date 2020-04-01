package com.ft.universalpublishing.documentstore.health;

import com.ft.platform.dropwizard.AdvancedHealthCheck;
import com.ft.platform.dropwizard.AdvancedResult;
import com.ft.universalpublishing.documentstore.service.PublicConceptsApiService;

public class PublicConceptsApiConnectionHealthCheck extends AdvancedHealthCheck {
    private static final String MESSAGE = "Public Concepts Api is not healthy";

    private final PublicConceptsApiService service;
    private final HealthcheckParameters healthcheckParameters;

    public PublicConceptsApiConnectionHealthCheck(PublicConceptsApiService service,
            HealthcheckParameters healthcheckParameters) {
        super(healthcheckParameters.getName());
        this.service = service;
        this.healthcheckParameters = healthcheckParameters;
    }

    @Override
    protected AdvancedResult checkAdvanced() {
        if (service.isHealthcheckOK()) {
            return AdvancedResult.healthy("OK");
        }

        return AdvancedResult.error(this, MESSAGE);
    }

    @Override
    protected int severity() {
        return healthcheckParameters.getSeverity();
    }

    @Override
    protected String businessImpact() {
        return healthcheckParameters.getBusinessImpact();
    }

    @Override
    protected String technicalSummary() {
        return healthcheckParameters.getTechnicalSummary();
    }

    @Override
    protected String panicGuideUrl() {
        return healthcheckParameters.getPanicGuideUrl();
    }

}
