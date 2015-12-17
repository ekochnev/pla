package com.pla.core.hcp.application.service;

import com.pla.core.hcp.application.command.CreateOrUpdateHCPCommand;
import com.pla.core.hcp.domain.model.HCP;
import com.pla.core.hcp.domain.model.HCPCode;
import com.pla.core.hcp.query.HCPFinder;
import com.pla.sharedkernel.util.SequenceGenerator;
import org.apache.commons.lang.StringUtils;
import org.nthdimenzion.common.service.JpaRepositoryFactory;
import org.nthdimenzion.ddd.domain.annotations.DomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

import static org.nthdimenzion.utils.UtilValidator.*;

/**
 * Created by Mohan Sharma on 12/17/2015.
 */
@DomainService
public class HCPService {
    JpaRepositoryFactory jpaRepositoryFactory;
    private SimpleJpaRepository<HCP, HCPCode> hcpRepository;
    private HCPFinder hcpFinder;
    SequenceGenerator sequenceGenerator;

    @Autowired
    public HCPService(JpaRepositoryFactory jpaRepositoryFactory, HCPFinder hcpFinder, SequenceGenerator sequenceGenerator){
        hcpRepository = jpaRepositoryFactory.getCrudRepository(HCP.class);
        this.hcpFinder = hcpFinder;
        this.sequenceGenerator = sequenceGenerator;
    }

    @Transactional
    public HCP createOrUpdateHCP(CreateOrUpdateHCPCommand createOrUpdateHCPCommand) {
        String runningSequence = sequenceGenerator.getSequence(HCP.class);
        runningSequence = String.format("%04d", Integer.parseInt(runningSequence.trim()));
        String hcpCodeFormatted = createOrUpdateHCPCommand.getHcpCode() != null ? createOrUpdateHCPCommand.getHcpCode() : HCPCode.getHCPCode(StringUtils.substring(createOrUpdateHCPCommand.getTown(), 0, 3), runningSequence, createOrUpdateHCPCommand.getHcpCategory());
        HCP hcp = hcpRepository.findOne(new HCPCode(hcpCodeFormatted));
        if(isEmpty(hcp)){
            hcp = new HCP();
        }
        hcp.updateWithHCPCode(new HCPCode(hcpCodeFormatted))
                .updateWithActivatedOn(createOrUpdateHCPCommand.getActivatedOn())
                .updateWithHcpCategory(createOrUpdateHCPCommand.getHcpCategory())
                .updateWithHcpAddress(createOrUpdateHCPCommand.getAddressLine1(), createOrUpdateHCPCommand.getAddressLine2(), createOrUpdateHCPCommand.getPostalCode(), createOrUpdateHCPCommand.getProvince(), createOrUpdateHCPCommand.getTown())
                .updateWithHCPName(createOrUpdateHCPCommand.getHcpName())
                .updateWithHcpStatus(createOrUpdateHCPCommand.getHcpStatus())
                .updateWithHcpContactDetail(createOrUpdateHCPCommand.getEmailId(), createOrUpdateHCPCommand.getWorkPhoneNumber())
                .updateWithHcpContactPersonDetail(createOrUpdateHCPCommand.getContactPersonDetail(), createOrUpdateHCPCommand.getContactPersonMobile(), createOrUpdateHCPCommand.getContactPersonWorkPhoneNumber(), createOrUpdateHCPCommand.getContactPersonEmailId());
        return hcpRepository.saveAndFlush(hcp);
    }

    public CreateOrUpdateHCPCommand getHCPByHCPCode(String hcpCode){
        HCP hcp = hcpRepository.findOne(new HCPCode(hcpCode));
        return CreateOrUpdateHCPCommand.setPropertiesFromHCPEntity(hcp);
    }
}
