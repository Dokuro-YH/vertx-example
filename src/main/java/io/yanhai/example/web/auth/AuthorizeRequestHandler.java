package io.yanhai.example.web.auth;

import java.util.function.Function;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;

/**
 * @author yanhai
 */
@VertxGen
public interface AuthorizeRequestHandler extends Handler<RoutingContext> {

  static AuthorizeRequestHandler create(final Function<User, Boolean> checkAuth) {
    return context -> {
      @Nullable User user = context.user();
      if (user == null) {
        context.fail(401);
      } else {
        boolean isAuthorized = checkAuth.apply(user);
        if (isAuthorized) {
          context.next();
        } else {
          context.fail(403);
        }
      }
    };
  }

  static AuthorizeRequestHandler create(final String authority) {
    return context -> {
      @Nullable User user = context.user();
      if (user == null) {
        context.fail(401);
      } else {
        user.isAuthorized(authority, ar -> {
          if (ar.succeeded() && ar.result()) {
            context.next();
          } else {
            context.fail(403);
          }
        });
      }
    };
  }

}
