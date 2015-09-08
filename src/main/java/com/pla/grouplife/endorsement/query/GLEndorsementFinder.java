package com.pla.grouplife.endorsement.query;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.BasicDBObject;
import com.pla.grouplife.sharedresource.model.GLEndorsementType;
import com.pla.sharedkernel.identifier.EndorsementId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.nthdimenzion.utils.UtilValidator.isEmpty;
import static org.nthdimenzion.utils.UtilValidator.isNotEmpty;

/**
 * Created by Samir on 8/11/2015.
 */
@Service
public class GLEndorsementFinder {

    @Autowired
    private MongoTemplate mongoTemplate;

    public Map findEndorsementById(String endorsementId) {
        BasicDBObject query = new BasicDBObject();
        query.put("_id", endorsementId);
        Map proposal = mongoTemplate.findOne(new BasicQuery(query), Map.class, "group_life_endorsement");
        return proposal;
    }


    public List<Map> searchEndorsement(GLEndorsementType endorsementType, String endorsementNumber, String endorsementId, String policyNumber, String policyHolderName, String[] statuses) {
        Criteria criteria = Criteria.where("status").in(statuses);
        if (endorsementType == null && isEmpty(endorsementNumber) && isEmpty(endorsementId) && isEmpty(policyNumber) && isEmpty(policyHolderName)) {
            return Lists.newArrayList();
        }
        if (isNotEmpty(endorsementId)) {
            criteria = criteria.and("_id").is(new EndorsementId(endorsementId));
        }
        if (endorsementType != null) {
            criteria = criteria.and("endorsementType").is(endorsementType.name());
        }
        if (isNotEmpty(endorsementNumber)) {
            criteria = criteria.and("endorsementNumber.endorsementNumber").is(endorsementNumber);
        }
        if (isNotEmpty(policyHolderName)) {
            String policyHolderNamePattern = "^" + policyHolderName;
            criteria = criteria.and("policy.policyHolderName").regex(Pattern.compile(policyHolderNamePattern, Pattern.CASE_INSENSITIVE));
        }

        if (isNotEmpty(policyNumber)) {
            criteria = criteria.and("policy.policyNumber.policyNumber").is(policyNumber);
        }
        Query query = new Query(criteria);
        query.with(new Sort(Sort.Direction.ASC, "endorsementNumber.endorsementNumber"));
        return mongoTemplate.find(query, Map.class, "group_life_endorsement");
    }

    public Map<String, Object> getPolicyDetail(String endorsementId){
        Criteria endorsementCriteria = Criteria.where("_id").is(endorsementId);
        Query query = new Query(endorsementCriteria);
        query.fields().include("policy.policyId").include("policy.policyHolderName");
        List<Map> glEndorsement = mongoTemplate.find(query, Map.class, "group_life_endorsement");
        if (isEmpty(glEndorsement)){
            return Collections.EMPTY_MAP;
        }
        Criteria policyCriteria = Criteria.where("_id").is(((Map) glEndorsement.get(0).get("policy")).get("policyId"));
        Query policyQuery = new Query(policyCriteria);
        policyQuery.fields().include("inceptionOn").include("expiredOn");
        List<Map> glPolicy = mongoTemplate.find(policyQuery, Map.class, "group_life_policy");
        if (isEmpty(glPolicy)){
            return Collections.EMPTY_MAP;
        }
        Map<String,Object> policyDetailMap = Maps.newLinkedHashMap();
        policyDetailMap.put("policyHolderName",((Map) glEndorsement.get(0).get("policy")).get("policyHolderName"));
        policyDetailMap.put("inceptionOn",glPolicy.get(0).get("inceptionOn"));
        policyDetailMap.put("expiredOn",glPolicy.get(0).get("expiredOn"));
        return policyDetailMap;
    }
}
