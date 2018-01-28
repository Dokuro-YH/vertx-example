package io.yanhai.example.user.jdbc;

import io.vertx.ext.auth.jdbc.JDBCAuth;
import io.vertx.ext.jdbc.JDBCClient;
import io.yanhai.example.common.JdbcResourceManager;

/**
 * @author yanhai
 */
public interface JdbcUserManager extends JdbcResourceManager {

  String DEFAULT_FIND_ONE_SQL = "SELECT USERNAME, PASSWORD FROM \"USER\" WHERE USERNAME = ?";
  String DEFAULT_FIND_ALL_SQL = "SELECT USERNAME, PASSWORD FROM \"USER\"";
  String DEFAULT_CREATE_SQL = "INSERT INTO \"USER\" (USERNAME, PASSWORD, PASSWORD_SALT) VALUES (?, ?, ?)";
  String DEFAULT_UPDATE_SQL = "UPDATE \"USER\" SET PASSWORD=?, PASSWORD_SALT=? WHERE USERNAME = ?";
  String DEFAULT_DELETE_SQL = "DELETE FROM \"USER\" WHERE USERNAME = ?";

  static JdbcUserManager create(JDBCAuth authProvider, JDBCClient jdbcClient) {
    return new JdbcUserManagerImpl(authProvider, jdbcClient, DEFAULT_FIND_ONE_SQL, DEFAULT_FIND_ALL_SQL,
        DEFAULT_CREATE_SQL, DEFAULT_UPDATE_SQL, DEFAULT_DELETE_SQL);
  }
}
