package com.pla.grouphealth.claim.cashless.presentation.dto;

import com.google.common.collect.Sets;
import com.pla.core.domain.model.plan.Plan;
import com.pla.core.domain.model.plan.PlanCoverage;
import com.pla.core.domain.model.plan.PlanCoverageBenefit;
import com.pla.grouphealth.claim.cashless.domain.model.PreAuthorizationRequestBenefitDetail;
import com.pla.grouphealth.sharedresource.model.vo.BenefitPremiumLimit;
import com.pla.sharedkernel.domain.model.CoverageBenefitDefinition;
import lombok.*;
import org.nthdimenzion.utils.UtilValidator;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.nthdimenzion.utils.UtilValidator.*;

/**
 * Created by Mohan Sharma on 1/18/2016.
 */
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class CoverageBenefitDetailDto {
    private String coverageName;
    private String coverageCode;
    private String coverageId;
    BigDecimal sumAssured;
    private Set<BenefitDetailDto> benefitDetails;
    private BigDecimal totalAmountPaid;
    private BigDecimal balanceAmount;
    private BigDecimal reserveAmount;
    private BigDecimal eligibleAmount;
    private BigDecimal approvedAmount;

    @Getter
    @Setter
    @EqualsAndHashCode
    @NoArgsConstructor
    public class BenefitDetailDto {
        private String benefitName;
        private String benefitCode;
        private BigDecimal probableClaimAmount;
    }

    public CoverageBenefitDetailDto updateWithCoverageName(String coverageName){
        this.coverageName = coverageName;
        return this;
    }

    public CoverageBenefitDetailDto updateWithCoverageCode(String coverageCode){
        this.coverageCode = coverageCode;
        return this;
    }

    public CoverageBenefitDetailDto updateWithSumAssured(BigDecimal sumAssured){
        this.sumAssured = sumAssured;
        return this;
    }

    public CoverageBenefitDetailDto updateWithBenefitDetails(Set<BenefitDetailDto> benefitDetails){
        this.benefitDetails = benefitDetails;
        return this;
    }

    public CoverageBenefitDetailDto updateWithApprovedAmount(BigDecimal approvedAmount){
        this.approvedAmount = approvedAmount;
        return this;
    }

    public CoverageBenefitDetailDto updateWithTotalAmountPaid(BigDecimal totalAmountPaid) {
        this.totalAmountPaid = totalAmountPaid;
        return this;
    }

    public CoverageBenefitDetailDto updateWithReserveAmount(BigDecimal reserveAmount) {
        this.reserveAmount = reserveAmount;
        return this;
    }

    public CoverageBenefitDetailDto updateWithCoverageId(String coverageId) {
        this.coverageId = coverageId;
        return this;
    }

    public CoverageBenefitDetailDto updateWithProbableClaimAmount(String coverageId, Set<BenefitDetailDto> benefitDetails, List<Map<String, Object>> finalRefurbishedList) {
        if(isNotEmpty(benefitDetails)){
            benefitDetails.stream().forEach(benefitDto -> {
                BigDecimal probableClaimAmount = getProbableClaimAmount(benefitDto.getBenefitCode(), coverageId, finalRefurbishedList);
                benefitDto.setProbableClaimAmount(probableClaimAmount);
            });
        }
        return this;
    }

    private BigDecimal getProbableClaimAmount(String benefitCode, String coverageId, List<Map<String, Object>> finalRefurbishedList) {
        BigDecimal probableClaimAmount = BigDecimal.ZERO;
        for(Map<String, Object> map : finalRefurbishedList) {
            String coverageIdFromMap = map.get("coverageId").toString();
            String benefitCodeIdFromMap = map.get("benefitCode").toString();
            if (benefitCode.equalsIgnoreCase(benefitCodeIdFromMap) && coverageId.equalsIgnoreCase(coverageIdFromMap)) {
                probableClaimAmount = (BigDecimal)map.get("payableAmount");
            }
        }
        return probableClaimAmount;
    }

    public CoverageBenefitDetailDto updateWithBalanceAndEligibleAmount() {
        BigDecimal sumAssured = this.sumAssured;
        BigDecimal totalAmountPaid = this.totalAmountPaid;
        if(sumAssured.compareTo(totalAmountPaid) == 1){
            BigDecimal balanceAmount = sumAssured.subtract(totalAmountPaid);
            this.balanceAmount = balanceAmount;
            this.eligibleAmount = balanceAmount;
        }
        if(sumAssured.compareTo(totalAmountPaid) == 0){
            this.balanceAmount = BigDecimal.ZERO;
            this.eligibleAmount = BigDecimal.ZERO;
        }
        return this;
    }
}