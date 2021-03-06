package com.pla.core.query;

import org.nthdimenzion.ddd.domain.annotations.Finder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

/**
 * Created by User on 3/30/2015.
 */
@Finder
@Service
public class BranchFinder {

    public static final String FIND_ALL_BRANCH_QUERY = "SELECT br.branch_code AS branchCode,rg.region_code AS regionCode, rg.region_name AS regionName, br.branch_name AS branchName,bmf.employee_id AS branchManager,bmf.first_name AS branchManagerFirstName, bmf.last_name AS branchManagerLastName, bmf.from_date AS branchManagerFromDate,bmf.thru_date AS branchManagerToDate, " +
            "bbf.employee_id AS branchBDE,bbf.first_name AS branchBDEFirstName, bbf.last_name AS branchBDELastName, bbf.from_date AS branchBDEFromDate, bbf.thru_date AS branchBDEToDate " +
            "FROM branch br " +
            "JOIN region_branch rb ON rb.branch_code = br.branch_code " +
            "JOIN region rg ON rg.region_code = rb.region_code " +
            "LEFT JOIN branch_manager_fulfillment bmf ON " +
            /*"bmf.employee_id = br.current_branch_manager AND " +*/
            "bmf.branch_code = br.branch_code AND ((bmf.thru_date IS NULL ) OR (bmf.thru_date >= CURDATE())) " +
            "LEFT JOIN branch_bde_fulfillment bbf ON " +
            /*"bbf.employee_id = br.current_branch_bde AND" +*/
            " bbf.branch_code = br.branch_code AND ((bbf.thru_date IS NULL) OR (bbf.thru_date >= CURDATE()))  ";
    public static final String FIND_BRANCH_BY_ID_QUERY = "SELECT br.branch_name AS branchName,br.branch_code AS branchCode,br.current_branch_manager AS currentBranchManager, br.current_branch_bde AS currentBranchBDE, rg.region_code AS regionCode, rg.region_name AS regionName, bmf.first_name AS currentBranchManagerFirstName, bmf.last_name AS currentBranchManagerLastName, bmf.from_date AS currentBranchManagerFromDate, bbf.first_name AS currentBranchBDFirstName, bbf.last_name AS currentBranchBDELastName, bbf.from_date AS currentBranchBDEFromDate," +
            " bmf.thru_date AS currentBranchManagerToDate,bbf.thru_date AS currentBranchBDEToDate  " +
            " FROM branch br  " +
            " JOIN region_branch rb ON rb.branch_code = br.branch_code " +
            " JOIN region rg ON rg.region_code = rb.region_code " +
            " LEFT JOIN branch_manager_fulfillment bmf ON bmf.employee_id = br.current_branch_manager AND bmf.branch_code = br.branch_code AND bmf.thru_date IS NULL " +
            " LEFT JOIN branch_bde_fulfillment bbf ON bbf.employee_id = br.current_branch_bde AND bbf.branch_code = br.branch_code AND bbf.thru_date IS NULL " +
            " WHERE br.branch_code=:branchId";
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public List<Map<String, Object>> getAllBranch() {
        return namedParameterJdbcTemplate.query(FIND_ALL_BRANCH_QUERY, new ColumnMapRowMapper());
    }


    public Map<String, Object> getBranchById(String branchId) {
        return namedParameterJdbcTemplate.queryForMap(FIND_BRANCH_BY_ID_QUERY, new MapSqlParameterSource().addValue("branchId", branchId));
    }
}
