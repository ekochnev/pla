package com.pla.grouphealth.quotation.domain.service;

import com.google.common.collect.Lists;
import com.pla.grouphealth.quotation.domain.model.GHQuotationProcessor;
import com.pla.sharedkernel.util.RolesUtil;
import org.junit.Before;
import org.junit.Test;
import org.nthdimenzion.security.service.UserLoginDetailDto;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.Assert.assertNotNull;

/**
 * Created by Samir on 4/25/2015.
 */
public class QuotationRoleAdapterTest {

    UserLoginDetailDto userLoginDetailDto;

    @Before
    public void setUp() {
        userLoginDetailDto = UserLoginDetailDto.createUserLoginDetailDto("", "");
    }

    @Test
    public void shouldReturnQuotationProcessorWhenUserHasQuotationProcessorRole() {
        SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(RolesUtil.GROUP_HEALTH_QUOTATION_PROCESSOR_ROLE);
        List<SimpleGrantedAuthority> authorities = Lists.newArrayList();
        authorities.add(simpleGrantedAuthority);
        userLoginDetailDto.setAuthorities(authorities);
        GHQuotationRoleAdapter quotationRoleAdapter = new GHQuotationRoleAdapter();
        GHQuotationProcessor ghQuotationProcessor = quotationRoleAdapter.userToQuotationProcessor(userLoginDetailDto);
        assertNotNull(ghQuotationProcessor);
    }

    @Test(expected = AuthorizationServiceException.class)
    public void shouldReturnAuthorizationExceptionUserHasNotAdminRole() {
        UserLoginDetailDto userLoginDetailDto = UserLoginDetailDto.createUserLoginDetailDto("", "");
        SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority("ROLE_USER");
        List<SimpleGrantedAuthority> authorities = Lists.newArrayList();
        authorities.add(simpleGrantedAuthority);
        userLoginDetailDto.setAuthorities(authorities);
        GHQuotationRoleAdapter quotationRoleAdapter = new GHQuotationRoleAdapter();
        quotationRoleAdapter.userToQuotationProcessor(userLoginDetailDto);
    }
}
