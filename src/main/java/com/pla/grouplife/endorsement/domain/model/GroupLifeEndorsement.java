package com.pla.grouplife.endorsement.domain.model;

import com.pla.grouplife.sharedresource.model.GLEndorsementType;
import com.pla.grouplife.sharedresource.model.vo.GLProposerDocument;
import com.pla.sharedkernel.domain.model.EndorsementNumber;
import com.pla.sharedkernel.domain.model.EndorsementStatus;
import com.pla.sharedkernel.domain.model.Policy;
import com.pla.sharedkernel.identifier.EndorsementId;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.axonframework.domain.AbstractAggregateRoot;
import org.axonframework.eventsourcing.annotation.AggregateIdentifier;
import org.joda.time.DateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Created by Samir on 8/3/2015.
 */
@Document(collection = "group_life_endorsement")
@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class GroupLifeEndorsement extends AbstractAggregateRoot<EndorsementId> {

    @Id
    @AggregateIdentifier
    private EndorsementId endorsementId;

    private EndorsementNumber endorsementNumber;

    private GLEndorsementType endorsementType;

    private EndorsementStatus status;

    private GLEndorsement endorsement;

    private Policy policy;

    private DateTime effectiveDate;

    private Set<GLProposerDocument> proposerDocuments;

    private DateTime submittedOn;

    public GroupLifeEndorsement(EndorsementId endorsementId, EndorsementNumber endorsementNumber, Policy policy, GLEndorsementType endorsementType) {
        checkArgument(endorsementId != null, "Endorsement ID cannot be empty");
        checkArgument(endorsementNumber != null, "Endorsement Number cannot be empty");
        checkArgument(policy != null, "Policy cannot be empty");
        this.endorsementId = endorsementId;
        this.endorsementNumber = endorsementNumber;
        this.status = EndorsementStatus.DRAFT;
        this.policy = policy;
        this.endorsementType = endorsementType;
    }

    public GroupLifeEndorsement updateWithEndorsementDetail(GLEndorsement endorsement) {
        this.endorsement = endorsement;
        return this;
    }

    public GroupLifeEndorsement updateWithDocuments(Set<GLProposerDocument> proposerDocuments) {
        this.proposerDocuments = proposerDocuments;
        return this;
    }

    public GroupLifeEndorsement submit(DateTime effectiveDate, EndorsementStatus status, String submittedBy) {
        this.status = status;
        this.effectiveDate = effectiveDate;
        return this;
    }

    public GroupLifeEndorsement cancel(DateTime cancelledOn, String cancelledBy) {
        this.status = EndorsementStatus.CANCELLED;
        return this;
    }

    public GroupLifeEndorsement reject(DateTime rejectedOn, String rejectedBy) {
        this.status = EndorsementStatus.REJECTED;
        return this;
    }

    public GroupLifeEndorsement putOnHold(DateTime holdOn, String holdBy) {
        this.status = EndorsementStatus.HOLD;
        return this;
    }

    public GroupLifeEndorsement paymentPending() {
        this.status = EndorsementStatus.PAYMENT_PENDING;
        return this;
    }

    public GroupLifeEndorsement paymentReceived() {
        this.status = EndorsementStatus.PAYMENT_RECEIVED;
        return this;
    }

    public GroupLifeEndorsement refundPending() {
        this.status = EndorsementStatus.REFUND_PENDING;
        return this;
    }

    public GroupLifeEndorsement refundProcessed() {
        this.status = EndorsementStatus.REFUND_PROCESSED;
        return this;
    }

    public GroupLifeEndorsement submitForApproval(DateTime now, String username, String comment) {
        this.submittedOn = now;
        this.status = EndorsementStatus.APPROVER_PENDING_ACCEPTANCE;
        return this;
    }

    @Override
    public EndorsementId getIdentifier() {
        return endorsementId;
    }


}
