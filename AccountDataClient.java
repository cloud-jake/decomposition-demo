package com.carddemo.authorizationservice.client;

import com.carddemo.authorizationservice.client.dto.AccountData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * This client acts as the Anti-Corruption Layer. It calls a new, simple REST API
 * exposed on the monolith to fetch data from legacy data stores like VSAM.
 */
@Component
@RequiredArgsConstructor
public class AccountDataClient {

    private final RestTemplate restTemplate;

    @Value("${monolith.api.base-url}")
    private String monolithApiBaseUrl;

    /**
     * Fetches account data from the monolith's new REST endpoint.
     *
     * @param cardNumber The card number to look up.
     * @return AccountData retrieved from the monolith.
     */
    public AccountData getAccountDataByCardNumber(String cardNumber) {
        // The endpoint "/api/v1/accounts/by-card/{cardNumber}" is a hypothetical
        // new endpoint you would create on the monolith, for example, using
        // CICS Web Services or z/OS Connect.
        String url = monolithApiBaseUrl + "/api/v1/accounts/by-card/" + cardNumber;

        // The RestTemplate handles the HTTP call and JSON deserialization.
        return restTemplate.getForObject(url, AccountData.class);
    }
}