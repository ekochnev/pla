package com.pla.individuallife.quotation.domain.model;

import com.pla.core.domain.model.agent.AgentId;
import com.pla.grouplife.quotation.domain.model.IQuotation;
import com.pla.sharedkernel.identifier.PlanId;
import com.pla.sharedkernel.identifier.QuotationId;
import lombok.Getter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;

import javax.persistence.*;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.pla.individuallife.quotation.domain.exception.QuotationException.raiseQuotationNotModifiableException;
import static org.nthdimenzion.utils.UtilValidator.isNotEmpty;

/**
 * Created by Karunakar on 5/13/2015.
 */
@Entity
@Table(name = "individual_life_quotation")
@Getter
public class ILQuotation implements IQuotation {


    @EmbeddedId
    private QuotationId quotationId;

    private String quotationCreator;

    @Embedded
    private AgentId agentId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "firstName", column = @Column(name = "proposedFirstName")),
            @AttributeOverride(name = "surname", column = @Column(name = "proposerSurname")),
            @AttributeOverride(name = "nrcNumber", column = @Column(name = "proposerNrcNumber")),
            @AttributeOverride(name = "mobileNumber", column = @Column(name = "proposerMobileNumber")),
            @AttributeOverride(name = "emailAddress", column = @Column(name = "proposerEmailAddress")),
            @AttributeOverride(name = "gender", column = @Column(name = "proposerGender")),
            @AttributeOverride(name = "title", column = @Column(name = "proposerTitle")),
            @AttributeOverride(name = "dateOfBirth", column = @Column(name = "proposerDateOfBirth")),
    })
    private Proposer proposer;

    @Embedded
    private ProposedAssured proposedAssured;

    @Enumerated(EnumType.STRING)
    private ILQuotationStatus ilQuotationStatus;

    private int versionNumber;

    private String quotationNumber;

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
    private LocalDate generatedOn;

    @Column(name = "parent_quotation_id")
    private String parentQuotationId;

    @Embedded
    private PlanDetail planDetail;

    @Column( columnDefinition = "BOOLEAN DEFAULT false" )
    private boolean isAssuredTheProposer;

    @Cascade(CascadeType.ALL)
    @ElementCollection
    @CollectionTable(name = "individual_life_quotation_rider", joinColumns = @JoinColumn(name = "quotation_id"))
    private Set<RiderDetail> riderDetails;

    private ILQuotation() {
    }

    public ILQuotation(ILQuotationProcessor quotationCreator, String quotationARId, String quotationNumber, QuotationId quotationId, AgentId agentId, ProposedAssured proposedAssured, PlanId planId, ILQuotationStatus ilQuotationStatus, int versionNumber) {
        checkArgument(quotationCreator != null);
        checkArgument(isNotEmpty(quotationNumber));
        checkArgument(quotationId != null);
        checkArgument(agentId != null);
        checkArgument(proposedAssured != null);
        checkArgument(proposedAssured.getFirstName() != null);
        checkArgument(proposedAssured.getSurname() != null);
        checkArgument(proposedAssured.getTitle() != null);
        checkArgument(proposedAssured.getNrcNumber() != null);
        checkArgument(planId != null);
        checkArgument(ILQuotationStatus.DRAFT.equals(ilQuotationStatus));
        this.quotationCreator = quotationCreator.getUserName();
        this.quotationId = quotationId;
        this.agentId = agentId;
        this.proposedAssured = proposedAssured;
        this.ilQuotationStatus = ilQuotationStatus;
        this.versionNumber = versionNumber;
        this.quotationNumber = quotationNumber;
        this.parentQuotationId = quotationARId;
        this.planDetail = new PlanDetail(planId, null, null, null);
    }

    private ILQuotation(ILQuotationProcessor quotationCreator, String quotationNumber, QuotationId quotationId) {

    }

    public static ILQuotation createWithBasicDetail(ILQuotationProcessor quotationCreator, String quotationARId, String quotationNumber, QuotationId quotationId, AgentId agentId, ProposedAssured proposedAssured, PlanId planid) {
        return new ILQuotation(quotationCreator, quotationARId, quotationNumber, quotationId, agentId, proposedAssured, planid, ILQuotationStatus.DRAFT, 0);
    }

    public void updateWithProposer(ILQuotationProcessor quotationProcessor, Proposer proposer) {
        checkArgument(quotationProcessor != null, " The user need to be an Quotation Processor.");
        checkInvariant();
        this.proposer = proposer;
    }

    public void updateWithAssured(ILQuotationProcessor quotationProcessor, ProposedAssured proposedAssured, boolean isAssuredTheProposer) {
        checkArgument(quotationProcessor != null, " The user need to be an Quotation Processor.");
        checkInvariant();
        this.proposedAssured = proposedAssured;
        this.isAssuredTheProposer = isAssuredTheProposer;

        if (isAssuredTheProposer) {
            this.proposer = convertAssuredToProposer(proposedAssured);
        }
    }

    public ILQuotation updateWithPlan(ILQuotationProcessor quotationProcessor, PlanDetail planDetail, Set<RiderDetail> riders) {
        checkInvariant();
        this.planDetail = planDetail;
        if (riders != null)
            this.riderDetails = riders;
        return this;
    }

    @Override
    public void closeQuotation() {
        this.ilQuotationStatus = ILQuotationStatus.CLOSED;
    }

    @Override
    public void purgeQuotation() {
        this.ilQuotationStatus = ILQuotationStatus.PURGED;
    }

    @Override
    public void declineQuotation() {
        this.ilQuotationStatus = ILQuotationStatus.DECLINED;
    }

    @Override
    public void generateQuotation(LocalDate generatedOn) {
        this.ilQuotationStatus = ILQuotationStatus.GENERATED;
        this.generatedOn = generatedOn;
    }

    @Override
    public boolean requireVersioning() {
        return ILQuotationStatus.GENERATED.equals(this.ilQuotationStatus);
    }

    public ILQuotation cloneQuotation(ILQuotationProcessor quotationCreator, QuotationId quotationId, String quotationNumber,
                                      int versionNumber) {
        checkArgument(isNotEmpty(quotationNumber));
        checkArgument(quotationCreator != null, "User is does not have Quotation Preprocessor Role.");
        checkArgument(quotationId != null);

        ILQuotation newQuotation = new ILQuotation();
        newQuotation.quotationCreator = quotationCreator.getUserName();
        newQuotation.quotationId = quotationId;
        newQuotation.versionNumber = versionNumber;
        newQuotation.ilQuotationStatus = ILQuotationStatus.DRAFT;
        newQuotation.quotationNumber = quotationNumber;
        newQuotation.parentQuotationId = this.parentQuotationId;
        newQuotation.proposer = this.proposer;
        newQuotation.agentId = this.agentId;
        newQuotation.proposedAssured = this.proposedAssured;
        newQuotation.planDetail = this.planDetail;
        return newQuotation;
    }

    private void checkInvariant() {
        if (ILQuotationStatus.CLOSED.equals(this.ilQuotationStatus) || ILQuotationStatus.DECLINED.equals(this.ilQuotationStatus)) {
            raiseQuotationNotModifiableException();
        }
    }

    private Proposer convertAssuredToProposer(ProposedAssured proposedAssured) {
        Proposer proposer = new Proposer(proposedAssured.getTitle(), proposedAssured.getFirstName(), proposedAssured.getSurname(), proposedAssured.getNrcNumber(), proposedAssured.getDateOfBirth(), proposedAssured.getGender(), proposedAssured.getMobileNumber(), proposedAssured.getEmailAddress());
        return proposer;
    }

}