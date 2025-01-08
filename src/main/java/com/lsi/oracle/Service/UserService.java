package com.lsi.oracle.Service;

import com.lsi.oracle.Controller.DTO.Request.UserRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Service
public class UserService {
  private final JdbcTemplate jdbcTemplate;

  public UserService(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public List<Map<String, Object>> listUsers() {
    String sql = "SELECT username FROM all_users"; // Adjust query if needed to suit your database schema
    return jdbcTemplate.queryForList(sql);
  }

  public String createUser(UserRequest userRequest) throws SQLException {
    String sql = String.format(
            "CREATE USER %s IDENTIFIED BY %s DEFAULT TABLESPACE %s TEMPORARY TABLESPACE %s",
            userRequest.username(),
            userRequest.password(),
            userRequest.defaultTablespace(),
            userRequest.tempTablespace()
    );
    jdbcTemplate.execute(sql);

    if (userRequest.role() != null && !userRequest.role().isEmpty()) {
      String grantRoleSql = String.format("GRANT %s TO %s", userRequest.role(), userRequest.username());
      jdbcTemplate.execute(grantRoleSql);
    }

    return "SQL: " + sql + " :: executed successfully";
  }



  public String modifyUser(UserRequest userRequest) throws SQLException {
    if (userRequest.password() != null && !userRequest.password().isEmpty()) {
      String changePasswordSql = String.format("ALTER USER %s IDENTIFIED BY %s", userRequest.username(), userRequest.password());
      jdbcTemplate.execute(changePasswordSql);
    }
    if (userRequest.defaultTablespace() != null && !userRequest.defaultTablespace().isEmpty()) {
      String changeDefaultTablespaceSql = String.format("ALTER USER %s DEFAULT TABLESPACE %s", userRequest.username(), userRequest.defaultTablespace());
      jdbcTemplate.execute(changeDefaultTablespaceSql);
    }
    if (userRequest.tempTablespace() != null && !userRequest.tempTablespace().isEmpty()) {
      String changeTempTablespaceSql = String.format("ALTER USER %s TEMPORARY TABLESPACE %s", userRequest.username(), userRequest.tempTablespace());
      jdbcTemplate.execute(changeTempTablespaceSql);
    }

    return "User " + userRequest.username() + " modified successfully.";
  }

  public String deleteUser (String username) throws SQLException {
    String sql = String.format("DROP USER %s CASCADE", username.toUpperCase());
    jdbcTemplate.execute(sql);
    return "SQL: " + sql + " :: executed successfully";
  }
}
