package com.lsi.oracle.Service;

import com.lsi.oracle.Controller.DTO.Request.UserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
public class UserService {
  private JdbcTemplate jdbcTemplate;

  public UserService(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public String createUser(UserRequest userRequest) throws SQLException  {
    return "sql : TODO need to complete :: execute successfully";
  }
}
