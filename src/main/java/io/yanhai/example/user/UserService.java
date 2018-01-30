package io.yanhai.example.user;

import java.util.List;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jdbc.JDBCAuth;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.Router;
import io.yanhai.example.util.RoutingUtils;

/**
 * @author yanhai
 */
@VertxGen
public interface UserService {

  static UserService create(JDBCAuth jdbcAuth, JDBCClient client) {
    return new UserServiceImpl(jdbcAuth, client);
  }

  static Router router(Vertx vertx, UserService userService) {
    Router router = Router.router(vertx);

    router.get("/:username").handler(ctx -> {
      @Nullable String username = ctx.pathParam("username");
      userService.getUser(username, RoutingUtils.withSuccess(ctx, RoutingUtils.json()));
    });

    router.get().handler(ctx -> {
      userService.queryUser(RoutingUtils.withSuccess(ctx, RoutingUtils.json()));
    });

    router.post().handler(ctx -> {
      @Nullable JsonObject body = ctx.getBodyAsJson();
      if (body == null) {
        ctx.fail(400);
      } else {
        userService.addUser(new User(body), RoutingUtils.withSuccess(ctx, RoutingUtils.created()));
      }
    });

    router.put("/:username").handler(ctx -> {
      @Nullable String username = ctx.pathParam("username");
      @Nullable JsonObject body = ctx.getBodyAsJson();
      if (username == null || body == null) {
        ctx.fail(400);
      } else {
        userService.updateUser(username, new User(body), RoutingUtils.withSuccess(ctx, RoutingUtils.noContent()));
      }
    });

    router.delete("/:username").handler(ctx -> {
      @Nullable String username = ctx.pathParam("username");
      if (username == null) {
        ctx.fail(400);
      } else {
        userService.removeUser(username, RoutingUtils.withSuccess(ctx, RoutingUtils.noContent()));
      }
    });

    return router;
  }

  @Fluent
  UserService getUser(String username, Handler<AsyncResult<User>> handler);

  @Fluent
  UserService queryUser(Handler<AsyncResult<List<User>>> handler);

  @Fluent
  UserService addUser(User user, Handler<AsyncResult<Void>> handler);

  @Fluent
  UserService updateUser(String username, User user, Handler<AsyncResult<Void>> handler);

  @Fluent
  UserService removeUser(String username, Handler<AsyncResult<Void>> handler);

}
