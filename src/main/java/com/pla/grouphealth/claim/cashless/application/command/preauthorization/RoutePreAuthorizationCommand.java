package com.pla.grouphealth.claim.cashless.application.command.preauthorization;

import com.pla.grouphealth.claim.cashless.presentation.dto.preauthorization.PreAuthorizationClaimantDetailCommand;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by Mohan Sharma on 1/25/2016.
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class RoutePreAuthorizationCommand {
    private PreAuthorizationClaimantDetailCommand preAuthorizationClaimantDetailCommand;
    private String userName;
}