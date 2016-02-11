package com.pla.grouphealth.claim.cashless.domain.event.claim;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * Created by Mohan Sharma on 1/14/2016.
 */
@AllArgsConstructor
@Getter
@NoArgsConstructor
@ToString
public class GroupHealthCashlessClaimClosureEvent implements Serializable{
    private static final long serialVersionUID = 4401616796204536261L;
    private String groupHealthCashlessClaimId;
}