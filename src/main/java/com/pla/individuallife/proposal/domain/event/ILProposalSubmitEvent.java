package com.pla.individuallife.proposal.domain.event;

import com.pla.sharedkernel.identifier.ProposalId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Created by Admin on 7/29/2015.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ILProposalSubmitEvent {

    private ProposalId proposalId;

}
