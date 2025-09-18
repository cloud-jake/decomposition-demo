package com.carddemo.authorizationservice.client.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO representing the data returned from the monolith's Account Data API.
 * This decouples the microservice from the monolith's internal data structures (e.g., COBOL copybooks).
 */
@Data
public class AccountData {
    private Long accountId;
    private Long customerId;
    private String acctStatus;
    private BigDecimal currentBalance;
    private BigDecimal availableCredit;
}