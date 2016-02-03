package com.pla.grouplife.claim.domain.model;

import lombok.*;
import org.joda.time.LocalDate;

import java.util.List;

/**
 * Created by ak
 */
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
@Setter(value = AccessLevel.PACKAGE)
@AllArgsConstructor

public class DisabilityClaimRegistration {

    private LocalDate dateOfDisability;
    private DisabilityNature natureOfDisability;
    private DisabilityExtent  extendOfDisability;
    private LocalDate dateOfDiagnosis;
    private String  exactDiagnosis;
    private String nameOfDoctorAndHospitalAddress;
    private String contactNumberOfHospital;
    private LocalDate  dateOfFirstConsultation;
    private String treatmentTaken;
    private List<AssuredTaskOfDailyLiving> capabilityOfAssuredDailyLiving;
    private String assuredGainfulActivities;
    private String detailsOfWorkActivities;
    private LocalDate fromActivitiesDate;
    private AssuredConfinedToHouse assuredConfinedToIndoor;
    private LocalDate fromIndoorDate;
    private String assuredIndoorDetails;
    private AssuredOutdoorActive  assuredAbleToGetOutdoor;
    private LocalDate fromOutdoorDate;
    private String assuredOutdoorDetails;
    private String visitingMedicalOfficerDetails;

}
