package com.pla.grouphealth.claim.cashless.application.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mongodb.gridfs.GridFSDBFile;
import com.pla.core.SBCM.domain.model.ServiceBenefitCoverageMapping;
import com.pla.core.SBCM.repository.SBCMRepository;
import com.pla.core.domain.model.plan.Plan;
import com.pla.core.domain.model.plan.PlanCoverage;
import com.pla.core.domain.model.plan.PlanCoverageBenefit;
import com.pla.core.domain.model.plan.PlanDetail;
import com.pla.core.dto.CoverageDto;
import com.pla.core.hcp.domain.model.HCP;
import com.pla.core.hcp.domain.model.HCPCode;
import com.pla.core.hcp.domain.model.HCPRate;
import com.pla.core.hcp.domain.model.HCPServiceDetail;
import com.pla.core.hcp.query.HCPFinder;
import com.pla.core.hcp.repository.HCPRateRepository;
import com.pla.core.query.CoverageFinder;
import com.pla.core.repository.PlanRepository;
import com.pla.grouphealth.claim.cashless.application.command.UpdateCommentCommand;
import com.pla.grouphealth.claim.cashless.domain.exception.GenerateReminderFollowupException;
import com.pla.grouphealth.claim.cashless.domain.model.*;
import com.pla.grouphealth.claim.cashless.presentation.dto.*;
import com.pla.grouphealth.claim.cashless.query.PreAuthorizationFinder;
import com.pla.grouphealth.claim.cashless.repository.PreAuthorizationRepository;
import com.pla.grouphealth.claim.cashless.repository.PreAuthorizationRequestRepository;
import com.pla.grouphealth.policy.domain.model.GroupHealthPolicy;
import com.pla.grouphealth.policy.domain.model.PolicyStatus;
import com.pla.grouphealth.policy.repository.GHPolicyRepository;
import com.pla.grouphealth.proposal.presentation.dto.GHProposalMandatoryDocumentDto;
import com.pla.grouphealth.sharedresource.model.vo.*;
import com.pla.publishedlanguage.dto.ClientDocumentDto;
import com.pla.publishedlanguage.dto.SearchDocumentDetailDto;
import com.pla.publishedlanguage.underwriter.contract.IUnderWriterAdapter;
import com.pla.sharedkernel.domain.model.CoverageBenefitDefinition;
import com.pla.sharedkernel.domain.model.FamilyId;
import com.pla.sharedkernel.domain.model.ProcessType;
import com.pla.sharedkernel.domain.model.Relationship;
import com.pla.sharedkernel.identifier.BenefitId;
import com.pla.sharedkernel.identifier.CoverageId;
import lombok.NoArgsConstructor;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.nthdimenzion.axonframework.repository.GenericMongoRepository;
import org.nthdimenzion.ddd.domain.annotations.DomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.nthdimenzion.utils.UtilValidator.isEmpty;
import static org.nthdimenzion.utils.UtilValidator.isNotEmpty;
import static org.springframework.data.domain.Sort.*;
import static org.springframework.util.Assert.*;
import static org.springframework.util.Assert.notNull;

/**
 * Author - Mohan Sharma Created on 1/6/2016.
 */
@DomainService
@NoArgsConstructor
public class PreAuthorizationRequestService {
    @Autowired
    private PreAuthorizationRepository preAuthorizationRepository;
    @Autowired
    private HCPFinder hcpFinder;
    @Autowired
    private GHPolicyRepository ghPolicyRepository;
    @Autowired
    private PlanRepository planRepository;
    @Autowired
    private GenericMongoRepository<PreAuthorizationRequest> preAuthorizationRequestMongoRepository;
    @Autowired
    private PreAuthorizationRequestRepository preAuthorizationRequestRepository;
    @Autowired
    private IUnderWriterAdapter underWriterAdapter;
    @Autowired
    private GridFsTemplate gridFsTemplate;
    @Autowired
    private PreAuthorizationFinder preAuthorizationFinder;
    @Autowired
    CoverageFinder coverageFinder;
    @Autowired
    SBCMRepository sbcmRepository;
    @Autowired
    HCPRateRepository hcpRateRepository;

    @Transactional
    public PreAuthorizationClaimantDetailCommand getPreAuthorizationByPreAuthorizationIdAndClientId(PreAuthorizationId preAuthorizationId, String clientId) {
        PreAuthorization preAuthorization = preAuthorizationRepository.findOne(preAuthorizationId);
        if(isNotEmpty(preAuthorization)){
            return constructPreAuthorizationClaimantDetailDtoFromPreAuthorization(preAuthorization, clientId);
        }
        return PreAuthorizationClaimantDetailCommand.getInstance();
    }

    private PreAuthorizationClaimantDetailCommand constructPreAuthorizationClaimantDetailDtoFromPreAuthorization(PreAuthorization preAuthorization, String clientId) {
        PreAuthorizationDetail preAuthorizationDetail = preAuthorization.getPreAuthorizationDetails().iterator().next();
        notNull(preAuthorizationDetail, "PreAuthorizationDetail cannot be null");
        PreAuthorizationClaimantDetailCommand preAuthorizationClaimantDetailCommand = new PreAuthorizationClaimantDetailCommand();
        preAuthorizationClaimantDetailCommand.updateWithBatchNumber(preAuthorization.getBatchNumber())
                .updateWithPreAuthorizationId(preAuthorization.getPreAuthorizationId())
                .updateWithPreAuthorizationDate(preAuthorization.getBatchDate().toLocalDate())
                .updateWithClaimantHCPDetailDto(constructClaimantHCPDetailDto(preAuthorization.getHcpCode(), preAuthorizationDetail.getHospitalizationEvent()))
                .updateWithClaimantPolicyDetailDto(constructClaimantPolicyDetailDto(preAuthorizationDetail.getPolicyNumber(), clientId, preAuthorization.getPreAuthorizationDetails(), preAuthorization.getHcpCode()))
                .updateWithDiagnosisTreatment(constructDiagnosisTreatmentDto(preAuthorization))
                .updateWithIllnessDetails(constructIllnessDetailDto(preAuthorization))
                .updateWithDrugServices(constructDrugServiceDtos(preAuthorization));
        return preAuthorizationClaimantDetailCommand;
    }

    private List<DrugServiceDto> constructDrugServiceDtos(PreAuthorization preAuthorization) {
        return isNotEmpty(preAuthorization.getPreAuthorizationDetails()) ? preAuthorization.getPreAuthorizationDetails().parallelStream().map(new Function<PreAuthorizationDetail, DrugServiceDto>() {
            @Override
            public DrugServiceDto apply(PreAuthorizationDetail preAuthorizationDetail) {
                return new DrugServiceDto()
                        .updateWithDetails(preAuthorizationDetail);
            }
        }).collect(Collectors.toList()) : Lists.newArrayList();
    }

    private IllnessDetailDto constructIllnessDetailDto(PreAuthorization preAuthorization) {
        PreAuthorizationDetail preAuthorizationDetail = isNotEmpty(preAuthorization.getPreAuthorizationDetails()) ? preAuthorization.getPreAuthorizationDetails().iterator().next() : null;
        if(isNotEmpty(preAuthorizationDetail)){
            return new IllnessDetailDto().updateWithDetails(preAuthorizationDetail);
        }
        return null;
    }

    private List<DiagnosisTreatmentDto> constructDiagnosisTreatmentDto(PreAuthorization preAuthorization) {
        return isNotEmpty(preAuthorization.getPreAuthorizationDetails()) ? preAuthorization.getPreAuthorizationDetails().parallelStream().map(new Function<PreAuthorizationDetail, DiagnosisTreatmentDto>() {
            @Override
            public DiagnosisTreatmentDto apply(PreAuthorizationDetail preAuthorizationDetail) {
                return new DiagnosisTreatmentDto()
                        .updateWithDetails(preAuthorizationDetail);
            }
        }).collect(Collectors.toList()) : Lists.newArrayList();
    }

    private ClaimantPolicyDetailDto constructClaimantPolicyDetailDto(String policyNumber, String clientId, Set<PreAuthorizationDetail> preAuthorizationDetails, HCPCode hcpCode) {
        GroupHealthPolicy groupHealthPolicy = ghPolicyRepository.findPolicyByPolicyNumber(policyNumber);
        if(isNotEmpty(groupHealthPolicy)){
            GHProposer ghProposer = groupHealthPolicy.getProposer();
            if(isNotEmpty(ghProposer)) {
                ClaimantPolicyDetailDto claimantPolicyDetailDto = new ClaimantPolicyDetailDto()
                        .updateWithPreAuthorizationClaimantProposerDetail(ghProposer.getContactDetail(), ghProposer.getProposerName(), ghProposer.getProposerCode())
                        .updateWithPolicyName(groupHealthPolicy.getSchemeName())
                        .updateWithPolicyNumber(isNotEmpty(groupHealthPolicy.getPolicyNumber()) ? groupHealthPolicy.getPolicyNumber().getPolicyNumber() : StringUtils.EMPTY);
                return updateWithPlanDetailsToClaimantDto(groupHealthPolicy, claimantPolicyDetailDto, clientId, preAuthorizationDetails, hcpCode);
            }

        }
        return ClaimantPolicyDetailDto.getInstance();
    }

    private ClaimantPolicyDetailDto updateWithPlanDetailsToClaimantDto(GroupHealthPolicy groupHealthPolicy, ClaimantPolicyDetailDto claimantPolicyDetailDto, String clientId, Set<PreAuthorizationDetail> preAuthorizationDetails, HCPCode hcpCode) {
        Set<GHInsured> insureds = groupHealthPolicy.getInsureds();
        GHInsured groupHealthInsured = null;
        GHInsuredDependent ghInsuredDependent = null;
        if(isNotEmpty(insureds)){
            Optional<GHInsured> groupHealthInsuredOptional = insureds.stream().filter(ghInsured -> ghInsured.getFamilyId().getFamilyId().equalsIgnoreCase(clientId)).findFirst();
            if(groupHealthInsuredOptional.isPresent()) {
                groupHealthInsured = groupHealthInsuredOptional.get();
                claimantPolicyDetailDto
                        .updateWithCategory(groupHealthInsured.getCategory())
                        .updateWithRelationship(Relationship.SELF)
                        .updateWithAssuredDetails(groupHealthInsured);
            }
            if(isEmpty(groupHealthInsured)) {
                Optional<GHInsuredDependent> ghInsuredDependentOptional = insureds.stream().flatMap(new Function<GHInsured, Stream<GHInsuredDependent>>() {
                    @Override
                    public Stream<GHInsuredDependent> apply(GHInsured ghInsured) {
                        return ghInsured.getInsuredDependents().stream();
                    }
                }).filter(gHInsuredDependent -> gHInsuredDependent.getFamilyId().getFamilyId().equalsIgnoreCase(clientId)).findFirst();
                if(ghInsuredDependentOptional.isPresent()) {
                    ghInsuredDependent = ghInsuredDependentOptional.get();
                    final GHInsuredDependent finalGhInsuredDependent = ghInsuredDependent;
                    groupHealthInsured = insureds.parallelStream().filter(new Predicate<GHInsured>() {
                        @Override
                        public boolean test(GHInsured ghInsured) {
                            return ghInsured.getInsuredDependents().stream().filter(gHInsuredDependent -> gHInsuredDependent.getFamilyId().getFamilyId().equalsIgnoreCase(finalGhInsuredDependent.getFamilyId().getFamilyId())).findFirst().isPresent();
                        }
                    }).findFirst().get();
                    claimantPolicyDetailDto
                            .updateWithCategory(ghInsuredDependent.getCategory())
                            .updateWithRelationship(ghInsuredDependent.getRelationship())
                            .updateWithDependentAssuredDetail(ghInsuredDependent, groupHealthInsured);
                }
            }
        }
        GHPlanPremiumDetail planDetail = isNotEmpty(groupHealthInsured) ? groupHealthInsured.getPlanPremiumDetail() : ghInsuredDependent.getPlanPremiumDetail();
        if(isNotEmpty(planDetail)){
            List<Plan> plans = planRepository.findPlanByCodeAndName(planDetail.getPlanCode());
            if(isNotEmpty(plans)){
                Plan plan = plans.get(0);
                claimantPolicyDetailDto
                        .updateWithSumAssured(planDetail.getSumAssured())
                        .updateWithCoverages(constructCoverageBenefitDetails(planDetail, clientId))
                        .updateWithPlanCode(plan.getPlanDetail())
                        .updateWithPlanName(plan.getPlanDetail())
                        .updateWithCoverageDetails(constructProbableClaimAmountForServices(claimantPolicyDetailDto.getCoverageBenefitDetails(), preAuthorizationDetails, plan, hcpCode));
            }
        }
        return claimantPolicyDetailDto;
    }

    private Set<CoverageBenefitDetailDto>  constructProbableClaimAmountForServices(Set<CoverageBenefitDetailDto> coverageBenefitDetails, Set<PreAuthorizationDetail> preAuthorizationDetails, Plan plan, HCPCode hcpCode) {
        Set<String> services = getServicesFromPreAuthDetails(preAuthorizationDetails);
        Set<ServiceBenefitCoverageMapping> sbcmSet = null;
        List<Map<String, Object>> refurbishedList = Lists.newArrayList();
        if(isNotEmpty(services) && isNotEmpty(plan.getPlanDetail())){
            sbcmSet = services.parallelStream().map(new Function<String, List<ServiceBenefitCoverageMapping>>() {
                @Override
                public List<ServiceBenefitCoverageMapping> apply(String service) {
                    List<ServiceBenefitCoverageMapping> serviceBenefitCoverageMappings = sbcmRepository.findAllByPlanCodeAndService(plan.getPlanDetail().getPlanCode(), service);
                    //notEmpty(serviceBenefitCoverageMappings, "No Service Benefit Mapping Found For Plan - "+plan.getPlanDetail().getPlanName()+" and Service - "+service);
                    return serviceBenefitCoverageMappings;
                }
            }).flatMap(sbcmList -> sbcmList.stream()).collect(Collectors.toSet());
        }
        if(isNotEmpty(sbcmSet)){
            Map<ServiceBenefitCoverageMapping.CoverageBenefit, List<ServiceBenefitCoverageMapping>> result = sbcmSet.parallelStream().collect(Collectors.groupingBy(ServiceBenefitCoverageMapping::getCoverageBenefit));
            if(isNotEmpty(result)) {
                refurbishedList = result.values().stream().map(new Function<List<ServiceBenefitCoverageMapping>, Map<String, Object>>() {
                    @Override
                    public Map<String, Object> apply(List<ServiceBenefitCoverageMapping> serviceBenefitCoverageMappings) {
                        Map<String, Object> map = Maps.newHashMap();
                        if(isNotEmpty(serviceBenefitCoverageMappings)) {
                            ServiceBenefitCoverageMapping sbcm = serviceBenefitCoverageMappings.iterator().next();
                            map.put("coverageId", sbcm.getCoverageId());
                            map.put("coverageCode", sbcm.getCoverageCode());
                            map.put("coverageName", sbcm.getCoverageName());
                            map.put("benefitId", sbcm.getBenefitId());
                            map.put("benefitName", sbcm.getBenefitName());
                            map.put("services", getListOfServices(serviceBenefitCoverageMappings));
                            notNull(getCoverageBenefitDefinition(sbcm.getBenefitId(), plan.getCoverages()), "CoverageBenefitDefinition null for Benefit - "+sbcm.getBenefitId());
                            map.put("coverageBenefitDefinition", getCoverageBenefitDefinition(sbcm.getBenefitId(), plan.getCoverages()));
                        }
                        return map;
                    }
                }).collect(Collectors.toList());
            }
        }
        if(isNotEmpty(refurbishedList)){
            refurbishedList = refurbishedList.stream().filter(new Predicate<Map<String, Object>>() {
                @Override
                public boolean test(Map<String, Object> map) {
                    String coverageId = isNotEmpty(map.get("coverageId")) ? map.get("coverageId").toString() : StringUtils.EMPTY;
                    if(isNotEmpty(coverageId)) {
                        return coverageBenefitDetails.stream().filter(dto -> dto.getCoverageId().equals(coverageId)).findFirst().isPresent();
                    }
                    String coverageCode = isNotEmpty(map.get("coverageCode")) ? map.get("coverageCode").toString() : StringUtils.EMPTY;
                    return coverageBenefitDetails.stream().filter(dto -> dto.getCoverageCode().equals(coverageCode)).findFirst().isPresent();
                }
            }).collect(Collectors.toList());
        }
        refurbishedList.stream().forEach(map -> {
            Set<String> serviceList = isNotEmpty(map.get("services")) ? (Set<String>)map.get("services") : Sets.newHashSet();
            BigDecimal payableAmount = BigDecimal.ZERO;
            serviceList.stream().forEach(service -> {
                HCPRate hcpRate = hcpRateRepository.findHCPRateByHCPCodeAndService(hcpCode, service);
                notNull(hcpRate, "No HCP Rate configured for hcp- " + hcpCode + " service - " + service);
                HCPServiceDetail hcpServiceDetail = isNotEmpty(hcpRate.getHcpServiceDetails()) ? hcpRate.getHcpServiceDetails().iterator().next() : null;
                notNull(hcpRate, "No HCP Rate configured as no HCPServiceDetail found.");
                int lengthOfStay = getLengthOfStayByService(service, preAuthorizationDetails);
                BigDecimal amount = calculateProbableClaimAmount(lengthOfStay, hcpServiceDetail.getNormalAmount(), (CoverageBenefitDefinition)map.get("coverageBenefitDefinition"));
                payableAmount.add(amount);
                map.put("payableAmount", payableAmount);
            });
        });
        final List<Map<String, Object>> finalRefurbishedList = refurbishedList;
        return coverageBenefitDetails.stream().map(new Function<CoverageBenefitDetailDto, CoverageBenefitDetailDto>() {
            @Override
            public CoverageBenefitDetailDto apply(CoverageBenefitDetailDto coverageBenefitDetailDto) {
                String coverageId = coverageBenefitDetailDto.getCoverageCode();
                Set<CoverageBenefitDetailDto.BenefitDetailDto> benefitDetails = coverageBenefitDetailDto.getBenefitDetails();
                coverageBenefitDetailDto.updateWithProbableClaimAmount(coverageId, benefitDetails, finalRefurbishedList);
                return coverageBenefitDetailDto;
            }
        }).collect(Collectors.toSet());
    }

    private BigDecimal calculateProbableClaimAmount(int lengthOfStay, BigDecimal normalAmount, CoverageBenefitDefinition coverageBenefitDefinition) {
        if(CoverageBenefitDefinition.DAY.equals(coverageBenefitDefinition))
            return normalAmount.multiply(new BigDecimal(lengthOfStay));
        return normalAmount;
    }

    private int getLengthOfStayByService(String service, Set<PreAuthorizationDetail> preAuthorizationDetails) {
        for(PreAuthorizationDetail preAuthorizationDetail : preAuthorizationDetails){
            if(preAuthorizationDetail.getService().trim().equalsIgnoreCase(service.trim()))
                return preAuthorizationDetail.getDiagnosisTreatmentSurgeryLengthOStay();
        }
        return 1;
    }

    private CoverageBenefitDefinition getCoverageBenefitDefinition(BenefitId benefitId, Set<PlanCoverage> coverages) {
        if(isNotEmpty(coverages)){
            Set<PlanCoverageBenefit> planCoverageBenefits = coverages.stream().flatMap(coverage -> coverage.getPlanCoverageBenefits().stream()).collect(Collectors.toSet());
            for(PlanCoverageBenefit planCoverageBenefit : planCoverageBenefits) {
                if (planCoverageBenefit.getBenefitId().equals(benefitId)) {
                    return planCoverageBenefit.getDefinedPer();
                }
            }
        }
        return null;
    }

    private Set<String> getListOfServices(List<ServiceBenefitCoverageMapping> serviceBenefitCoverageMappings) {
        return isNotEmpty(serviceBenefitCoverageMappings) ? serviceBenefitCoverageMappings.stream().map(ServiceBenefitCoverageMapping::getService).collect(Collectors.toSet()) : Sets.newHashSet();
    }

    private Set<String> getServicesFromPreAuthDetails(Set<PreAuthorizationDetail> preAuthorizationDetails) {
        return isNotEmpty(preAuthorizationDetails) ? preAuthorizationDetails.stream().map(PreAuthorizationDetail::getService).collect(Collectors.toSet()) : Sets.newHashSet();
    }

    private Set<CoverageBenefitDetailDto> constructCoverageBenefitDetails(GHPlanPremiumDetail planDetail, String clientId) {
        return isNotEmpty(planDetail) ? isNotEmpty(planDetail.getCoveragePremiumDetails()) ? planDetail.getCoveragePremiumDetails().parallelStream().map(new Function<GHCoveragePremiumDetail, CoverageBenefitDetailDto>() {
            @Override
            public CoverageBenefitDetailDto apply(GHCoveragePremiumDetail ghCoveragePremiumDetail) {
                CoverageBenefitDetailDto coverageBenefitDetailDto = new CoverageBenefitDetailDto();
                if (isNotEmpty(ghCoveragePremiumDetail.getCoverageId())) {
                    CoverageDto coverageDto = coverageFinder.findCoverageById(ghCoveragePremiumDetail.getCoverageId().getCoverageId());
                    coverageBenefitDetailDto
                            .updateWithCoverageName(coverageDto.getCoverageName())
                            .updateWithCoverageId(coverageDto.getCoverageId())
                            .updateWithCoverageCode(coverageDto.getCoverageCode())
                            .updateWithSumAssured(ghCoveragePremiumDetail.getSumAssured())
                            .updateWithTotalAmountPaid(getTotalAmountPaidTillNow(planDetail, clientId))
                            .updateWithReserveAmount(getReservedAmountOfTheClient(clientId))
                            .updateWithBalanceAndEligibleAmount()
                            .updateWithBenefitDetails(constructBenefitDetails(coverageDto.getBenefitDtos(), coverageBenefitDetailDto, isNotEmpty(ghCoveragePremiumDetail.getBenefitPremiumLimits()) ? ghCoveragePremiumDetail.getBenefitPremiumLimits() :  Sets.newHashSet()));
                }
                return coverageBenefitDetailDto;
            }
        }).collect(Collectors.toSet()) : Sets.newHashSet() : Sets.newHashSet();
    }

    private BigDecimal getReservedAmountOfTheClient(String clientId) {
        return BigDecimal.ZERO;
    }

    private BigDecimal getTotalAmountPaidTillNow(GHPlanPremiumDetail planDetail, String clientId) {
        return BigDecimal.ZERO;
    }

    private Set<CoverageBenefitDetailDto.BenefitDetailDto> constructBenefitDetails(List<Map<String, Object>> benefitDtos, CoverageBenefitDetailDto coverageBenefitDetailDto, Set<BenefitPremiumLimit> benefitPremiumLimits) {
        Set<CoverageBenefitDetailDto.BenefitDetailDto> benefits = coverageBenefitDetailDto.getBenefitDetails();
        if(isEmpty(benefits))
            benefits = Sets.newHashSet();
        for(BenefitPremiumLimit benefitPremiumLimit : benefitPremiumLimits){
            for(Map<String, Object> benefit : benefitDtos){
                String benefitCode = String.valueOf(new BigDecimal(benefitPremiumLimit.getBenefitCode()).intValue());
                if(benefitCode.equals(benefit.get("benefitCode"))){
                    CoverageBenefitDetailDto.BenefitDetailDto benefitDetailDto = coverageBenefitDetailDto.new BenefitDetailDto();
                    benefitDetailDto.setBenefitName(isNotEmpty(benefit.get("benefitName")) ? benefit.get("benefitName").toString() : StringUtils.EMPTY);
                    benefitDetailDto.setBenefitCode(isNotEmpty(benefit.get("benefitCode")) ? benefit.get("benefitCode").toString() : StringUtils.EMPTY);
                    benefits.add(benefitDetailDto);
                }
            }
        }
        return benefits;
    }

    private ClaimantHCPDetailDto constructClaimantHCPDetailDto(HCPCode hcpCode, String hospitalizationEvent) {
        HCP hcp = hcpFinder.getHCPByHCPCode(hcpCode.getHcpCode());
        if(isNotEmpty(hcp)){
            ClaimantHCPDetailDto claimantHCPDetailDto = new ClaimantHCPDetailDto();
            claimantHCPDetailDto.updateWithHospitalizationEvent(hospitalizationEvent)
                    .updateWithAddress(hcp.getHcpAddress())
                    .updateWithHCPName(hcp.getHcpName())
                    .updateWithHCPCode(hcp.getHcpCode());
            return claimantHCPDetailDto;
        }
        return ClaimantHCPDetailDto.getInstance();
    }

    @Transactional
    public PreAuthorizationRequestId createUpdatePreAuthorizationRequest(PreAuthorizationClaimantDetailCommand preAuthorizationClaimantDetailCommand) throws GenerateReminderFollowupException {
        String preAuthorizationRequestId = preAuthorizationClaimantDetailCommand.getPreAuthorizationRequestId();
        PreAuthorizationRequest preAuthorizationRequest = isNotEmpty(preAuthorizationRequestId) ?
                isNotEmpty(getPreAuthorizationRequestById(new PreAuthorizationRequestId(preAuthorizationRequestId)))
                        ? getPreAuthorizationRequestById(new PreAuthorizationRequestId(preAuthorizationRequestId))
                        : new PreAuthorizationRequest(PreAuthorizationRequest.Status.INTIMATION) : new PreAuthorizationRequest(PreAuthorizationRequest.Status.INTIMATION);
        preAuthorizationRequest.updateWithPreAuthorizationRequestId(preAuthorizationClaimantDetailCommand.getPreAuthorizationId())
                .updateWithPreAuthorizationId(preAuthorizationClaimantDetailCommand.getPreAuthorizationId())
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
                .updateWithPreAuthorizationRequestDrugService(preAuthorizationClaimantDetailCommand.getDrugServicesDtos());
        preAuthorizationRequest = preAuthorizationRequestRepository.save(preAuthorizationRequest);
        preAuthorizationRequest.savedRegisterFollowUpReminders();
        return preAuthorizationRequest.getPreAuthorizationRequestId();
    }

    private PreAuthorizationRequest getPreAuthorizationRequestById(PreAuthorizationRequestId preAuthorizationRequestId) {
        return preAuthorizationRequestRepository.findOne(preAuthorizationRequestId);
    }

    public List<GHProposalMandatoryDocumentDto> findMandatoryDocuments(FamilyId familyId) {
        GroupHealthPolicy groupHealthPolicy = ghPolicyRepository.findDistinctPolicyByFamilyId(familyId, PolicyStatus.IN_FORCE);
        if(isEmpty(groupHealthPolicy)){
            groupHealthPolicy = ghPolicyRepository.findDistinctPolicyByDependentFamilyId(familyId, PolicyStatus.IN_FORCE);
        }
        List<SearchDocumentDetailDto> documentDetailDtos = Lists.newArrayList();
        GHPlanPremiumDetail planPremiumDetail = getGHPlanPremiumDetailByFamilyId(familyId, groupHealthPolicy);
        SearchDocumentDetailDto searchDocumentDetailDto = new SearchDocumentDetailDto(planPremiumDetail.getPlanId());
        documentDetailDtos.add(searchDocumentDetailDto);
        if (isNotEmpty(planPremiumDetail.getCoveragePremiumDetails())) {
            List<CoverageId> coverageIds = planPremiumDetail.getCoveragePremiumDetails().stream().map(new Function<GHCoveragePremiumDetail, CoverageId>() {
                @Override
                public CoverageId apply(GHCoveragePremiumDetail ghCoveragePremiumDetail) {
                    return ghCoveragePremiumDetail.getCoverageId();
                }
            }).collect(Collectors.toList());
            documentDetailDtos.add(new SearchDocumentDetailDto(planPremiumDetail.getPlanId(), coverageIds));
        }
        Set<ClientDocumentDto> mandatoryDocuments = underWriterAdapter.getMandatoryDocumentsForApproverApproval(documentDetailDtos, ProcessType.CLAIM);
        List<GHProposalMandatoryDocumentDto> mandatoryDocumentDtos = Lists.newArrayList();
        Set<GHProposerDocument> uploadedDocuments = isNotEmpty(groupHealthPolicy.getProposerDocuments()) ? groupHealthPolicy.getProposerDocuments() : Sets.newHashSet();
        if (isNotEmpty(mandatoryDocuments)) {
            mandatoryDocumentDtos = mandatoryDocuments.stream().map(new Function<ClientDocumentDto, GHProposalMandatoryDocumentDto>() {
                @Override
                public GHProposalMandatoryDocumentDto apply(ClientDocumentDto clientDocumentDto) {
                    GHProposalMandatoryDocumentDto mandatoryDocumentDto = new GHProposalMandatoryDocumentDto(clientDocumentDto.getDocumentCode(), clientDocumentDto.getDocumentName());
                    Optional<GHProposerDocument> proposerDocumentOptional = uploadedDocuments.stream().filter(new Predicate<GHProposerDocument>() {
                        @Override
                        public boolean test(GHProposerDocument ghProposerDocument) {
                            return clientDocumentDto.getDocumentCode().equals(ghProposerDocument.getDocumentId());
                        }
                    }).findAny();
                    if (proposerDocumentOptional.isPresent()) {
                        try {
                            if (isNotEmpty(proposerDocumentOptional.get().getGridFsDocId())) {
                                GridFSDBFile gridFSDBFile = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(proposerDocumentOptional.get().getGridFsDocId())));
                                mandatoryDocumentDto.setFileName(gridFSDBFile.getFilename());
                                mandatoryDocumentDto.setContentType(gridFSDBFile.getContentType());
                                mandatoryDocumentDto.setGridFsDocId(gridFSDBFile.getId().toString());
                                mandatoryDocumentDto.updateWithContent(IOUtils.toByteArray(gridFSDBFile.getInputStream()));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return mandatoryDocumentDto;
                }
            }).collect(Collectors.toList());
        }
        return mandatoryDocumentDtos;
    }

    private GHPlanPremiumDetail getGHPlanPremiumDetailByFamilyId(FamilyId familyId, GroupHealthPolicy groupHealthPolicy) {
        Set<GHInsured> insureds = groupHealthPolicy.getInsureds();
        GHInsured groupHealthInsured = null;
        GHInsuredDependent ghInsuredDependent = null;
        if(isNotEmpty(insureds)){
            Optional<GHInsured> groupHealthInsuredOptional = insureds.stream().filter(ghInsured -> ghInsured.getFamilyId().equals(familyId)).findFirst();
            if(groupHealthInsuredOptional.isPresent()) {
                groupHealthInsured = groupHealthInsuredOptional.get();
            }
            if(isEmpty(groupHealthInsured)) {
                Optional<GHInsuredDependent> ghInsuredDependentOptional = insureds.stream().flatMap(new Function<GHInsured, Stream<GHInsuredDependent>>() {
                    @Override
                    public Stream<GHInsuredDependent> apply(GHInsured ghInsured) {
                        return ghInsured.getInsuredDependents().stream();
                    }
                }).filter(gHInsuredDependent -> gHInsuredDependent.getFamilyId().equals(familyId)).findFirst();
                if(ghInsuredDependentOptional.isPresent()) {
                    ghInsuredDependent = ghInsuredDependentOptional.get();
                }
            }
        }
        return isNotEmpty(groupHealthInsured) ? groupHealthInsured.getPlanPremiumDetail() : ghInsuredDependent.getPlanPremiumDetail();
    }

    public List<PreAuthorizationClaimantDetailCommand> getPreAuthorizationRequestByCriteria(SearchPreAuthorizationRecordDto searchPreAuthorizationRecordDto) {
        List<PreAuthorizationRequest> preAuthorizationRequests = preAuthorizationFinder.getPreAuthorizationRequestByCriteria(searchPreAuthorizationRecordDto);
        return convertPreAuthorizationListToPreAuthorizationClaimantDetailCommand(preAuthorizationRequests);
    }

    private List<PreAuthorizationClaimantDetailCommand> convertPreAuthorizationListToPreAuthorizationClaimantDetailCommand(List<PreAuthorizationRequest> preAuthorizationRequests) {
        return isNotEmpty(preAuthorizationRequests) ? preAuthorizationRequests.parallelStream().map(new Function<PreAuthorizationRequest, PreAuthorizationClaimantDetailCommand>() {
            @Override
            public PreAuthorizationClaimantDetailCommand apply(PreAuthorizationRequest preAuthorizationRequest) {
                return new PreAuthorizationClaimantDetailCommand()
                        .updateWithBatchNumber(preAuthorizationRequest.getBatchNumber())
                        .updateWithPreAuthorizationRequestId(preAuthorizationRequest.getPreAuthorizationRequestId())
                        .updateWithClaimType(preAuthorizationRequest.getClaimType())
                        .updateWithClaimIntimationDate(preAuthorizationRequest.getClaimIntimationDate())
                        .updateWithPolicy(preAuthorizationRequest.getPreAuthorizationRequestPolicyDetail())
                        .updateWithHcp(preAuthorizationRequest.getPreAuthorizationRequestHCPDetail());
            }
        }).collect(Collectors.toList()) : Lists.newArrayList();
    }

    public PreAuthorizationClaimantDetailCommand getPreAuthorizationClaimantDetailCommandFromPreAuthorizationRequestId(PreAuthorizationRequestId preAuthorizationRequestId){
        PreAuthorizationRequest preAuthorizationRequest = getPreAuthorizationRequestById(preAuthorizationRequestId);
        notNull(preAuthorizationRequest, "No PreAuthorizationRequest found with given Id");
        if(isNotEmpty(preAuthorizationRequest))
            return constructPreAuthorizationClaimantDetailCommand(preAuthorizationRequest);
        return null;
    }

    private PreAuthorizationClaimantDetailCommand constructPreAuthorizationClaimantDetailCommand(PreAuthorizationRequest preAuthorizationRequest) {
        PreAuthorizationClaimantDetailCommand preAuthorizationClaimantDetailCommand = new PreAuthorizationClaimantDetailCommand();
        if(isNotEmpty(preAuthorizationRequest)) {
            preAuthorizationClaimantDetailCommand
                    .updateWithPreAuthorizationRequestId(preAuthorizationRequest.getPreAuthorizationRequestId())
                    .updateWithPreAuthorizationId(preAuthorizationRequest.getPreAuthorizationId())
                    .updateWithBatchNumber(preAuthorizationRequest.getBatchNumber())
                    .updateWithClaimType(preAuthorizationRequest.getClaimType())
                    .updateWithClaimIntimationDate(preAuthorizationRequest.getClaimIntimationDate())
                    .updateWithPreAuthorizationDate(preAuthorizationRequest.getClaimIntimationDate())
                    .updateWithClaimantHCPDetailDto(constructClaimantHCPDetailDtoFromPreAuthorizationRequestHCPDetail(preAuthorizationRequest.getPreAuthorizationRequestHCPDetail()))
                    .updateWithDiagnosisTreatment(constructDiagnosisTreatmentDtoListFromPreAuthorizationRequest(preAuthorizationRequest.getPreAuthorizationRequestDiagnosisTreatmentDetails()))
                    .updateWithIllnessDetails(constructIllnessDetailDtoFromPreAuthorizationRequest(preAuthorizationRequest.getPreAuthorizationRequestIllnessDetail()))
                    .updateWithDrugServices(constructDrugServiceDtoFromPreAuthorizationRequest(preAuthorizationRequest.getPreAuthorizationRequestDrugServices()))
                    .updateWithClaimantPolicyDetailDto(constructClaimantPolicyDetailDtoFromPreAuthorizationRequest(preAuthorizationRequest.getPreAuthorizationRequestPolicyDetail(), preAuthorizationRequest.getRelationship(), preAuthorizationRequest.getCategory(), preAuthorizationRequest.getGhProposer()));
        }
        return preAuthorizationClaimantDetailCommand;
    }

    private ClaimantPolicyDetailDto constructClaimantPolicyDetailDtoFromPreAuthorizationRequest(PreAuthorizationRequestPolicyDetail preAuthorizationRequestPolicyDetail, String relationship, String category, GHProposer ghProposer) {
        ClaimantPolicyDetailDto claimantPolicyDetailDto = null;
        if(isNotEmpty(preAuthorizationRequestPolicyDetail)){
            claimantPolicyDetailDto = new ClaimantPolicyDetailDto()
                    .updateWithPolicyNumber(preAuthorizationRequestPolicyDetail.getPolicyNumber())
                    .updateWithPolicyName(preAuthorizationRequestPolicyDetail.getPolicyName())
                    .updateWithPlanCode(preAuthorizationRequestPolicyDetail.getPlanCode())
                    .updateWithPlanName(preAuthorizationRequestPolicyDetail.getPlanName())
                    .updateWithSumAssured(preAuthorizationRequestPolicyDetail.getSumAssured())
                    .updateWithRelationship(Relationship.valueOf(relationship))
                    .updateWithCategory(category)
                    .updateWithAssuredDetail(constructAssuredDetailFromPreAuthorizationRequestAssuredDetail(preAuthorizationRequestPolicyDetail.getAssuredDetail()))
                    .updateWithDependentAssuredDetail(constructDependentAssuredDetailFromPreAuthorizationRequestAssuredDetail(preAuthorizationRequestPolicyDetail.getAssuredDetail()))
                    .updateWithCoverageDetails(constructCoverageListFromPreAuthorizationRequestAssuredDetail(claimantPolicyDetailDto, preAuthorizationRequestPolicyDetail.getCoverageDetailDtoList()))
                    .updateWithProposerDetail(constructProposerDetailsFromPreAuthorizationRequestAssuredDetail(ghProposer));
        }
        return claimantPolicyDetailDto;
    }

    private PreAuthorizationClaimantProposerDetail constructProposerDetailsFromPreAuthorizationRequestAssuredDetail(GHProposer proposer) {
        PreAuthorizationClaimantProposerDetail preAuthorizationClaimantProposerDetail = null;
        if(isNotEmpty(proposer)){
            preAuthorizationClaimantProposerDetail = new PreAuthorizationClaimantProposerDetail();
            preAuthorizationClaimantProposerDetail.updateWithProposerDetails(proposer);
        }
        return preAuthorizationClaimantProposerDetail;
    }

    private Set<CoverageBenefitDetailDto> constructCoverageListFromPreAuthorizationRequestAssuredDetail(ClaimantPolicyDetailDto claimantPolicyDetailDto, Set<PreAuthorizationRequestCoverageDetail> coverageDetailDtoList) {
        return isNotEmpty(coverageDetailDtoList) ? coverageDetailDtoList.parallelStream().map(new Function<PreAuthorizationRequestCoverageDetail, CoverageBenefitDetailDto>() {
            @Override
            public CoverageBenefitDetailDto apply(PreAuthorizationRequestCoverageDetail preAuthorizationRequestCoverageDetail) {
                CoverageBenefitDetailDto coverageBenefitDetailDto = new CoverageBenefitDetailDto();
                try {
                    BeanUtils.copyProperties(coverageBenefitDetailDto, preAuthorizationRequestCoverageDetail);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
                return coverageBenefitDetailDto;
            }
        }).collect(Collectors.toSet()) : Sets.newHashSet();
    }

    private DependentAssuredDetail constructDependentAssuredDetailFromPreAuthorizationRequestAssuredDetail(PreAuthorizationRequestAssuredDetail assuredDetail) {
        DependentAssuredDetail dependentAssuredDetail = null;
        if(assuredDetail.isDependentAssuredDetailPresent()){
            try {
                dependentAssuredDetail = new DependentAssuredDetail();
                BeanUtils.copyProperties(dependentAssuredDetail, assuredDetail);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return dependentAssuredDetail;
    }

    private AssuredDetail constructAssuredDetailFromPreAuthorizationRequestAssuredDetail(PreAuthorizationRequestAssuredDetail preAuthorizationRequestAssuredDetail) {
        AssuredDetail assuredDetail = null;
        if(!preAuthorizationRequestAssuredDetail.isDependentAssuredDetailPresent()){
            try {
                assuredDetail = new AssuredDetail();
                BeanUtils.copyProperties(assuredDetail, preAuthorizationRequestAssuredDetail);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return assuredDetail;
    }

    private List<DrugServiceDto> constructDrugServiceDtoFromPreAuthorizationRequest(Set<PreAuthorizationRequestDrugService> preAuthorizationRequestDrugServices) {
        return isNotEmpty(preAuthorizationRequestDrugServices) ? preAuthorizationRequestDrugServices.parallelStream().map(new Function<PreAuthorizationRequestDrugService, DrugServiceDto>() {
            @Override
            public DrugServiceDto apply(PreAuthorizationRequestDrugService preAuthorizationRequestDrugService) {
                DrugServiceDto drugServiceDto = new DrugServiceDto();
                try {
                    BeanUtils.copyProperties(drugServiceDto, preAuthorizationRequestDrugService);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
                return drugServiceDto;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList()) :  Lists.newArrayList();
    }

    private IllnessDetailDto constructIllnessDetailDtoFromPreAuthorizationRequest(PreAuthorizationRequestIllnessDetail preAuthorizationRequestIllnessDetail) {
        IllnessDetailDto illnessDetailDto = null;
        if(isNotEmpty(preAuthorizationRequestIllnessDetail)){
            illnessDetailDto = new IllnessDetailDto();
            try {
                BeanUtils.copyProperties(illnessDetailDto, preAuthorizationRequestIllnessDetail);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return illnessDetailDto;
    }

    private List<DiagnosisTreatmentDto>  constructDiagnosisTreatmentDtoListFromPreAuthorizationRequest(Set<PreAuthorizationRequestDiagnosisTreatmentDetail> preAuthorizationRequestDiagnosisTreatmentDetails) {
        return isNotEmpty(preAuthorizationRequestDiagnosisTreatmentDetails) ? preAuthorizationRequestDiagnosisTreatmentDetails.stream().map(new Function<PreAuthorizationRequestDiagnosisTreatmentDetail, DiagnosisTreatmentDto>() {
            @Override
            public DiagnosisTreatmentDto apply(PreAuthorizationRequestDiagnosisTreatmentDetail preAuthorizationRequestDiagnosisTreatmentDetail) {
                DiagnosisTreatmentDto diagnosisTreatmentDto = new DiagnosisTreatmentDto();
                try {
                    BeanUtils.copyProperties(diagnosisTreatmentDto, preAuthorizationRequestDiagnosisTreatmentDetail);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
                return diagnosisTreatmentDto;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList()) : Lists.newArrayList();
    }

    private ClaimantHCPDetailDto constructClaimantHCPDetailDtoFromPreAuthorizationRequestHCPDetail(PreAuthorizationRequestHCPDetail preAuthorizationRequestHCPDetail) {
        ClaimantHCPDetailDto claimantHCPDetailDto = null;
        if(isNotEmpty(preAuthorizationRequestHCPDetail)){
            claimantHCPDetailDto = new ClaimantHCPDetailDto();
            try {
                BeanUtils.copyProperties(claimantHCPDetailDto, preAuthorizationRequestHCPDetail);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return claimantHCPDetailDto;
    }

    public List<PreAuthorizationClaimantDetailCommand> getPreAuthorizationForDefaultList() {
        List<PreAuthorizationClaimantDetailCommand> result = Lists.newArrayList();
        PageRequest pageRequest = new PageRequest(0, 300, new Sort(new Order(Direction.DESC, "createdOn")));
        Page<PreAuthorizationRequest> pages = preAuthorizationRequestRepository.findAll(pageRequest);
        if(isNotEmpty(pages) && isNotEmpty(pages.getContent()))
            result = convertPreAuthorizationListToPreAuthorizationClaimantDetailCommand(pages.getContent());
        return result;
    }

    public Set<CommentDetail> updateComments(UpdateCommentCommand updateCommentCommand) {
        PreAuthorizationRequest preAuthorizationRequest = getPreAuthorizationRequestById(new PreAuthorizationRequestId(updateCommentCommand.getPreAuthorizationRequestId()));
        if(isNotEmpty(preAuthorizationRequest)){
            CommentDetail commentDetail = new CommentDetail()
                    .updateWithComments(updateCommentCommand.getComments())
                    .updateWithCommentDateTime(updateCommentCommand.getCommentDateTime())
                    .updateWithUserName(updateCommentCommand.getUserDetails());
            preAuthorizationRequest.updateWithComments(commentDetail);
            preAuthorizationRequestRepository.save(preAuthorizationRequest);
        }
        return preAuthorizationRequest.getCommentDetails();
    }
}