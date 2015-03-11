/*
 * Copyright (c) 3/11/15 3:36 PM .NthDimenzion,Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package org.nthdimenzion.presentation;

import com.google.common.collect.Lists;
import lombok.Getter;
import org.springframework.validation.ObjectError;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Nthdimenzion
 * Date: 4/6/13
 * Time: 4:02 PM
 */
@Getter
public final class Result {

    private static String successMsg = "";

    private static String failureMsg = "Could not complete operation,Contact Administrator";

    enum RESULT_TYPE {
        success("200"), error("500");

        private String code;

        RESULT_TYPE(String code) {
            this.code = code;

        }
    }

    private String status;

    private String message;

    private RESULT_TYPE resultType;

    private String id;

    private String failureDetails;


    public Result(String message, RESULT_TYPE resultType) {
        this.message = message;
        this.resultType = resultType;
        this.status = resultType.code;
    }


    static Result FailureResultWithErrorDetails(String message, String failureDetails) {
        Result result = new Result(message, RESULT_TYPE.error);
        result.failureDetails = failureDetails;
        return result;
    }

    public Result(String message, RESULT_TYPE resultType, String id) {
        this(message, resultType);
        this.id = id;
    }

    public static Result Success() {
        return new Result(successMsg, RESULT_TYPE.success);
    }

    public static Result Success(String message) {
        return new Result(message, RESULT_TYPE.success);
    }

    public static Result Success(String message, String id) {
        return new Result(message, RESULT_TYPE.success, id);
    }

    public static Result Failure(String message) {
        return new Result(message, RESULT_TYPE.error);
    }

    public static Result Failure(String message, List<ObjectError> errors) {
        final List<String> errorCodes = Lists.newArrayList();
        for (ObjectError error : errors) {
            errorCodes.add(error.getDefaultMessage());
        }
        return FailureResultWithErrorDetails(message, org.thymeleaf.util.StringUtils.join(errorCodes, ","));
    }

    public static Result Failure() {
        return new Result(failureMsg, RESULT_TYPE.error);
    }


    @Override
    public String toString() {
        return "Result{" +
                "message='" + message + '\'' +
                ", resultType=" + resultType +
                '}';
    }
}
