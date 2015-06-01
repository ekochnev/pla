package com.pla.grouplife.quotation.application.command;

import com.pla.sharedkernel.identifier.QuotationId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Created by Samir on 5/31/2015.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PurgeGLQuotationCommand {

    private QuotationId quotationId;
}