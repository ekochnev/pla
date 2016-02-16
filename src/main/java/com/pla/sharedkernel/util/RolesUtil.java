/*
 * Copyright (c) 3/9/15 11:25 AM .NthDimenzion,Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.pla.sharedkernel.util;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * @author: Samir
 * @since 1.0 09/03/2015
 */
public class RolesUtil {


    public static final String GROUP_LIFE_QUOTATION_PROCESSOR_ROLE = "ROLE_GROUP_LIFE_QUOTATION_PROCESSOR";
    public static final String GROUP_HEALTH_QUOTATION_PROCESSOR_ROLE = "ROLE_GROUP_HEALTH_QUOTATION_PROCESSOR";
    public static final String INDIVIDUAL_LIFE_QUOTATION_PROCESSOR_ROLE = "ROLE_INDIVIDUAL_LIFE_QUOTATION_PROCESSOR";
    public static final String INDIVIDUAL_LIFE__PROPOSAL_PROCESSOR_ROLE = "ROLE_INDIVIDUAL_LIFE_PROPOSAL_PROCESSOR";
    public static final String INDIVIDUAL_LIFE_ENDORSEMENT_PROCESSOR_ROLE = "ROLE_INDIVIDUAL_LIFE_ENDORSEMENT_PROCESSOR";
    public static final String INDIVIDUAL_LIFE_ENDORSEMENT_APPROVER_ROLE = "ROLE_INDIVIDUAL_LIFE_ENDORSEMENT_APPROVER";
    public static final String GROUP_LIFE_PROPOSAL_PROCESSOR_ROLE = "ROLE_GROUP_LIFE_PROPOSAL_PROCESSOR";
    public static final String GROUP_LIFE_ENDORSEMENT_PROCESSOR_ROLE = "ROLE_GROUP_LIFE_ENDORSEMENT_PROCESSOR";
    public static final String GROUP_LIFE_CLAIM_PROCESSOR_ROLE = "ROLE_GROUP_LIFE_CLAIM_PROCESSOR";
    public static final String GROUP_LIFE_CLAIM_REGISTRATION_PROCESSOR_ROLE = "ROLE_GROUP_LIFE_CLAIM_REGISTRATION_PROCESSOR";
    public static final String GROUP_LIFE_CLAIM_SETTLEMENT_PROCESSOR = "ROLE_GROUP_LIFE_CLAIM_SETTLEMENT_PROCESSOR";
    public static final String INDIVIDUAL_LIFE_CLAIM_INTIMATION_PROCESSOR_ROLE = "ROLE_GROUP_LIFE_CLAIM_INTIMATION_PROCESSOR";

    private static final String ADMIN_ROLE = "ROLE_ADMIN";
    public static final String GROUP_HEALTH_PROPOSAL_PROCESSOR_ROLE = "ROLE_GROUP_HEALTH_PROPOSAL_PROCESSOR";
    public static final String GROUP_HEALTH_PROPOSAL_APPROVER_ROLE = "ROLE_GROUP_HEALTH_PROPOSAL_APPROVER";
    public static final String GROUP_LIFE_PROPOSAL_APPROVER_ROLE = "ROLE_GROUP_LIFE_PROPOSAL_APPROVER";
    public static final String GROUP_LIFE_ENDORSEMENT_APPROVER_ROLE = "ROLE_GROUP_LIFE_ENDORSEMENT_APPROVER";
    public static final String GROUP_LIFE_CLAIM_APPROVER_ROLE = "ROLE_GROUP_LIFE_CLAIM_APPROVER";
    public static final String INDIVIDUAL_LIFE_CLAIM_APPROVER_ROLE = "ROLE_INDIVIDUAL_LIFE_CLAIM_APPROVER";
    public static final String INDIVIDUAL_LIFE_PROPOSAL_APPROVER_ROLE = "ROLE_INDIVIDUAL_LIFE_PROPOSAL_APPROVER";
    public static final String GROUP_HEALTH_PRE_AUTHORIZATION_PROCESSOR_ROLE = "ROLE_GROUP_HEALTH_PRE_AUTHORIZATION_PROCESSOR";
    public static final String GROUP_HEALTH_PRE_AUTHORIZATION_UNDERWRITER_ROLE = "ROLE_GROUP_HEALTH_PRE_AUTHORIZATION_UNDERWRITER";
    public static final String GROUP_HEALTH_CASHLESS_CLAIM_PROCESSOR_ROLE = "ROLE_GROUP_HEALTH_CASHLESS_CLAIM_PROCESSOR";
    public static final String GROUP_HEALTH_CASHLESS_CLAIM_UNDERWRITER_ROLE = "ROLE_GROUP_HEALTH_CASHLESS_CLAIM_UNDERWRITER";

    private RolesUtil() {
    }

    public static boolean hasAdminRole(Collection<? extends GrantedAuthority> authorities) {
        long count = hasRole(ADMIN_ROLE, authorities);
        return count == 1;
    }

    public static boolean hasQuotationProcessorRole(Collection<? extends GrantedAuthority> authorities) {
        long count = hasRole(GROUP_LIFE_QUOTATION_PROCESSOR_ROLE, authorities);
        return count == 1;
    }

    public static boolean hasGroupHealthQuotationProcessorRole(Collection<? extends GrantedAuthority> authorities) {
        long count = hasRole(GROUP_HEALTH_QUOTATION_PROCESSOR_ROLE, authorities);
        return count == 1;
    }

    public static boolean hasIndividualLifeQuotationProcessorRole(Collection<? extends GrantedAuthority> authorities) {
        long count = hasRole(INDIVIDUAL_LIFE_QUOTATION_PROCESSOR_ROLE, authorities);
        return count == 1;
    }

    public static boolean hasIndividualLifeProposalProcessorRole(Collection<? extends GrantedAuthority> authorities) {
        long count = hasRole(INDIVIDUAL_LIFE__PROPOSAL_PROCESSOR_ROLE, authorities);
        return count == 1;
    }

    public static boolean hasGroupLifeProposalProcessorRole(Collection<? extends GrantedAuthority> authorities) {
        long count = hasRole(GROUP_LIFE_PROPOSAL_PROCESSOR_ROLE, authorities);
        return count == 1;
    }

    public static boolean hasIndividualLifeEndorsementProcessorRole(Collection<? extends GrantedAuthority> authorities) {
        long count = hasRole(INDIVIDUAL_LIFE_ENDORSEMENT_PROCESSOR_ROLE, authorities);
        return count == 1;
    }

    public static boolean hasGroupLifeEndorsementProcessorRole(Collection<? extends GrantedAuthority> authorities) {
        long count = hasRole(GROUP_LIFE_ENDORSEMENT_PROCESSOR_ROLE, authorities);
        return count == 1;
    }


    public static boolean hasGroupHealthProposalProcessorRole(Collection<? extends GrantedAuthority> authorities) {
        long count = hasRole(GROUP_HEALTH_PROPOSAL_PROCESSOR_ROLE, authorities);
        return count == 1;
    }

    public static boolean hasGroupHealthProposalApproverRole(Collection<? extends GrantedAuthority> authorities) {
        long count = hasRole(GROUP_HEALTH_PROPOSAL_APPROVER_ROLE, authorities);
        return count == 1;
    }

    public static boolean hasGroupLifeProposalApproverRole(Collection<? extends GrantedAuthority> authorities) {
        long count = hasRole(GROUP_LIFE_PROPOSAL_APPROVER_ROLE, authorities);
        return count == 1;
    }
    public static boolean hasIndividualLifeEndorsementApproverRole(Collection<? extends GrantedAuthority> authorities) {
        long count = hasRole(INDIVIDUAL_LIFE_ENDORSEMENT_APPROVER_ROLE, authorities);
        return count == 1;
    }

    public static boolean hasGroupLifeEndorsementApproverRole(Collection<? extends GrantedAuthority> authorities) {
        long count = hasRole(GROUP_LIFE_ENDORSEMENT_APPROVER_ROLE, authorities);
        return count == 1;
    }

    public static boolean hasIndividualLifeProposalApproverRole(Collection<? extends GrantedAuthority> authorities) {
        long count = hasRole(INDIVIDUAL_LIFE_PROPOSAL_APPROVER_ROLE, authorities);
        return count == 1;
    }
    public static boolean hasIndividualLifeClaimProcessorRole(Collection<? extends GrantedAuthority> authorities) {
        long count = hasRole(INDIVIDUAL_LIFE_CLAIM_INTIMATION_PROCESSOR_ROLE, authorities);
        return count == 1;
    }
    public static boolean hasGroupLifeClaimProcessorRole(Collection<? extends GrantedAuthority> authorities) {
        long count = hasRole(GROUP_LIFE_CLAIM_PROCESSOR_ROLE, authorities);
        return count == 1;
    }

    public static boolean hasGroupLifeClaimSettlementProcessor(Collection<? extends GrantedAuthority> authorities) {
        long count = hasRole(GROUP_LIFE_CLAIM_SETTLEMENT_PROCESSOR, authorities);
        return count == 1;
    }
    public static boolean hasGroupLifeClaimRegistrationProcessorRole(Collection<? extends GrantedAuthority> authorities) {
        long count = hasRole(GROUP_LIFE_CLAIM_REGISTRATION_PROCESSOR_ROLE, authorities);
        return count == 1;
    }
    public static boolean hasGroupLifeClaimApproverRole(Collection<? extends GrantedAuthority> authorities) {
        long count = hasRole(GROUP_LIFE_CLAIM_APPROVER_ROLE, authorities);
        return count == 1;
    }

    public static boolean hasGroupHealthPreAuthorizationProcessorRole(Collection<? extends GrantedAuthority> authorities) {
        long count = hasRole(GROUP_HEALTH_PRE_AUTHORIZATION_PROCESSOR_ROLE, authorities);
        return count == 1;
    }

    public static boolean hasGroupHealthPreAuthorizationUnderWriterRole(Collection<? extends GrantedAuthority> authorities) {
        long count = hasRole(GROUP_HEALTH_PRE_AUTHORIZATION_UNDERWRITER_ROLE, authorities);
        return count == 1;
    }

    public static boolean hasGroupHealthCashlessClaimProcessorRole(Collection<? extends GrantedAuthority> authorities) {
        long count = hasRole(GROUP_HEALTH_CASHLESS_CLAIM_PROCESSOR_ROLE, authorities);
        return count == 1;
    }

    public static boolean hasGroupHealthCashlessClaimUnderWriterRole(Collection<? extends GrantedAuthority> authorities) {
        long count = hasRole(GROUP_HEALTH_CASHLESS_CLAIM_UNDERWRITER_ROLE, authorities);
        return count == 1;
    }

    private static long hasRole(final String role, final Collection<? extends GrantedAuthority> authorities) {
        long count = authorities.stream().filter(new Predicate<GrantedAuthority>() {
            @Override
            public boolean test(GrantedAuthority grantedAuthority) {
                return role.equals(grantedAuthority.getAuthority());
            }
        }).count();
        return count;
    }
}
