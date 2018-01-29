package io.yanhai.example.user.jdbc;

import io.vertx.ext.auth.jdbc.JDBCAuth;
import io.vertx.ext.jdbc.JDBCClient;
import io.yanhai.example.common.JdbcResourceManager;

/**
 * @author yanhai
 */
public interface JdbcUserManager extends JdbcResourceManager {

  String DEFAULT_FIND_ONE_SQL = "select username, password from \"user\" where username = ?";
  String DEFAULT_FIND_ALL_SQL = "select username, password from \"user\"";
  String DEFAULT_CREATE_SQL = "insert into \"user\" (username, password, password_salt) values (?, ?, ?)";
  String DEFAULT_UPDATE_SQL = "update \"user\" set password=?, password_salt=? where username = ?";
  String DEFAULT_DELETE_SQL = "delete from \"user\" where username = ?";

  static JdbcUserManager create(JDBCAuth authProvider, JDBCClient jdbcClient) {
    return new JdbcUserManagerImpl(authProvider, jdbcClient, DEFAULT_FIND_ONE_SQL, DEFAULT_FIND_ALL_SQL,
        DEFAULT_CREATE_SQL, DEFAULT_UPDATE_SQL, DEFAULT_DELETE_SQL);
  }
}
