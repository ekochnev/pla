package com.pla.publishedlanguage.contract;

import org.springframework.security.core.Authentication;

/**
 * Author - Mohan Sharma Created on 1/27/2016.
 */
public interface IAuthenticationFacade {
    Authentication getAuthentication();
}