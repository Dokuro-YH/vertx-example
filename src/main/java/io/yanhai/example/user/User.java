package io.yanhai.example.user;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.cli.annotations.Description;
import io.vertx.core.json.JsonObject;

/**
 * @author yanhai
 */
@DataObject
public class User {

  private String username;
  private String password;

  public User() {
  }

  public User(String username, String password) {
    this.username = username;
    this.password = password;
  }

  public User(String s) {
    this(new JsonObject(s));
  }

  public User(JsonObject json) {
    this.username = json.getString("username");
    this.password = json.getString("password");
  }

  @Description("用户名")
  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

}
