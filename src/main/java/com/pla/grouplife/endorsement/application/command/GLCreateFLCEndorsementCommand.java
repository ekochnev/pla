package com.pla.grouplife.endorsement.application.command;

import com.pla.grouplife.sharedresource.model.GLEndorsementType;
import com.pla.grouplife.sharedresource.model.vo.Insured;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Created by Admin on 28-Dec-15.
 */
@Getter
@Setter
@AllArgsConstructor
public class GLCreateFLCEndorsementCommand {
    private GLEndorsementType endorsementType;
    private String policyId;
    private Insured insured;
    private BigDecimal freeCoverLimit;
}