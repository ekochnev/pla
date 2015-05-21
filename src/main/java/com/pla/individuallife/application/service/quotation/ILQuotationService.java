package com.pla.individuallife.application.service.quotation;

import com.pla.individuallife.domain.model.quotation.ProposedAssured;
import com.pla.individuallife.domain.model.quotation.Proposer;
import com.pla.individuallife.query.*;
import com.pla.individuallife.query.ILQuotationDto;
import com.pla.individuallife.query.ILQuotationFinder;
import com.pla.sharedkernel.identifier.QuotationId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Karunakar on 5/13/2015.
 */
@Service
public class ILQuotationService {


    private ILQuotationFinder ilQuotationFinder;

    @Autowired
    public ILQuotationService(ILQuotationFinder ilQuotationFinder) {
        this.ilQuotationFinder = ilQuotationFinder;
    }


    public PremiumDetailDto getPremiumDetail(QuotationId quotationId) {
        return new PremiumDetailDto();
    }

    public PremiumDetailDto getReCalculatePremium(PremiumDetailDto premiumDetailDto) {
        return new PremiumDetailDto();
    }


    public ProposerDto getProposerDetail(QuotationId quotationId) {
        Map quotation = ilQuotationFinder.getQuotationById(quotationId.getQuotationId());
        Proposer proposer = (Proposer) quotation.get("proposer");
        return new ProposerDto(proposer);
    }

    public ProposedAssuredDto getAssuredDetail(QuotationId quotationId) {
        Map quotation = ilQuotationFinder.getQuotationById(quotationId.getQuotationId());
        ProposedAssured assured = (ProposedAssured) quotation.get("proposedAssured");
        return new ProposedAssuredDto(assured);
    }


    public List<ILQuotationDto> getAllQuotation() {
        List<Map> allQuotations = ilQuotationFinder.getAllQuotation();
        List<ILQuotationDto> ILQuotationDtoList = allQuotations.stream().map(new TransformToILQuotationDto()).collect(Collectors.toList());
        return ILQuotationDtoList;
    }



    private class TransformToILQuotationDto implements Function<Map, ILQuotationDto> {

        @Override
        public ILQuotationDto apply(Map map) {
            return null;
        }
    }
}