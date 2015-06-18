package com.pla.grouplife.quotation.application.command;

import com.pla.grouplife.quotation.domain.model.*;
import com.pla.grouplife.quotation.repository.GlQuotationRepository;
import com.pla.publishedlanguage.contract.IPremiumCalculator;
import com.pla.publishedlanguage.domain.model.ComputedPremiumDto;
import com.pla.publishedlanguage.domain.model.PremiumCalculationDto;
import com.pla.publishedlanguage.domain.model.PremiumFrequency;
import com.pla.publishedlanguage.domain.model.PremiumInfluencingFactor;
import com.pla.grouplife.quotation.domain.service.GroupLifeQuotationService;
import com.pla.grouplife.quotation.query.GLQuotationFinder;
import com.pla.grouplife.quotation.query.InsuredDto;
import com.pla.grouplife.quotation.query.PremiumDetailDto;
import com.pla.sharedkernel.identifier.CoverageId;
import com.pla.sharedkernel.identifier.PlanId;
import com.pla.sharedkernel.identifier.QuotationId;
import org.axonframework.commandhandling.annotation.CommandHandler;
import org.axonframework.repository.Repository;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.nthdimenzion.presentation.AppUtils.getAge;
import static org.nthdimenzion.utils.UtilValidator.isEmpty;
import static org.nthdimenzion.utils.UtilValidator.isNotEmpty;

/**
 * Created by Samir on 4/15/2015.
 */
@Component
public class GlQuotationCommandHandler {

    private Repository<GroupLifeQuotation> glQuotationMongoRepository;

    private GroupLifeQuotationService groupLifeQuotationService;

    private IPremiumCalculator premiumCalculator;

    private GLQuotationFinder glQuotationFinder;

    private GlQuotationRepository glQuotationRepository;


    @Autowired
    public GlQuotationCommandHandler(Repository<GroupLifeQuotation> glQuotationMongoRepository, GroupLifeQuotationService groupLifeQuotationService, IPremiumCalculator premiumCalculator, GLQuotationFinder glQuotationFinder, GlQuotationRepository glQuotationRepository) {
        this.glQuotationMongoRepository = glQuotationMongoRepository;
        this.groupLifeQuotationService = groupLifeQuotationService;
        this.glQuotationFinder = glQuotationFinder;
        this.premiumCalculator = premiumCalculator;
        this.glQuotationRepository = glQuotationRepository;
    }

    @CommandHandler
    public String createQuotation(CreateGLQuotationCommand createGLQuotationCommand) {
        GroupLifeQuotation groupLifeQuotation = groupLifeQuotationService.createQuotation(createGLQuotationCommand.getAgentId(), createGLQuotationCommand.getProposerName(), createGLQuotationCommand.getUserDetails());
        glQuotationMongoRepository.add(groupLifeQuotation);
        return groupLifeQuotation.getIdentifier().getQuotationId();
    }

    @CommandHandler
    public String updateWithProposer(UpdateGLQuotationWithProposerCommand updateGLQuotationWithProposerCommand) {
        GroupLifeQuotation groupLifeQuotation = glQuotationMongoRepository.load(new QuotationId(updateGLQuotationWithProposerCommand.getQuotationId()));
        boolean isVersioningRequire = groupLifeQuotation.requireVersioning();
        groupLifeQuotation = groupLifeQuotationService.updateWithProposer(groupLifeQuotation, updateGLQuotationWithProposerCommand.getProposerDto(), updateGLQuotationWithProposerCommand.getUserDetails());
        if (isVersioningRequire) {
            glQuotationMongoRepository.add(groupLifeQuotation);
        }
        return groupLifeQuotation.getIdentifier().getQuotationId();
    }

    @CommandHandler
    public String updateWithAgentDetail(UpdateGLQuotationWithAgentCommand updateGLQuotationWithAgentCommand) {
        GroupLifeQuotation groupLifeQuotation = glQuotationMongoRepository.load(new QuotationId(updateGLQuotationWithAgentCommand.getQuotationId()));
        boolean isVersioningRequire = groupLifeQuotation.requireVersioning();
        groupLifeQuotation = groupLifeQuotationService.updateWithAgent(groupLifeQuotation, updateGLQuotationWithAgentCommand.getAgentId(), updateGLQuotationWithAgentCommand.getUserDetails());
        if (isVersioningRequire) {
            glQuotationMongoRepository.add(groupLifeQuotation);
        }
        return groupLifeQuotation.getIdentifier().getQuotationId();
    }

    @CommandHandler
    public String generateQuotation(GenerateGLQuotationCommand generateGLQuotationCommand) {
        GroupLifeQuotation groupLifeQuotation = glQuotationMongoRepository.load(new QuotationId(generateGLQuotationCommand.getQuotationId()));
        List<GroupLifeQuotation> generatedQuotations = glQuotationRepository.findQuotationByQuotNumberAndStatusByExcludingGivenQuotId(groupLifeQuotation.getQuotationNumber(), groupLifeQuotation.getQuotationId(), QuotationStatus.GENERATED.name());
        groupLifeQuotation.generateQuotation(LocalDate.now(), generatedQuotations);
        return groupLifeQuotation.getIdentifier().getQuotationId();
    }

    @CommandHandler
    public String updateWithInsureDetail(UpdateGLQuotationWithInsuredCommand updateGLQuotationWithInsuredCommand) {
        List<InsuredDto> insuredDtos = updateGLQuotationWithInsuredCommand.getInsuredDtos();
        Set<Insured> insureds = insuredDtos.stream().map(new Function<InsuredDto, Insured>() {
            @Override
            public Insured apply(InsuredDto insuredDto) {
                InsuredDto.PlanPremiumDetailDto premiumDetail = insuredDto.getPlanPremiumDetail();
                String occupationClass = glQuotationFinder.getOccupationClass(insuredDto.getOccupationClass());
                BigDecimal basicAnnualPremium = premiumDetail.getPremiumAmount() != null ? premiumDetail.getPremiumAmount() :
                        computePlanBasicAnnualPremium(premiumDetail.getPlanId(), premiumDetail.getSumAssured().toPlainString(), String.valueOf(getAge(insuredDto.getDateOfBirth())), occupationClass, insuredDto.getGender().name(), 365, null);
                final InsuredBuilder[] insuredBuilder = {Insured.getInsuredBuilder(new PlanId(premiumDetail.getPlanId()), premiumDetail.getPlanCode(), basicAnnualPremium, premiumDetail.getSumAssured())};
                insuredBuilder[0].withCategory(insuredDto.getOccupationCategory()).withInsuredName(insuredDto.getSalutation(), insuredDto.getFirstName(), insuredDto.getLastName())
                        .withAnnualIncome(insuredDto.getAnnualIncome()).withOccupation(insuredDto.getOccupationClass()).
                        withInsuredNrcNumber(insuredDto.getNrcNumber()).withCompanyName(insuredDto.getCompanyName())
                        .withManNumber(insuredDto.getManNumber()).withDateOfBirth(insuredDto.getDateOfBirth()).withGender(insuredDto.getGender()).withNoOfAssured(insuredDto.getNoOfAssured());
                insuredDto.getCoveragePremiumDetails().forEach(coveragePremiumDetail -> {
                    BigDecimal coverageBasicPremium = coveragePremiumDetail.getPremium() == null ? computePlanBasicAnnualPremium(premiumDetail.getPlanId(), coveragePremiumDetail.getSumAssured().toPlainString(), String.valueOf(getAge(insuredDto.getDateOfBirth())), null, insuredDto.getGender().name(), 365, coveragePremiumDetail.getCoverageId()) : coveragePremiumDetail.getPremium();
                    insuredBuilder[0] = insuredBuilder[0].withCoveragePremiumDetail(coveragePremiumDetail.getCoverageName(), coveragePremiumDetail.getCoverageCode(), coveragePremiumDetail.getCoverageId(), coverageBasicPremium);
                });
                Set<InsuredDependent> insuredDependents = getInsuredDependent(insuredDto.getInsuredDependents());
                insuredBuilder[0] = insuredBuilder[0].withDependents(insuredDependents);
                return insuredBuilder[0].build();
            }
        }).collect(Collectors.toSet());
        GroupLifeQuotation groupLifeQuotation = glQuotationMongoRepository.load(new QuotationId(updateGLQuotationWithInsuredCommand.getQuotationId()));
        boolean isVersioningRequire = groupLifeQuotation.requireVersioning();
        groupLifeQuotation = groupLifeQuotationService.updateInsured(groupLifeQuotation, insureds, updateGLQuotationWithInsuredCommand.getUserDetails());
        PremiumDetailDto premiumDetailDto = new PremiumDetailDto(BigDecimal.valueOf(20), 365);
        groupLifeQuotation = groupLifeQuotationService.updateWithPremiumDetail(groupLifeQuotation, premiumDetailDto, updateGLQuotationWithInsuredCommand.getUserDetails());
        if (isVersioningRequire) {
            glQuotationMongoRepository.add(groupLifeQuotation);
        }
        return groupLifeQuotation.getIdentifier().getQuotationId();
    }


    @CommandHandler
    public String updateWithPremiumDetail(UpdateGLQuotationWithPremiumDetailCommand updateGLQuotationWithPremiumDetailCommand) {
        GroupLifeQuotation groupLifeQuotation = glQuotationMongoRepository.load(new QuotationId(updateGLQuotationWithPremiumDetailCommand.getQuotationId()));
        boolean isVersioningRequire = groupLifeQuotation.requireVersioning();
        groupLifeQuotation = populateAnnualBasicPremiumOfInsured(groupLifeQuotation, updateGLQuotationWithPremiumDetailCommand.getUserDetails(), updateGLQuotationWithPremiumDetailCommand.getPremiumDetailDto());
        if (isVersioningRequire) {
            glQuotationMongoRepository.add(groupLifeQuotation);
        }
        return groupLifeQuotation.getIdentifier().getQuotationId();
    }


    private GroupLifeQuotation populateAnnualBasicPremiumOfInsured(GroupLifeQuotation groupLifeQuotation, UserDetails userDetails, PremiumDetailDto premiumDetailDto) {
        if (premiumDetailDto.getPolicyTermValue() != 365) {
            Set<Insured> insureds = groupLifeQuotation.getInsureds();
            for (Insured insured : insureds) {
                PlanPremiumDetail planPremiumDetail = insured.getPlanPremiumDetail();
                String occupationClass = glQuotationFinder.getOccupationClass(insured.getOccupationClass());
                BigDecimal insuredPlanProratePremium = insured.getNoOfAssured() != null ? planPremiumDetail.getPremiumAmount() : computeBasicProratePremium(planPremiumDetail.getPlanId().getPlanId(), planPremiumDetail.getSumAssured().toPlainString(), getAge(insured.getDateOfBirth()).toString(), occupationClass, insured.getGender().name(), premiumDetailDto.getPolicyTermValue(), null);
                insured.updatePlanPremiumAmount(insuredPlanProratePremium);
                if (isNotEmpty(insured.getCoveragePremiumDetails())) {
                    Set<CoveragePremiumDetail> coveragePremiumDetails = insured.getCoveragePremiumDetails();
                    for (CoveragePremiumDetail coveragePremiumDetail : coveragePremiumDetails) {
                        BigDecimal insuredCoveragePremiumDetail = insured.getNoOfAssured() != null ? coveragePremiumDetail.getPremium() : computeBasicProratePremium(planPremiumDetail.getPlanId().getPlanId(), coveragePremiumDetail.getSumAssured().toPlainString(), getAge(insured.getDateOfBirth()).toString(), occupationClass, insured.getGender().name(), premiumDetailDto.getPolicyTermValue(), coveragePremiumDetail.getCoverageId().getCoverageId());
                        coveragePremiumDetail.updateWithPremium(insuredCoveragePremiumDetail);
                    }
                }
                for (InsuredDependent insuredDependent : insured.getInsuredDependents()) {
                    PlanPremiumDetail insuredDependentPlanPremiumDetail = insuredDependent.getPlanPremiumDetail();
                    String dependentOccupationClass = glQuotationFinder.getOccupationClass(insuredDependent.getOccupationClass());
                    BigDecimal insuredDependentPlanProratePremium = insuredDependent.getNoOfAssured() != null ? insuredDependentPlanPremiumDetail.getPremiumAmount() : computeBasicProratePremium(insuredDependentPlanPremiumDetail.getPlanId().getPlanId(), insuredDependentPlanPremiumDetail.getSumAssured().toPlainString(), getAge(insuredDependent.getDateOfBirth()).toString(), dependentOccupationClass, insuredDependent.getGender().name(), premiumDetailDto.getPolicyTermValue(), null);
                    insuredDependent.updatePlanPremiumAmount(insuredDependentPlanProratePremium);
                    Set<CoveragePremiumDetail> insuredDependentCoveragePremiumDetails = insuredDependent.getCoveragePremiumDetails();
                    if (isNotEmpty(insuredDependentCoveragePremiumDetails)) {
                        for (CoveragePremiumDetail insuredDependentCoveragePremiumDetail : insuredDependentCoveragePremiumDetails) {
                            BigDecimal insuredCoveragePremiumDetail = insuredDependent.getNoOfAssured() != null ? insuredDependentCoveragePremiumDetail.getPremium() : computeBasicProratePremium(insuredDependentPlanPremiumDetail.getPlanId().getPlanId(), insuredDependentCoveragePremiumDetail.getSumAssured().toPlainString(), getAge(insuredDependent.getDateOfBirth()).toString(), dependentOccupationClass, insuredDependent.getGender().name(), premiumDetailDto.getPolicyTermValue(), insuredDependentCoveragePremiumDetail.getCoverageId().getCoverageId());
                            insuredDependentCoveragePremiumDetail.updateWithPremium(insuredCoveragePremiumDetail);
                        }
                    }
                }
            }
            groupLifeQuotation = groupLifeQuotationService.updateInsured(groupLifeQuotation, insureds, userDetails);
        }
        groupLifeQuotation = groupLifeQuotationService.updateWithPremiumDetail(groupLifeQuotation, premiumDetailDto, userDetails);
        return groupLifeQuotation;
    }

    @CommandHandler
    public GroupLifeQuotation recalculatePremium(GLRecalculatedInsuredPremiumCommand glRecalculatedInsuredPremiumCommand) {
        GroupLifeQuotation groupLifeQuotation = glQuotationRepository.findOne(new QuotationId(glRecalculatedInsuredPremiumCommand.getQuotationId()));
        groupLifeQuotation = populateAnnualBasicPremiumOfInsured(groupLifeQuotation, glRecalculatedInsuredPremiumCommand.getUserDetails(), glRecalculatedInsuredPremiumCommand.getPremiumDetailDto());
        return groupLifeQuotation;

    }

    private Set<InsuredDependent> getInsuredDependent(Set<InsuredDto.InsuredDependentDto> insuredDependentDtos) {
        Set<InsuredDependent> insuredDependents = insuredDependentDtos.stream().map(new Function<InsuredDto.InsuredDependentDto, InsuredDependent>() {
            @Override
            public InsuredDependent apply(InsuredDto.InsuredDependentDto insuredDependentDto) {
                InsuredDto.PlanPremiumDetailDto premiumDetail = insuredDependentDto.getPlanPremiumDetail();
                String occupationClass = glQuotationFinder.getOccupationClass(insuredDependentDto.getOccupationClass());
                BigDecimal basicAnnualPremium = premiumDetail.getPremiumAmount() != null ? premiumDetail.getPremiumAmount() : computePlanBasicAnnualPremium(premiumDetail.getPlanId(), premiumDetail.getSumAssured().toPlainString(),
                        String.valueOf(getAge(insuredDependentDto.getDateOfBirth()))
                        , isNotEmpty(occupationClass) ? occupationClass : null, insuredDependentDto.getGender().name(), 365, null);
                final InsuredDependentBuilder[] insuredDependentBuilder = {InsuredDependent.getInsuredDependentBuilder(new PlanId(premiumDetail.getPlanId()), premiumDetail.getPlanCode(), basicAnnualPremium, premiumDetail.getSumAssured())};
                insuredDependentBuilder[0].withCategory(insuredDependentDto.getOccupationCategory()).withInsuredName(insuredDependentDto.getSalutation(), insuredDependentDto.getFirstName(), insuredDependentDto.getLastName())
                        .withInsuredNrcNumber(insuredDependentDto.getNrcNumber()).withCompanyName(insuredDependentDto.getCompanyName()).withOccupationClass(insuredDependentDto.getOccupationClass())
                        .withDateOfBirth(insuredDependentDto.getDateOfBirth()).withGender(insuredDependentDto.getGender())
                        .withRelationship(insuredDependentDto.getRelationship()).withNoOfAssured(insuredDependentDto.getNoOfAssured());
                insuredDependentDto.getCoveragePremiumDetails().forEach(coveragePremiumDetail -> {
                    BigDecimal coverageBasicPremium = coveragePremiumDetail.getPremium() == null ? computePlanBasicAnnualPremium(premiumDetail.getPlanId(), coveragePremiumDetail.getSumAssured().toPlainString(), String.valueOf(getAge(insuredDependentDto.getDateOfBirth())), null, insuredDependentDto.getGender().name(), 365, coveragePremiumDetail.getCoverageId()) : coveragePremiumDetail.getPremium();
                    insuredDependentBuilder[0] = insuredDependentBuilder[0].withCoveragePremiumDetail(coveragePremiumDetail.getCoverageName(), coveragePremiumDetail.getCoverageCode(), coveragePremiumDetail.getCoverageId(), coverageBasicPremium);
                });
                return insuredDependentBuilder[0].build();
            }
        }).collect(Collectors.toSet());
        return insuredDependents;
    }

    private BigDecimal computePlanBasicAnnualPremium(String planId, String sumAssured, String age, String occupationClass, String gender, int noOfDays, String coverageId) {
        List<PremiumInfluencingFactor> setUpPremiumInfluencingFactors = getPremiumInfluencingFactors(planId, coverageId, LocalDate.now());
        PremiumCalculationDto premiumCalculationDto = new PremiumCalculationDto(new PlanId(planId), LocalDate.now(), PremiumFrequency.ANNUALLY, noOfDays);
        if (findPremiumInfluencingFactor(PremiumInfluencingFactor.SUM_ASSURED, setUpPremiumInfluencingFactors) != null) {
            premiumCalculationDto.addInfluencingFactorItemValue(PremiumInfluencingFactor.SUM_ASSURED, sumAssured);
        }
        if (findPremiumInfluencingFactor(PremiumInfluencingFactor.AGE, setUpPremiumInfluencingFactors) != null) {
            premiumCalculationDto.addInfluencingFactorItemValue(PremiumInfluencingFactor.AGE, age);
        }
        if (isNotEmpty(occupationClass) && findPremiumInfluencingFactor(PremiumInfluencingFactor.OCCUPATION_CLASS, setUpPremiumInfluencingFactors) != null) {
            premiumCalculationDto.addInfluencingFactorItemValue(PremiumInfluencingFactor.OCCUPATION_CLASS, occupationClass);
        }
        if (findPremiumInfluencingFactor(PremiumInfluencingFactor.GENDER, setUpPremiumInfluencingFactors) != null) {
            premiumCalculationDto.addInfluencingFactorItemValue(PremiumInfluencingFactor.GENDER, gender);
        }
        if (findPremiumInfluencingFactor(PremiumInfluencingFactor.POLICY_TERM, setUpPremiumInfluencingFactors) != null) {
            premiumCalculationDto.addInfluencingFactorItemValue(PremiumInfluencingFactor.POLICY_TERM, String.valueOf(1));
        }
        if (findPremiumInfluencingFactor(PremiumInfluencingFactor.PREMIUM_PAYMENT_TERM, setUpPremiumInfluencingFactors) != null) {
            premiumCalculationDto.addInfluencingFactorItemValue(PremiumInfluencingFactor.PREMIUM_PAYMENT_TERM, String.valueOf(1));
        }
        if (isNotEmpty(coverageId)) {
            premiumCalculationDto = premiumCalculationDto.addCoverage(new CoverageId(coverageId));
        }
        List<ComputedPremiumDto> computedPremiums = premiumCalculator.calculateBasicPremium(premiumCalculationDto);
        BigDecimal annualBasicPremium = ComputedPremiumDto.getAnnualPremium(computedPremiums);
        return annualBasicPremium;
    }

    private BigDecimal computeBasicProratePremium(String planId, String sumAssured, String age, String occupationClass, String gender, int noOfDays, String coverageId) {
        List<PremiumInfluencingFactor> setUpPremiumInfluencingFactors = getPremiumInfluencingFactors(planId, coverageId, LocalDate.now());
        PremiumCalculationDto premiumCalculationDto = new PremiumCalculationDto(new PlanId(planId), LocalDate.now(), PremiumFrequency.ANNUALLY, noOfDays);
        if (findPremiumInfluencingFactor(PremiumInfluencingFactor.SUM_ASSURED, setUpPremiumInfluencingFactors) != null) {
            premiumCalculationDto.addInfluencingFactorItemValue(PremiumInfluencingFactor.SUM_ASSURED, sumAssured);
        }
        if (findPremiumInfluencingFactor(PremiumInfluencingFactor.AGE, setUpPremiumInfluencingFactors) != null) {
            premiumCalculationDto.addInfluencingFactorItemValue(PremiumInfluencingFactor.AGE, age);
        }
        if (isNotEmpty(occupationClass) && findPremiumInfluencingFactor(PremiumInfluencingFactor.OCCUPATION_CLASS, setUpPremiumInfluencingFactors) != null) {
            premiumCalculationDto.addInfluencingFactorItemValue(PremiumInfluencingFactor.OCCUPATION_CLASS, occupationClass);
        }
        if (findPremiumInfluencingFactor(PremiumInfluencingFactor.GENDER, setUpPremiumInfluencingFactors) != null) {
            premiumCalculationDto.addInfluencingFactorItemValue(PremiumInfluencingFactor.GENDER, gender);
        }
        if (findPremiumInfluencingFactor(PremiumInfluencingFactor.POLICY_TERM, setUpPremiumInfluencingFactors) != null) {
            premiumCalculationDto.addInfluencingFactorItemValue(PremiumInfluencingFactor.POLICY_TERM, String.valueOf(1));
        }
        if (findPremiumInfluencingFactor(PremiumInfluencingFactor.PREMIUM_PAYMENT_TERM, setUpPremiumInfluencingFactors) != null) {
            premiumCalculationDto.addInfluencingFactorItemValue(PremiumInfluencingFactor.PREMIUM_PAYMENT_TERM, String.valueOf(1));
        }
        if (isNotEmpty(coverageId)) {
            premiumCalculationDto = premiumCalculationDto.addCoverage(new CoverageId(coverageId));
        }
        BigDecimal installmentPremium = premiumCalculator.computeProratePremium(premiumCalculationDto);
        return installmentPremium;
    }

    private PremiumInfluencingFactor findPremiumInfluencingFactor(PremiumInfluencingFactor premiumInfluencingFactor, List<PremiumInfluencingFactor> premiumInfluencingFactors) {
        Optional<PremiumInfluencingFactor> premiumInfluencingFactorOptional = premiumInfluencingFactors.stream().filter(new Predicate<PremiumInfluencingFactor>() {
            @Override
            public boolean test(PremiumInfluencingFactor candidate) {
                return premiumInfluencingFactor.equals(candidate);
            }
        }).findAny();
        return premiumInfluencingFactorOptional.isPresent() ? premiumInfluencingFactorOptional.get() : null;
    }

    private List<PremiumInfluencingFactor> getPremiumInfluencingFactors(String planId, String coverageId, LocalDate effectiveDate) {
        if (isEmpty(coverageId)) {
            return premiumCalculator.getPremiumInfluencingFactors(new PlanId(planId), effectiveDate);

        }
        return premiumCalculator.getPremiumInfluencingFactors(new PlanId(planId), new CoverageId(coverageId), effectiveDate);
    }

    @CommandHandler
    public void purgeGLQuotation(PurgeGLQuotationCommand purgeGLQuotationCommand) {
        GroupLifeQuotation groupLifeQuotation = glQuotationMongoRepository.load(purgeGLQuotationCommand.getQuotationId());
        groupLifeQuotation.purgeQuotation();
    }

    @CommandHandler
    public void closureGLQuotation(ClosureGLQuotationCommand closureGLQuotationCommand) {
        GroupLifeQuotation groupLifeQuotation = glQuotationMongoRepository.load(closureGLQuotationCommand.getQuotationId());
        groupLifeQuotation.declineQuotation();
    }
}
