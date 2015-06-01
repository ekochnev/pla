package com.pla.grouplife.quotation.domain.model;

import com.pla.core.domain.model.agent.AgentId;
import com.pla.grouplife.quotation.domain.event.ProposerAddedEvent;
import com.pla.grouplife.quotation.domain.event.GLQuotationClosedEvent;
import com.pla.grouplife.quotation.domain.event.GLQuotationGeneratedEvent;
import com.pla.sharedkernel.identifier.QuotationId;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.axonframework.domain.AbstractAggregateRoot;
import org.axonframework.eventsourcing.annotation.AggregateIdentifier;
import org.joda.time.LocalDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Set;
import java.util.function.ToIntFunction;

import static com.google.common.base.Preconditions.checkArgument;
import static com.pla.grouplife.quotation.domain.exception.QuotationException.raiseQuotationNotModifiableException;
import static org.nthdimenzion.utils.UtilValidator.isNotEmpty;

/**
 * Created by Samir on 4/7/2015.
 */
@Document(collection = "group_life_quotation")
@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class GroupLifeQuotation extends AbstractAggregateRoot<QuotationId> implements IQuotation {

    @Id
    @AggregateIdentifier
    private QuotationId quotationId;

    private String quotationCreator;

    private AgentId agentId;

    private Proposer proposer;

    private Set<Insured> insureds;

    private QuotationStatus quotationStatus;

    private int versionNumber=1;

    private String quotationNumber;

    private LocalDate generatedOn;

    private QuotationId parentQuotationId;

    private PremiumDetail premiumDetail;

    private GroupLifeQuotation(String quotationCreator, String quotationNumber, QuotationId quotationId, AgentId agentId, Proposer proposer, QuotationStatus quotationStatus, int versionNumber) {
        checkArgument(isNotEmpty(quotationCreator));
        checkArgument(isNotEmpty(quotationNumber));
        checkArgument(quotationId != null);
        checkArgument(agentId != null);
        checkArgument(proposer != null);
        checkArgument(QuotationStatus.DRAFT.equals(quotationStatus));
        this.quotationCreator = quotationCreator;
        this.quotationId = quotationId;
        this.agentId = agentId;
        this.proposer = proposer;
        this.quotationStatus = quotationStatus;
        this.versionNumber = versionNumber;
        this.quotationNumber = quotationNumber;
    }

    private GroupLifeQuotation(String quotationNumber, String quotationCreator, QuotationId quotationId, int versionNumber, QuotationStatus quotationStatus) {
        checkArgument(isNotEmpty(quotationNumber));
        checkArgument(isNotEmpty(quotationCreator));
        checkArgument(quotationId != null);
        checkArgument(QuotationStatus.DRAFT.equals(quotationStatus));
        this.quotationNumber = quotationNumber;
        this.quotationCreator = quotationCreator;
        this.quotationId = quotationId;
        this.versionNumber = versionNumber;
        this.quotationStatus = quotationStatus;
    }

    public static GroupLifeQuotation createWithAgentAndProposerDetail(String quotationNumber, String quotationCreator, QuotationId quotationId, AgentId agentId, Proposer proposer) {
        return new GroupLifeQuotation(quotationCreator, quotationNumber, quotationId, agentId, proposer, QuotationStatus.DRAFT, 0);
    }

    public GroupLifeQuotation updateWithPremiumDetail(PremiumDetail premiumDetail) {
        checkInvariant();
        this.premiumDetail = premiumDetail;
        return this;
    }

    public GroupLifeQuotation updateWithAgent(AgentId agentId) {
        checkInvariant();
        this.agentId = agentId;
        return this;
    }

    public GroupLifeQuotation updateWithProposer(Proposer proposer) {
        checkInvariant();
        this.proposer = proposer;
        return this;
    }

    public GroupLifeQuotation updateWithInsured(Set<Insured> insureds) {
        checkInvariant();
        this.insureds = insureds;
        return this;
    }

    @Override
    public QuotationId getIdentifier() {
        return quotationId;
    }

    @Override
    public void closeQuotation() {
        this.quotationStatus = QuotationStatus.CLOSED;
        registerEvent(new GLQuotationClosedEvent(quotationId));
    }

    @Override
    public void purgeQuotation() {
        this.quotationStatus = QuotationStatus.PURGED;
    }

    @Override
    public void declineQuotation() {
        this.quotationStatus = QuotationStatus.DECLINED;
    }

    @Override
    public void generateQuotation(LocalDate generatedOn) {
        this.quotationStatus = QuotationStatus.GENERATED;
        this.generatedOn = generatedOn;
        if (proposer != null && proposer.getContactDetail() != null) {
            ProposerContactDetail proposerContactDetail = proposer.getContactDetail();
            registerEvent(new ProposerAddedEvent(proposer.getProposerName(), proposer.getProposerCode(),
                    proposerContactDetail.getAddressLine1(), proposerContactDetail.getAddressLine2(), proposerContactDetail.getPostalCode(),
                    proposerContactDetail.getProvince(), proposerContactDetail.getTown(), proposerContactDetail.getEmailAddress()));
            registerEvent(new GLQuotationGeneratedEvent(quotationId));
        }
    }

    @Override
    public boolean requireVersioning() {
        return QuotationStatus.GENERATED.equals(this.quotationStatus);
    }

    public GroupLifeQuotation cloneQuotation(String quotationNumber, String quotationCreator, QuotationId quotationId) {
        GroupLifeQuotation newQuotation = new GroupLifeQuotation(quotationNumber, quotationCreator, quotationId, this.versionNumber + 1, QuotationStatus.DRAFT);
        newQuotation.parentQuotationId = this.quotationId;
        newQuotation.proposer = this.proposer;
        newQuotation.agentId = this.agentId;
        newQuotation.insureds = this.insureds;
        newQuotation.premiumDetail = this.premiumDetail;
        return newQuotation;
    }

    private void checkInvariant() {
        if (QuotationStatus.CLOSED.equals(this.quotationStatus) || QuotationStatus.DECLINED.equals(this.quotationStatus)) {
            raiseQuotationNotModifiableException();
        }
    }

    public BigDecimal getNetAnnualPremiumPaymentAmount(PremiumDetail premiumDetail, BigDecimal totalInsuredPremiumAmount) {
        if (premiumDetail.getPolicyTermValue() != 365) {
            totalInsuredPremiumAmount = premiumDetail.getPremiumInstallment().getInstallmentAmount();
        } else {
            Policy annualPolicy = premiumDetail.getAnnualPolicy();
            totalInsuredPremiumAmount = annualPolicy != null ? annualPolicy.getPremium() : totalInsuredPremiumAmount;
        }
        BigDecimal addOnBenefitAmount = premiumDetail.getAddOnBenefit() != null ? totalInsuredPremiumAmount.multiply((premiumDetail.getAddOnBenefit().divide(new BigDecimal(100))).setScale(2, BigDecimal.ROUND_CEILING)) : BigDecimal.ZERO;
        BigDecimal profitAndSolvencyAmount = premiumDetail.getProfitAndSolvency() != null ? totalInsuredPremiumAmount.multiply((premiumDetail.getProfitAndSolvency().divide(new BigDecimal(100))).setScale(2, BigDecimal.ROUND_CEILING)) : BigDecimal.ZERO;
        BigDecimal discountAmount = premiumDetail.getDiscount() != null ? totalInsuredPremiumAmount.multiply((premiumDetail.getDiscount().divide(new BigDecimal(100))).setScale(2, BigDecimal.ROUND_CEILING)) : BigDecimal.ZERO;
        BigDecimal netPremiumAmount = (totalInsuredPremiumAmount.add(addOnBenefitAmount).add(profitAndSolvencyAmount)).subtract(discountAmount);
        return netPremiumAmount;
    }

    public Integer getTotalNoOfLifeCovered() {
        Integer totalNoOfLifeCovered = insureds.size();
        Integer dependentSize = insureds.stream().mapToInt(new ToIntFunction<Insured>() {
            @Override
            public int applyAsInt(Insured value) {
                return isNotEmpty(value.getInsuredDependents()) ? value.getInsuredDependents().size() : 0;
            }
        }).sum();
        totalNoOfLifeCovered = totalNoOfLifeCovered + dependentSize;
        return totalNoOfLifeCovered;
    }

    public BigDecimal getTotalSumAssured() {
        BigDecimal totalSumAssured = BigDecimal.ZERO;
        if (isNotEmpty(insureds)) {
            for (Insured insured : insureds) {
                PlanPremiumDetail insuredPlanPremiumDetail = insured.getPlanPremiumDetail();
                totalSumAssured = totalSumAssured.add(insuredPlanPremiumDetail.getSumAssured());
                if (isNotEmpty(insured.getInsuredDependents())) {
                    for (InsuredDependent insuredDependent : insured.getInsuredDependents()) {
                        totalSumAssured = totalSumAssured.add(insuredDependent.getPlanPremiumDetail().getSumAssured());
                    }
                }
            }
        }
        return totalSumAssured;
    }

    public BigDecimal getTotalBasicPremiumForInsured() {
        BigDecimal totalBasicAnnualPremium = BigDecimal.ZERO;
        for (Insured insured : this.insureds) {
            totalBasicAnnualPremium = totalBasicAnnualPremium.add(insured.getBasicAnnualPremium());
        }
        return totalBasicAnnualPremium;
    }
}