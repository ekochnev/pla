package com.pla.grouphealth.claim.cashless.query;

import com.google.common.collect.Lists;
import com.pla.grouphealth.claim.cashless.domain.model.claim.GroupHealthCashlessClaim;
import com.pla.grouphealth.claim.cashless.domain.model.preauthorization.PreAuthorizationRequest;
import com.pla.grouphealth.claim.cashless.domain.model.preauthorization.PreAuthorizationRequest.Status;
import com.pla.grouphealth.claim.cashless.presentation.dto.claim.SearchClaimAmendDetailDto;
import com.pla.grouphealth.claim.cashless.presentation.dto.claim.SearchReopenedClaimDetailDto;
import com.pla.grouphealth.claim.cashless.presentation.dto.claim.SearchGroupHealthCashlessClaimRecordDto;
import com.pla.grouphealth.claim.cashless.presentation.dto.preauthorization.SearchPreAuthorizationRecordDto;
import org.nthdimenzion.ddd.domain.annotations.Finder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.pla.grouphealth.claim.cashless.domain.model.claim.GroupHealthCashlessClaim.Status.*;
import static org.nthdimenzion.utils.UtilValidator.isEmpty;
import static org.nthdimenzion.utils.UtilValidator.isNotEmpty;

/**
 * Author - Mohan Sharma Created on 12/30/2015.
 */
@Finder
@Component
public class GHCashlessClaimFinder {

    private MongoTemplate mongoTemplate;
    private  final  String PRE_AUTHORIZATION_DETAIL="PRE_AUTHORIZATION_DETAIL";

    private  final String GH_CASHLESS_CLAIM="GROUP_HEALTH_CASHLESS_CLAIM";
    @Autowired
    public void setDataSource(MongoTemplate mongoTemplate) {

        this.mongoTemplate = mongoTemplate;
    }

    public List<PreAuthorizationRequest> searchPreAuthorizationRecord(SearchPreAuthorizationRecordDto searchPreAuthorizationRecordDto, String username){
        if(isEmpty(searchPreAuthorizationRecordDto.getBatchNumber())&& isEmpty(searchPreAuthorizationRecordDto.getClientId()) &&
                isEmpty(searchPreAuthorizationRecordDto.getPolicyNumber())&&isEmpty(searchPreAuthorizationRecordDto.getPreAuthorizationId())&&
                isEmpty(searchPreAuthorizationRecordDto.getHcpCode()) && isEmpty(searchPreAuthorizationRecordDto.getPolicyHolderName()) ) {

            return Lists.newArrayList();
        }
        Query query = new Query();
        query.addCriteria(new Criteria().and("preAuthorizationUnderWriterUserId").is(username));
        if(isNotEmpty(searchPreAuthorizationRecordDto.getUnderwriterLevel()) && searchPreAuthorizationRecordDto.getUnderwriterLevel().equalsIgnoreCase("LEVEL1")){
            query.addCriteria(new Criteria().and("status").is(Status.UNDERWRITING_LEVEL1));
        }
        if(isNotEmpty(searchPreAuthorizationRecordDto.getUnderwriterLevel()) && searchPreAuthorizationRecordDto.getUnderwriterLevel().equalsIgnoreCase("LEVEL2")){
            query.addCriteria(new Criteria().and("status").is(Status.UNDERWRITING_LEVEL2));
        }
        if(isNotEmpty(searchPreAuthorizationRecordDto.getBatchNumber())){
            query.addCriteria(new Criteria().and("batchNumber").is(searchPreAuthorizationRecordDto.getBatchNumber()));
        }
        if(isNotEmpty(searchPreAuthorizationRecordDto.getHcpCode())){
            query.addCriteria(new Criteria().and("preAuthorizationRequestHCPDetail.hcpCode").is(searchPreAuthorizationRecordDto.getHcpCode()));
        }
        if(isNotEmpty(searchPreAuthorizationRecordDto.getPolicyHolderName())){
            query.addCriteria(new Criteria().and("ghProposer.proposerName").regex("^" + searchPreAuthorizationRecordDto.getPolicyHolderName(), "i"));

        }
        if(isNotEmpty(searchPreAuthorizationRecordDto.getPolicyNumber())){
            query.addCriteria(new Criteria().and("preAuthorizationRequestPolicyDetail.policyNumber").is(searchPreAuthorizationRecordDto.getPolicyNumber()));
        }
        if(isNotEmpty(searchPreAuthorizationRecordDto.getClientId())){
            query.addCriteria(new Criteria().and("preAuthorizationRequestPolicyDetail.assuredDetail.clientId").is(searchPreAuthorizationRecordDto.getClientId()));
        }
        if(isNotEmpty(searchPreAuthorizationRecordDto.getPreAuthorizationId())){
            query.addCriteria(new Criteria().and("preAuthorizationRequestId").is(searchPreAuthorizationRecordDto.getPreAuthorizationId()));
        }
        query.with(new Sort(Sort.Direction.ASC, "preAuthorizationRequestId"));
        return mongoTemplate.find(query, PreAuthorizationRequest.class, "PRE_AUTHORIZATION_REQUEST");

    }

    public List<PreAuthorizationRequest> getPreAuthorizationRequestByCriteria(SearchPreAuthorizationRecordDto searchPreAuthorizationRecordDto, List usernames) {
        if(isEmpty(searchPreAuthorizationRecordDto.getBatchNumber())&& isEmpty(searchPreAuthorizationRecordDto.getClientId()) &&
                isEmpty(searchPreAuthorizationRecordDto.getPolicyNumber())&&isEmpty(searchPreAuthorizationRecordDto.getPreAuthorizationId())&&
                isEmpty(searchPreAuthorizationRecordDto.getHcpCode()) && isEmpty(searchPreAuthorizationRecordDto.getPolicyHolderName())) {

            return Lists.newArrayList();
        }
        Query query = new Query();
        query.addCriteria(new Criteria().and("preAuthorizationProcessorUserId").in(usernames));
        query.addCriteria(new Criteria().and("status").in(Lists.newArrayList(Status.INTIMATION, Status.EVALUATION, Status.RETURNED)));
        if(isNotEmpty(searchPreAuthorizationRecordDto.getBatchNumber())){
            query.addCriteria(new Criteria().and("batchNumber").is(searchPreAuthorizationRecordDto.getBatchNumber()));
        }
        if(isNotEmpty(searchPreAuthorizationRecordDto.getHcpCode())){
            query.addCriteria(new Criteria().and("preAuthorizationRequestHCPDetail.hcpCode").is(searchPreAuthorizationRecordDto.getHcpCode()));
        }
        if(isNotEmpty(searchPreAuthorizationRecordDto.getPolicyNumber())){
            query.addCriteria(new Criteria().and("preAuthorizationRequestPolicyDetail.policyNumber").is(searchPreAuthorizationRecordDto.getPolicyNumber()));
        }
        if(isNotEmpty(searchPreAuthorizationRecordDto.getClientId())){
            query.addCriteria(new Criteria().and("preAuthorizationRequestPolicyDetail.assuredDetail.clientId").is(searchPreAuthorizationRecordDto.getClientId()));
        }
        if(isNotEmpty(searchPreAuthorizationRecordDto.getPolicyHolderName())){
            query.addCriteria(new Criteria().and("ghProposer.proposerName").regex("^" + searchPreAuthorizationRecordDto.getPolicyHolderName(), "i"));;
        }
        if(isNotEmpty(searchPreAuthorizationRecordDto.getPreAuthorizationId())){
            query.addCriteria(new Criteria().and("preAuthorizationRequestId").is(searchPreAuthorizationRecordDto.getPreAuthorizationId()));
        }
        query.with(new Sort(Sort.Direction.ASC, "preAuthorizationRequestId"));
        return mongoTemplate.find(query, PreAuthorizationRequest.class, "PRE_AUTHORIZATION_REQUEST");
    }

    public List<GroupHealthCashlessClaim> getCashlessClaimByCriteria(SearchGroupHealthCashlessClaimRecordDto searchGroupHealthCashlessClaimRecordDto, ArrayList<String> usernames){
        if(isEmpty(searchGroupHealthCashlessClaimRecordDto.getBatchNumber())&& isEmpty(searchGroupHealthCashlessClaimRecordDto.getClientId())&&
                isEmpty(searchGroupHealthCashlessClaimRecordDto.getGroupHealthCashlessClaimId()) && isEmpty(searchGroupHealthCashlessClaimRecordDto.getHcpCode())
                && isEmpty(searchGroupHealthCashlessClaimRecordDto.getPolicyHolderName()) && isEmpty(searchGroupHealthCashlessClaimRecordDto.getPolicyNumber())
                &&isEmpty(searchGroupHealthCashlessClaimRecordDto.getUnderwriterLevel()) && isEmpty(searchGroupHealthCashlessClaimRecordDto.getAssuredNRCNumber())
                && isEmpty(searchGroupHealthCashlessClaimRecordDto.getPolicyHolderName()) && isEmpty(searchGroupHealthCashlessClaimRecordDto.getAssuredFirstName())
                && isEmpty(searchGroupHealthCashlessClaimRecordDto.getAssuredLastName())){
            return Lists.newArrayList();
        }
        Query query = new Query();
        query.addCriteria(new Criteria().and("claimProcessorUserId").in(usernames));
        query.addCriteria(new Criteria().and("status").in(INTIMATION, EVALUATION, RETURNED));
        if(isNotEmpty(searchGroupHealthCashlessClaimRecordDto.getBatchNumber())){
            query.addCriteria(new Criteria().and("batchNumber").is(searchGroupHealthCashlessClaimRecordDto.getBatchNumber()));
        }
        if(isNotEmpty(searchGroupHealthCashlessClaimRecordDto.getHcpCode())){
            query.addCriteria(new Criteria().and("groupHealthCashlessClaimHCPDetail.hcpCode.hcpCode").is(searchGroupHealthCashlessClaimRecordDto.getHcpCode()));
        }
        if(isNotEmpty(searchGroupHealthCashlessClaimRecordDto.getPolicyNumber())){
            query.addCriteria(new Criteria().and("groupHealthCashlessClaimPolicyDetail.policyNumber.policyNumber").is(searchGroupHealthCashlessClaimRecordDto.getPolicyNumber()));
        }
        if(isNotEmpty(searchGroupHealthCashlessClaimRecordDto.getAssuredNRCNumber())){
            query.addCriteria(new Criteria().and("groupHealthCashlessClaimPolicyDetail.assuredDetail.nrcNumber").is(searchGroupHealthCashlessClaimRecordDto.getAssuredNRCNumber()));
        }
        if(isNotEmpty(searchGroupHealthCashlessClaimRecordDto.getPolicyHolderName())){
            query.addCriteria(new Criteria().and("ghProposer.proposerName").is(searchGroupHealthCashlessClaimRecordDto.getPolicyHolderName()));
        }

        if(isNotEmpty(searchGroupHealthCashlessClaimRecordDto.getAssuredLastName())) {
            query.addCriteria(new Criteria().and("groupHealthCashlessClaimPolicyDetail.assuredDetail.surname").regex("^"+searchGroupHealthCashlessClaimRecordDto.getAssuredLastName(),"i"));
        }

        if(isNotEmpty(searchGroupHealthCashlessClaimRecordDto.getAssuredFirstName())){
           query.addCriteria(new Criteria().and("groupHealthCashlessClaimPolicyDetail.assuredDetail.firstName").regex("^"+searchGroupHealthCashlessClaimRecordDto.getAssuredFirstName(),"i"));
        }
        if(isNotEmpty(searchGroupHealthCashlessClaimRecordDto.getClientId())){
            query.addCriteria(new Criteria().and("groupHealthCashlessClaimPolicyDetail.assuredDetail.clientId").is(searchGroupHealthCashlessClaimRecordDto.getClientId()));
        }
        if(isNotEmpty(searchGroupHealthCashlessClaimRecordDto.getGroupHealthCashlessClaimId())){
            query.addCriteria(new Criteria().and("groupHealthCashlessClaimId").is(searchGroupHealthCashlessClaimRecordDto.getGroupHealthCashlessClaimId()));
        }
        query.with(new Sort(Sort.Direction.ASC, "groupHealthCashlessClaimId"));
        return mongoTemplate.find(query, GroupHealthCashlessClaim.class, "GROUP_HEALTH_CASHLESS_CLAIM");
    }

    public List<GroupHealthCashlessClaim> searchGroupHealthCashlessClaimForUnderwriterByCriteria(SearchGroupHealthCashlessClaimRecordDto searchGroupHealthCashlessClaimRecordDto, List username){
        if(isEmpty(searchGroupHealthCashlessClaimRecordDto.getBatchNumber())&& isEmpty(searchGroupHealthCashlessClaimRecordDto.getClientId())&&
                isEmpty(searchGroupHealthCashlessClaimRecordDto.getGroupHealthCashlessClaimId()) && isEmpty(searchGroupHealthCashlessClaimRecordDto.getHcpCode())
                && isEmpty(searchGroupHealthCashlessClaimRecordDto.getPolicyNumber())
                &&isEmpty(searchGroupHealthCashlessClaimRecordDto.getUnderwriterLevel())&& isEmpty(searchGroupHealthCashlessClaimRecordDto.getAssuredNRCNumber())
                && isEmpty(searchGroupHealthCashlessClaimRecordDto.getPolicyHolderName()) && isEmpty(searchGroupHealthCashlessClaimRecordDto.getAssuredFirstName())
                && isEmpty(searchGroupHealthCashlessClaimRecordDto.getAssuredLastName())){
            return Lists.newArrayList();
        }
        Query query = new Query();
        query.addCriteria(new Criteria().and("claimUnderWriterUserId").in(username));
        if(isNotEmpty(searchGroupHealthCashlessClaimRecordDto.getUnderwriterLevel()) && searchGroupHealthCashlessClaimRecordDto.getUnderwriterLevel().equalsIgnoreCase("LEVEL1")){
            query.addCriteria(new Criteria().and("status").is(Status.UNDERWRITING_LEVEL1));
        }
        if(isNotEmpty(searchGroupHealthCashlessClaimRecordDto.getUnderwriterLevel()) && searchGroupHealthCashlessClaimRecordDto.getUnderwriterLevel().equalsIgnoreCase("LEVEL2")){
            query.addCriteria(new Criteria().and("status").is(Status.UNDERWRITING_LEVEL2));
        }
        if(isNotEmpty(searchGroupHealthCashlessClaimRecordDto.getBatchNumber())){
            query.addCriteria(new Criteria().and("batchNumber").is(searchGroupHealthCashlessClaimRecordDto.getBatchNumber()));
        }
        if(isNotEmpty(searchGroupHealthCashlessClaimRecordDto.getHcpCode())){
            query.addCriteria(new Criteria().and("groupHealthCashlessClaimHCPDetail.hcpCode.hcpCode").is(searchGroupHealthCashlessClaimRecordDto.getHcpCode()));
        }
        if(isNotEmpty(searchGroupHealthCashlessClaimRecordDto.getAssuredNRCNumber())){
            query.addCriteria(new Criteria().and("groupHealthCashlessClaimPolicyDetail.assuredDetail.nrcNumber").is(searchGroupHealthCashlessClaimRecordDto.getAssuredNRCNumber()));
        }
        if(isNotEmpty(searchGroupHealthCashlessClaimRecordDto.getPolicyHolderName())){
            query.addCriteria(new Criteria().and("ghProposer.proposerName").is(searchGroupHealthCashlessClaimRecordDto.getPolicyHolderName()));
        }

        if(isNotEmpty(searchGroupHealthCashlessClaimRecordDto.getAssuredLastName())) {
            query.addCriteria(new Criteria().and("groupHealthCashlessClaimPolicyDetail.assuredDetail.surname").regex("^"+searchGroupHealthCashlessClaimRecordDto.getAssuredLastName(),"i"));
        }

        if(isNotEmpty(searchGroupHealthCashlessClaimRecordDto.getAssuredFirstName())){
            query.addCriteria(new Criteria().and("groupHealthCashlessClaimPolicyDetail.assuredDetail.firstName").regex("^"+searchGroupHealthCashlessClaimRecordDto.getAssuredFirstName(),"i"));
        }
        if(isNotEmpty(searchGroupHealthCashlessClaimRecordDto.getPolicyNumber())){
            query.addCriteria(new Criteria().and("groupHealthCashlessClaimPolicyDetail.policyNumber.policyNumber").is(searchGroupHealthCashlessClaimRecordDto.getPolicyNumber()));
        }
        if(isNotEmpty(searchGroupHealthCashlessClaimRecordDto.getClientId())){
            query.addCriteria(new Criteria().and("groupHealthCashlessClaimPolicyDetail.assuredDetail.clientId").is(searchGroupHealthCashlessClaimRecordDto.getClientId()));
        }
        if(isNotEmpty(searchGroupHealthCashlessClaimRecordDto.getGroupHealthCashlessClaimId())){
            query.addCriteria(new Criteria().and("groupHealthCashlessClaimId").is(searchGroupHealthCashlessClaimRecordDto.getGroupHealthCashlessClaimId()));
        }
        query.with(new Sort(Sort.Direction.ASC, "groupHealthCashlessClaimId"));
        return mongoTemplate.find(query, GroupHealthCashlessClaim.class, "GROUP_HEALTH_CASHLESS_CLAIM");

    }

    public List<GroupHealthCashlessClaim> searchCashlessClaimBillMismatchCriteria(SearchGroupHealthCashlessClaimRecordDto searchGroupHealthCashlessClaimRecordDto, List username){
        if(isEmpty(searchGroupHealthCashlessClaimRecordDto.getBatchNumber())&& isEmpty(searchGroupHealthCashlessClaimRecordDto.getClientId())&&
                isEmpty(searchGroupHealthCashlessClaimRecordDto.getGroupHealthCashlessClaimId()) && isEmpty(searchGroupHealthCashlessClaimRecordDto.getHcpCode())
               && isEmpty(searchGroupHealthCashlessClaimRecordDto.getPolicyNumber())&& isEmpty(searchGroupHealthCashlessClaimRecordDto.getAssuredNRCNumber())
                && isEmpty(searchGroupHealthCashlessClaimRecordDto.getPolicyHolderName()) && isEmpty(searchGroupHealthCashlessClaimRecordDto.getAssuredFirstName())
                && isEmpty(searchGroupHealthCashlessClaimRecordDto.getAssuredLastName())){
            return Lists.newArrayList();
        }
        Query query = new Query();
        query.addCriteria(new Criteria().and("billMismatchProcessorId").in(username));
        query.addCriteria(new Criteria().and("status").in(BILL_MISMATCHED));
        if(isNotEmpty(searchGroupHealthCashlessClaimRecordDto.getBatchNumber())){
            query.addCriteria(new Criteria().and("batchNumber").is(searchGroupHealthCashlessClaimRecordDto.getBatchNumber()));
        }
        if(isNotEmpty(searchGroupHealthCashlessClaimRecordDto.getHcpCode())){
            query.addCriteria(new Criteria().and("groupHealthCashlessClaimHCPDetail.hcpCode.hcpCode").is(searchGroupHealthCashlessClaimRecordDto.getHcpCode()));
        }
        if(isNotEmpty(searchGroupHealthCashlessClaimRecordDto.getPolicyNumber())){
            query.addCriteria(new Criteria().and("groupHealthCashlessClaimPolicyDetail.policyNumber.policyNumber").is(searchGroupHealthCashlessClaimRecordDto.getPolicyNumber()));
        }
        if(isNotEmpty(searchGroupHealthCashlessClaimRecordDto.getClientId())){
            query.addCriteria(new Criteria().and("groupHealthCashlessClaimPolicyDetail.assuredDetail.clientId").is(searchGroupHealthCashlessClaimRecordDto.getClientId()));
        }
        if(isNotEmpty(searchGroupHealthCashlessClaimRecordDto.getGroupHealthCashlessClaimId())){
            query.addCriteria(new Criteria().and("groupHealthCashlessClaimId").is(searchGroupHealthCashlessClaimRecordDto.getGroupHealthCashlessClaimId()));
        }
        if(isNotEmpty(searchGroupHealthCashlessClaimRecordDto.getAssuredNRCNumber())){
            query.addCriteria(new Criteria().and("groupHealthCashlessClaimPolicyDetail.assuredDetail.nrcNumber").is(searchGroupHealthCashlessClaimRecordDto.getAssuredNRCNumber()));
        }
        if(isNotEmpty(searchGroupHealthCashlessClaimRecordDto.getPolicyHolderName())){
            query.addCriteria(new Criteria().and("ghProposer.proposerName").is(searchGroupHealthCashlessClaimRecordDto.getPolicyHolderName()));
        }

        if(isNotEmpty(searchGroupHealthCashlessClaimRecordDto.getAssuredLastName())) {
            query.addCriteria(new Criteria().and("groupHealthCashlessClaimPolicyDetail.assuredDetail.surname").regex("^"+searchGroupHealthCashlessClaimRecordDto.getAssuredLastName(),"i"));
        }

        if(isNotEmpty(searchGroupHealthCashlessClaimRecordDto.getAssuredFirstName())){
            query.addCriteria(new Criteria().and("groupHealthCashlessClaimPolicyDetail.assuredDetail.firstName").regex("^"+searchGroupHealthCashlessClaimRecordDto.getAssuredFirstName(),"i"));
        }
        query.with(new Sort(Sort.Direction.ASC, "groupHealthCashlessClaimId"));
        return mongoTemplate.find(query, GroupHealthCashlessClaim.class, "GROUP_HEALTH_CASHLESS_CLAIM");

    }

    public List<GroupHealthCashlessClaim> searchCashlessClaimServiceMismatchCriteria(SearchGroupHealthCashlessClaimRecordDto searchGroupHealthCashlessClaimRecordDto, List username){
        if(isEmpty(searchGroupHealthCashlessClaimRecordDto.getBatchNumber())&& isEmpty(searchGroupHealthCashlessClaimRecordDto.getClientId())&&
                isEmpty(searchGroupHealthCashlessClaimRecordDto.getGroupHealthCashlessClaimId()) && isEmpty(searchGroupHealthCashlessClaimRecordDto.getHcpCode())
                && isEmpty(searchGroupHealthCashlessClaimRecordDto.getPolicyNumber())&& isEmpty(searchGroupHealthCashlessClaimRecordDto.getAssuredNRCNumber())
                && isEmpty(searchGroupHealthCashlessClaimRecordDto.getPolicyHolderName()) && isEmpty(searchGroupHealthCashlessClaimRecordDto.getAssuredFirstName())
                && isEmpty(searchGroupHealthCashlessClaimRecordDto.getAssuredLastName())){
            return Lists.newArrayList();
        }
        Query query = new Query();
        query.addCriteria(new Criteria().and("serviceMismatchProcessorId").in(username));
        query.addCriteria(new Criteria().and("status").in(SERVICE_MISMATCHED));
        if(isNotEmpty(searchGroupHealthCashlessClaimRecordDto.getBatchNumber())){
            query.addCriteria(new Criteria().and("batchNumber").is(searchGroupHealthCashlessClaimRecordDto.getBatchNumber()));
        }
        if(isNotEmpty(searchGroupHealthCashlessClaimRecordDto.getHcpCode())){
            query.addCriteria(new Criteria().and("groupHealthCashlessClaimHCPDetail.hcpCode.hcpCode").is(searchGroupHealthCashlessClaimRecordDto.getHcpCode()));
        }
        if(isNotEmpty(searchGroupHealthCashlessClaimRecordDto.getPolicyNumber())){
            query.addCriteria(new Criteria().and("groupHealthCashlessClaimPolicyDetail.policyNumber.policyNumber").is(searchGroupHealthCashlessClaimRecordDto.getPolicyNumber()));
        }
        if(isNotEmpty(searchGroupHealthCashlessClaimRecordDto.getClientId())){
            query.addCriteria(new Criteria().and("groupHealthCashlessClaimPolicyDetail.assuredDetail.clientId").is(searchGroupHealthCashlessClaimRecordDto.getClientId()));
        }
        if(isNotEmpty(searchGroupHealthCashlessClaimRecordDto.getGroupHealthCashlessClaimId())){
            query.addCriteria(new Criteria().and("groupHealthCashlessClaimId").is(searchGroupHealthCashlessClaimRecordDto.getGroupHealthCashlessClaimId()));
        }
        if(isNotEmpty(searchGroupHealthCashlessClaimRecordDto.getAssuredNRCNumber())){
            query.addCriteria(new Criteria().and("groupHealthCashlessClaimPolicyDetail.assuredDetail.nrcNumber").is(searchGroupHealthCashlessClaimRecordDto.getAssuredNRCNumber()));
        }
        if(isNotEmpty(searchGroupHealthCashlessClaimRecordDto.getPolicyHolderName())){
            query.addCriteria(new Criteria().and("ghProposer.proposerName").is(searchGroupHealthCashlessClaimRecordDto.getPolicyHolderName()));
        }

        if(isNotEmpty(searchGroupHealthCashlessClaimRecordDto.getAssuredLastName())) {
            query.addCriteria(new Criteria().and("groupHealthCashlessClaimPolicyDetail.assuredDetail.surname").regex("^"+searchGroupHealthCashlessClaimRecordDto.getAssuredLastName(),"i"));
        }

        if(isNotEmpty(searchGroupHealthCashlessClaimRecordDto.getAssuredFirstName())){
            query.addCriteria(new Criteria().and("groupHealthCashlessClaimPolicyDetail.assuredDetail.firstName").regex("^"+searchGroupHealthCashlessClaimRecordDto.getAssuredFirstName(),"i"));
        }
        query.with(new Sort(Sort.Direction.ASC, "groupHealthCashlessClaimId"));
        return mongoTemplate.find(query, GroupHealthCashlessClaim.class, "GROUP_HEALTH_CASHLESS_CLAIM");

    }

    public List<GroupHealthCashlessClaim> searchReopenedGroupHealthCashlessClaimByCriteria(SearchReopenedClaimDetailDto searchReopenedClaimDetailDto, List<String> usernames) {
        if(isEmpty(searchReopenedClaimDetailDto.getAssuredFirstName())&& isEmpty(searchReopenedClaimDetailDto.getAssuredLastName())&& isEmpty(searchReopenedClaimDetailDto.getClientId())&&
                isEmpty(searchReopenedClaimDetailDto.getAssuredNRCNumber()) && isEmpty(searchReopenedClaimDetailDto.getClaimNumber())
                && isEmpty(searchReopenedClaimDetailDto.getPolicyHolderName()) && isEmpty(searchReopenedClaimDetailDto.getPolicyNumber())){
            return Lists.newArrayList();
        }
        Query query = new Query();
        query.addCriteria(new Criteria().and("claimReopenProcessorUserId").in(usernames));
        query.addCriteria(new Criteria().and("status").in(CANCELLED, REPUDIATED));
        if(isNotEmpty(searchReopenedClaimDetailDto.getPolicyHolderName())){
            query.addCriteria(new Criteria().and("ghProposer.proposerName").regex("^"+searchReopenedClaimDetailDto.getPolicyHolderName(), "i"));
        }
        if(isNotEmpty(searchReopenedClaimDetailDto.getClaimNumber())){
            query.addCriteria(new Criteria().and("groupHealthCashlessClaimId").is(searchReopenedClaimDetailDto.getClaimNumber()));
        }
        if(isNotEmpty(searchReopenedClaimDetailDto.getPolicyNumber())){
            query.addCriteria(new Criteria().and("groupHealthCashlessClaimPolicyDetail.policyNumber.policyNumber").is(searchReopenedClaimDetailDto.getPolicyNumber()));
        }
        if(isNotEmpty(searchReopenedClaimDetailDto.getClientId())){
            query.addCriteria(new Criteria().and("groupHealthCashlessClaimPolicyDetail.assuredDetail.clientId").is(searchReopenedClaimDetailDto.getClientId()));
        }
        if(isNotEmpty(searchReopenedClaimDetailDto.getAssuredFirstName())){
            query.addCriteria(new Criteria().and("groupHealthCashlessClaimPolicyDetail.assuredDetail.firstName").regex("^"+searchReopenedClaimDetailDto.getAssuredFirstName(), "i"));
            /*query.addCriteria(new Criteria().orOperator(
                    Criteria.where("groupHealthCashlessClaimPolicyDetail.assuredDetail.firstName").regex("^"+searchReopenedClaimDetailDto.getAssuredName(), "i"),
                    Criteria.where("groupHealthCashlessClaimPolicyDetail.assuredDetail.surname").regex("^"+searchReopenedClaimDetailDto.getAssuredName(), "i")));*/
        }
        if(isNotEmpty(searchReopenedClaimDetailDto.getAssuredLastName())){
            query.addCriteria(new Criteria().and("groupHealthCashlessClaimPolicyDetail.assuredDetail.surname").regex("^"+searchReopenedClaimDetailDto.getAssuredLastName(), "i"));
        }
        if(isNotEmpty(searchReopenedClaimDetailDto.getAssuredNRCNumber())){
            query.addCriteria(new Criteria().and("groupHealthCashlessClaimId.assuredDetail.nrcNumber").is(searchReopenedClaimDetailDto.getAssuredNRCNumber()));
        }
        query.with(new Sort(Sort.Direction.ASC, "groupHealthCashlessClaimId"));
        return mongoTemplate.find(query, GroupHealthCashlessClaim.class, "GROUP_HEALTH_CASHLESS_CLAIM");
    }

    public List<GroupHealthCashlessClaim> searchCashlessClaimWhichCanBeAmendedByCriteria(SearchClaimAmendDetailDto searchClaimAmendDetailDto, ArrayList<String> users) {
        if(isEmpty(searchClaimAmendDetailDto.getAssuredFirstName())&& isEmpty(searchClaimAmendDetailDto.getAssuredLastName())&& isEmpty(searchClaimAmendDetailDto.getClientId())&&
                isEmpty(searchClaimAmendDetailDto.getAssuredNRCNumber()) && isEmpty(searchClaimAmendDetailDto.getClaimNumber())
                && isEmpty(searchClaimAmendDetailDto.getPolicyHolderName()) && isEmpty(searchClaimAmendDetailDto.getPolicyNumber())){
            return Lists.newArrayList();
        }
        Query query = new Query();
        query.addCriteria(new Criteria().and("claimAmendmentProcessorUserId").in(users));
        query.addCriteria(new Criteria().and("status").in(APPROVED, DISBURSED, AWAITING_DISBURSEMENT));
        if(isNotEmpty(searchClaimAmendDetailDto.getPolicyHolderName())){
            query.addCriteria(new Criteria().and("ghProposer.proposerName").regex("^"+searchClaimAmendDetailDto.getPolicyHolderName(), "i"));
        }
        if(isNotEmpty(searchClaimAmendDetailDto.getClaimNumber())){
            query.addCriteria(new Criteria().and("groupHealthCashlessClaimId").is(searchClaimAmendDetailDto.getClaimNumber()));
        }
        if(isNotEmpty(searchClaimAmendDetailDto.getPolicyNumber())){
            query.addCriteria(new Criteria().and("groupHealthCashlessClaimPolicyDetail.policyNumber.policyNumber").is(searchClaimAmendDetailDto.getPolicyNumber()));
        }
        if(isNotEmpty(searchClaimAmendDetailDto.getClientId())){
            query.addCriteria(new Criteria().and("groupHealthCashlessClaimPolicyDetail.assuredDetail.clientId").is(searchClaimAmendDetailDto.getClientId()));
        }
        if(isNotEmpty(searchClaimAmendDetailDto.getAssuredFirstName())){
            query.addCriteria(new Criteria().and("groupHealthCashlessClaimPolicyDetail.assuredDetail.firstName").regex("^"+searchClaimAmendDetailDto.getAssuredFirstName(), "i"));
        }
        if(isNotEmpty(searchClaimAmendDetailDto.getAssuredLastName())){
            query.addCriteria(new Criteria().and("groupHealthCashlessClaimPolicyDetail.assuredDetail.surname").regex("^"+searchClaimAmendDetailDto.getAssuredLastName(), "i"));
        }
        if(isNotEmpty(searchClaimAmendDetailDto.getAssuredNRCNumber())){
            query.addCriteria(new Criteria().and("groupHealthCashlessClaimId.assuredDetail.nrcNumber").is(searchClaimAmendDetailDto.getAssuredNRCNumber()));
        }
        query.with(new Sort(Sort.Direction.ASC, "groupHealthCashlessClaimId"));
        return mongoTemplate.find(query, GroupHealthCashlessClaim.class, "GROUP_HEALTH_CASHLESS_CLAIM");
    }
}
