package com.pla.grouphealth.proposal.application.command;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.security.core.userdetails.UserDetails;

import javax.validation.constraints.NotNull;

/**
 * Created by Samir on 4/14/2015.
 */
@Getter
@Setter
@NoArgsConstructor
public class UpdateGHProposalWithAgentCommand {

    @NotNull(message = "{Agent ID cannot be null}")
    @NotEmpty(message = "{Agent ID cannot be empty}")
    private String agentId;

    @NotNull(message = "{Proposer name cannot be null}")
    @NotEmpty(message = "{Proposer name cannot be empty}")
    private String proposerName;

    private String proposalId;

    private UserDetails userDetails;
}
