package com.pla.grouphealth.quotation.domain.model;

import com.pla.sharedkernel.domain.model.Gender;
import com.pla.sharedkernel.domain.model.Relationship;
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
 * Created by Samir on 4/8/2015.
 */
@Getter
public class GHInsuredDependentBuilder {

    private String companyName;

    private String manNumber;

    private String nrcNumber;

    private String salutation;

    private String firstName;

    private String lastName;

    private LocalDate dateOfBirth;

    private Gender gender;

    private String category;

    private Relationship relationship;

    private GHPlanPremiumDetail planPremiumDetail;

    private Set<CoveragePremiumDetail> coveragePremiumDetails;

    GHInsuredDependentBuilder(PlanId planId, String planCode, BigDecimal premiumAmount, BigDecimal sumAssured) {
        checkArgument(planId != null);
        checkArgument(isNotEmpty(planCode));
        checkArgument(premiumAmount != null);
         GHPlanPremiumDetail planPremiumDetail = new GHPlanPremiumDetail(planId, planCode, premiumAmount, sumAssured);
        this.planPremiumDetail = planPremiumDetail;
    }

    public GHInsuredDependentBuilder withInsuredName(String salutation, String firstName, String lastName) {
        this.salutation = salutation;
        this.firstName = firstName;
        this.lastName = lastName;
        return this;
    }

    public GHInsuredDependentBuilder withInsuredNrcNumber(String nrcNumber) {
        this.nrcNumber = nrcNumber;
        return this;
    }

    public GHInsuredDependentBuilder withCompanyName(String companyName) {
        this.companyName = companyName;
        return this;
    }

    public GHInsuredDependentBuilder withManNumber(String manNumber) {
        this.manNumber = manNumber;
        return this;
    }

    public GHInsuredDependentBuilder withDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        return this;
    }

    public GHInsuredDependentBuilder withGender(Gender gender) {
        this.gender = gender;
        return this;
    }

    public GHInsuredDependentBuilder withCategory(String category) {
        this.category = category;
        return this;
    }

    public GHInsuredDependentBuilder withRelationship(Relationship relationship) {
        this.relationship = relationship;
        return this;
    }

    public GHInsuredDependentBuilder withCoveragePremiumDetail(String coverageName, String coverageCode, String coverageId, BigDecimal premium) {
        CoveragePremiumDetail coveragePremiumDetail = new CoveragePremiumDetail(coverageName, coverageCode, new CoverageId(coverageId), premium);
        if (isEmpty(this.coveragePremiumDetails)) {
            this.coveragePremiumDetails = new HashSet<>();
        }
        this.coveragePremiumDetails.add(coveragePremiumDetail);
        return this;
    }

    public GHInsuredDependent build() {
        return new GHInsuredDependent(this);
    }
}