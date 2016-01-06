package com.pla.grouphealth.claim.cashless.presentation.controller;

import com.pla.grouphealth.claim.cashless.application.service.PreAuthorizationRequestService;
import com.pla.grouphealth.claim.cashless.domain.model.PreAuthorizationId;
import com.pla.grouphealth.claim.cashless.presentation.dto.PreAuthorizationClaimantDetailDto;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by Mohan Sharma on 1/6/2016.
 */
@RestController
@RequestMapping(value = "/grouphealth/claim/cashless/preauthorizationrequest")
public class PreAuthorizationRequestController {
    @Autowired
    private CommandGateway commandGateway;
    @Autowired
    private PreAuthorizationRequestService preAuthorizationRequestService;

    @RequestMapping(value = "/getPreAuthorizationById", method = RequestMethod.GET)
    public PreAuthorizationClaimantDetailDto getPreAuthorizationById(@RequestParam String preAuthorizationId){
        return preAuthorizationRequestService.getPreAuthorizationById(new PreAuthorizationId(preAuthorizationId));
    }

    @RequestMapping(value = "/getPolicyByPreAuthorizationId", method = RequestMethod.GET)
    public Map<String, Object> getPolicyByPreAuthorizationId(@RequestParam String preAuthorizationId){
        return preAuthorizationRequestService.getPolicyByPreAuthorizationId(new PreAuthorizationId(preAuthorizationId));
    }
}
