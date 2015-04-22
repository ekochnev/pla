package com.pla.quotation.domain.service;

import com.pla.quotation.domain.model.grouplife.GLQuotationProcessor;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import static com.pla.sharedkernel.util.RolesUtil.hasQuotationProcessorRole;

/**
 * Created by Samir on 4/8/2015.
 */
@Component
public class QuotationRoleAdapter {

    public GLQuotationProcessor userToQuotationProcessor(UserDetails userDetails) {

        boolean hasQuotationProcessorRole = hasQuotationProcessorRole(userDetails.getAuthorities());
        if (!hasQuotationProcessorRole) {
            throw new AuthorizationServiceException("User does not have Quotation processor(ROLE_QUOTATION_PROCESSOR) authority");
        }
        return new GLQuotationProcessor(userDetails.getUsername());
    }


}
