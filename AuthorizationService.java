package com.carddemo.authorizationservice.service;

import com.carddemo.authorizationservice.client.AccountDataClient;
import com.carddemo.authorizationservice.client.dto.AccountData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthorizationService {

    private final AccountDataClient accountDataClient;
    private final JmsTemplate jmsTemplate;

    private static final String REPLY_QUEUE = "AWS.M2.CARDDEMO.PAUTH.REPLY";

    public void processAuthorization(String message) {
        // Step 1: Parse the incoming CSV message
        // In a real app, use a robust CSV parsing library.
        String[] parts = message.split(",");
        String cardNum = parts[2];
        BigDecimal transactionAmt = new BigDecimal(parts[8]);
        String transactionId = parts[17];

        String authIdCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String authRespCode;
        String authRespReason;
        BigDecimal approvedAmt;

        try {
            // Step 2: Call the monolith's new API (Anti-Corruption Layer) to get account data
            log.info("Calling monolith Account Data API for card number: {}", cardNum);
            AccountData accountData = accountDataClient.getAccountDataByCardNumber(cardNum);
            log.info("Received account data from monolith: {}", accountData);

            // Step 3: Apply business rules
            if (!"A".equals(accountData.getAcctStatus())) {
                authRespCode = "05"; // Decline
                authRespReason = "INAC"; // Inactive Account
                approvedAmt = BigDecimal.ZERO;
                log.warn("Declining authorization for inactive account. Card: {}, Status: {}", cardNum, accountData.getAcctStatus());
            } else if (transactionAmt.compareTo(accountData.getAvailableCredit()) > 0) {
                authRespCode = "51"; // Decline
                authRespReason = "FNDS"; // Insufficient Funds
                approvedAmt = BigDecimal.ZERO;
                log.warn("Declining authorization for insufficient funds. Card: {}, Requested: {}, Available: {}",
                        cardNum, transactionAmt, accountData.getAvailableCredit());
            } else {
                authRespCode = "00"; // Approve
                authRespReason = "APPR"; // Approved
                approvedAmt = transactionAmt;
                log.info("Approving authorization for card: {}. Amount: {}", cardNum, approvedAmt);
            }

        } catch (Exception e) {
            log.error("Error processing authorization for card: {}", cardNum, e);
            authRespCode = "91"; // System Error
            authRespReason = "SYSE"; // System Error
            approvedAmt = BigDecimal.ZERO;
        }

        // Step 4: Build and send the response message to the reply queue
        String responseMessage = String.join(",",
                cardNum, transactionId, authIdCode, authRespCode, authRespReason, approvedAmt.toPlainString()
        );

        log.info("Sending response to queue '{}': {}", REPLY_QUEUE, responseMessage);
        jmsTemplate.convertAndSend(REPLY_QUEUE, responseMessage);
    }
}