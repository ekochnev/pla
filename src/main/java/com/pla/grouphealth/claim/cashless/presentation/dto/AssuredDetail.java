package com.pla.grouphealth.claim.cashless.presentation.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.joda.deser.DateTimeDeserializer;
import com.fasterxml.jackson.datatype.joda.deser.LocalDateDeserializer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.math.BigDecimal;

/**
 * Created by Mohan Sharma on 1/7/2016.
 */
@Getter
@Setter
@NoArgsConstructor
public class AssuredDetail {
    private String salutation;
    private String firstName;
    private String surname;
    private LocalDate dateOfBirth;
    private int ageNextBirthday;
    private String nrcNumber;
    private String gender;
    private BigDecimal sumAssured;
    private BigDecimal reserveAmount;
    private String category;
    private String manNumber;
    private String clientId;
}
