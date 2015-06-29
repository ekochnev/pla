package com.pla.individuallife.proposal.application.command;


import com.pla.sharedkernel.identifier.ProposalId;

import java.util.concurrent.TimeoutException;

/**
 * Created by Prasant on 26-May-15.
 */
public interface ILProposalCommandGateway {

    ProposalId createProposal(ILCreateProposalCommand proposalCommand)
            throws TimeoutException, InterruptedException;

    void updateCompulsoryHealthStatement(ILUpdateCompulsoryHealthStatementCommand updateCompulsoryHealthStatementCommand)
            throws  TimeoutException,InterruptedException;

    void updateFamilyPersonal(ILUpdateFamilyPersonalDetailsCommand questionCommand)
            throws TimeoutException, InterruptedException;


    String updateWithPlandetail(ILProposalUpdateWithPlanAndBeneficiariesCommand updateWithPlanAndBeneficiariesCommand)
            throws TimeoutException, InterruptedException;

    String updateWithProposer(ILProposalUpdateWithProposerCommand cmd)
            throws TimeoutException, InterruptedException;
}
