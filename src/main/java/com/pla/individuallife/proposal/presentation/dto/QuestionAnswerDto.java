package com.pla.individuallife.proposal.presentation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Created by Prasant on 04-Jun-15.
 */
@Getter
@NoArgsConstructor
public class QuestionAnswerDto {

   private Long questionId;
    private Boolean response;

    public QuestionAnswerDto(long questionId, boolean response) {
        this.questionId = questionId;
        this.response = response;
    }
}