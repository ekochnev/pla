package com.pla.grouphealth.quotation.domain.model;

import com.pla.sharedkernel.domain.model.Gender;
import com.pla.sharedkernel.identifier.PlanId;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.joda.time.LocalDate;
import org.nthdimenzion.ddd.domain.annotations.ValueObject;

import java.math.BigDecimal;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static org.nthdimenzion.utils.UtilValidator.isNotEmpty;

/**
 * Created by Samir on 4/7/2015.
 */
@ValueObject
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
@Setter(value = AccessLevel.PACKAGE)
public class GHInsured {

    private String companyName;

    private String manNumber;

    private String nrcNumber;

    private String salutation;

    private String firstName;

    private String lastName;

    private LocalDate dateOfBirth;

    private Gender gender;

    private String category;

    private BigDecimal annualIncome;

    private String occupationClass;

    private String occupationCategory;

    private Integer noOfAssured;

    private Set<GHInsuredDependent> insuredDependents;

    private GHPlanPremiumDetail planPremiumDetail;

    private Set<CoveragePremiumDetail> coveragePremiumDetails;

    GHInsured(GHInsuredBuilder insuredBuilder) {
        checkArgument(insuredBuilder != null);
        this.planPremiumDetail = insuredBuilder.getPlanPremiumDetail();
        this.companyName = insuredBuilder.getCompanyName();
        this.manNumber = insuredBuilder.getManNumber();
        this.salutation = insuredBuilder.getSalutation();
        this.nrcNumber = insuredBuilder.getNrcNumber();
        this.firstName = insuredBuilder.getFirstName();
        this.lastName = insuredBuilder.getLastName();
        this.dateOfBirth = insuredBuilder.getDateOfBirth();
        this.gender = insuredBuilder.getGender();
        this.category = insuredBuilder.getCategory();
        this.insuredDependents = insuredBuilder.getInsuredDependents();
        this.annualIncome = insuredBuilder.getAnnualIncome();
        this.occupationClass = insuredBuilder.getOccupation();
        this.coveragePremiumDetails = insuredBuilder.getCoveragePremiumDetails();

    }

    public static GHInsuredBuilder getInsuredBuilder(PlanId planId, String planCode, BigDecimal premiumAmount, BigDecimal sumAssured) {
        return new GHInsuredBuilder(planId, planCode, premiumAmount, sumAssured);
    }

    public GHInsured updatePlanPremiumAmount(BigDecimal premiumAmount) {
        this.planPremiumDetail = this.planPremiumDetail.updatePremiumAmount(premiumAmount);
        return this;
    }

    public BigDecimal getBasicAnnualPremium() {
        BigDecimal basicAnnualPremium = planPremiumDetail.getPremiumAmount();
        if (isNotEmpty(coveragePremiumDetails)) {
            for (CoveragePremiumDetail coveragePremiumDetail : coveragePremiumDetails) {
                basicAnnualPremium = basicAnnualPremium.add(coveragePremiumDetail.getPremium());
            }
        }
        basicAnnualPremium = basicAnnualPremium.add(getBasicAnnualPremiumForDependent());
        return basicAnnualPremium;
    }

    private BigDecimal getBasicAnnualPremiumForDependent() {
        BigDecimal basicAnnualPremiumOfDependent = BigDecimal.ZERO;
        if (isNotEmpty(this.insuredDependents)) {
            for (GHInsuredDependent insuredDependent : this.insuredDependents) {
                 GHPlanPremiumDetail planPremiumDetail = insuredDependent.getPlanPremiumDetail();
                basicAnnualPremiumOfDependent = basicAnnualPremiumOfDependent.add(planPremiumDetail != null ? planPremiumDetail.getPremiumAmount() : BigDecimal.ZERO);
                if (isNotEmpty(insuredDependent.getCoveragePremiumDetails())) {
                    for (CoveragePremiumDetail coveragePremiumDetail : insuredDependent.getCoveragePremiumDetails()) {
                        basicAnnualPremiumOfDependent = basicAnnualPremiumOfDependent.add(coveragePremiumDetail.getPremium());
                    }
                }
            }
        }
        return basicAnnualPremiumOfDependent;
    }
}
