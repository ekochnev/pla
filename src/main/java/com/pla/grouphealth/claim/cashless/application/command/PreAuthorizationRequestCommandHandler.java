package com.pla.grouphealth.claim.cashless.application.command;

import com.pla.grouphealth.claim.cashless.application.service.PreAuthorizationRequestService;
import com.pla.grouphealth.claim.cashless.domain.exception.GenerateReminderFollowupException;
import com.pla.grouphealth.claim.cashless.domain.exception.RoutingLevelNotFoundException;
import com.pla.grouphealth.claim.cashless.domain.model.CommentDetail;
import com.pla.grouphealth.claim.cashless.domain.model.PreAuthorizationRequest;
import com.pla.grouphealth.claim.cashless.presentation.dto.PreAuthorizationClaimantDetailCommand;
import com.pla.grouphealth.sharedresource.model.vo.GHProposerDocument;
import com.pla.publishedlanguage.dto.UnderWriterRoutingLevelDetailDto;
import com.pla.sharedkernel.domain.model.ProcessType;
import com.pla.sharedkernel.domain.model.RoutingLevel;
import com.pla.underwriter.domain.model.UnderWriterInfluencingFactor;
import org.axonframework.commandhandling.annotation.CommandHandler;
import org.axonframework.repository.Repository;
import org.joda.time.LocalDate;
import org.nthdimenzion.utils.UtilValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.axonframework.common.Assert.isTrue;
import static org.nthdimenzion.utils.UtilValidator.isEmpty;
import static org.nthdimenzion.utils.UtilValidator.isNotEmpty;
import static org.springframework.util.Assert.notNull;

/**
 * Author - Mohan Sharma Created on 1/4/2016.
 */
@Component
public class PreAuthorizationRequestCommandHandler {
    @Autowired
    PreAuthorizationRequestService preAuthorizationRequestService;
    @Autowired
    private Repository<PreAuthorizationRequest> preAuthorizationRequestMongoRepository;

    @CommandHandler
    public String updatePreAuthorizationRequest(UpdatePreAuthorizationCommand updatePreAuthorizationCommand) throws GenerateReminderFollowupException, RoutingLevelNotFoundException {
        PreAuthorizationClaimantDetailCommand preAuthorizationClaimantDetailCommand = updatePreAuthorizationCommand.getPreAuthorizationClaimantDetailCommand();
        String preAuthorizationRequestId = preAuthorizationClaimantDetailCommand.getPreAuthorizationRequestId();
        notNull(preAuthorizationRequestId ,"PreAuthorizationRequestId is empty for the record");
        PreAuthorizationRequest preAuthorizationRequest = preAuthorizationRequestMongoRepository.load(preAuthorizationRequestId);
        preAuthorizationRequest = populateDetailsToPreAuthorization(preAuthorizationClaimantDetailCommand, preAuthorizationRequest);
        preAuthorizationRequest.updateStatus(PreAuthorizationRequest.Status.EVALUATION)
                .updateWithProcessorUserId(preAuthorizationClaimantDetailCommand.getPreAuthProcessorUserId());
        if(preAuthorizationClaimantDetailCommand.isSubmitEventFired()) {
            RoutingLevel routingLevel = updatePreAuthorizationCommand.getRoutingLevel();
            if(routingLevel.equals(RoutingLevel.UNDERWRITING_LEVEL_ONE))
                preAuthorizationRequest.updateStatus(PreAuthorizationRequest.Status.UNDERWRITING_LEVEL1);
            if(routingLevel.equals(RoutingLevel.UNDERWRITING_LEVEL_TWO))
                preAuthorizationRequest.updateStatus(PreAuthorizationRequest.Status.UNDERWRITING_LEVEL1);
            preAuthorizationRequest
                    .updatePreAuthorizationSubmitted(Boolean.TRUE)
                    .updateWithSubmittedDate(LocalDate.now());
        }
        return preAuthorizationRequest.getIdentifier();
    }

    @CommandHandler
    public Set<CommentDetail> updateComments(UpdateCommentCommand updateCommentCommand){
        return null;//preAuthorizationRequestService.updateComments(updateCommentCommand);
    }

    @CommandHandler
    public boolean approvePreAuthorization(ApprovePreAuthorizationCommand approvePreAuthorizationCommand){
        PreAuthorizationClaimantDetailCommand preAuthorizationClaimantDetailCommand = approvePreAuthorizationCommand.getPreAuthorizationClaimantDetailCommand();
        String preAuthorizationRequestId = preAuthorizationClaimantDetailCommand.getPreAuthorizationRequestId();
        notNull(preAuthorizationRequestId ,"PreAuthorizationRequestId is empty for the record");
        PreAuthorizationRequest preAuthorizationRequest = preAuthorizationRequestMongoRepository.load(preAuthorizationRequestId);
        preAuthorizationRequest = populateDetailsToPreAuthorization(preAuthorizationClaimantDetailCommand, preAuthorizationRequest);
        preAuthorizationRequest.updateStatus(PreAuthorizationRequest.Status.APPROVED);
        return Boolean.TRUE;
    }

    @CommandHandler
    public boolean rejectPreAuthorization(RejectPreAuthorizationCommand rejectPreAuthorizationCommand){
        PreAuthorizationClaimantDetailCommand preAuthorizationClaimantDetailCommand = rejectPreAuthorizationCommand.getPreAuthorizationClaimantDetailCommand();
        String preAuthorizationRequestId = preAuthorizationClaimantDetailCommand.getPreAuthorizationRequestId();
        notNull(preAuthorizationRequestId ,"PreAuthorizationRequestId is empty for the record");
        PreAuthorizationRequest preAuthorizationRequest = preAuthorizationRequestMongoRepository.load(preAuthorizationRequestId);
        preAuthorizationRequest = populateDetailsToPreAuthorization(preAuthorizationClaimantDetailCommand, preAuthorizationRequest);
        preAuthorizationRequest.updateStatus(PreAuthorizationRequest.Status.REJECTED);
        return Boolean.TRUE;
    }

    @CommandHandler
    public boolean returnByUnderwriter(ReturnPreAuthorizationCommand returnPreAuthorizationCommand) {
        PreAuthorizationClaimantDetailCommand preAuthorizationClaimantDetailCommand = returnPreAuthorizationCommand.getPreAuthorizationClaimantDetailCommand();
        String preAuthorizationRequestId = preAuthorizationClaimantDetailCommand.getPreAuthorizationRequestId();
        notNull(preAuthorizationRequestId ,"PreAuthorizationRequestId is empty for the record");
        PreAuthorizationRequest preAuthorizationRequest = preAuthorizationRequestMongoRepository.load(preAuthorizationRequestId);
        preAuthorizationRequest = populateDetailsToPreAuthorization(preAuthorizationClaimantDetailCommand, preAuthorizationRequest);
        preAuthorizationRequest.updateStatus(PreAuthorizationRequest.Status.RETURNED);
        return Boolean.TRUE;
    }

    @CommandHandler
    public boolean routeToSeniorUnderwriter(RoutePreAuthorizationCommand routePreAuthorizationCommand) {
        PreAuthorizationClaimantDetailCommand preAuthorizationClaimantDetailCommand = routePreAuthorizationCommand.getPreAuthorizationClaimantDetailCommand();
        String preAuthorizationRequestId = preAuthorizationClaimantDetailCommand.getPreAuthorizationRequestId();
        notNull(preAuthorizationRequestId ,"PreAuthorizationRequestId is empty for the record");
        PreAuthorizationRequest preAuthorizationRequest = preAuthorizationRequestMongoRepository.load(preAuthorizationRequestId);
        preAuthorizationRequest = populateDetailsToPreAuthorization(preAuthorizationClaimantDetailCommand, preAuthorizationRequest);
        /*
        * logic to get the senior underwriter userId
        * preAuthorizationRequest.updateWithPreAuthorizationUnderWriterUserId(userId);
        * */
        preAuthorizationRequest.updateStatus(PreAuthorizationRequest.Status.UNDERWRITING_LEVEL2);
        return Boolean.TRUE;
    }

    @CommandHandler
    public boolean removeAdditionalDocument(PreAuthorizationRemoveAdditionalCommand preAuthorizationRemoveAdditionalCommand) {
        boolean result = Boolean.FALSE;
        PreAuthorizationRequest preAuthorizationRequest = preAuthorizationRequestMongoRepository.load(preAuthorizationRemoveAdditionalCommand.getPreAuthorizationId());
        if(isNotEmpty(preAuthorizationRequest)){
            Set<GHProposerDocument> ghProposerDocuments = preAuthorizationRequest.getProposerDocuments();
            result =  removeDocumentByGridFsDocId(ghProposerDocuments, preAuthorizationRemoveAdditionalCommand.getGridFsDocId());
        }
        return result;
    }

    private PreAuthorizationRequest populateDetailsToPreAuthorization(PreAuthorizationClaimantDetailCommand preAuthorizationClaimantDetailCommand, PreAuthorizationRequest preAuthorizationRequest) {
        preAuthorizationRequest
                .updateWithPreAuthorizationDate(preAuthorizationClaimantDetailCommand.getPreAuthorizationDate())
                .updateWithCategory(preAuthorizationClaimantDetailCommand.getClaimantPolicyDetailDto())
                .updateWithRelationship(preAuthorizationClaimantDetailCommand.getClaimantPolicyDetailDto())
                .updateWithClaimType(preAuthorizationClaimantDetailCommand.getClaimType())
                .updateWithClaimIntimationDate(preAuthorizationClaimantDetailCommand.getClaimIntimationDate())
                .updateWithBatchNumber(preAuthorizationClaimantDetailCommand.getBatchNumber())
                .updateWithProposerDetail(preAuthorizationClaimantDetailCommand.getClaimantPolicyDetailDto())
                .updateWithPreAuthorizationRequestPolicyDetail(preAuthorizationClaimantDetailCommand)
                .updateWithPreAuthorizationRequestHCPDetail(preAuthorizationClaimantDetailCommand.getClaimantHCPDetailDto())
                .updateWithPreAuthorizationRequestDiagnosisTreatmentDetail(preAuthorizationClaimantDetailCommand.getDiagnosisTreatmentDtos())
                .updateWithPreAuthorizationRequestIllnessDetail(preAuthorizationClaimantDetailCommand.getIllnessDetailDto())
                .updateWithPreAuthorizationRequestDrugService(preAuthorizationClaimantDetailCommand.getDrugServicesDtos())
                .updateWithComments(preAuthorizationClaimantDetailCommand.getCommentDetails());
        return preAuthorizationRequest;
    }

    private boolean removeDocumentByGridFsDocId(Set<GHProposerDocument> ghProposerDocuments, String gridFsDocId) {
        if(UtilValidator.isNotEmpty(ghProposerDocuments)) {
            for (Iterator iterator = ghProposerDocuments.iterator(); iterator.hasNext(); ) {
                GHProposerDocument ghProposerDocument = (GHProposerDocument) iterator.next();
                if (ghProposerDocument.getGridFsDocId().equals(gridFsDocId)) {
                    iterator.remove();
                    return Boolean.TRUE;
                }
            }
        }
        return Boolean.FALSE;
    }

}
