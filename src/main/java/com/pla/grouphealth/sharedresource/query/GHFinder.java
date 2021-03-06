package com.pla.grouphealth.sharedresource.query;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.BasicDBObject;
import com.pla.sharedkernel.identifier.QuotationId;
import org.nthdimenzion.ddd.domain.annotations.Finder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.nthdimenzion.utils.UtilValidator.isEmpty;
import static org.nthdimenzion.utils.UtilValidator.isNotEmpty;

/**
 * Created by Samir on 6/24/2015.
 */

@Finder
@Service
public class GHFinder {

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private MongoTemplate mongoTemplate;

    public static final String FIND_OCCUPATION_CLASS_QUERY = "SELECT code,description FROM occupation_class WHERE description=:occupation";

    public static final String FIND_ACTIVE_AGENT_BY_FIRST_NAME_QUERY = "SELECT * FROM agent_team_branch_view WHERE firstName =:firstName";

    public static final String FIND_ACTIVE_AGENT_BY_ID_QUERY = "select * from agent_team_branch_view where agentId =:agentId AND agentStatus='ACTIVE'";

    public static final String FIND_AGENT_PLANS_QUERY = "SELECT agent_id as agentId,plan_id as planId FROM `agent_authorized_plan` WHERE agent_id=:agentId";

    public static final String FIND_COVERAGE_BY_ID_QUERY = "SELECT coverage_code AS coverageCode,coverage_name AS coverageName FROM coverage WHERE coverage_id=:coverageId";

    public static final String FIND_COVERAGE_BY_CODE_QUERY = "SELECT coverage_id AS coverageId, coverage_code AS coverageCode,coverage_name AS coverageName FROM coverage WHERE coverage_code=:coverageCode";

    public static final String FIND_GEO_BY_ID_QUERY = "SELECT geo_id AS geoId,parent_geo_id AS parentGeoId,geo_type AS geoType,geo_description AS geoName FROM geo WHERE geo_id=:geoId";

    @Autowired
    public void setDataSource(DataSource dataSource, MongoTemplate mongoTemplate) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.mongoTemplate = mongoTemplate;
    }

    public String getOccupationClass(String occupation) {
        List<Map<String, Object>> occupationClassList = namedParameterJdbcTemplate.query(FIND_OCCUPATION_CLASS_QUERY, new MapSqlParameterSource().addValue("occupation", occupation), new ColumnMapRowMapper());
        if (isNotEmpty(occupationClassList)) {
            Map<String, Object> occupationClassMap = occupationClassList.get(0);
            return (String) occupationClassMap.get("code");
        }
        return "";
    }

    public Map searchQuotationById(QuotationId quotationId) {
        Criteria criteria = Criteria.where("_id").is(quotationId);
        Query query = new Query(criteria);
        Map quotationMap = mongoTemplate.findOne(query, Map.class, "group_health_quotation");
        return quotationMap;
    }

    public List<Map> searchQuotation(String quotationNumber, String agentCode, String proposerName, String agentName, String quotationId, String[] statuses) {
        Criteria criteria = Criteria.where("quotationStatus").in(statuses);
        boolean areFieldsOtherThanAgentNameNotEmpty = false;
        if (isEmpty(quotationNumber) && isEmpty(quotationId) && isEmpty(agentCode) && isEmpty(proposerName) && isEmpty(agentName)) {
            return Lists.newArrayList();
        }
        if (isNotEmpty(quotationId)) {
            areFieldsOtherThanAgentNameNotEmpty = true;
            criteria = criteria.and("_id").is(new QuotationId(quotationId));
        }
        if (isNotEmpty(quotationNumber)) {
            areFieldsOtherThanAgentNameNotEmpty = true;
            criteria = criteria.and("quotationNumber").is(quotationNumber);
        }
        if (isNotEmpty(agentCode)) {
            areFieldsOtherThanAgentNameNotEmpty = true;
            criteria = criteria != null ? criteria.and("agentId.agentId").is(agentCode) : Criteria.where("agentId.agentId").is(agentCode);
        }
        if (isNotEmpty(proposerName)) {
            areFieldsOtherThanAgentNameNotEmpty = true;
            String proposerPattern = "^" + proposerName;
            criteria = criteria != null ? criteria.and("proposer.proposerName").regex(Pattern.compile(proposerPattern, Pattern.CASE_INSENSITIVE)) : Criteria.where("proposer.proposerName").regex(Pattern.compile(proposerPattern, Pattern.CASE_INSENSITIVE));
        }
        Set<String> agentIds = null;
        if (isNotEmpty(agentName)) {
            List<Map<String, Object>> agentList = namedParameterJdbcTemplate.queryForList(FIND_ACTIVE_AGENT_BY_FIRST_NAME_QUERY, new MapSqlParameterSource().addValue("firstName", agentName));
            agentIds = agentList.stream().map(new Function<Map<String, Object>, String>() {
                @Override
                public String apply(Map<String, Object> stringObjectMap) {
                    return (String) stringObjectMap.get("agentId");
                }
            }).collect(Collectors.toSet());
        }
        if (isNotEmpty(agentIds)) {
            criteria = criteria.and("agentId.agentId").in(agentIds);
        }
        if (isEmpty(agentIds) && !areFieldsOtherThanAgentNameNotEmpty) {
            return Lists.newArrayList();
        }
        Query query = new Query(criteria);
        query.with(new Sort(Sort.Direction.ASC, "quotationNumber"));
        query.with(new Sort(Sort.Direction.DESC, "versionNumber"));
        return mongoTemplate.find(query, Map.class, "group_health_quotation");
    }

    public List<Map<String, Object>> getAgentAuthorizedPlan(String agentId) {
        return namedParameterJdbcTemplate.query(FIND_AGENT_PLANS_QUERY, new MapSqlParameterSource().addValue("agentId", agentId), new ColumnMapRowMapper());
    }

    public Map<String, Object> getCoverageDetail(String coverageId) {
        return namedParameterJdbcTemplate.queryForMap(FIND_COVERAGE_BY_ID_QUERY, new MapSqlParameterSource().addValue("coverageId", coverageId));
    }

    public Map<String, Object> findCoverageDetailByCoverageCode(String coverageCode) {
        return namedParameterJdbcTemplate.queryForMap(FIND_COVERAGE_BY_CODE_QUERY, new MapSqlParameterSource().addValue("coverageCode", coverageCode));
    }

    public Map<String, Object> findGeoDetail(String geoId) {
        return namedParameterJdbcTemplate.queryForMap(FIND_GEO_BY_ID_QUERY, new MapSqlParameterSource().addValue("geoId", geoId));
    }

    public Map<String, Object> getAgentById(String agentId) {
        Preconditions.checkArgument(isNotEmpty(agentId));
        List<Map<String, Object>> agentList = namedParameterJdbcTemplate.queryForList(FIND_ACTIVE_AGENT_BY_ID_QUERY, new MapSqlParameterSource().addValue("agentId", agentId));
        return isNotEmpty(agentList) ? agentList.get(0) : Maps.newHashMap();
    }

    public Map findProposalById(String proposalId) {
        BasicDBObject query = new BasicDBObject();
        query.put("_id", proposalId);
        Map proposal = mongoTemplate.findOne(new BasicQuery(query), Map.class, "group_health_proposal");
        return proposal;
    }
}
