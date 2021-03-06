package com.pla.individuallife.sharedresource.model.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

/**
 * Created by Karunakar on 7/6/2015.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GeneralDetailQuestion {
    private String questionId;
    private boolean answer;
    private Set<GeneralDetailsQuestionResponse> answerResponse;

}
