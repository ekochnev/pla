package com.pla.quotation.application.command.grouplife;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.security.core.userdetails.UserDetails;

import javax.validation.constraints.NotNull;

/**
 * Created by Samir on 4/14/2015.
 */
@Getter
@Setter
public class CreateGLQuotationCommand {

    @NotNull(message = "{Agent ID cannot be null}")
    @NotEmpty(message = "{Agent ID cannot be empty}")
    private String agentId;

    @NotNull(message = "{Proposer name cannot be null}")
    @NotEmpty(message = "{Proposer name cannot be empty}")
    private String proposerName;

    private String agentName;

    private String teamName;

    private String branchName;

    private UserDetails userDetails;
}