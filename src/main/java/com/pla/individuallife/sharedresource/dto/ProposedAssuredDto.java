package com.pla.individuallife.sharedresource.dto;

import com.pla.individuallife.proposal.presentation.dto.EmploymentDto;
import com.pla.individuallife.proposal.presentation.dto.ResidentialAddressDto;
import com.pla.individuallife.proposal.presentation.dto.SpouseDto;
import com.pla.individuallife.sharedresource.model.vo.EmploymentDetail;
import com.pla.individuallife.sharedresource.model.vo.ResidentialAddress;
import com.pla.sharedkernel.domain.model.Gender;
import com.pla.sharedkernel.domain.model.MaritalStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

/*import org.hibernate.validator.constraints.NotEmpty;*/
/**
 * Created by Prasant on 26-May-15.
 */
@Getter
@Setter
@NoArgsConstructor
public class ProposedAssuredDto {

    private String title;
    private String firstName;
    private String surname;
    private String otherName;
    private String nrc;
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime dateOfBirth;
    private Gender gender;
    private String mobileNumber;
    private String emailAddress;
    private MaritalStatus maritalStatus;
    private ResidentialAddressDto residentialAddress;
    private EmploymentDto employment;
    private SpouseDto spouse;
    private String occupation;
    private String relationshipId;


    public ProposedAssuredDto(String title, String firstName, String surname, String nrc, DateTime dateOfBirth, Gender gender, String mobileNumber, String emailAddress, MaritalStatus maritalStatus, String spouseFirstName, String spouseLastName, String spouseEmailAddress, String spouseMobilNumber, EmploymentDetail employmentDetail, ResidentialAddress residentialAddress, String otherName,String relationshipId) {

        this.title = title;
        this.firstName = firstName;
        this.surname = surname;
        this.nrc = nrc;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.mobileNumber = mobileNumber;
        this.emailAddress = emailAddress;
        this.maritalStatus = maritalStatus;
        SpouseDto spouseDto = new SpouseDto();
        spouseDto.setEmailAddress(spouseEmailAddress);
        spouseDto.setMobileNumber(spouseMobilNumber);
        spouseDto.setFirstName(spouseFirstName);
        spouseDto.setSurname(spouseLastName);
        this.spouse = spouseDto;
        EmploymentDto eDto = new EmploymentDto();
        eDto.setAddress1(employmentDetail.getAddress().getAddress1());
        eDto.setAddress2(employmentDetail.getAddress().getAddress2());
        eDto.setEmployer(employmentDetail.getEmployer());
        eDto.setEmploymentDate(employmentDetail.getEmploymentDate());
        eDto.setEmploymentType(employmentDetail.getEmploymentTypeId());
        eDto.setOccupation(employmentDetail.getOccupationClass());
        eDto.setPostalCode(employmentDetail.getAddress().getPostalCode());
        eDto.setProvince(employmentDetail.getAddress().getProvince());
        eDto.setTown(employmentDetail.getAddress().getTown());
        eDto.setWorkPhone(employmentDetail.getWorkPhone());
        this.employment = eDto;
        this.otherName = otherName;
        ResidentialAddressDto rDto = new ResidentialAddressDto();
        rDto.setAddress1(residentialAddress.getAddress().getAddress1());
        rDto.setAddress2(residentialAddress.getAddress().getAddress2());
        rDto.setEmailAddress(residentialAddress.getEmailAddress());
        rDto.setHomePhone(residentialAddress.getHomePhone());
        rDto.setPostalCode(residentialAddress.getAddress().getPostalCode());
        rDto.setProvince(residentialAddress.getAddress().getProvince());
        rDto.setTown(residentialAddress.getAddress().getTown());
        this.residentialAddress = rDto;
        this.relationshipId = relationshipId;

    }

    public String getOccupation() {
        return occupation;
    }
}
