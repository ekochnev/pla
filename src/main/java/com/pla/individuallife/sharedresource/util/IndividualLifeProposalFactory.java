package com.pla.individuallife.sharedresource.util;

import com.pla.core.domain.model.agent.AgentId;
import com.pla.grouplife.proposal.domain.model.GroupLifeProposal;
import com.pla.grouplife.proposal.domain.service.GLProposalNumberGenerator;
import com.pla.grouplife.proposal.query.GLProposalFinder;
import com.pla.grouplife.sharedresource.model.vo.Industry;
import com.pla.grouplife.sharedresource.model.vo.Insured;
import com.pla.grouplife.sharedresource.model.vo.PremiumDetail;
import com.pla.grouplife.sharedresource.model.vo.Proposer;
import com.pla.grouplife.sharedresource.query.GLFinder;
import com.pla.sharedkernel.domain.model.Quotation;
import com.pla.sharedkernel.identifier.OpportunityId;
import com.pla.sharedkernel.identifier.ProposalId;
import com.pla.sharedkernel.identifier.ProposalNumber;
import com.pla.sharedkernel.identifier.QuotationId;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by User on 6/30/2015.
 */
@Component
public class IndividualLifeProposalFactory {
    private GLFinder glFinder;

    private GLProposalFinder glProposalFinder;

    private GLProposalNumberGenerator glProposalNumberGenerator;

    @Autowired
    public IndividualLifeProposalFactory(GLFinder glFinder, GLProposalFinder glProposalFinder, GLProposalNumberGenerator glProposalNumberGenerator) {
        this.glFinder = glFinder;
        this.glProposalFinder = glProposalFinder;
        this.glProposalNumberGenerator = glProposalNumberGenerator;
    }

    public GroupLifeProposal createProposal(String quotationId, ProposalId proposalId) {
        Map quotationMap = glFinder.getQuotationById(quotationId);
        boolean samePlanForAllRelation = quotationMap.get("samePlanForAllRelation") != null ? (boolean) quotationMap.get("samePlanForAllRelation") : false;
        boolean samePlanForAllCategory = quotationMap.get("samePlanForAllCategory") != null ? (boolean) quotationMap.get("samePlanForAllCategory") : false;
        String schemeName = quotationMap.get("schemeName")!=null?(String) quotationMap.get("schemeName"):"";
        String quotationNumber = (String) quotationMap.get("quotationNumber");
        Industry industry = (Industry) quotationMap.get("industry");
        Integer versionNumber = (Integer) quotationMap.get("versionNumber");
        OpportunityId opportunityId = quotationMap.get("opportunityId")!=null?(OpportunityId) quotationMap.get("opportunityId"):null;
        Map proposalMap = glProposalFinder.findProposalByQuotationNumber(quotationNumber);
        ProposalNumber proposalNumber = proposalMap != null ? (ProposalNumber) proposalMap.get("proposalNumber") : new ProposalNumber(glProposalNumberGenerator.getProposalNumber(GroupLifeProposal.class, LocalDate.now()));
        Quotation quotation = new Quotation(quotationNumber, versionNumber, new QuotationId(quotationId));
        AgentId agentId = (AgentId) quotationMap.get("agentId");
        Set<Insured> insureds = new HashSet<Insured>((List) quotationMap.get("insureds"));
        PremiumDetail premiumDetail = (PremiumDetail) quotationMap.get("premiumDetail");
        Proposer proposer = (Proposer) quotationMap.get("proposer");
        GroupLifeProposal groupLifeProposal = new GroupLifeProposal(proposalId, quotation, proposalNumber);
        groupLifeProposal.updateFlagSamePlanForAllRelation(samePlanForAllRelation);
        groupLifeProposal.updateFlagSamePlanForAllCategory(samePlanForAllCategory);
        groupLifeProposal.updateWithSchemeName(schemeName);
        groupLifeProposal = groupLifeProposal.updateWithAgentId(agentId,null,null).updateWithProposer(proposer)
                .updateWithInsureds(insureds).updateWithPremiumDetail(premiumDetail).updateWithIndustry(industry).updateWithOpportunityId(opportunityId);
        return groupLifeProposal;
    }
}