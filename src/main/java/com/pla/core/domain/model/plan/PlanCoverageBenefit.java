package com.pla.core.domain.model.plan;

import com.google.common.base.Preconditions;
import com.pla.core.domain.model.BenefitId;
import com.pla.core.domain.model.CoverageId;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;
import java.math.BigDecimal;

/**
 * @author: pradyumna
 * @since 1.0 13/03/2015
 */
@ToString
@Getter(AccessLevel.PACKAGE)
@EqualsAndHashCode(of = {"coverageId", "benefitId"})
@Embeddable
class PlanCoverageBenefit {

    @Transient
    private BenefitId benefitId;

    @Column(name = "benefit_id")
    private String benefitDBId;

    private CoverageBenefitDefinition definedPer;
    private CoverageBenefitType coverageBenefitType;
    private BigDecimal benefitLimit;
    private BigDecimal maxLimit;

    protected PlanCoverageBenefit() {
    }

    PlanCoverageBenefit(CoverageId coverageId,
                        BenefitId benefitId, CoverageBenefitDefinition definedPer,
                        CoverageBenefitType coverageBenefitType,
                        BigDecimal benefitLimit, BigDecimal maxLimit) {

        Preconditions.checkArgument(coverageId != null, "Expected coverageId!=null, but %s!=null", coverageId);
        Preconditions.checkArgument(benefitId != null, "Expected benefitId!=null, but %s!=null", benefitId);
        Preconditions.checkArgument(definedPer != null, "Expected definedPer!=null, but %s!=null.", definedPer);
        Preconditions.checkArgument(coverageBenefitType != null, "Expected coverageBenefitType!=null, but %s!=null.", coverageBenefitType);
        Preconditions.checkArgument(benefitLimit != null, "Expected limit!=null, but %s!=null.", benefitLimit);
        this.benefitId = benefitId;
        this.definedPer = definedPer;
        this.coverageBenefitType = coverageBenefitType;
        this.benefitLimit = benefitLimit;
        this.maxLimit = maxLimit;
        this.benefitDBId = benefitId.toString();
    }

    BenefitId getBenefitId() {
        return new BenefitId(this.benefitDBId);
    }
}
