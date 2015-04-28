package com.pla.core.dto;

import com.pla.sharedkernel.identifier.LineOfBusinessId;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Created by Admin on 4/1/2015.
 */
@Getter
@Setter
@ToString
public class GeneralInformationDto {

    private String productLineInformationId;
    private LineOfBusinessId productLine;
    private List<ProductLineProcessItemDto> quotationProcessItems;
    private List<ProductLineProcessItemDto> enrollmentProcessItems;
    private List<ProductLineProcessItemDto> reinstatementProcessItems;
    private List<ProductLineProcessItemDto> endorsementProcessItems;
    private List<ProductLineProcessItemDto> claimProcessItems;
    private List<PolicyFeeProcessItemDto> policyFeeProcessItems;
    private List<PolicyProcessMinimumLimitItemDto> policyProcessMinimumLimitItems;
    private List<ProductLineProcessItemDto> surrenderProcessItems;
    private List<ProductLineProcessItemDto> maturityProcessItems;
    private List<PremiumFrequencyFollowUpDto> premiumFollowUpFrequency;
    private String organizationInformationId;
    private List<DiscountFactorInformationDto> discountFactorItems;
    private List<ModalFactorInformationDto> modelFactorItems;
    private ServiceTaxDto serviceTax;

    public GeneralInformationDto() {
    }
}
