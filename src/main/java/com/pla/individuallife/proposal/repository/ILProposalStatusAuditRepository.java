package com.pla.individuallife.proposal.repository;

import com.pla.individuallife.proposal.domain.model.ILProposalStatusAudit;
import com.pla.sharedkernel.identifier.ProposalId;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Created by Admin on 8/3/2015.
 */
public interface ILProposalStatusAuditRepository extends MongoRepository<ILProposalStatusAudit, ObjectId> {

    public List<ILProposalStatusAudit> findByProposalId(ProposalId proposalId);
}
