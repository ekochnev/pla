package com.pla.grouphealth.claim.cashless.domain.model.claim;

import com.google.common.collect.Sets;
import com.pla.grouphealth.claim.cashless.domain.model.preauthorization.PreAuthorizationRequestCoverageDetail;
import lombok.*;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.nthdimenzion.utils.UtilValidator.isNotEmpty;

/**
 * Author - Mohan Sharma Created on 1/11/2016.
 */
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class GroupHealthCashlessClaimCoverageDetail {
    private String coverageId;
    private String coverageCode;
    private String coverageName;
    private BigDecimal sumAssured;
    private Set<GroupHealthCashlessClaimBenefitDetail> benefitDetails;
    private BigDecimal totalAmountPaid;
    private BigDecimal balanceAmount;
    private BigDecimal reserveAmount;
    private BigDecimal deductibleAmount;
    private BigDecimal deductiblePercentage;
    private String deductibleType;

    public GroupHealthCashlessClaimCoverageDetail updateWithCoverageName(String coverageName) {
        this.coverageName = coverageName;
        return this;
    }

    public GroupHealthCashlessClaimCoverageDetail updateWithCoverageId(String coverageId) {
        this.coverageId = coverageId;
        return this;
    }

    public GroupHealthCashlessClaimCoverageDetail updateWithCoverageCode(String coverageCode) {
        this.coverageCode = coverageCode;
        return this;
    }

    public GroupHealthCashlessClaimCoverageDetail updateWithSumAssured(BigDecimal sumAssured) {
        this.sumAssured = sumAssured;
        return this;
    }

    public GroupHealthCashlessClaimCoverageDetail updateWithTotalAmountPaid(BigDecimal totalAmountPaid) {
        this.totalAmountPaid = totalAmountPaid;
        return this;
    }

    public GroupHealthCashlessClaimCoverageDetail updateWithReserveAmount(BigDecimal reservedAmount) {
        this.reserveAmount = reservedAmount;
        return this;
    }

    public GroupHealthCashlessClaimCoverageDetail updateWithBalanceAmount() {
        BigDecimal sumAssured = this.sumAssured;
        BigDecimal totalAmountPaid = this.totalAmountPaid;
        BigDecimal reservedAmount = this.reserveAmount;
        if(sumAssured.compareTo(totalAmountPaid) == 1){
            BigDecimal balanceAmount = sumAssured.subtract(totalAmountPaid);
            this.balanceAmount = balanceAmount;
            //this.eligibleAmount = balanceAmount;

           /* if(balanceAmount.compareTo(deductibleAmount) == 1) {
                this.eligibleAmount = deductibleAmount;
            }
            if(balanceAmount.compareTo(deductibleAmount) == -1){
                this.eligibleAmount = balanceAmount;
            }*/
        }
        if(sumAssured.compareTo(totalAmountPaid) == 0 || sumAssured.compareTo(totalAmountPaid) == -1){
            this.balanceAmount = BigDecimal.ZERO;
            //this.eligibleAmount = BigDecimal.ZERO;
        }
        return this;
    }

    public GroupHealthCashlessClaimCoverageDetail updateWithEligibleAmount() {
        BigDecimal totalProbableClaimAmount = getTotalProbableClaimAmount();
        if(isNotEmpty(this.deductibleType) && this.deductibleType.equals("PERCENTAGE")){
            BigDecimal deductibleAmount = getPercentageValue(totalProbableClaimAmount, this.deductiblePercentage);
            this.deductibleAmount = deductibleAmount;
        }
        this.benefitDetails.stream().map(benefit -> benefit.updateWithEligibleAmount(this.balanceAmount)).collect(Collectors.toList());
        return this;
    }

    public BigDecimal getTotalProbableClaimAmount() {
        BigDecimal totalProbableClaimAmount = BigDecimal.ZERO;
        for(GroupHealthCashlessClaimBenefitDetail benefit : this.getBenefitDetails()){
            totalProbableClaimAmount = totalProbableClaimAmount.add(benefit.getProbableClaimAmount());
        }
        return totalProbableClaimAmount;
    }


    public BigDecimal getSumOfTotalApprovedAmount() {
        BigDecimal totalAmountPaidWithoutCurrentApproveAmount = BigDecimal.ZERO;
        for(GroupHealthCashlessClaimBenefitDetail benefit : this.getBenefitDetails()){
            if(isNotEmpty(benefit.getApprovedAmount())){
                totalAmountPaidWithoutCurrentApproveAmount = totalAmountPaidWithoutCurrentApproveAmount.add(benefit.getApprovedAmount());
            }
        }
        return totalAmountPaidWithoutCurrentApproveAmount;
    }

    public GroupHealthCashlessClaimCoverageDetail updateWithBenefitDetails(Set<GroupHealthCashlessClaimBenefitDetail> benefitDetails) {
        this.benefitDetails = benefitDetails;
        return this;
    }

    public GroupHealthCashlessClaimCoverageDetail updateWithProbableClaimAmount(String coverageId, Set<GroupHealthCashlessClaimBenefitDetail> benefitDetails, List<Map<String, Object>> finalRefurbishedList) {
        if(isNotEmpty(benefitDetails)){
            benefitDetails.stream().forEach(benefitDto -> {
                BigDecimal probableClaimAmount = getProbableClaimAmount(benefitDto.getBenefitCode(), coverageId, finalRefurbishedList);
                benefitDto.setProbableClaimAmount(probableClaimAmount);
            });
        }
        return this;
    }

    public BigDecimal getPercentageValue(BigDecimal base, BigDecimal pct){
        if(isNotEmpty(base))
            return base.multiply(pct).divide(new BigDecimal(100));
        return BigDecimal.ZERO;
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

    public GroupHealthCashlessClaimCoverageDetail updateWithDeductibleAmount(BigDecimal deductibleAmount) {
        this.deductibleAmount = deductibleAmount;
        return this;
    }

    public GroupHealthCashlessClaimCoverageDetail updateWithDeductiblePercentage(BigDecimal deductiblePercentage) {
        this.deductiblePercentage = deductiblePercentage;
        return this;
    }

    public GroupHealthCashlessClaimCoverageDetail updateWithDeductibleType(String deductibleType) {
        this.deductibleType = deductibleType;
        return this;
    }
}
