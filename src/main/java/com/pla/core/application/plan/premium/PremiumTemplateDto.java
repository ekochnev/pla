/*
 * Copyright (c) 3/26/15 8:09 PM .NthDimenzion,Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.pla.core.application.plan.premium;

import com.pla.publishedlanguage.domain.model.PremiumInfluencingFactor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

/**
 * @author: Samir
 * @since 1.0 26/03/2015
 */
@Getter
@Setter
@NoArgsConstructor
public class PremiumTemplateDto {

    private String planId;

    private String coverageId;

    @NotNull
    @NotEmpty
    private PremiumInfluencingFactor[] premiumInfluencingFactors;

    private MultipartFile file;

}
