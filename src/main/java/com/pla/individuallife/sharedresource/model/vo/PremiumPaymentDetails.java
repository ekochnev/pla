package com.pla.individuallife.sharedresource.model.vo;

import com.pla.publishedlanguage.domain.model.PremiumFrequency;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.joda.time.DateTime;

/**
 * Created by Karunakar on 7/2/2015.
 */
@Getter
@Setter
@ToString
public class PremiumPaymentDetails {
    private PremiumDetail premiumDetail;
    private PremiumFrequency premiumFrequency;
    private PremiumPaymentMethod premiumPaymentMethod;
    private EmployerDetails employerDetails;
    private BankDetails bankDetails;
    private DateTime proposalSignDate;
}
