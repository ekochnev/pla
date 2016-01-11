package com.pla.grouphealth.claim.cashless.domain.model;

import lombok.*;
import org.hibernate.annotations.Immutable;
import org.nthdimenzion.ddd.domain.annotations.ValueObject;

import javax.persistence.Embeddable;

/**
 * Created by Mohan Sharma on 1/9/2016.
 */
@ValueObject
@Immutable
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@Getter
public class PreAuthorizationRequestIllnessDetail {
    private String HTN;
    private String HTNDetails;
    private String idhHOD;
    private String IHDHODDetails;
    private String diabetes;
    private String diabetesDetails;
    private String asthmaCOPDTB;
    private String asthmaCOPDTBDetails;
    private String STDHIVAIDS;
    private String STDHIVAIDSDetails;
    private String arthritis;
    private String arthritisDetails;
    private String cancerTumorCyst;
    private String cancerTumorCystDetails;
    private String alcoholDrugAbuse;
    private String alcoholDrugAbuseDetails;
    private String psychiatricCondition;
    private String psychiatricConditionDetails;
}
