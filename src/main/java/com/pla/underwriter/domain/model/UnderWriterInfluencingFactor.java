package com.pla.underwriter.domain.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.pla.publishedlanguage.contract.IPlanAdapter;
import lombok.Getter;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.nthdimenzion.utils.UtilValidator.isNotEmpty;

/**
 * Created by Admin on 5/8/2015.
 */
@Getter
public enum UnderWriterInfluencingFactor {

    SUM_ASSURED("Sum Assured"){
        @Override
        public Map<String, Object> transformUnderWriterDocumentLineItem(String fromValue, String toValue) {
            Map<String,Object> influencingFactorValueMap = Maps.newLinkedHashMap();
            influencingFactorValueMap.put(InfluencingFactorRange.SUM_ASSURED_FROM.description,fromValue);
            influencingFactorValueMap.put(InfluencingFactorRange.SUM_ASSURED_TO.description,toValue);
            return influencingFactorValueMap;
        }

        @Override
        public String getRoutingLevelInfluencingFactorFromValue(Map<String, Object> influencingFactorValueMap) {
            return influencingFactorValueMap.get(InfluencingFactorRange.SUM_ASSURED_FROM.description)!=null?
                    influencingFactorValueMap.get(InfluencingFactorRange.SUM_ASSURED_FROM.description).toString():"0";
        }

        @Override
        public String getInfluencingFactorToValue(Map<String, Object> influencingFactorValueMap) {
            return influencingFactorValueMap.get(InfluencingFactorRange.SUM_ASSURED_TO.description)!=null?
                    influencingFactorValueMap.get(InfluencingFactorRange.SUM_ASSURED_TO.description).toString():"0";
        }

        @Override
        public List<String> getInfluencingFactorRange(List<String> influencingFactorRange) {
            influencingFactorRange.add(InfluencingFactorRange.SUM_ASSURED_FROM.description);
            influencingFactorRange.add(InfluencingFactorRange.SUM_ASSURED_TO.description);
            return influencingFactorRange;
        }

        @Override
        public Map<Object, Map<String, Object>> groupUnderWriterRoutingLevelItem(Map<String, Object> underWriterLineItem, Map<Object, Map<String, Object>> underWriterInfluencingFactorMap) {
            List<String> influencingFactorRange = Lists.newArrayList();
            influencingFactorRange = this.getInfluencingFactorRange(influencingFactorRange);
            Map<String,Object> underWriterLineItemMap = Maps.newLinkedHashMap();
            for (Map.Entry<String, Object> underWriterRoutingEntryMap : underWriterLineItem.entrySet()){
                if (influencingFactorRange.contains(underWriterRoutingEntryMap.getKey())){
                    underWriterLineItemMap.put(underWriterRoutingEntryMap.getKey(),underWriterRoutingEntryMap.getValue());
                }
            }
            underWriterInfluencingFactorMap.put(SUM_ASSURED,underWriterLineItemMap);
            return underWriterInfluencingFactorMap;
        }

        @Override
        public boolean isValueAvailableForTheProduct(Row currentRow, String planCode, String coverageId, Map<String, Integer> indexMap, StringBuilder errorMessageBuilder, IPlanAdapter iPlanAdapter, String fromValue, String toValue) {
            String errorMessage = "Defined Sum Assured not available for the Product";
            if (currentRow!=null) {
                Cell sumAssuredFrom = currentRow.getCell(indexMap.get(InfluencingFactorRange.SUM_ASSURED_FROM.description));
                Cell sumAssuredTo = currentRow.getCell(indexMap.get(InfluencingFactorRange.SUM_ASSURED_TO.description));
                String sumAssuredFromCellValue = Cell.CELL_TYPE_NUMERIC == sumAssuredFrom.getCellType() ? ((Double) sumAssuredFrom.getNumericCellValue()).toString() : sumAssuredFrom.getStringCellValue();
                String sumAssuredToCellValue = Cell.CELL_TYPE_NUMERIC == sumAssuredTo.getCellType() ? ((Double) sumAssuredTo.getNumericCellValue()).toString() : sumAssuredTo.getStringCellValue();
                BigDecimal sumAssuredFromCell = NumberUtils.isNumber(sumAssuredFromCellValue) == true ? new BigDecimal(sumAssuredFromCellValue) : BigDecimal.ZERO;
                BigDecimal sumAssuredToCell = NumberUtils.isNumber(sumAssuredToCellValue) == true ? new BigDecimal(sumAssuredToCellValue) : BigDecimal.ZERO;
                boolean isValid = !(isNotEmpty(planCode) && isNotEmpty(coverageId)) ? (iPlanAdapter.isValidPlanSumAssured(planCode, sumAssuredFromCell) && iPlanAdapter.isValidPlanSumAssured(planCode, sumAssuredToCell)) :
                        (iPlanAdapter.isValidCoverageSumAssured(planCode, coverageId, sumAssuredFromCell) && iPlanAdapter.isValidCoverageSumAssured(planCode, coverageId, sumAssuredToCell));
                if (!isValid)
                    errorMessageBuilder.append(errorMessage);
                return isValid;
            }
            else {
                boolean isValid = !(isNotEmpty(planCode) && isNotEmpty(coverageId)) ? (iPlanAdapter.isValidPlanSumAssured(planCode, new BigDecimal(fromValue)) && iPlanAdapter.isValidPlanSumAssured(planCode,new BigDecimal(toValue))):
                        (iPlanAdapter.isValidCoverageSumAssured(planCode, coverageId, new BigDecimal(fromValue)) && iPlanAdapter.isValidCoverageSumAssured(planCode, coverageId,  new BigDecimal(toValue)));
                if (!isValid)
                    errorMessageBuilder.append(errorMessage);
                return isValid;
            }
        }

        @Override
        public boolean isValueIsInRange(String value, String fromValue, String toValue) {
            BigDecimal valueToBeChecked = new BigDecimal(value);
            BigDecimal sumAssuredFromValue = new BigDecimal(fromValue);
            BigDecimal sumAssuredToValue = new BigDecimal(toValue);
            return (valueToBeChecked.compareTo(sumAssuredFromValue) >= 0 && valueToBeChecked.compareTo(sumAssuredToValue) <= 0);
        }

    },
    AGE("Age"){
        @Override
        public Map<String, Object> transformUnderWriterDocumentLineItem(String fromValue, String toValue) {
            Map<String,Object> influencingFactorValueMap = Maps.newLinkedHashMap();
            influencingFactorValueMap.put(InfluencingFactorRange.AGE_FROM.description,fromValue);
            influencingFactorValueMap.put(InfluencingFactorRange.AGE_TO.description,toValue);
            return influencingFactorValueMap;
        }

        @Override
        public String getRoutingLevelInfluencingFactorFromValue(Map<String, Object> influencingFactorValueMap) {
            return influencingFactorValueMap.get(InfluencingFactorRange.AGE_FROM.description)!=null?
                    String.valueOf(new BigDecimal(influencingFactorValueMap.get(InfluencingFactorRange.AGE_FROM.description).toString()).intValue()):"0";
        }

        @Override
        public String getInfluencingFactorToValue(Map<String, Object> influencingFactorValueMap) {
            return influencingFactorValueMap.get(InfluencingFactorRange.AGE_TO.description)!=null?
                    String.valueOf(new BigDecimal(influencingFactorValueMap.get(InfluencingFactorRange.AGE_TO.description).toString()).intValue()):"0";
        }

        @Override
        public List<String> getInfluencingFactorRange(List<String> influencingFactorRange) {
            influencingFactorRange.add(InfluencingFactorRange.AGE_FROM.description);
            influencingFactorRange.add(InfluencingFactorRange.AGE_TO.description);
            return influencingFactorRange;
        }

        @Override
        public Map<Object, Map<String, Object>> groupUnderWriterRoutingLevelItem(Map<String, Object> underWriterLineItem, Map<Object, Map<String, Object>> underWriterInfluencingFactorMap) {
            List<String> influencingFactorRange = Lists.newArrayList();
            influencingFactorRange = this.getInfluencingFactorRange(influencingFactorRange);
            Map<String,Object> underWriterLineItemMap = Maps.newLinkedHashMap();
            for (Map.Entry<String, Object> underWriterRoutingEntryMap : underWriterLineItem.entrySet()){
                if (influencingFactorRange.contains(underWriterRoutingEntryMap.getKey())){
                    underWriterLineItemMap.put(underWriterRoutingEntryMap.getKey(),underWriterRoutingEntryMap.getValue());
                }
            }
            underWriterInfluencingFactorMap.put(AGE,underWriterLineItemMap);
            return underWriterInfluencingFactorMap;
        }

        @Override
        public boolean isValueAvailableForTheProduct(Row currentRow, String planCode, String coverageId, Map<String, Integer> indexMap, StringBuilder errorMessageBuilder, IPlanAdapter iPlanAdapter, String fromValue, String toValue) {
            if (currentRow!=null){
                int ageFromIndex = indexMap.get(InfluencingFactorRange.AGE_FROM.description);
                int ageToIndex= indexMap.get(InfluencingFactorRange.AGE_TO.description);
                Cell ageFrom = currentRow.getCell(ageFromIndex);
                Cell ageTo = currentRow.getCell(ageToIndex);
                String cellValue = Cell.CELL_TYPE_NUMERIC == ageFrom.getCellType() ? ((Double) ageFrom.getNumericCellValue()).toString() : ageFrom.getStringCellValue();
                String ageToCellValue = Cell.CELL_TYPE_NUMERIC == ageTo.getCellType() ? ((Double) ageTo.getNumericCellValue()).toString() : ageTo.getStringCellValue();
                Integer ageFromCellValue = NumberUtils.isNumber(cellValue)==true?Double.valueOf(cellValue).intValue():0;
                Integer ageToValue  = NumberUtils.isNumber(ageToCellValue)==true?Double.valueOf(ageToCellValue).intValue():0;
                boolean isValid = !(isNotEmpty(planCode) && isNotEmpty(coverageId))?(iPlanAdapter.isValidPlanAge(planCode,ageFromCellValue) && iPlanAdapter.isValidPlanAge(planCode,ageToValue)):
                        (iPlanAdapter.isValidCoverageAge(planCode, coverageId, ageFromCellValue) && iPlanAdapter.isValidCoverageAge(planCode, coverageId, ageToValue));
                if (!isValid)
                    errorMessageBuilder.append(" Defined Age is not available for the Product ,");
                return isValid;
            }
            else {
                boolean isValid = !(isNotEmpty(planCode) && isNotEmpty(coverageId))?(iPlanAdapter.isValidPlanAge(planCode,Integer.valueOf(fromValue)) && iPlanAdapter.isValidPlanAge(planCode,Integer.valueOf(toValue))):
                        (iPlanAdapter.isValidCoverageAge(planCode, coverageId, Integer.valueOf(fromValue)) && iPlanAdapter.isValidCoverageAge(planCode, coverageId, Integer.valueOf(toValue)));
                if (!isValid)
                    errorMessageBuilder.append(" Defined Age is not available for the Product ,");

                return isValid;
            }
        }

        @Override
        public boolean isValueIsInRange(String value, String fromValue, String toValue) {
            return (Integer.valueOf(value).compareTo(Integer.valueOf(fromValue))>=0 && Integer.valueOf(value).compareTo(Integer.valueOf(toValue)) <= 0);
        }
    },

    BMI("BMI"){
        @Override
        public Map<String, Object> transformUnderWriterDocumentLineItem(String fromValue, String toValue) {
            Map<String,Object> influencingFactorValueMap = Maps.newLinkedHashMap();
            influencingFactorValueMap.put(InfluencingFactorRange.BMI_FROM.description,fromValue);
            influencingFactorValueMap.put(InfluencingFactorRange.BMI_TO.description,toValue);
            return influencingFactorValueMap;
        }

        @Override
        public String getRoutingLevelInfluencingFactorFromValue(Map<String, Object> influencingFactorValueMap) {
            return influencingFactorValueMap.get(InfluencingFactorRange.BMI_FROM.description)!=null?
                    influencingFactorValueMap.get(InfluencingFactorRange.BMI_FROM.description).toString():"0";
        }

        @Override
        public String getInfluencingFactorToValue(Map<String, Object> influencingFactorValueMap) {
            return influencingFactorValueMap.get(InfluencingFactorRange.BMI_TO.description)!=null?
                    influencingFactorValueMap.get(InfluencingFactorRange.BMI_TO.description).toString():"0";
        }

        @Override
        public List<String> getInfluencingFactorRange(List<String> influencingFactorRange) {
            influencingFactorRange.add(InfluencingFactorRange.BMI_FROM.description);
            influencingFactorRange.add(InfluencingFactorRange.BMI_TO.description);
            return influencingFactorRange;
        }

        @Override
        public Map<Object, Map<String, Object>> groupUnderWriterRoutingLevelItem(Map<String, Object> underWriterLineItem, Map<Object, Map<String, Object>> underWriterInfluencingFactorMap) {
            List<String> influencingFactorRange = Lists.newArrayList();
            influencingFactorRange = this.getInfluencingFactorRange(influencingFactorRange);
            Map<String,Object> underWriterLineItemMap = Maps.newLinkedHashMap();
            for (Map.Entry<String, Object> underWriterRoutingEntryMap : underWriterLineItem.entrySet()){
                if (influencingFactorRange.contains(underWriterRoutingEntryMap.getKey())){
                    underWriterLineItemMap.put(underWriterRoutingEntryMap.getKey(),underWriterRoutingEntryMap.getValue());
                }
            }
            underWriterInfluencingFactorMap.put(BMI,underWriterLineItemMap);
            return underWriterInfluencingFactorMap;
        }
        @Override
        public boolean isValueAvailableForTheProduct(Row currentRow, String planCode, String coverageId, Map<String, Integer> indexMap, StringBuilder errorMessageBuilder, IPlanAdapter iPlanAdapter, String fromValue, String toValue) {
            return true;
        }

        @Override
        public boolean isValueIsInRange(String value, String fromValue, String toValue) {
            return (Double.valueOf(value).compareTo(Double.valueOf(fromValue))>=0 && Double.valueOf(value).compareTo(Double.valueOf(toValue)) <=0);
        }

    },
    HEIGHT("Height"){
        @Override
        public Map<String, Object> transformUnderWriterDocumentLineItem(String fromValue, String toValue) {
            Map<String,Object> influencingFactorValueMap = Maps.newLinkedHashMap();
            influencingFactorValueMap.put(InfluencingFactorRange.HEIGHT_FROM.description,fromValue);
            influencingFactorValueMap.put(InfluencingFactorRange.HEIGHT_TO.description,toValue);
            return influencingFactorValueMap;
        }

        @Override
        public String getRoutingLevelInfluencingFactorFromValue(Map<String, Object> influencingFactorValueMap) {
            return influencingFactorValueMap.get(InfluencingFactorRange.HEIGHT_FROM.description)!=null?
                    influencingFactorValueMap.get(InfluencingFactorRange.HEIGHT_FROM.description).toString():"0";
        }

        @Override
        public String getInfluencingFactorToValue(Map<String, Object> influencingFactorValueMap) {
            return influencingFactorValueMap.get(InfluencingFactorRange.HEIGHT_TO.description)!=null?
                    influencingFactorValueMap.get(InfluencingFactorRange.HEIGHT_TO.description).toString():"0";
        }

        @Override
        public List<String> getInfluencingFactorRange(List<String> influencingFactorRange) {
            influencingFactorRange.add(InfluencingFactorRange.HEIGHT_FROM.description);
            influencingFactorRange.add(InfluencingFactorRange.HEIGHT_TO.description);
            return influencingFactorRange;
        }

        @Override
        public Map<Object, Map<String, Object>> groupUnderWriterRoutingLevelItem(Map<String, Object> underWriterLineItem, Map<Object, Map<String, Object>> underWriterInfluencingFactorMap) {
            List<String> influencingFactorRange = Lists.newArrayList();
            influencingFactorRange = this.getInfluencingFactorRange(influencingFactorRange);
            Map<String,Object> underWriterLineItemMap = Maps.newLinkedHashMap();
            for (Map.Entry<String, Object> underWriterRoutingEntryMap : underWriterLineItem.entrySet()){
                if (influencingFactorRange.contains(underWriterRoutingEntryMap.getKey())){
                    underWriterLineItemMap.put(underWriterRoutingEntryMap.getKey(),underWriterRoutingEntryMap.getValue());
                }
            }
            underWriterInfluencingFactorMap.put(HEIGHT,underWriterLineItemMap);
            return underWriterInfluencingFactorMap;
        }

        @Override
        public boolean isValueAvailableForTheProduct(Row currentRow, String planCode, String coverageId, Map<String, Integer> indexMap, StringBuilder errorMessageBuilder, IPlanAdapter iPlanAdapter, String fromValue, String toValue) {
            return true;
        }

        @Override
        public boolean isValueIsInRange(String value, String fromValue, String toValue) {
            return (Double.valueOf(value).compareTo(Double.valueOf(fromValue))>=0 && Double.valueOf(value).compareTo(Double.valueOf(toValue)) <= 0);
        }
    },

    WEIGHT("Weight"){
        @Override
        public Map<String, Object> transformUnderWriterDocumentLineItem(String fromValue, String toValue) {
            Map<String,Object> influencingFactorValueMap = Maps.newLinkedHashMap();
            influencingFactorValueMap.put(InfluencingFactorRange.WEIGHT_FROM.description,fromValue);
            influencingFactorValueMap.put(InfluencingFactorRange.WEIGHT_TO.description,toValue);
            return influencingFactorValueMap;
        }

        @Override
        public String getRoutingLevelInfluencingFactorFromValue(Map<String, Object> influencingFactorValueMap) {
            return influencingFactorValueMap.get(InfluencingFactorRange.WEIGHT_FROM.description)!=null?
                    influencingFactorValueMap.get(InfluencingFactorRange.WEIGHT_FROM.description).toString():"0";
        }

        @Override
        public String getInfluencingFactorToValue(Map<String, Object> influencingFactorValueMap) {
            return influencingFactorValueMap.get(InfluencingFactorRange.WEIGHT_TO.description)!=null?
                    influencingFactorValueMap.get(InfluencingFactorRange.WEIGHT_TO.description).toString():"0";
        }

        @Override
        public List<String> getInfluencingFactorRange(List<String> influencingFactorRange) {
            influencingFactorRange.add(InfluencingFactorRange.WEIGHT_FROM.description);
            influencingFactorRange.add(InfluencingFactorRange.WEIGHT_TO.description);
            return influencingFactorRange;
        }

        @Override
        public Map<Object, Map<String, Object>> groupUnderWriterRoutingLevelItem(Map<String, Object> underWriterLineItem, Map<Object, Map<String, Object>> underWriterInfluencingFactorMap) {
            List<String> influencingFactorRange = Lists.newArrayList();
            influencingFactorRange = this.getInfluencingFactorRange(influencingFactorRange);
            Map<String,Object> underWriterLineItemMap = Maps.newLinkedHashMap();
            for (Map.Entry<String, Object> underWriterRoutingEntryMap : underWriterLineItem.entrySet()){
                if (influencingFactorRange.contains(underWriterRoutingEntryMap.getKey())){
                    underWriterLineItemMap.put(underWriterRoutingEntryMap.getKey(),underWriterRoutingEntryMap.getValue());
                }
            }
            underWriterInfluencingFactorMap.put(WEIGHT,underWriterLineItemMap);
            return underWriterInfluencingFactorMap;
        }

        @Override
        public boolean isValueAvailableForTheProduct(Row currentRow, String planCode, String coverageId, Map<String, Integer> indexMap, StringBuilder errorMessageBuilder, IPlanAdapter iPlanAdapter, String fromValue, String toValue) {
            return true;
        }

        @Override
        public boolean isValueIsInRange(String value, String fromValue, String toValue) {
            return (Double.valueOf(value).compareTo(Double.valueOf(fromValue))>=0 && Double.valueOf(value).compareTo(Double.valueOf(toValue))  <= 0);
        }

    },

    CLAIM_AMOUNT("Claim Amount"){
        @Override
        public Map<String, Object> transformUnderWriterDocumentLineItem(String fromValue, String toValue) {
            Map<String,Object> influencingFactorValueMap = Maps.newLinkedHashMap();
            influencingFactorValueMap.put(InfluencingFactorRange.CLAIM_AMOUNT_FROM.description,fromValue);
            influencingFactorValueMap.put(InfluencingFactorRange.CLAIM_AMOUNT_TO.description,toValue);
            return influencingFactorValueMap;
        }

        @Override
        public String getRoutingLevelInfluencingFactorFromValue(Map<String, Object> influencingFactorValueMap) {
            return influencingFactorValueMap.get(InfluencingFactorRange.CLAIM_AMOUNT_FROM.description)!=null?
                    influencingFactorValueMap.get(InfluencingFactorRange.CLAIM_AMOUNT_FROM.description).toString():"0";
        }

        @Override
        public String getInfluencingFactorToValue(Map<String, Object> influencingFactorValueMap) {
            return influencingFactorValueMap.get(InfluencingFactorRange.CLAIM_AMOUNT_TO.description)!=null?
                    influencingFactorValueMap.get(InfluencingFactorRange.CLAIM_AMOUNT_TO.description).toString():"0";
        }

        @Override
        public List<String> getInfluencingFactorRange(List<String> influencingFactorRange) {
            influencingFactorRange.add(InfluencingFactorRange.CLAIM_AMOUNT_FROM.description);
            influencingFactorRange.add(InfluencingFactorRange.CLAIM_AMOUNT_TO.description);
            return influencingFactorRange;
        }

        @Override
        public Map<Object, Map<String, Object>> groupUnderWriterRoutingLevelItem(Map<String, Object> underWriterLineItem, Map<Object,Map<String,Object>> underWriterInfluencingFactorMap) {
            List<String> influencingFactorRange = Lists.newArrayList();
            influencingFactorRange = this.getInfluencingFactorRange(influencingFactorRange);
            Map<String,Object> underWriterLineItemMap = Maps.newLinkedHashMap();
            for (Map.Entry<String, Object> underWriterRoutingEntryMap : underWriterLineItem.entrySet()){
                if (influencingFactorRange.contains(underWriterRoutingEntryMap.getKey())){
                    underWriterLineItemMap.put(underWriterRoutingEntryMap.getKey(),underWriterRoutingEntryMap.getValue());
                }
            }
            underWriterInfluencingFactorMap.put(CLAIM_AMOUNT,underWriterLineItemMap);
            return underWriterInfluencingFactorMap;
        }

        @Override
        public boolean isValueAvailableForTheProduct(Row currentRow, String planCode, String coverageId, Map<String, Integer> indexMap, StringBuilder errorMessageBuilder, IPlanAdapter iPlanAdapter, String fromValue, String toValue) {
            return true;
        }

        @Override
        public boolean isValueIsInRange(String value, String fromValue, String toValue) {
            BigDecimal valueToBeChecked = new BigDecimal(value);
            BigDecimal claimAmountFrom = new BigDecimal(fromValue);
            BigDecimal claimAmountTo = new BigDecimal(toValue);
            return (valueToBeChecked.compareTo(claimAmountFrom) >= 0 && valueToBeChecked.compareTo(claimAmountTo)  <= 0);
        }

    };

    private String description;

    UnderWriterInfluencingFactor(String description) {
        this.description = description;
    }
    public abstract Map<String,Object> transformUnderWriterDocumentLineItem(String fromValue, String toValue);
    public abstract String getRoutingLevelInfluencingFactorFromValue(Map<String, Object> influencingFactorValueMap);
    public abstract String getInfluencingFactorToValue(Map<String,Object> influencingFactorValueMap);
    public abstract List<String> getInfluencingFactorRange(List<String> influencingFactorRange);
    public abstract Map<Object,Map<String,Object>> groupUnderWriterRoutingLevelItem(Map<String,Object> underWriterLineItem, Map<Object,Map<String,Object>> underWriterInfluencingFactorMap);
    public abstract boolean isValueAvailableForTheProduct(Row currentRow, String planCode, String coverageId, Map<String, Integer> indexMap, StringBuilder errorMessageBuilder, IPlanAdapter iPlanAdapter, String fromValue, String toValue);
    public abstract boolean isValueIsInRange(String value,String fromValue,String toValue);
}

enum InfluencingFactorRange{
    SUM_ASSURED_FROM("Sum Assured From"),SUM_ASSURED_TO("Sum Assured To"),
    AGE_FROM("Age From"),AGE_TO("Age To"),
    BMI_FROM("BMI From"),BMI_TO("BMI To"),
    WEIGHT_FROM("Weight From"),WEIGHT_TO("Weight To"),
    HEIGHT_FROM("Height From"),HEIGHT_TO("Height To"),
    CLAIM_AMOUNT_FROM("Claim Amount From"),CLAIM_AMOUNT_TO("Claim Amount To");

    String description;

    InfluencingFactorRange(String description) {
        this.description = description;
    }
}