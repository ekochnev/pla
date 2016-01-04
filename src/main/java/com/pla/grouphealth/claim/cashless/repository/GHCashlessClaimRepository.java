package com.pla.grouphealth.claim.cashless.repository;

import com.pla.grouphealth.claim.cashless.domain.model.GHCashlessClaim;
import com.pla.grouphealth.claim.cashless.domain.model.GHCashlessClaimId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Mohan Sharma on 12/30/2015.
 */
public interface GHCashlessClaimRepository extends MongoRepository<GHCashlessClaim, GHCashlessClaimId>{
}