package com.lsi.oracle.Service;

import com.lsi.oracle.Controller.DTO.Request.TablespaceRequest;
import com.lsi.oracle.Controller.DTO.Request.UserRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
public class TablespaceService {
    private JdbcTemplate jdbcTemplate;

    public TablespaceService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String createTablespace(TablespaceRequest tablespaceRequest) throws SQLException {
        String sql = "CREATE TABLESPACE " + tablespaceRequest.name() + " DATAFILE '"+tablespaceRequest.datafile_path()+"'" + " SIZE "+tablespaceRequest.size()+"M ";
        jdbcTemplate.execute(sql);
        return "sql : "+sql+" :: execute successfully";
    }

    public String deleteTablespace(TablespaceRequest tablespaceRequest)throws SQLException {
        String sql = "DROP TABLESPACE " +  tablespaceRequest.name().toUpperCase() +" INCLUDING CONTENTS AND DATAFILES" ;
        jdbcTemplate.execute(sql);
        return "sql : "+sql+" :: execute successfully";
    }
}
