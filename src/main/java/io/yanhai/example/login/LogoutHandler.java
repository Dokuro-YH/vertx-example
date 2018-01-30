package io.yanhai.example.login;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * @author yanhai
 */
@VertxGen
public interface LogoutHandler extends Handler<RoutingContext> {

  String DEFAULT_SESSION_COOKIE_NAME = "vertx-web.session";

  static LogoutHandler create() {
    return new LogoutHandlerImpl(DEFAULT_SESSION_COOKIE_NAME);
  }

  static LogoutHandler create(String sessionCookieName) {
    return new LogoutHandlerImpl(sessionCookieName);
  }

  @Fluent
  LogoutHandler setSessionCookieName(String sessionCookieName);
}
