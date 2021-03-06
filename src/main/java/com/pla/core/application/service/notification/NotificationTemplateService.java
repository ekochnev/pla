package com.pla.core.application.service.notification;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.pla.core.domain.model.notification.*;
import com.pla.core.query.NotificationFinder;
import com.pla.publishedlanguage.contract.IProcessInfoAdapter;
import com.pla.sharedkernel.application.CreateNotificationHistoryCommand;
import com.pla.sharedkernel.domain.model.ProcessType;
import com.pla.sharedkernel.domain.model.ReminderTypeEnum;
import com.pla.sharedkernel.domain.model.WaitingForEnum;
import com.pla.sharedkernel.exception.ProcessInfoException;
import com.pla.sharedkernel.identifier.LineOfBusinessEnum;
import org.apache.commons.io.FileUtils;
import org.apache.poi.xwpf.converter.core.FileURIResolver;
import org.apache.poi.xwpf.converter.xhtml.XHTMLConverter;
import org.apache.poi.xwpf.converter.xhtml.XHTMLOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.docx4j.model.datastorage.migration.VariablePrepare;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.joda.time.LocalDate;
import org.nthdimenzion.common.AppConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static org.nthdimenzion.utils.UtilValidator.isNotEmpty;

/**
 * Created by Admin on 6/30/2015.
 */
@Service
public class NotificationTemplateService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private NotificationFinder notificationFinder;

    @Autowired
    private IProcessInfoAdapter iProcessInfoAdapter;

    public static ImmutableMap<LineOfBusinessEnum,String> proposalEntitiesMap = ImmutableMap.of(LineOfBusinessEnum.GROUP_LIFE,"group_life_proposal",LineOfBusinessEnum.GROUP_HEALTH,"group_health_proposal");

    public static ImmutableMap<LineOfBusinessEnum,String> quotationEntitiesMap = ImmutableMap.of(LineOfBusinessEnum.GROUP_LIFE,"group_life_quotation",LineOfBusinessEnum.GROUP_HEALTH,"group_health_quotation",
            LineOfBusinessEnum.INDIVIDUAL_LIFE,"individual_life_quotation");


    public HashMap<String,String> getQuotationNotificationTemplateData(LineOfBusinessEnum lineOfBusiness, String quotationId) throws ProcessInfoException {
        int closureTimePeriod = iProcessInfoAdapter.getClosureTimePeriod(lineOfBusiness, ProcessType.QUOTATION);
        HashMap<String,String> notificationQuotationMap = null;
        if (LineOfBusinessEnum.INDIVIDUAL_LIFE.equals(lineOfBusiness)){
            notificationQuotationMap = notificationFinder.findILQuotationProposerDetail(quotationId);
            notificationQuotationMap.put("closureDays",String.valueOf(closureTimePeriod));
            return notificationQuotationMap;
        }
        Criteria quotationCriteria = Criteria.where("_id").is(quotationId);
        Query query = new Query(quotationCriteria);
        query.fields().include("proposer.proposerName").include("quotationNumber").include("sharedOn").include("insureds.planPremiumDetail.planId").
                include("proposer.contactDetail.addressLine1").include("proposer.contactDetail.addressLine2").include("proposer.contactDetail.province").include("proposer.contactDetail.town")
                .include("proposer.contactDetail.emailAddress").exclude("_id");
        List<Map> quotationNotificationDetail =  mongoTemplate.find(query, Map.class, quotationEntitiesMap.get(lineOfBusiness));
        HashMap<String,String> emailContentMap = transformQuotationNotificationData(quotationNotificationDetail);
        emailContentMap.put("closureDays",String.valueOf(closureTimePeriod));
        checkArgument(isNotEmpty(quotationNotificationDetail), "Notification detail not found for the quotation");
        return emailContentMap;
    }

    public HashMap<String,String> getProposalNotificationTemplateData(LineOfBusinessEnum lineOfBusiness, String proposalId){
        Criteria proposalCriteria = Criteria.where("_id").is(proposalId);
        Query query = new Query(proposalCriteria);
        query.fields().include("proposer.proposerName").include("proposalNumber.proposalNumber").include("sharedOn").include("insureds.planPremiumDetail.planId").
                include("proposer.contactDetail.addressLine1").include("proposer.contactDetail.addressLine2").include("proposer.contactDetail.province").include("proposer.contactDetail.town")
                .include("proposer.contactDetail.emailAddress").exclude("_id");
        List<Map> proposalNotificationDetailMap =  mongoTemplate.find(query, Map.class, proposalEntitiesMap.get(lineOfBusiness));
        HashMap<String,String>  proposalNotificationMap = transformQuotationNotificationData(proposalNotificationDetailMap);
        return proposalNotificationMap;
    }

    public String getRequestNumberBy(LineOfBusinessEnum lineOfBusinessEnum,ProcessType processType,String id) throws ProcessInfoException {
        switch (processType){
            case QUOTATION:
                return getQuotationNotificationTemplateData(lineOfBusinessEnum,id).get("requestNumber").toString();
            case PROPOSAL:
                return getProposalNotificationTemplateData(lineOfBusinessEnum,id).get("requestNumber").toString();
        }
        return "";
    }

    private HashMap<String,String> transformQuotationNotificationData(List<Map> notificationData){
        HashMap<String,String> notificationQuotationMap = new HashMap<String,String>();
        notificationData.parallelStream().map(new Function<Map, Map<String, String>>() {
            @Override
            public Map<String, String> apply(Map map) {
                Map<String, Object> proposerMap = (Map) map.get("proposer");
                if (isNotEmpty(proposerMap)) {
                    notificationQuotationMap.put("proposerName", proposerMap.get("proposerName")!=null?proposerMap.get("proposerName").toString():"");
                    Map<String, Object> contactDetailMap = (Map) proposerMap.get("contactDetail");
                    notificationQuotationMap.put("addressLine1",contactDetailMap.get("addressLine1")!=null?contactDetailMap.get("addressLine1").toString():"");
                    notificationQuotationMap.put("addressLine2", contactDetailMap.get("addressLine2")!=null?contactDetailMap.get("addressLine2").toString():"");
                    notificationQuotationMap.put("province", contactDetailMap.get("province")!=null?contactDetailMap.get("province").toString():"");
                    notificationQuotationMap.put("town", contactDetailMap.get("town")!=null?contactDetailMap.get("town").toString():"");
                    notificationQuotationMap.put("emailAddress", contactDetailMap.get("emailAddress")!=null?contactDetailMap.get("emailAddress").toString():"");
                    notificationQuotationMap.put("requestNumber", map.get("quotationNumber")!=null?map.get("quotationNumber").toString():"");
                }
                return notificationQuotationMap;
            }
        }).collect(Collectors.toList());
        return notificationQuotationMap;
    }

    public CreateNotificationHistoryCommand generateHistoryDetail(String notificationId,String[] recipientMailAddress,String emailBody){
        Criteria notificationCriteria = Criteria.where("_id").is(notificationId);
        Query query = new Query(notificationCriteria);
        NotificationHistory notificationHistory =  mongoTemplate.findOne(query, NotificationHistory.class);
        if (notificationHistory!=null) {
            return new CreateNotificationHistoryCommand(notificationHistory.getRequestNumber(), notificationHistory.getRoleType(), notificationHistory.getLineOfBusiness(),
                    notificationHistory.getProcessType(), notificationHistory.getWaitingFor(), notificationHistory.getReminderType(), recipientMailAddress, emailBody.getBytes(), notificationId);
        }
        return null;
    }

    public byte[] printNotificationHistory(String notificationId){
        Criteria notificationCriteria = Criteria.where("_id").is(notificationId);
        Query query = new Query(notificationCriteria);
        NotificationHistory notificationHistory =  mongoTemplate.findOne(query, NotificationHistory.class);
        if (notificationHistory!=null) {
            return notificationHistory.getReminderTemplate();
        }
        return null;
    }

    public NotificationBuilder generateNotification(LineOfBusinessEnum lineOfBusiness, ProcessType process, WaitingForEnum waitingFor, ReminderTypeEnum reminderType,
                                                    String requestNumber, String roleType, byte[] templateFile, HashMap<String, String> notificationDetail) throws Exception {
        checkArgument(notificationDetail !=null,"Notification details cannot be empty");
        checkArgument(templateFile != null, "Notification Template is not uploaded");
        NotificationBuilder notificationBuilder = Notification.builder();
        notificationBuilder.withLineOfBusiness(lineOfBusiness)
                .withProcessType(process)
                .withRequestNumber(requestNumber)
                .withWaitingFor(waitingFor)
                .withReminderType(reminderType)
                .withEmailAddress(notificationDetail.get("emailAddress") != null ? notificationDetail.get("emailAddress").toString() : "")
                .withReminderTemplate(convert(templateFile, notificationDetail, lineOfBusiness,requestNumber))
                .withRoleType(roleType);
        return notificationBuilder;
    }

    public byte[] convert(byte[] templateFile,HashMap<String,String> notificationMap,LineOfBusinessEnum lineOfBusiness,String requestNumber) throws Exception {
        File tempFile  = new File("./src/main/resources/emailtemplate/notification_"+requestNumber+".docx");
        Files.write(templateFile, tempFile);
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(tempFile);
        VariablePrepare.prepare(wordMLPackage);
        MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
        notificationMap.put("systemDate", LocalDate.now().toString(AppConstants.DD_MM_YYY_FORMAT));
        notificationMap.put("lineOfBusiness",lineOfBusiness.toString());
        documentPart.variableReplace(notificationMap);
        wordMLPackage.save(tempFile);
        String htmlContent = convertDocxToHtml(tempFile);
        return htmlContent.getBytes();
    }

    public String convertDocxToHtml(File tempFile) throws IOException {
        XWPFDocument document = new XWPFDocument(FileUtils.openInputStream(tempFile));
        XHTMLOptions options = XHTMLOptions.create().URIResolver(new FileURIResolver(new File("word/media")));
        OutputStream outputStream = new ByteArrayOutputStream();
        XHTMLConverter.getInstance().convert(document, outputStream, options);
        FileUtils.forceDelete(tempFile);
        return outputStream.toString();
    }
}
