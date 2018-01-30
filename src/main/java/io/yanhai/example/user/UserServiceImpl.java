package io.yanhai.example.user;

import java.util.List;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.auth.jdbc.JDBCAuth;
import io.vertx.ext.jdbc.JDBCClient;
import io.yanhai.example.util.Helpers;
import io.yanhai.example.util.JdbcClientUtils;

/**
 * @author yanhai
 */
public class UserServiceImpl implements UserService {

  private final JDBCAuth jdbcAuth;
  private final JDBCClient client;

  static final String QUERY_USER_SQL = "select username, password from \"user\" where username = ?";
  static final String QUERY_USERS_SQL = "select username, password from \"user\"";
  static final String ADD_USER_SQL = "insert into \"user\"(username, password, password_salt) values (?, ?, ?)";
  static final String UPDATE_USER_SQL = "update \"user\" set password = ?, password_salt = ? where username = ?";
  static final String REMOVE_USER_SQL = "delete from \"user\" where username = ?";

  public UserServiceImpl(JDBCAuth jdbcAuth, JDBCClient client) {
    this.jdbcAuth = jdbcAuth;
    this.client = client;
  }

  @Override
  public UserService getUser(String username, Handler<AsyncResult<User>> handler) {
    JdbcClientUtils
        .querySingle(client, QUERY_USER_SQL, username)
        .map(option -> option.map(User::new).orElse(null))
        .setHandler(handler);
    return this;
  }

  @Override
  public UserService queryUser(Handler<AsyncResult<List<User>>> handler) {
    JdbcClientUtils
        .query(client, QUERY_USERS_SQL, new JsonArray())
        .map(Helpers.fmap(User::new))
        .setHandler(handler);
    return this;
  }

  @Override
  public UserService addUser(User user, Handler<AsyncResult<Void>> handler) {
    String salt = jdbcAuth.generateSalt();
    JsonArray params = new JsonArray()
        .add(user.getUsername())
        .add(jdbcAuth.computeHash(user.getPassword(), salt))
        .add(salt);
    JdbcClientUtils
        .updateNoResult(client, ADD_USER_SQL, params)
        .setHandler(handler);
    return this;
  }

  @Override
  public UserService updateUser(String username, User user, Handler<AsyncResult<Void>> handler) {
    String salt = jdbcAuth.generateSalt();
    JsonArray params = new JsonArray()
        .add(jdbcAuth.computeHash(user.getPassword(), salt))
        .add(salt)
        .add(username);
    JdbcClientUtils
        .updateNoResult(client, UPDATE_USER_SQL, params)
        .setHandler(handler);
    return this;
  }

  @Override
  public UserService removeUser(String username, Handler<AsyncResult<Void>> handler) {
    JdbcClientUtils
        .updateNoResult(client, REMOVE_USER_SQL, new JsonArray().add(username))
        .setHandler(handler);
    return this;
  }

}
