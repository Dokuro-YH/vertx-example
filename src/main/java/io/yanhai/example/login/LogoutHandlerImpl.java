package io.yanhai.example.login;

import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;

/**
 * @author yanhai
 */
public class LogoutHandlerImpl implements LogoutHandler {

  private String sessionCookieName;

  @Override
  public LogoutHandler setSessionCookieName(String sessionCookieName) {
    this.sessionCookieName = sessionCookieName;
    return this;
  }

  public LogoutHandlerImpl(String sessionCookieName) {
    this.sessionCookieName = sessionCookieName;
  }

  @Override
  public void handle(RoutingContext context) {
    User user = context.user();
    Session session = context.session();
    if (user != null) {
      user.clearCache();
    }

    if (session != null) {
      session.destroy();
    }

    context.removeCookie(sessionCookieName);
    context.response().end();
  }
}
