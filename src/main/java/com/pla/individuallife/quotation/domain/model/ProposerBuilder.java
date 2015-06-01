package com.pla.individuallife.quotation.domain.model;

import com.pla.sharedkernel.domain.model.Gender;
import lombok.Getter;
import org.joda.time.LocalDate;

import static com.google.common.base.Preconditions.checkArgument;
import static org.nthdimenzion.utils.UtilValidator.isNotEmpty;

/**
 * Created by Karunakar on 5/13/2015.
 */
@Getter
public class ProposerBuilder {

    private String proposerTitle;

    private String proposerFName;

    private String proposerSurname;

    private String proposerNRC;

    private LocalDate dateOfBirth;

    private Gender gender;

    private String mobileNumber;

    private String emailId;

    ProposerBuilder(String proposerTitle, String proposerFName, String proposerSurname, String proposerNRC, LocalDate dateOfBirth, Gender gender, String mobileNumber, String emailId) {
        checkArgument(isNotEmpty(proposerTitle));
        checkArgument(isNotEmpty(proposerFName));
        checkArgument(isNotEmpty(proposerSurname));
        checkArgument(isNotEmpty(proposerNRC));
        checkArgument(dateOfBirth != null);
        checkArgument(gender != null);
        checkArgument(isNotEmpty(mobileNumber));
        this.proposerTitle = proposerTitle;
        this.proposerFName = proposerFName;
        this.proposerSurname = proposerSurname;
        this.proposerNRC = proposerNRC;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.mobileNumber = mobileNumber;
        this.emailId = emailId;
    }

    public Proposer build() {
        return new Proposer(this);
    }
}