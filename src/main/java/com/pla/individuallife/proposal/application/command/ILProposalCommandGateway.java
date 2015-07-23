package com.pla.individuallife.proposal.application.command;


import com.pla.sharedkernel.identifier.ProposalId;

import java.util.concurrent.TimeoutException;

/**
 * Created by Prasant on 26-May-15.
 */
public interface ILProposalCommandGateway {

    ProposalId createProposal(ILCreateProposalCommand proposalCommand)
            throws TimeoutException, InterruptedException;

    void updateCompulsoryHealthStatement(ILProposalUpdateCompulsoryHealthStatementCommand updateCompulsoryHealthStatementCommand)
            throws  TimeoutException,InterruptedException;

    void updateFamilyPersonal(ILProposalUpdateFamilyPersonalDetailsCommand questionCommand)
            throws TimeoutException, InterruptedException;


    String updateWithPlandetail(ILProposalUpdateWithPlanAndBeneficiariesCommand updateWithPlanAndBeneficiariesCommand)
            throws TimeoutException, InterruptedException;

    String updateWithProposer(ILProposalUpdateWithProposerCommand cmd)
            throws TimeoutException, InterruptedException;

    void updateGeneralDetails(ILProposalUpdateGeneralDetailsCommand cmd)
            throws TimeoutException, InterruptedException;

    void updateAdditionalDetails(ILProposalUpdateAdditionalDetailsCommand cmd)
            throws TimeoutException, InterruptedException;

    void updatePremiumPaymentDetails(ILProposalUpdatePremiumPaymentDetailsCommand cmd)
            throws TimeoutException, InterruptedException;

    void uploadMandatoryDocument(ILProposalDocumentCommand cmd)
            throws TimeoutException, InterruptedException;

    void updateProposedAssuredAndAgents(ILUpdateProposalWithProposedAssuredCommand cmd)
            throws TimeoutException, InterruptedException;

    void submitProposal(SubmitILProposalCommand cmd)
            throws TimeoutException, InterruptedException;

    void approveProposal(ILProposalApprovalCommand cmd)
            throws TimeoutException, InterruptedException;

    void returnProposal(ILProposalApprovalCommand cmd)
            throws TimeoutException, InterruptedException;

    void holdProposal(ILProposalApprovalCommand cmd)
            throws TimeoutException, InterruptedException;

    void rejectProposal(ILProposalApprovalCommand cmd)
            throws TimeoutException, InterruptedException;

    void routeToNextLevel(ILProposalUnderwriterNextLevelCommand cmd)
            throws TimeoutException, InterruptedException;
}
