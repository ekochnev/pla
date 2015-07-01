package com.pla.individuallife.proposal.presentation.controller;

import com.google.common.collect.Lists;
import com.pla.core.query.PlanFinder;
import com.pla.individuallife.proposal.application.command.*;
import com.pla.individuallife.proposal.presentation.dto.ILSearchProposalDto;
import com.pla.individuallife.proposal.presentation.dto.QuestionDto;
import com.pla.individuallife.proposal.query.ILProposalFinder;
import com.pla.individuallife.quotation.application.service.ILQuotationAppService;
import com.pla.individuallife.quotation.presentation.dto.ILSearchQuotationDto;
import com.pla.individuallife.quotation.query.ILQuotationFinder;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.gateway.GatewayProxyFactory;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static org.nthdimenzion.presentation.AppUtils.getLoggedInUserDetail;

/**
 * Created by Prasant on 26-May-15.
 */
@Controller
@RequestMapping(value = "/individuallife/proposal")
public class ILProposalSetUpController {

    private final ILProposalCommandGateway proposalCommandGateway;
    private final PlanFinder planFinder;

    @Autowired
    private ILQuotationFinder ilQuotationFinder;

    @Autowired
    private ILProposalFinder ilProposalFinder;

    @Autowired
    private ILQuotationAppService ilQuotationService;

    @Autowired
    public ILProposalSetUpController(CommandBus commandBus, PlanFinder planFinder) {
        this.planFinder = planFinder;
        GatewayProxyFactory factory = new GatewayProxyFactory(commandBus);
        proposalCommandGateway = factory.createGateway(ILProposalCommandGateway.class);
    }

    @ResponseBody
    @RequestMapping(value = "/create")
    public ResponseEntity<Map> createProposal(@RequestBody ILCreateProposalCommand createProposalCommand, BindingResult bindingResult, HttpServletRequest request) {
        String proposalId = null;

        if (bindingResult.hasErrors()) {
            return new ResponseEntity(bindingResult.getAllErrors(), HttpStatus.PRECONDITION_FAILED);
        }
        try {
            proposalId = new ObjectId().toString();
            UserDetails userDetails = getLoggedInUserDetail(request);
            createProposalCommand.setUserDetails(userDetails);
            createProposalCommand.setProposalId(proposalId);
            proposalCommandGateway.createProposal(createProposalCommand);
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Map map = new HashMap<>();
        map.put("msg", "Proposal got created successfully");
        map.put("proposalId", proposalId);
        return new ResponseEntity(map, HttpStatus.OK);
    }

    @ResponseBody
    @RequestMapping(value = "/updateproposer")
    public ResponseEntity<Map> updateWithProposerDetails(@RequestBody ILProposalUpdateWithProposerCommand cmd, BindingResult bindingResult, HttpServletRequest request) {
        String proposalId = cmd.getProposalId();
        if (bindingResult.hasErrors()) {
            return new ResponseEntity(bindingResult.getAllErrors(), HttpStatus.PRECONDITION_FAILED);
        }
        try {
            UserDetails userDetails = getLoggedInUserDetail(request);
            cmd.setUserDetails(userDetails);
            proposalCommandGateway.updateWithProposer(cmd);
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Map map = new HashMap<>();
        map.put("msg", "Proposal updated with Proposer Details successfully");
        map.put("proposalId", proposalId);
        return new ResponseEntity(map, HttpStatus.OK);
    }

    @ResponseBody
    @RequestMapping(value = "/updateplan")
    public ResponseEntity<Map> updateWithPlanDetails(@RequestBody ILProposalUpdateWithPlanAndBeneficiariesCommand cmd, BindingResult bindingResult, HttpServletRequest request) {
        String proposalId = cmd.getProposalId();
        if (bindingResult.hasErrors()) {
            return new ResponseEntity(bindingResult.getAllErrors(), HttpStatus.PRECONDITION_FAILED);
        }
        try {
            UserDetails userDetails = getLoggedInUserDetail(request);
            cmd.setUserDetails(userDetails);
            proposalCommandGateway.updateWithPlandetail(cmd);
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Map map = new HashMap<>();
        map.put("msg", "Proposal updated with Plan and Beneficiary Details successfully");
        map.put("proposalId", proposalId);
        return new ResponseEntity(map, HttpStatus.OK);
    }



    @ResponseBody
    @RequestMapping(value = "/updatecompulsoryhealthstatement", method = RequestMethod.POST)
    public ResponseEntity<Map> updateCompulsoryHealthStatement(
            @RequestBody ILProposalUpdateCompulsoryHealthStatementCommand cmd,
            HttpServletRequest request,
            BindingResult bindingResult) {
        String proposalId = cmd.getProposalId();
        if (bindingResult.hasErrors()) {
            return new ResponseEntity(bindingResult.getAllErrors(), HttpStatus.PRECONDITION_FAILED);
        }
        try {
            UserDetails userDetails = getLoggedInUserDetail(request);
            cmd.setUserDetails(userDetails);
            proposalCommandGateway.updateCompulsoryHealthStatement(cmd);
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<QuestionDto> list=cmd.getCompulsoryHealthDetails();
        Map map = new HashMap<>();
        map.put("msg", "Proposal updated with Compulsory Health Statement Details successfully");
        map.put("proposalId", proposalId);
        map.put("questions", list);
        return new ResponseEntity(map, HttpStatus.OK);
    }

    @ResponseBody
    @RequestMapping(value = "/updategeneraldetails", method = RequestMethod.POST)
    public ResponseEntity<Map> updateGeneralDetails(
            @RequestBody ILProposalUpdateGeneralDetailsCommand cmd,
            HttpServletRequest request,
            BindingResult bindingResult) {
        String proposalId = cmd.getProposalId();
        if (bindingResult.hasErrors()) {
            return new ResponseEntity(bindingResult.getAllErrors(), HttpStatus.PRECONDITION_FAILED);
        }
        try {
            UserDetails userDetails = getLoggedInUserDetail(request);
            cmd.setUserDetails(userDetails);
            proposalCommandGateway.updateGeneralDetails(cmd);
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<QuestionDto> questions = cmd.getGenerateDetails();
        Map map = new HashMap<>();
        map.put("msg", "Proposal updated with General Details successfully");
        map.put("proposalId", proposalId);
        map.put("questions", questions);
        return new ResponseEntity(map, HttpStatus.OK);
    }

    @ResponseBody
    @RequestMapping(value = "/updateadditionaldetails", method = RequestMethod.POST)
    public ResponseEntity<Map> updateAdditionalDetails(
            @RequestBody ILProposalUpdateAdditionalDetailsCommand cmd,
            HttpServletRequest request,
            BindingResult bindingResult) {
        String proposalId = cmd.getProposalId();
        if (bindingResult.hasErrors()) {
            return new ResponseEntity(bindingResult.getAllErrors(), HttpStatus.PRECONDITION_FAILED);
        }
        try {
            UserDetails userDetails = getLoggedInUserDetail(request);
            cmd.setUserDetails(userDetails);
            proposalCommandGateway.updateAdditionalDetails(cmd);
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Map map = new HashMap<>();
        map.put("msg", "Proposal updated with Additional Details successfully");
        map.put("proposalId", proposalId);
        return new ResponseEntity(map, HttpStatus.OK);
    }

    @ResponseBody
    @RequestMapping(value = "/updatefamily", method = RequestMethod.POST)
    public ResponseEntity updateFamilyPersonalDetails(@RequestBody ILProposalUpdateFamilyPersonalDetailsCommand cmd,HttpServletRequest request,
                                         BindingResult bindingResult) {

        String proposalId = cmd.getProposalId();
        if (bindingResult.hasErrors()) {
            return new ResponseEntity(bindingResult.getAllErrors(), HttpStatus.PRECONDITION_FAILED);
        }

        try {
            UserDetails userDetails = getLoggedInUserDetail(request);
            cmd.setUserDetails(userDetails);
            proposalCommandGateway.updateFamilyPersonal(cmd);
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Map map = new HashMap<>();
        map.put("msg", "Proposal updated with Family and Personal Details successfully");
        map.put("proposalId", proposalId);
        return new ResponseEntity(map, HttpStatus.OK);
    }

    @RequestMapping(value="/searchQuotation", method = RequestMethod.POST)
    public ModelAndView searchQuotation(ILSearchQuotationDto searchIlDto) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("searchResult", ilQuotationService.searchQuotation(searchIlDto));
        modelAndView.addObject("searchCriteria", searchIlDto);
        modelAndView.setViewName("pla/individuallife/proposal/searchquotationforilproposal");
        return modelAndView;
    }

    @RequestMapping(method = RequestMethod.POST)
    public ModelAndView searchProposal(ILSearchProposalDto ilSearchProposalDto) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("pla/individuallife/proposal/index");
        try {
            modelAndView.addObject("searchResult", ilProposalFinder.searchProposal(ilSearchProposalDto));
        } catch (Exception e) {
            modelAndView.addObject("searchResult", Lists.newArrayList());
        }
        modelAndView.addObject("searchCriteria", ilSearchProposalDto);
        return modelAndView;
    }

}
