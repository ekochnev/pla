package com.pla.individuallife.endorsement.domain.model;

import com.pla.individuallife.policy.presentation.dto.ILPolicyDto;
import com.pla.individuallife.sharedresource.model.ILEndorsementType;
import com.pla.individuallife.sharedresource.model.vo.PremiumDetail;
import com.pla.sharedkernel.domain.model.EndorsementNumber;
import com.pla.sharedkernel.domain.model.Policy;
import com.pla.sharedkernel.domain.model.PolicyNumber;
import com.pla.sharedkernel.identifier.EndorsementId;
import com.pla.sharedkernel.identifier.PolicyId;

import java.util.Set;

/**
 * Created by Samir on 8/27/2015.
 */
public class ILEndorsementProcessor {

    private String userName;

    public ILEndorsementProcessor(String userName) {
        this.userName = userName;
    }

    public IndividualLifeEndorsement createEndorsement(String endorsementId, String endorsementRequestNumber, String policyId, ILPolicyDto ilPolicyDto) {
        //EndorsementId endorsementId = new EndorsementId(endorsementRequestNumber);
        //String endorsementRequestNumber = endorsementRequestNumber;
        //Policy policy = new Policy(new PolicyId(policyId), new PolicyNumber(policyNumber), policyHolderName);
        //ILEndorsementType endorsementType = ilPolicyDto.getIlEndorsementType();
        //endorsementType.populateInfoByType(ilPolicyDto);
        IndividualLifeEndorsement individualLifeEndorsement = new IndividualLifeEndorsement(endorsementId, endorsementRequestNumber, ilPolicyDto, ilPolicyDto.getIlEndorsementType());
        return individualLifeEndorsement;
    }

    public IndividualLifeEndorsement updateWithPremiumDetail(IndividualLifeEndorsement individualLifeEndorsement, PremiumDetail premiumDetail) {
        return individualLifeEndorsement.updateWithPremiumDetail(premiumDetail);
    }
}
