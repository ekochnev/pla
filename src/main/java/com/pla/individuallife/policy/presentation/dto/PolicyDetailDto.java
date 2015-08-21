package com.pla.individuallife.policy.presentation.dto;

import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;

/**
 * Created by Admin on 8/4/2015.
 */
@Getter
@Setter
public class PolicyDetailDto {

    private String policyId;

    private String policyHolderName;

    private DateTime inceptionDate;

    private DateTime expiryDate;

    private String policyNumber;

    private String status;
}
