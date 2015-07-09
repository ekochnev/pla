package com.pla.grouphealth.policy.domain.service;

import com.google.common.collect.Maps;
import com.pla.core.domain.model.agent.AgentId;
import com.pla.grouphealth.policy.domain.model.GroupHealthPolicy;
import com.pla.grouphealth.sharedresource.model.vo.GHInsured;
import com.pla.grouphealth.sharedresource.model.vo.GHInsuredDependent;
import com.pla.grouphealth.sharedresource.model.vo.GHPremiumDetail;
import com.pla.grouphealth.sharedresource.model.vo.GHProposer;
import com.pla.grouphealth.sharedresource.query.GHFinder;
import com.pla.sharedkernel.domain.model.*;
import com.pla.sharedkernel.identifier.PolicyId;
import com.pla.sharedkernel.identifier.ProposalId;
import com.pla.sharedkernel.identifier.ProposalNumber;
import com.pla.sharedkernel.util.SequenceGenerator;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.nthdimenzion.utils.UtilValidator.isNotEmpty;

/**
 * Created by Samir on 7/8/2015.
 */
@Component
public class GHPolicyFactory {

    private GHFinder ghFinder;

    private SequenceGenerator sequenceGenerator;

    private GHPolicyNumberGenerator ghPolicyNumberGenerator;

    @Autowired
    public GHPolicyFactory(GHFinder ghFinder, SequenceGenerator sequenceGenerator, GHPolicyNumberGenerator ghPolicyNumberGenerator) {
        this.ghFinder = ghFinder;
        this.sequenceGenerator = sequenceGenerator;
        this.ghPolicyNumberGenerator = ghPolicyNumberGenerator;
    }

    public GroupHealthPolicy createPolicy(ProposalId proposalId) {
        Map proposalMap = ghFinder.findProposalById(proposalId.getProposalId());
        AgentId agentId = (AgentId) proposalMap.get("agentId");
        Set<GHInsured> insureds = new HashSet((List) proposalMap.get("insureds"));
        GHPremiumDetail premiumDetail = (GHPremiumDetail) proposalMap.get("premiumDetail");
        GHProposer proposer = (GHProposer) proposalMap.get("proposer");
        ProposalNumber proposalNumber = (ProposalNumber) proposalMap.get("proposalNumber");
        String policyNumberInString = ghPolicyNumberGenerator.getPolicyNumber(GroupHealthPolicy.class, agentId);
        PolicyNumber policyNumber = new PolicyNumber(policyNumberInString);
        PolicyId policyId = new PolicyId(ObjectId.get().toString());
        DateTime policyInceptionDate = DateTime.now();
        DateTime policyExpireDate = policyInceptionDate.plusDays(premiumDetail.getPolicyTermValue());
        Proposal proposal = new Proposal(proposalId, proposalNumber);
        GroupHealthPolicy groupHealthPolicy = new GroupHealthPolicy(policyId, policyNumber, proposal, policyInceptionDate, policyExpireDate);
        insureds = populateFamilyId(insureds);
        groupHealthPolicy = groupHealthPolicy.updateWithAgentId(agentId).updateWithProposer(proposer).updateWithInsured(insureds);
        PolicyPremium policyPremium = getPolicyPremium(premiumDetail);
        groupHealthPolicy = groupHealthPolicy.updateWithPremium(policyPremium);
        return groupHealthPolicy;
    }

    private PolicyPremium getPolicyPremium(GHPremiumDetail premiumDetail) {
        PolicyPremium policyPremium = null;
        if (premiumDetail.getOptedFrequencyPremium() != null && premiumDetail.getPremiumInstallment() == null) {
            policyPremium = new PolicyPremium(premiumDetail.getOptedFrequencyPremium().getPremiumFrequency(), premiumDetail.getOptedFrequencyPremium().getPremium());
        } else {
            policyPremium = new PolicyPremium(premiumDetail.getPremiumInstallment().getNoOfInstallment(), premiumDetail.getPremiumInstallment().getInstallmentAmount());
        }
        return policyPremium;
    }

    private Set<GHInsured> populateFamilyId(Set<GHInsured> insureds) {
        Map<String, Object> entitySequenceMap = sequenceGenerator.getEntitySequenceMap(GHInsured.class);
        final Integer[] sequenceNumber = {((Integer) entitySequenceMap.get("sequenceNumber")) + 1};
        insureds.forEach(insured -> {
            if (insured.getNoOfAssured() == null) {
                String selfFamilySequence = sequenceNumber[0] + "01";
                sequenceNumber[0] = sequenceNumber[0] + 1;
                FamilyId familyId = new FamilyId(selfFamilySequence);
                insured = insured.updateWithFamilyId(familyId);
            }
            if (isNotEmpty(insured.getInsuredDependents())) {
                Map<Relationship, List<Integer>> relationshipSequenceMap = groupDependentSequenceByRelation(insured.getInsuredDependents());
                insured.getInsuredDependents().forEach(insuredDependent -> {
                    if (insuredDependent.getNoOfAssured() == null) {
                        List<Integer> sequenceList = relationshipSequenceMap.get(insuredDependent.getRelationship());
                        String selfFamilySequence = sequenceNumber[0].toString() + sequenceList.get(0);
                        sequenceList.remove(0);
                        sequenceNumber[0] = sequenceNumber[0] + 1;
                        relationshipSequenceMap.put(insuredDependent.getRelationship(), sequenceList);
                        FamilyId familyId = new FamilyId(selfFamilySequence);
                        insuredDependent = insuredDependent.updateWithFamilyId(familyId);
                    }
                });
            }
        });
        sequenceGenerator.updateSequence(sequenceNumber[0], (Integer) entitySequenceMap.get("sequenceId"));
        return insureds;
    }

    private Map<Relationship, List<Integer>> groupDependentSequenceByRelation(Set<GHInsuredDependent> insuredDependents) {
        Map<Relationship, List<Integer>> dependentSequenceMap = Maps.newHashMap();
        final int[] currentSequence = {3};
        insuredDependents.forEach(insuredDependent -> {
            if (dependentSequenceMap.get(insuredDependent.getRelationship()) == null) {
                List<Integer> sequenceList = new ArrayList<Integer>();
                sequenceList.add((Relationship.SPOUSE.equals(insuredDependent.getRelationship()) ? 2 : currentSequence[0]));
                currentSequence[0] = currentSequence[0] + 1;
                dependentSequenceMap.put(insuredDependent.getRelationship(), sequenceList);
            } else {
                List<Integer> sequenceList = dependentSequenceMap.get(insuredDependent.getRelationship());
                currentSequence[0] = sequenceList.get((sequenceList.size() - 1));
                currentSequence[0] = currentSequence[0] + 1;
                sequenceList.add(currentSequence[0]);
            }
        });
        return dependentSequenceMap;
    }

}
