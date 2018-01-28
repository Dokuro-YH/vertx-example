package io.yanhai.example.user.jdbc;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jdbc.JDBCAuth;
import io.vertx.ext.jdbc.JDBCClient;
import io.yanhai.example.common.JdbcResourceManagerImpl;

/**
 * @author yanhai
 */
public class JdbcUserManagerImpl extends JdbcResourceManagerImpl implements JdbcUserManager {

  private final JDBCAuth authProvider;

  public JdbcUserManagerImpl(JDBCAuth authProvider, JDBCClient jdbcClient,
      String findOneSQL, String findAllSQL, String createSQL, String updateSQL, String deleteSQL) {
    super(jdbcClient, findOneSQL, findAllSQL, createSQL, updateSQL, deleteSQL);
    this.authProvider = authProvider;
  }

  @Override
  protected JsonArray convertToCreateParams(JsonObject json) {
    String username = json.getString("username");
    String password = json.getString("password");
    String salt = authProvider.generateSalt();
    String hash = authProvider.computeHash(password, salt);

    return new JsonArray().add(username).add(hash).add(salt);
  }

  @Override
  protected JsonArray convertToUpdateParams(String username, JsonObject json) {
    String password = json.getString("password");
    String salt = authProvider.generateSalt();
    String hash = authProvider.computeHash(password, salt);

    return new JsonArray().add(hash).add(salt).add(username);
  }

  @Override
  protected String generateId(JsonObject json) {
    return json.getString("username");
  }

  @Override
  protected JsonObject mapRow(JsonArray json) {
    return new JsonObject()
        .put("username", json.getString(0));
  }

}
