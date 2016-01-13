package com.pla.grouplife.sharedresource.exception;

/**
 * Created by Samir on 5/18/2015.
 */
public class GLInsuredTemplateExcelParseException extends RuntimeException {

    private GLInsuredTemplateExcelParseException(String message) {
        super(message);
    }

    public static void raiseNotValidHeaderException() {
        throw new GLInsuredTemplateExcelParseException("Header is not valid.");
    }

    public static void raiseNotValidFirstHeaderException() {
        throw new GLInsuredTemplateExcelParseException("First row should contain relationship as Self.");
    }

    public static void raiseFileIsBlank() {
        throw new GLInsuredTemplateExcelParseException("Please share either proposed assured details or number of proposed assured.");
    }

    public static void raiseNotSamePlanForAllCategoryException() {
        throw new GLInsuredTemplateExcelParseException("For all Self relationship plan code should be same");
    }

    public static void raiseNotSamePlanForAllCategoryException(String relation) {
        throw new GLInsuredTemplateExcelParseException("For all "+relation +" relationship the cover details should be same");
    }

    public static void raiseNotSamePlanForAllRelationshipException() {
        throw new GLInsuredTemplateExcelParseException("For all relationship other then Self; plan code should be same");
    }


    public static void raiseNotSamePlanForAllRelationshipException(String category) {
        throw new GLInsuredTemplateExcelParseException("For all "+category +" category the cover details should be same");
    }

    public static void raiseNotSamePlanForAllCategoryAndRelationshipException() {
        throw new GLInsuredTemplateExcelParseException("For all relationship and Category cover should be same");
    }

    public static void raiseNotValidValueException(String message) {
        throw new GLInsuredTemplateExcelParseException(message);
    }
}
