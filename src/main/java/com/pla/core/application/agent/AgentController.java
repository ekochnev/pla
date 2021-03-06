/*
 * Copyright (c) 3/19/15 9:09 PM .NthDimenzion,Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.pla.core.application.agent;

import com.google.common.base.Preconditions;
import com.pla.core.application.exception.AgentApplicationException;
import com.pla.core.domain.model.agent.Agent;
import com.pla.core.dto.ChannelTypeDto;
import com.pla.core.dto.DesignationDto;
import com.pla.core.query.AgentFinder;
import com.pla.core.query.TeamFinder;
import com.pla.publishedlanguage.contract.ISMEGateway;
import com.pla.publishedlanguage.domain.model.EmployeeDto;
import com.pla.sharedkernel.util.SequenceGenerator;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.nthdimenzion.presentation.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import static org.nthdimenzion.presentation.AppUtils.getLoggedInUserDetail;
import static org.nthdimenzion.utils.UtilValidator.isNotEmpty;

/**
 * @author: Samir
 * @since 1.0 19/03/2015
 */
@Controller
@RequestMapping(value = "/core/agent")
public class AgentController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentController.class);

    private CommandGateway commandGateway;

    private ISMEGateway smeGateway;

    private AgentFinder agentFinder;

    private SequenceGenerator sequenceGenerator;

    private TeamFinder teamFinder;

    private MongoTemplate mongoTemplate;

    @Autowired
    public AgentController(CommandGateway commandGateway, AgentFinder agentFinder, SequenceGenerator sequenceGenerator, TeamFinder teamFinder, ISMEGateway smeGateway, MongoTemplate mongoTemplate) {
        this.commandGateway = commandGateway;
        this.agentFinder = agentFinder;
        this.sequenceGenerator = sequenceGenerator;
        this.teamFinder = teamFinder;
        this.smeGateway = smeGateway;
        this.mongoTemplate = mongoTemplate;
    }

    @RequestMapping(value = "/listagent", method = RequestMethod.GET)
    public ModelAndView viewAgents() {
        List<Map<String, Object>> nonTerminatedAgents = agentFinder.getAllNonTerminatedAgent();
        List<Map<String, Object>> allAgentPlans = agentFinder.getAllAgentPlan();
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("pla/core/agent/viewagent");
        List<Map<String, Object>> agentContacts = agentFinder.getAllAgentContacts();
        modelAndView.addObject("agentList", CreateAgentCommand.transformToAgentCommand(nonTerminatedAgents, allAgentPlans, mongoTemplate.findAll(Map.class, "PLAN"), agentContacts));
        return modelAndView;
    }


    @RequestMapping(value = "/opencreatepage", method = RequestMethod.GET)
    public View openAgentCreatePage() {
        return new RedirectView("createagent", true);
    }

    @RequestMapping(value = "/getagentid", method = RequestMethod.GET)
    @ResponseBody
    public String getAgentId() {
        return sequenceGenerator.getSequence(Agent.class);
    }

    @RequestMapping(value = "/createagent", method = RequestMethod.GET)
    public ModelAndView createAgentPage() {
        return new ModelAndView("pla/core/agent/createagent");
    }


    @RequestMapping(value = "/getteams", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String, Object>> getAllActiveTeam() {
        return teamFinder.getAllActiveTeam();
    }

    @RequestMapping(value = "/openeditpage", method = RequestMethod.GET)
    public ModelAndView openAgentEditPage() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("pla/core/agent/createagent");
        return modelAndView;
    }

    @RequestMapping(value = "/agentdetail", method = RequestMethod.GET)
    @ResponseBody
    public CreateAgentCommand getAgentDetail(@RequestParam("agentId") String agentId) {
        Preconditions.checkArgument(isNotEmpty(agentId));
        Map<String, Object> agentDetail = agentFinder.getAgentById(agentId);
        List<Map<String, Object>> allAgentPlans = agentFinder.getAllAgentPlan();
        List<Map<String, Object>> agentContacts = agentFinder.getAllAgentContacts();
        return CreateAgentCommand.transformToAgentCommand(agentDetail, allAgentPlans, mongoTemplate.findAll(Map.class, "PLAN"), agentContacts);
    }

    @RequestMapping(value = "/viewagentdetail", method = RequestMethod.GET)
    public ModelAndView viewAgentDetail(@RequestParam("agentId") String agentId) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("pla/core/agent/agentDetail");
        Preconditions.checkArgument(isNotEmpty(agentId));
        Map<String, Object> agentDetail = agentFinder.getAgentById(agentId);
        List<Map<String, Object>> agentPlans = agentFinder.getAllAgentPlan();
        List<Map<String, Object>> agentContacts = agentFinder.getAllAgentContacts();
        modelAndView.addObject("agentDetail", CreateAgentCommand.transformToAgentCommand(agentDetail, agentPlans, mongoTemplate.findAll(Map.class, "PLAN"), agentContacts));
        return modelAndView;
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Result createAgent(@RequestBody @Valid CreateAgentCommand createAgentCommand, BindingResult bindingResult, HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return Result.failure("Error in creating agent", bindingResult.getAllErrors());
        }
        try {
            UserDetails userDetails = getLoggedInUserDetail(request);
            createAgentCommand.setUserDetails(userDetails);
            commandGateway.sendAndWait(createAgentCommand);
        } catch (AgentApplicationException e) {
            LOGGER.error("Error in creating agent", e);
            return Result.failure(e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Error in creating agent", e);
            return Result.failure(e.getMessage());
        }
        return Result.success("Agent created successfully");
    }

    @RequestMapping(value = "/createbroker", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Result createBroker(@RequestBody @Valid CreateAgentCommand createAgentCommand, BindingResult bindingResult, HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return Result.failure("Error in creating broker", bindingResult.getAllErrors());
        }
        try {
            UserDetails userDetails = getLoggedInUserDetail(request);
            createAgentCommand.setChannelType(new ChannelTypeDto("BROKER", "Broker"));
            createAgentCommand.getAgentProfile().setDesignationDto(new DesignationDto("AGENT","Agent"));
            createAgentCommand.setUserDetails(userDetails);
            commandGateway.sendAndWait(createAgentCommand);
        } catch (AgentApplicationException e) {
            LOGGER.error("Error in creating broker", e);
            return Result.failure(e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Error in creating agent", e);
            return Result.failure(e.getMessage());
        }
        return Result.success("Broker created successfully");
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Result updateAgent(@RequestBody @Valid UpdateAgentCommand updateAgentCommand, BindingResult bindingResult, HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return Result.failure("Error in updating agent", bindingResult.getAllErrors());
        }
        try {
            UserDetails userDetails = getLoggedInUserDetail(request);
            updateAgentCommand.setUserDetails(userDetails);
            commandGateway.sendAndWait(updateAgentCommand);
        } catch (AgentApplicationException e) {
            LOGGER.error("Error in updating agent", e);
            return Result.failure(e.getMessage());
        }
        return Result.success("Agent updated successfully");
    }

    @RequestMapping(value = "/getemployeedeatil", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.ALL_VALUE)
    @ResponseBody
    public Result getEmployeeDetail(@RequestParam(value = "employeeId", required = false) String employeeId, @RequestParam(value = "nrcNumber", required = false) String nrcNumber) {
        EmployeeDto employeeDto;
        try {
            if (isNotEmpty(nrcNumber)) {
                String nrcNumberWithoutSlash = nrcNumber.replaceAll("/", "").trim();
                Integer agentCount = agentFinder.findAgentCountByNrcNumber(nrcNumberWithoutSlash);
                if (agentCount != 0)
                    return Result.failure("Agent cannot be created as nrc number is in use");
            }
            employeeDto = smeGateway.getEmployeeDetailByIdOrByNRCNumber(employeeId, nrcNumber);
        } catch (Exception e) {
            return Result.failure(e.getLocalizedMessage());
        }
        return Result.success("Agent Details", employeeDto);
    }

    @RequestMapping(value = "/getallplan", method = RequestMethod.GET)
    @ResponseBody
    public List<Map> getAllPlans() {
        return mongoTemplate.findAll(Map.class, "PLAN");
    }

    /**
     * Used for Searching Agent for Individual Life
     *
     * @param searchStr
     * @return
     */
    @RequestMapping(value = "/search/{searchString}", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String, Object>> searchAgent(@PathVariable("searchString") String searchStr) {
        List<Map<String, Object>> agentList = agentFinder.searchAgent(searchStr);
        return agentList;
    }

    @RequestMapping(value = "/searchplan", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String, Object>> searchPlan(@RequestParam("agentId") String agentId) {
        List<Map<String, Object>> planList = agentFinder.searchPlanByAgentId(agentId);
        return planList;
    }

}
