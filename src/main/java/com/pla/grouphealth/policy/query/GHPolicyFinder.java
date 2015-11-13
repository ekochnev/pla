package com.pla.grouphealth.policy.query;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.nthdimenzion.utils.UtilValidator.isEmpty;
import static org.nthdimenzion.utils.UtilValidator.isNotEmpty;

/**
 * Created by Samir on 7/9/2015.
 */
@Service
public class GHPolicyFinder {

    private static final String GH_POLICY_COLLECTION_NAME = "group_health_policy";
    @Autowired
    private MongoTemplate mongoTemplate;

    public List<Map> findAllPolicy() {
        return mongoTemplate.findAll(Map.class, GH_POLICY_COLLECTION_NAME);
    }

    public Map findPolicyById(String policyId) {
        return mongoTemplate.findOne(new Query(Criteria.where("_id").is(policyId)), Map.class, GH_POLICY_COLLECTION_NAME);
    }

    public Map findProposalIdByPolicyId(String policyId) {
        Query query = new Query(Criteria.where("_id").is(policyId));
        query.fields().include("proposal");
        return mongoTemplate.findOne(query, Map.class, GH_POLICY_COLLECTION_NAME);
    }

    public List<Map> searchPolicy(String policyNumber, String policyHolderName,String proposalNumber) {
        if (isEmpty(policyHolderName) && isEmpty(policyNumber) && isEmpty(proposalNumber)) {
            return Lists.newArrayList();
        }
        Criteria criteria = Criteria.where("status").is("IN_FORCE");
        if (isNotEmpty(policyHolderName)) {
            String proposerPattern = "^" + policyHolderName;
            criteria = criteria.and("proposer.proposerName").regex(Pattern.compile(proposerPattern, Pattern.CASE_INSENSITIVE));
        }
        if (isNotEmpty(policyNumber)) {
            criteria = criteria.and("policyNumber.policyNumber").is(policyNumber);
        }
        if(isNotEmpty(proposalNumber)){
            criteria = criteria.and("proposal.proposalNumber.proposalNumber").is(proposalNumber);

        }
        Query query = new Query(criteria);
        query.with(new Sort(Sort.Direction.ASC, "policyNumber.policyNumber"));
        return mongoTemplate.find(query, Map.class, GH_POLICY_COLLECTION_NAME);
    }



}
