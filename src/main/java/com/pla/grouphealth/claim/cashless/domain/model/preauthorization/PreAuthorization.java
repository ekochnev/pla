package com.pla.grouphealth.claim.cashless.domain.model.preauthorization;

import com.pla.core.hcp.domain.model.HCPCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;
import java.util.Set;

import static org.nthdimenzion.utils.UtilValidator.*;

/**
 * Author - Mohan Sharma Created on 12/30/2015.
 */
@Document(collection = "PRE_AUTHORIZATION_DETAIL")
@NoArgsConstructor
@Getter
public class PreAuthorization {
    @Id
    private PreAuthorizationId preAuthorizationId;
    private HCPCode hcpCode;
    private String batchNumber;
    private DateTime batchDate;
    private Set<PreAuthorizationDetail> preAuthorizationDetails;
    private Set<Map<String, Object>> sameServicesPreviouslyAvailedPreAuth;
    private String batchUploaderUserId;

    public PreAuthorization updateWithPreAuthorizationDetail(Set<PreAuthorizationDetail> preAuthorizationDetails) {
        this.preAuthorizationDetails = preAuthorizationDetails;
        return this;
    }

    public PreAuthorization updateWithHcpCode(HCPCode hcpCode) {
        this.hcpCode = hcpCode;
        return this;
    }

    public PreAuthorization updateWithBatchDate(DateTime batchDate) {
        this.batchDate = batchDate;
        return this;
    }

    public PreAuthorization updateWithBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
        return this;
    }

    public PreAuthorization updateWithPreAuthorizationId(PreAuthorizationId preAuthorizationId) {
        this.preAuthorizationId = preAuthorizationId;
        return this;
    }

    public PreAuthorization updateWithSameServicesPreviouslyAvailedPreAuth(Set<Map<String, Object>> sameServicesPreviouslyAvailedPreAuth) {
        if(isNotEmpty(sameServicesPreviouslyAvailedPreAuth)){
            this.sameServicesPreviouslyAvailedPreAuth = sameServicesPreviouslyAvailedPreAuth;
        }
        return this;
    }

    public PreAuthorization updateWithBatchUploaderUserId(String batchUploaderUserId) {
        this.batchUploaderUserId = batchUploaderUserId;
        return this;
    }
}