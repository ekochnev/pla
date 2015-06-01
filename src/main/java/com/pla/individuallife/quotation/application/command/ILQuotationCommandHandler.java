package com.pla.individuallife.quotation.application.command;

import com.pla.core.domain.model.agent.AgentId;
import com.pla.individuallife.quotation.domain.model.IndividualLifeQuotation;
import com.pla.individuallife.quotation.domain.service.IndividualLifeQuotationService;
import com.pla.publishedlanguage.contract.IPremiumCalculator;
import com.pla.sharedkernel.identifier.PlanId;
import com.pla.sharedkernel.identifier.QuotationId;
import org.axonframework.commandhandling.annotation.CommandHandler;
import org.axonframework.repository.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by Karunakar on 5/13/2015.
 */
@Component
public class ILQuotationCommandHandler {

    private Repository<IndividualLifeQuotation> ilQuotationJpaRepository;


    private IndividualLifeQuotationService individualLifeQuotationService;

    private IPremiumCalculator premiumCalculator;

    @Autowired
    public ILQuotationCommandHandler(Repository<IndividualLifeQuotation> ilQuotationJpaRepository, IndividualLifeQuotationService individualLifeQuotationService, IPremiumCalculator premiumCalculator) {
        this.ilQuotationJpaRepository = ilQuotationJpaRepository;
        this.individualLifeQuotationService = individualLifeQuotationService;
        this.premiumCalculator = premiumCalculator;
    }

    @CommandHandler
    public String createQuotation(CreateILQuotationCommand cmd) {
        IndividualLifeQuotation individualLifeQuotation = individualLifeQuotationService.createQuotation(new AgentId(cmd.getAgentId()), cmd.getTitle(), cmd.getFirstName(),
                cmd.getSurname(), cmd.getNrcNumber(), new PlanId(cmd.getPlanId()), cmd.getUserDetails());
        ilQuotationJpaRepository.add(individualLifeQuotation);
        return individualLifeQuotation.getIdentifier().getQuotationId();
    }

    @CommandHandler
    public String updateWithProposer(UpdateILQuotationWithProposerCommand updateILQuotationWithProposerCommand) {
        IndividualLifeQuotation individualLifeQuotation = ilQuotationJpaRepository.load((new QuotationId(updateILQuotationWithProposerCommand.getQuotationId())));
        individualLifeQuotation = individualLifeQuotationService.updateWithProposer(individualLifeQuotation, updateILQuotationWithProposerCommand.getProposerDto(), updateILQuotationWithProposerCommand.getAgentId(), updateILQuotationWithProposerCommand.getUserDetails());
        return individualLifeQuotation.getIdentifier().getQuotationId();
    }

    @CommandHandler
    public String updateWithAssured(UpdateILQuotationWithAssuredCommand updateILQuotationWithProposerCommand) {
        IndividualLifeQuotation individualLifeQuotation = ilQuotationJpaRepository.load((new QuotationId(updateILQuotationWithProposerCommand.getQuotationId())));
        IndividualLifeQuotation individualLifeQuotation2 = individualLifeQuotationService.updateWithAssured(individualLifeQuotation, updateILQuotationWithProposerCommand.getProposedAssured(), updateILQuotationWithProposerCommand.getIsAssuredTheProposer(), updateILQuotationWithProposerCommand.getUserDetails());
        if (individualLifeQuotation.equals(individualLifeQuotation))
            return individualLifeQuotation.getIdentifier().getQuotationId();
        else {
            ilQuotationJpaRepository.add(individualLifeQuotation2);
            return individualLifeQuotation2.getIdentifier().getQuotationId();
        }
    }

    @CommandHandler
    public String updateWithPlan(UpdateILQuotationWithPlanCommand updateILQuotationWithPlanCommand) {
        IndividualLifeQuotation individualLifeQuotation = ilQuotationJpaRepository.load((new QuotationId(updateILQuotationWithPlanCommand.getQuotationId())));
        individualLifeQuotation = individualLifeQuotationService.updateWithPlan(individualLifeQuotation, updateILQuotationWithPlanCommand.getPlanDetailDto(), updateILQuotationWithPlanCommand.getUserDetails());
        return individualLifeQuotation.getIdentifier().getQuotationId();
    }

}
