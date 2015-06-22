package com.pla.individuallife.proposal.application.command;

import com.pla.individuallife.proposal.domain.model.ProposalPlanDetail;
import com.pla.individuallife.proposal.presentation.dto.ProposedAssuredDto;
import com.pla.individuallife.proposal.presentation.dto.ProposerDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Created by Prasant on 26-May-15.
 */
@Getter
@Setter
@NoArgsConstructor
public class ILCreateProposalCommand {

    private ProposedAssuredDto proposedAssured;
    private ProposerDto proposer;
    private ProposalPlanDetail planDetail;
    private UserDetails userDetails;

    private String proposalId;

}