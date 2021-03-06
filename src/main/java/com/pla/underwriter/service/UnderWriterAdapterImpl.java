package com.pla.underwriter.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.pla.client.domain.model.Client;
import com.pla.client.repository.ClientRepository;
import com.pla.publishedlanguage.dto.ClientDocumentDto;
import com.pla.publishedlanguage.dto.SearchDocumentDetailDto;
import com.pla.publishedlanguage.dto.UnderWriterRoutingLevelDetailDto;
import com.pla.publishedlanguage.underwriter.contract.IUnderWriterAdapter;
import com.pla.sharedkernel.domain.model.ProcessType;
import com.pla.sharedkernel.domain.model.RoutingLevel;
import com.pla.sharedkernel.identifier.ClientId;
import com.pla.underwriter.domain.model.*;
import com.pla.underwriter.exception.UnderWriterException;
import com.pla.underwriter.finder.UnderWriterFinder;
import org.nthdimenzion.utils.UtilValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.pla.underwriter.exception.UnderWriterException.raiseInfluencingFactorMismatchException;
import static com.pla.underwriter.exception.UnderWriterException.raiseUnderWriterNotFoundException;
import static org.nthdimenzion.utils.UtilValidator.isEmpty;

/**
 * Created by Admin on 5/27/2015.
 */
@Service
public class UnderWriterAdapterImpl implements IUnderWriterAdapter {

    private UnderWriterFinder underWriterFinder;

    private ClientRepository clientRepository;

    @Autowired
    public UnderWriterAdapterImpl(UnderWriterFinder underWriterFinder,ClientRepository clientRepository) {
        this.underWriterFinder = underWriterFinder;
        this.clientRepository = clientRepository;
    }

    @Override
    public RoutingLevel getRoutingLevel(UnderWriterRoutingLevelDetailDto underWriterRoutingLevelDetailDto) {
        RoutingLevel routingLevel = findRoutingLevel(underWriterRoutingLevelDetailDto);
        if (routingLevel != null) {
            return routingLevel;
        }
        if(underWriterRoutingLevelDetailDto.getClientId() != null) {
            return findRoutingLevelByClientId(underWriterRoutingLevelDetailDto.getClientId());
        } else {
            return null;
        }
    }

    @Override
    public List<ClientDocumentDto> getDocumentsForUnderWriterApproval(UnderWriterRoutingLevelDetailDto underWriterRoutingLevelDetailDto) {
        List<String> definedUnderWriterDocument = findDefinedUnderWriterDocument(underWriterRoutingLevelDetailDto);
        List<ClientDocumentDto> clientDocument = definedUnderWriterDocument.stream().map(new Function<String, ClientDocumentDto>() {
            @Override
            public ClientDocumentDto apply(String documentCode) {
                return new ClientDocumentDto(documentCode,false);
            }
        }).collect(Collectors.toList());
        if(underWriterRoutingLevelDetailDto.getClientId() != null) {
            List<String> documentDefinedForClient = findDefinedUnderWriterDocumentByClientId(underWriterRoutingLevelDetailDto.getClientId());
            for (ClientDocumentDto clientDocumentDto : clientDocument) {
                if (documentDefinedForClient.contains(clientDocumentDto.getDocumentCode())) {
                    clientDocumentDto.setHasSubmitted(true);
                }
            }
        }
        return clientDocument;
    }

    @Override
    public Set<ClientDocumentDto> getMandatoryDocumentsForApproverApproval(List<SearchDocumentDetailDto> searchDetailList, ProcessType processType) {
        Set<ClientDocumentDto> mandatoryDocumentSet = Sets.newLinkedHashSet();
        searchDetailList.parallelStream().map(new Function<SearchDocumentDetailDto, ClientDocumentDto>() {
            @Override
            public ClientDocumentDto apply(SearchDocumentDetailDto searchDocumentDetailDto) {
                List<Map<String, Object>> documentDetail = underWriterFinder.getMandatoryDocumentForPlanAndCoverage(searchDocumentDetailDto.getPlanId().getPlanId(), searchDocumentDetailDto.getCoverageIds(), processType);
                documentDetail.parallelStream().map(new Function<Map<String, Object>, ClientDocumentDto>() {
                    @Override
                    public ClientDocumentDto apply(Map<String, Object> documentMap) {
                        String documentName = (String) documentMap.get("documentName");
                        String documentCode = (String) documentMap.get("documentCode");
                        ClientDocumentDto clientDocumentDto = new ClientDocumentDto(documentCode, documentName,false);
                        mandatoryDocumentSet.add(clientDocumentDto);
                        return clientDocumentDto;
                    }
                }).collect(Collectors.toList());
                return new ClientDocumentDto();
            }
        }).collect(Collectors.toSet());
        return mandatoryDocumentSet;
    }

    public RoutingLevel findRoutingLevel(UnderWriterRoutingLevelDetailDto underWriterRoutingLevelDetailDto) {
        UnderWritingRoutingLevelItem underWritingRoutingLevelItem;
        try {
            UnderWriterRoutingLevel underWriterRoutingLevel = underWriterFinder.findUnderWriterRoutingLevel(underWriterRoutingLevelDetailDto);
            boolean hasAllInfluencingFactor = underWriterRoutingLevel.hasAllInfluencingFactor(transformUnderWriterInfluencingFactor(underWriterRoutingLevelDetailDto.getUnderWriterInfluencingFactor()));
            if (!hasAllInfluencingFactor) {
                raiseInfluencingFactorMismatchException();
            }
            Set<UnderWritingRoutingLevelItem> underWritingRoutingLevelItems = underWriterRoutingLevel.getUnderWritingRoutingLevelItems();
            underWritingRoutingLevelItem = findUnderWriterRoutingLevelItem(underWritingRoutingLevelItems, underWriterRoutingLevelDetailDto.getUnderWriterInfluencingFactor());
        }catch (UnderWriterException  e){
            return null;
        }
        catch (RuntimeException e){
            return null;
        }
        return underWritingRoutingLevelItem.getRoutingLevel();
    }

    private List<String> findDefinedUnderWriterDocument(UnderWriterRoutingLevelDetailDto underWriterRoutingLevelDetailDto){
        UnderWriterDocument underWriterDocument =  underWriterFinder.getUnderWriterDocumentSetUp(underWriterRoutingLevelDetailDto.getPlanId(), underWriterRoutingLevelDetailDto.getCoverageId(), underWriterRoutingLevelDetailDto.getEffectiveFrom(), underWriterRoutingLevelDetailDto.getProcess());
        boolean hasAllInfluencingFactor = underWriterDocument.hasAllInfluencingFactor(transformUnderWriterInfluencingFactor(underWriterRoutingLevelDetailDto.getUnderWriterInfluencingFactor()));
        if (!hasAllInfluencingFactor) {
            raiseInfluencingFactorMismatchException();
        }
        Set<UnderWriterDocumentItem> underWriterDocumentItems = underWriterDocument.getUnderWriterDocumentItems();
        UnderWriterDocumentItem underWriterDocumentItem = findUnderWriterDocumentLineItem(underWriterDocumentItems, underWriterRoutingLevelDetailDto.getUnderWriterInfluencingFactor());
        return Lists.newArrayList(underWriterDocumentItem.getDocumentIds());
    }

    private RoutingLevel findRoutingLevelByClientId(String clientId){
        Client client = clientRepository.findOne(new ClientId(clientId));
        List<Map<String,Object>> clientDocumentList = client.findClientDocument();
        List<Map<String,Object>> documentList = clientDocumentList.stream().filter(new Predicate<Map<String, Object>>() {
            @Override
            public boolean test(Map<String, Object> clientDocumentMap) {
                Optional<Map.Entry<String, Object>> optionalClientDocumentMap  = clientDocumentMap.entrySet().stream().findAny();
                return optionalClientDocumentMap.isPresent()?optionalClientDocumentMap.get().getValue()!=null?true:false:false;
            }}).collect(Collectors.toList());
        if (UtilValidator.isEmpty(documentList)){
            return null;
        }
        Optional<Map.Entry<String, Object>> optionalClientDocument = documentList.get(0).entrySet().stream().findAny();
        if (optionalClientDocument.isPresent()) {
            RoutingLevel routingLevel = (RoutingLevel) optionalClientDocument.get().getValue();
            return routingLevel;
        }
        return null;
    }

    private List<String> findDefinedUnderWriterDocumentByClientId(String clientId){
        Client client = clientRepository.findOne(new ClientId(clientId));
        List<Map<String,Object>>  clientDocumentList =  client.findClientDocument();
        List<String> documents =  clientDocumentList.parallelStream().map(new Function<Map<String,Object>, String>() {
            @Override
            public String apply(Map<String, Object> documentMap) {
                Optional<Map.Entry<String, Object>> optionalClientDocument =  documentMap.entrySet().parallelStream().findAny();
                return optionalClientDocument.isPresent()?optionalClientDocument.get().getValue()!=null?
                        optionalClientDocument.get().getKey():null:null;
            }
        }).collect(Collectors.toList());
        return documents;
    }

    public UnderWritingRoutingLevelItem findUnderWriterRoutingLevelItem(Set<UnderWritingRoutingLevelItem> underWriterItems, List<UnderWriterRoutingLevelDetailDto.UnderWriterInfluencingFactorItem> underWriterInfluencingFactorDetailDtos) throws UnderWriterException{
        List<UnderWritingRoutingLevelItem> underWriterItemList = underWriterItems.stream().filter(new Predicate<UnderWritingRoutingLevelItem>() {
            @Override
            public boolean test(UnderWritingRoutingLevelItem underWritingRoutingLevelItem) {
                int noOfMatch = 0;
                for (UnderWriterLineItem underWriterLineItem : underWritingRoutingLevelItem.getUnderWriterLineItems()) {
                    if (isMatchesInfluencingFactorAndValue(underWriterLineItem, underWriterInfluencingFactorDetailDtos)) {
                        noOfMatch = noOfMatch + 1;
                        continue;
                    }
                }
                return underWriterInfluencingFactorDetailDtos.size() == noOfMatch;
            }
        }).collect(Collectors.toList());
        if (isEmpty(underWriterItemList)) {
            raiseUnderWriterNotFoundException();
        }
        checkArgument(underWriterItemList.size() == 1);
        return underWriterItemList.get(0);
    }

    private UnderWriterDocumentItem findUnderWriterDocumentLineItem(Set<UnderWriterDocumentItem> underWriterDocumentItems, List<UnderWriterRoutingLevelDetailDto.UnderWriterInfluencingFactorItem> underWriterInfluencingFactor) {
        List<UnderWriterDocumentItem> underWriterItemList = underWriterDocumentItems.stream().filter(new Predicate<UnderWriterDocumentItem>() {
            @Override
            public boolean test(UnderWriterDocumentItem underWriterDocumentItem) {
                int noOfMatch = 0;
                for (UnderWriterLineItem underWriterLineItem : underWriterDocumentItem.getUnderWriterLineItems()) {
                    if (isMatchesInfluencingFactorAndValue(underWriterLineItem, underWriterInfluencingFactor)) {
                        noOfMatch = noOfMatch + 1;
                        continue;
                    }
                }
                return underWriterInfluencingFactor.size() == noOfMatch;
            }
        }).collect(Collectors.toList());
        if (isEmpty(underWriterItemList)) {
            raiseUnderWriterNotFoundException();
        }
        checkArgument(underWriterItemList.size() == 1);
        return underWriterItemList.get(0);
    }

    private boolean isMatchesInfluencingFactorAndValue(UnderWriterLineItem underWriterLineItem, List<UnderWriterRoutingLevelDetailDto.UnderWriterInfluencingFactorItem> underWriterInfluencingFactorDetailDtos) {
        for (UnderWriterRoutingLevelDetailDto.UnderWriterInfluencingFactorItem underWriterInfluencingFactorDetailDto : underWriterInfluencingFactorDetailDtos) {
            if (underWriterInfluencingFactorDetailDto.getUnderWriterInfluencingFactor().equals(underWriterLineItem.getUnderWriterInfluencingFactor().name())) {
                return underWriterLineItem.getUnderWriterInfluencingFactor().isValueIsInRange(underWriterInfluencingFactorDetailDto.getValue(), underWriterLineItem.getInfluencingItemFrom(), underWriterLineItem.getInfluencingItemTo());
            }
        }
        return false;
    }

    List<String> transformUnderWriterInfluencingFactor(List<UnderWriterRoutingLevelDetailDto.UnderWriterInfluencingFactorItem> underWriterInfluencingFactorDetailDtos) {
        List<String> underWriterInfluencingFactors = Lists.newArrayList();
        underWriterInfluencingFactorDetailDtos.forEach(influencingFactor -> {
            underWriterInfluencingFactors.add(influencingFactor.getUnderWriterInfluencingFactor());
        });
        return underWriterInfluencingFactors;
    }
}
