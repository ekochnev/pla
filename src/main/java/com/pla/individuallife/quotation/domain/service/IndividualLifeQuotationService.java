package com.pla.individuallife.quotation.domain.service;

import com.pla.core.domain.model.agent.AgentId;
import com.pla.individuallife.quotation.domain.model.*;
import com.pla.individuallife.quotation.presentation.dto.PlanDetailDto;
import com.pla.individuallife.quotation.presentation.dto.ProposedAssuredDto;
import com.pla.individuallife.quotation.presentation.dto.ProposerDto;
import com.pla.sharedkernel.identifier.PlanId;
import com.pla.sharedkernel.identifier.QuotationId;
import org.nthdimenzion.ddd.domain.annotations.DomainService;
import org.nthdimenzion.object.utils.IIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Created by Karunakar on 5/13/2015.
 */
@DomainService
public class IndividualLifeQuotationService {

    private ILQuotationRoleAdapter roleAdapter;

    private ILQuotationNumberGenerator ilQuotationNumberGenerator;

    private IIdGenerator idGenerator;

    @Autowired
    public IndividualLifeQuotationService(ILQuotationRoleAdapter roleAdapter, ILQuotationNumberGenerator ilquotationNumberGenerator, IIdGenerator idGenerator) {
        this.idGenerator = idGenerator;
        this.roleAdapter = roleAdapter;
        this.ilQuotationNumberGenerator = ilquotationNumberGenerator;
    }

    public IndividualLifeQuotation createQuotation(AgentId agentId, String assuredTitle, String assuredFName, String assuredSurname, String assuredNRC, PlanId planId, UserDetails userDetails) {
        ILQuotationProcessor IlQuotationProcessor = roleAdapter.userToQuotationProcessor(userDetails);
        QuotationId quotationId = new QuotationId(idGenerator.nextId());
        String quotationNumber = ilQuotationNumberGenerator.getQuotationNumber("5", "2", IndividualLifeQuotation.class);
        return IlQuotationProcessor.createIndividualLifeQuotation(quotationNumber, IlQuotationProcessor.getUserName(), quotationId, agentId, assuredTitle, assuredFName, assuredSurname, assuredNRC, planId);
    }

    public IndividualLifeQuotation updateWithProposer(IndividualLifeQuotation individualLifeQuotation, ProposerDto proposerDto, AgentId agentId, UserDetails userDetails) {
        ILQuotationProcessor IlQuotationProcessor = roleAdapter.userToQuotationProcessor(userDetails);
        individualLifeQuotation = checkQuotationNeedForVersioningAndGetQuotation(IlQuotationProcessor, individualLifeQuotation);
        ProposerBuilder proposerBuilder = Proposer.getProposerBuilder(proposerDto.getTitle(), proposerDto.getFirstName(), proposerDto.getSurname(), proposerDto.getNrcNumber(), proposerDto.getDateOfBirth(), proposerDto.getGender(), proposerDto.getMobileNumber(), proposerDto.getEmailAddress());
        return IlQuotationProcessor.updateWithProposerAndAgentId(individualLifeQuotation, proposerBuilder.build(), agentId);
    }

    public IndividualLifeQuotation updateWithAssured(IndividualLifeQuotation quotation, ProposedAssuredDto dto, Boolean isAssuredTheProposer, UserDetails userDetails) {
        ILQuotationProcessor quotationProcessor = roleAdapter.userToQuotationProcessor(userDetails);
        quotation = checkQuotationNeedForVersioningAndGetQuotation(quotationProcessor, quotation);
        ProposedAssuredBuilder proposedAssuredBuilder = ProposedAssured.proposedAssuredBuilder();
        proposedAssuredBuilder.withGender(dto.getGender()).withTitle(dto.getTitle()).withFirstName(dto.getFirstName()).withNrcNumber(dto.getNrcNumber()).withEmailAddress(dto.getEmailAddress()).withMobileNumber(dto.getMobileNumber()).withDateOfBirth(dto.getDateOfBirth()).withOccupation(dto.getOccupation()).withSurname(dto.getSurname());
        return quotationProcessor.updateWithAssured(quotation, proposedAssuredBuilder.build(), isAssuredTheProposer);
    }

    public IndividualLifeQuotation updateWithPlan(IndividualLifeQuotation individualLifeQuotation, PlanDetailDto planDetailDto, UserDetails userDetails) {
        ILQuotationProcessor IlQuotationProcessor = roleAdapter.userToQuotationProcessor(userDetails);
        individualLifeQuotation = checkQuotationNeedForVersioningAndGetQuotation(IlQuotationProcessor, individualLifeQuotation);
        PlanDetailBuilder planDetailBuilder = PlanDetail.getPlanDetailBuilder(planDetailDto.getPlanId(), planDetailDto.getPolicyTerm(), planDetailDto.getPremiumPaymentTerm(), planDetailDto.getSumAssured(), planDetailDto.getRiderDetails());
        return IlQuotationProcessor.updateWithPlan(individualLifeQuotation, planDetailBuilder.build());
    }

    private IndividualLifeQuotation checkQuotationNeedForVersioningAndGetQuotation(ILQuotationProcessor ILQuotationProcessor, IndividualLifeQuotation individualLifeQuotation) {
        if (!individualLifeQuotation.requireVersioning()) {
            return individualLifeQuotation;
        }
        String quotationNumber = ilQuotationNumberGenerator.getQuotationNumber("5", "2", IndividualLifeQuotation.class);
        QuotationId quotationId = new QuotationId(idGenerator.nextId());
        return individualLifeQuotation.cloneQuotation(quotationNumber, ILQuotationProcessor.getUserName(), quotationId);
    }


}