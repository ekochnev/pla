package com.pla.grouplife.quotation.application.command;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by Samir on 4/14/2015.
 */
@Getter
@Setter
public class UploadInsuredDetailDto {

    private boolean samePlanForAllRelation;

    private boolean samePlanForAllCategory;

    private MultipartFile file;

    private String quotationId;

    private boolean applyIndustryLoadingFactor;


}
