package io.yanhai.example.login;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.web.RoutingContext;

/**
 * @author yanhai
 */
@VertxGen
public interface JsonLoginHandler extends Handler<RoutingContext> {

  String DEFAULT_USERNAME_PARAM = "username";

  String DEFAULT_PASSWORD_PARAM = "password";

  static JsonLoginHandler create(AuthProvider authProvider) {
    return new JsonLoginHandlerImpl(authProvider, DEFAULT_USERNAME_PARAM, DEFAULT_PASSWORD_PARAM);
  }

  @Fluent
  JsonLoginHandler setUsernameParam(String usernameParam);

  @Fluent
  JsonLoginHandler setPasswordParam(String passwordParam);

}
