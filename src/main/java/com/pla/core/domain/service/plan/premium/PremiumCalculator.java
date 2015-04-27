package com.pla.core.domain.service.plan.premium;

import com.google.common.collect.Lists;
import com.pla.core.domain.model.generalinformation.OrganizationGeneralInformation;
import com.pla.core.domain.model.plan.premium.Premium;
import com.pla.core.domain.model.plan.premium.PremiumInfluencingFactorLineItem;
import com.pla.core.domain.model.plan.premium.PremiumItem;
import com.pla.core.query.PremiumFinder;
import com.pla.core.repository.OrganizationGeneralInformationRepository;
import com.pla.publishedlanguage.contract.IPremiumCalculator;
import com.pla.publishedlanguage.domain.model.ComputedPremiumDto;
import com.pla.publishedlanguage.domain.model.PremiumCalculationDto;
import com.pla.publishedlanguage.domain.model.PremiumFrequency;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.pla.core.domain.exception.PremiumException.raiseInfluencingFactorMismatchException;
import static com.pla.core.domain.exception.PremiumException.raisePremiumNotFoundException;
import static org.nthdimenzion.utils.UtilValidator.isEmpty;
import static org.nthdimenzion.utils.UtilValidator.isNotEmpty;

/**
 * Created by Samir on 4/10/2015.
 */
@Component(value = "premiumCalculator")
public class PremiumCalculator implements IPremiumCalculator {

    private PremiumFinder premiumFinder;

    private OrganizationGeneralInformationRepository organizationGeneralInformationRepository;

    @Autowired
    public PremiumCalculator(PremiumFinder premiumFinder, OrganizationGeneralInformationRepository organizationGeneralInformationRepository) {
        this.premiumFinder = premiumFinder;
        this.organizationGeneralInformationRepository = organizationGeneralInformationRepository;
    }

    @Override
    public List<ComputedPremiumDto> calculatePremium(PremiumCalculationDto premiumCalculationDto) {
        Premium premium = premiumFinder.findPremium(premiumCalculationDto);
        boolean hasAllInfluencingFactor = premium.hasAllInfluencingFactor(premiumCalculationDto.getInfluencingFactors());
        if (!hasAllInfluencingFactor) {
            raiseInfluencingFactorMismatchException();
        }
        Set<PremiumItem> premiumItems = premium.getPremiumItems();
        PremiumItem premiumItem = findPremiumItem(premiumItems, premiumCalculationDto.getPremiumCalculationInfluencingFactorItems());
        List<OrganizationGeneralInformation> organizationGeneralInformations = organizationGeneralInformationRepository.findAll();
        checkArgument(isNotEmpty(organizationGeneralInformations));
        return computePremium(premium, premiumItem, organizationGeneralInformations.get(0), premiumCalculationDto.getNoOfDays());
    }

    public List<ComputedPremiumDto> computePremium(Premium premium, PremiumItem premiumItem, OrganizationGeneralInformation organizationGeneralInformation, int noOfDays) {
        ComputedPremiumDto annualPremium = new ComputedPremiumDto(PremiumFrequency.ANNUALLY, premium.getAnnualPremium(premiumItem, organizationGeneralInformation.getDiscountFactorItems(), noOfDays));
        ComputedPremiumDto semiAnnualPremium = new ComputedPremiumDto(PremiumFrequency.SEMI_ANNUALLY, premium.getSemiAnnuallyPremium(premiumItem, organizationGeneralInformation.getModelFactorItems(), organizationGeneralInformation.getDiscountFactorItems(), noOfDays));
        ComputedPremiumDto quarterlyPremium = new ComputedPremiumDto(PremiumFrequency.QUARTERLY, premium.getQuarterlyPremium(premiumItem, organizationGeneralInformation.getModelFactorItems(), organizationGeneralInformation.getDiscountFactorItems(), noOfDays));
        ComputedPremiumDto monthlyPremium = new ComputedPremiumDto(PremiumFrequency.MONTHLY, premium.getMonthlyPremium(premiumItem, organizationGeneralInformation.getModelFactorItems(), noOfDays));
        List<ComputedPremiumDto> computedPremiumDtoList = Lists.newArrayList();
        computedPremiumDtoList.add(annualPremium);
        computedPremiumDtoList.add(semiAnnualPremium);
        computedPremiumDtoList.add(quarterlyPremium);
        computedPremiumDtoList.add(monthlyPremium);
        return computedPremiumDtoList;
    }


    public PremiumItem findPremiumItem(Set<PremiumItem> premiumItems, Set<PremiumCalculationDto.PremiumCalculationInfluencingFactorItem> premiumCalculationInfluencingFactorItems) {
        List<PremiumItem> premiumItemList = premiumItems.stream().filter(new FilterPremiumItemPredicate(premiumCalculationInfluencingFactorItems)).collect(Collectors.toList());
        if (isEmpty(premiumItemList)) {
            raisePremiumNotFoundException();
        }
        checkArgument(premiumItemList.size() == 1);
        return premiumItemList.get(0);
    }

    private class FilterPremiumItemPredicate implements Predicate<PremiumItem> {

        private Set<PremiumCalculationDto.PremiumCalculationInfluencingFactorItem> premiumCalculationInfluencingFactorItems;

        FilterPremiumItemPredicate(Set<PremiumCalculationDto.PremiumCalculationInfluencingFactorItem> premiumCalculationInfluencingFactorItems) {
            this.premiumCalculationInfluencingFactorItems = premiumCalculationInfluencingFactorItems;
        }

        @Override
        public boolean test(PremiumItem premiumItem) {
            boolean matchFound = true;
            int noOfMatch = 0;
            for (PremiumInfluencingFactorLineItem premiumInfluencingFactorLineItem : premiumItem.getPremiumInfluencingFactorLineItems()) {
                if (isMatchesInfluencingFactorAndValue(premiumInfluencingFactorLineItem, premiumCalculationInfluencingFactorItems)) {
                    noOfMatch = noOfMatch + 1;
                    continue;
                }
            }
            return premiumCalculationInfluencingFactorItems.size() == noOfMatch;
        }
    }

    private boolean isMatchesInfluencingFactorAndValue(PremiumInfluencingFactorLineItem premiumInfluencingFactorLineItem, Set<PremiumCalculationDto.PremiumCalculationInfluencingFactorItem> premiumCalculationInfluencingFactorItems) {
        for (PremiumCalculationDto.PremiumCalculationInfluencingFactorItem premiumCalculationInfluencingFactorItem : premiumCalculationInfluencingFactorItems) {
            if (premiumCalculationInfluencingFactorItem.getPremiumInfluencingFactor().equals(premiumInfluencingFactorLineItem.getPremiumInfluencingFactor())
                    && premiumCalculationInfluencingFactorItem.getValue().equals(premiumInfluencingFactorLineItem.getValue())) {
                return true;
            }
        }
        return false;
    }


}
