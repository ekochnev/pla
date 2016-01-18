package com.pla.individuallife.endorsement.domain.model;

import com.pla.sharedkernel.domain.model.FamilyId;
import lombok.Getter;

/**
 * Created by Samir on 8/4/2015.
 */
@Getter
public class ILAssuredNameEndorsement {

    private FamilyId familyId;

    private String salutation;

    private String firstName;

    private String lastName;

    public ILAssuredNameEndorsement(FamilyId familyId, String salutation, String firstName, String lastName) {
        this.familyId = familyId;
        this.salutation = salutation;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
