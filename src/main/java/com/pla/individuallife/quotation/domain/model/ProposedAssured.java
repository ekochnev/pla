package com.pla.individuallife.quotation.domain.model;

import com.pla.sharedkernel.domain.model.Gender;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;
import org.joda.time.Years;
import org.nthdimenzion.ddd.domain.annotations.ValueObject;

import javax.persistence.*;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Created by Karunakar on 5/18/2015.
 */
@Embeddable
@Table(name = "assured")
@ValueObject
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
@Setter(value = AccessLevel.PACKAGE)
public class ProposedAssured {

    private String assuredTitle;

    private String assuredFName;

    private String assuredSurname;

    private String assuredNRC;

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
    private LocalDate assuredDateOfBirth;

    @Transient
    private Number ageNextBirthDay;

    @Enumerated(EnumType.STRING)
    private Gender assuredGender;

    private String assuredMobileNumber;

    private String assuredEmailId;

    private String occupation;



    ProposedAssured(ProposedAssuredBuilder proposedAssuredBuilder) {
        checkArgument(proposedAssuredBuilder != null);
        this.assuredTitle = proposedAssuredBuilder.getAssuredTitle();
        this.assuredFName = proposedAssuredBuilder.getAssuredFName();
        this.assuredSurname = proposedAssuredBuilder.getAssuredSurname();
        this.assuredNRC = proposedAssuredBuilder.getAssuredNRC();
        this.assuredDateOfBirth = proposedAssuredBuilder.getDateOfBirth();
        this.assuredGender = proposedAssuredBuilder.getGender();
        this.assuredMobileNumber = proposedAssuredBuilder.getMobileNumber();
        this.assuredEmailId = proposedAssuredBuilder.getEmailId();
        this.occupation = proposedAssuredBuilder.getOccupation();
        this.ageNextBirthDay = proposedAssuredBuilder.getAgeNextBirthDay();
    }


    public static ProposedAssuredBuilder getAssuredBuilder( String title, String firstName, String surname, String nrc ) {
        return new ProposedAssuredBuilder( title,firstName,  surname,  nrc);
    }

    public static ProposedAssuredBuilder getAssuredBuilder( String title, String firstName, String surname, String nrc, LocalDate dateOfBirth, Number ageNextBirthDay, Gender gender, String mobileNumber, String emailId, String occupation ) {
        return new ProposedAssuredBuilder( title,firstName,  surname,  nrc, dateOfBirth, ageNextBirthDay, gender, mobileNumber, emailId,occupation );
    }

    public Number getAgeNextBirthDay() {
        return Years.yearsBetween(assuredDateOfBirth, LocalDate.now()).getYears() + 1;
    }
}
