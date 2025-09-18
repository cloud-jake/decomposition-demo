package com.carddemo.authorizationservice.messaging;

import com.carddemo.authorizationservice.service.AuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthorizationListener {

    private final AuthorizationService authorizationService;

    /**
     * Listens to the authorization request queue.
     * The destination name comes from the MQ Configuration in the README.
     */
    @JmsListener(destination = "AWS.M2.CARDDEMO.PAUTH.REQUEST")
    public void receiveAuthorizationRequest(String message) {
        log.info("Received authorization request message: '{}'", message);
        // The service will handle parsing, business logic, and sending the response.
        authorizationService.processAuthorization(message);
    }
}