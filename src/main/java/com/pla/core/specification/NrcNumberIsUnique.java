package com.pla.core.specification;

import com.pla.core.dto.AgentDto;
import com.pla.core.query.AgentFinder;
import com.pla.sharedkernel.specification.ISpecification;
import org.nthdimenzion.ddd.domain.annotations.Specification;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Admin on 4/14/2015.
 */
@Specification
public class NrcNumberIsUnique implements ISpecification<AgentDto> {

    private AgentFinder agentFinder;

    @Autowired
    NrcNumberIsUnique(AgentFinder agentFinder){
        this.agentFinder =agentFinder;
    }

    @Override
    public boolean isSatisfiedBy(AgentDto agentDto) {
        int agentCount = agentFinder.getAgentCountByNrcNumber(agentDto);
        return agentCount == 0;
    }
}
