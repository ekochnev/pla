package com.pla.grouplife.proposal.application.command;

import com.pla.grouplife.sharedresource.dto.PremiumDetailDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Created by Samir on 4/14/2015.
 */
@Getter
@Setter
@NoArgsConstructor
public class UpdateGLProposalWithPremiumDetailCommand {

    private String proposalId;

    private PremiumDetailDto premiumDetailDto;

    private UserDetails userDetails;
}
