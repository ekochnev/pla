package com.pla.core.application.notification;

import com.pla.core.application.service.notification.NotificationTemplateService;
import com.pla.core.domain.model.notification.*;
import com.pla.core.repository.NotificationTemplateRepository;
import com.pla.sharedkernel.application.CreateNotificationHistoryCommand;
import com.pla.sharedkernel.application.CreateProposalNotificationCommand;
import com.pla.sharedkernel.application.CreateQuotationNotificationCommand;
import org.axonframework.commandhandling.annotation.CommandHandler;
import org.nthdimenzion.common.service.JpaRepositoryFactory;
import org.nthdimenzion.object.utils.IIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Created by Admin on 6/29/2015.
 */
@Component
public class NotificationCommandHandler {

    private NotificationTemplateRepository notificationTemplateRepository;
    private IIdGenerator idGenerator;
    private JpaRepositoryFactory jpaRepositoryFactory;
    private NotificationTemplateService notificationTemplateService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    public NotificationCommandHandler(NotificationTemplateRepository notificationTemplateRepository, JpaRepositoryFactory jpaRepositoryFactory,IIdGenerator idGenerator, NotificationTemplateService notificationTemplateService){
        this.notificationTemplateRepository = notificationTemplateRepository;
        this.jpaRepositoryFactory = jpaRepositoryFactory;
        this.idGenerator = idGenerator;
        this.notificationTemplateService = notificationTemplateService;
    }

    @CommandHandler
    public void createQuotationNotification(CreateQuotationNotificationCommand createQuotationNotificationCommand) throws Exception {
        checkArgument(createQuotationNotificationCommand!=null,"Create Quotation command cannot be empty");
        JpaRepository<Notification, NotificationId> notificationRepository = jpaRepositoryFactory.getCrudRepository(Notification.class);
        HashMap<String,String> quotationNotificationDetailMap =   notificationTemplateService.getQuotationNotificationTemplateData(createQuotationNotificationCommand.getLineOfBusiness(), createQuotationNotificationCommand.getQuotationId().getQuotationId());
        NotificationTemplate notificationTemplate = notificationTemplateRepository.findNotification(createQuotationNotificationCommand.getLineOfBusiness(), createQuotationNotificationCommand.getProcessType(), createQuotationNotificationCommand.getWaitingFor(), createQuotationNotificationCommand.getReminderType());
        checkArgument(notificationTemplate != null, "Notification Template is not uploaded");
        NotificationBuilder notificationBuilder =  notificationTemplateService.generateNotification(createQuotationNotificationCommand.getLineOfBusiness(), createQuotationNotificationCommand.getProcessType(), createQuotationNotificationCommand.getWaitingFor(),
                createQuotationNotificationCommand.getReminderType(), createQuotationNotificationCommand.getQuotationId().getQuotationId(), createQuotationNotificationCommand.getRoleType(), notificationTemplate.getReminderFile(), quotationNotificationDetailMap);
        NotificationId notificationId = new NotificationId(idGenerator.nextId());
        Notification notification  = notificationBuilder.createNotification(notificationId);
        notificationRepository.save(notification);
    }

    @CommandHandler
    public void createProposalNotification(CreateProposalNotificationCommand createProposalNotificationCommand) throws Exception {
        checkArgument(createProposalNotificationCommand!=null,"Create Proposal command cannot be empty");
        JpaRepository<Notification, NotificationId> notificationRepository = jpaRepositoryFactory.getCrudRepository(Notification.class);
        NotificationTemplate notificationTemplate = notificationTemplateRepository.findNotification(createProposalNotificationCommand.getLineOfBusiness(), createProposalNotificationCommand.getProcessType(), createProposalNotificationCommand.getWaitingFor(), createProposalNotificationCommand.getReminderType());
        checkArgument(notificationTemplate!=null,"Notification Template is not uploaded");
        HashMap<String,String>  proposalNotificationDetailMap = notificationTemplateService.getQuotationNotificationTemplateData(createProposalNotificationCommand.getLineOfBusiness(), createProposalNotificationCommand.getProposalId().getProposalId());
        NotificationBuilder notificationBuilder = notificationTemplateService.generateNotification(createProposalNotificationCommand.getLineOfBusiness(), createProposalNotificationCommand.getProcessType(), createProposalNotificationCommand.getWaitingFor(),
                createProposalNotificationCommand.getReminderType(), createProposalNotificationCommand.getProposalId().getProposalId(), createProposalNotificationCommand.getRoleType(), notificationTemplate.getReminderFile(), proposalNotificationDetailMap);
        NotificationId notificationId = new NotificationId(idGenerator.nextId());
        Notification notification  = notificationBuilder.createNotification(notificationId);
        notificationRepository.save(notification);
    }

    @CommandHandler
    public void createNotificationHistory(CreateNotificationHistoryCommand createNotificationHistoryCommand) throws Exception {
        NotificationBuilder notificationBuilder = NotificationHistory.builder();
        notificationBuilder.withLineOfBusiness(createNotificationHistoryCommand.getLineOfBusiness())
                .withProcessType(createNotificationHistoryCommand.getProcessType())
                .withRequestNumber(createNotificationHistoryCommand.getRequestNumber())
                .withWaitingFor(createNotificationHistoryCommand.getWaitingFor())
                .withReminderType(createNotificationHistoryCommand.getReminderType())
                .withRecipientMailAddress(createNotificationHistoryCommand.getRecipientMailAddress())
                .withReminderTemplate(createNotificationHistoryCommand.getTemplate())
                .withRoleType(createNotificationHistoryCommand.getRoleType());
        NotificationHistory notificationHistory = notificationBuilder.createNotificationHistory();
        mongoTemplate.save(notificationHistory);
    }
}
