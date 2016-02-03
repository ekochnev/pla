package com.pla.grouplife.claim.domain.model;

import com.pla.sharedkernel.identifier.PlanId;
import lombok.*;
import org.nthdimenzion.ddd.domain.annotations.ValueObject;

import java.math.BigDecimal;

/**
 * Created by Mirror on 8/19/2015.
 */
@ValueObject
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
@Setter(value = AccessLevel.PACKAGE)
@AllArgsConstructor

public class PlanDetail {

    private PlanId planId;;

    private String planName;

    private String planCode;

    private BigDecimal premiumAmount;

    private BigDecimal sumAssured;
}
