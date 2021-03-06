package com.pla.grouplife.quotation.domain.event;

import com.pla.sharedkernel.identifier.QuotationId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Created by Samir on 4/8/2015.
 */
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GLQuotationSharedEvent {

    private QuotationId quotationId;

}
