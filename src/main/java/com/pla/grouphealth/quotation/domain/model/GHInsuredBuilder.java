package com.pla.grouphealth.quotation.domain.model;

import com.pla.sharedkernel.domain.model.Gender;
import com.pla.sharedkernel.identifier.CoverageId;
import com.pla.sharedkernel.identifier.PlanId;
import lombok.Getter;
import org.joda.time.LocalDate;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static org.nthdimenzion.utils.UtilValidator.isEmpty;
import static org.nthdimenzion.utils.UtilValidator.isNotEmpty;

/**
 * Created by Samir on 4/7/2015.
 */
@Getter
public class GHInsuredBuilder {

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

    private Set<GHInsuredDependent> insuredDependents;

    private GHPlanPremiumDetail planPremiumDetail;

    private String occupation;

    private Set< CoveragePremiumDetail> coveragePremiumDetails;

    GHInsuredBuilder(PlanId insuredPlan, String planCode, BigDecimal premiumAmount, BigDecimal sumAssured) {
        checkArgument(insuredPlan != null);
        checkArgument(isNotEmpty(planCode));
        checkArgument(premiumAmount != null);
         GHPlanPremiumDetail planPremiumDetail = new GHPlanPremiumDetail(insuredPlan, planCode, premiumAmount, sumAssured);
        this.planPremiumDetail = planPremiumDetail;
    }

    public GHInsuredBuilder withInsuredName(String salutation, String firstName, String lastName) {
        this.salutation = salutation;
        this.firstName = firstName;
        this.lastName = lastName;
        return this;
    }

    public GHInsuredBuilder withInsuredNrcNumber(String nrcNumber) {
        this.nrcNumber = nrcNumber;
        return this;
    }

    public GHInsuredBuilder withCompanyName(String companyName) {
        this.companyName = companyName;
        return this;
    }

    public GHInsuredBuilder withManNumber(String manNumber) {
        this.manNumber = manNumber;
        return this;
    }

    public GHInsuredBuilder withDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        return this;
    }

    public GHInsuredBuilder withGender(Gender gender) {
        this.gender = gender;
        return this;
    }

    public GHInsuredBuilder withCategory(String category) {
        this.category = category;
        return this;
    }

    public GHInsuredBuilder withDependents(Set<GHInsuredDependent> insuredDependents) {
        this.insuredDependents = insuredDependents;
        return this;
    }

    public GHInsuredBuilder withAnnualIncome(BigDecimal annualIncome) {
        this.annualIncome = annualIncome;
        return this;
    }

    public GHInsuredBuilder withOccupation(String occupation) {
        this.occupation = occupation;
        return this;
    }

    public GHInsuredBuilder withCoveragePremiumDetail(String coverageName, String coverageCode, String coverageId, BigDecimal premium) {
         CoveragePremiumDetail coveragePremiumDetail = new  CoveragePremiumDetail(coverageName, coverageCode, new CoverageId(coverageId), premium);
        if (isEmpty(this.coveragePremiumDetails)) {
            this.coveragePremiumDetails = new HashSet<>();
        }
        this.coveragePremiumDetails.add(coveragePremiumDetail);
        return this;
    }

    public GHInsured build() {
        return new GHInsured(this);
    }
}
