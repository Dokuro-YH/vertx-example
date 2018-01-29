package io.yanhai.example.user;

import io.vertx.core.json.JsonArray;
import io.vertx.ext.auth.jdbc.JDBCAuth;
import io.vertx.ext.jdbc.JDBCClient;
import io.yanhai.example.common.JdbcResourceManagerImpl;

/**
 * @author yanhai
 */
public class JdbcUserManagerImpl extends JdbcResourceManagerImpl<User> implements JdbcUserManager {

  private final JDBCAuth authProvider;

  public JdbcUserManagerImpl(JDBCAuth authProvider, JDBCClient jdbcClient,
      String findOneSQL, String findAllSQL, String createSQL, String updateSQL, String deleteSQL) {
    super(jdbcClient, findOneSQL, findAllSQL, createSQL, updateSQL, deleteSQL);
    this.authProvider = authProvider;
  }

  @Override
  protected JsonArray convertToCreateParams(User user) {
    String username = user.getUsername();
    String password = user.getPassword();
    String salt = authProvider.generateSalt();
    String hash = authProvider.computeHash(password, salt);

    return new JsonArray().add(username).add(hash).add(salt);
  }

  @Override
  protected JsonArray convertToUpdateParams(String username, User user) {
    String password = user.getPassword();
    String salt = authProvider.generateSalt();
    String hash = authProvider.computeHash(password, salt);

    return new JsonArray().add(hash).add(salt).add(username);
  }

  @Override
  protected String generateId(User user) {
    return user.getUsername();
  }

  @Override
  protected User mapRow(JsonArray json) {
    return new User(json.getString(0), null);
  }

}
