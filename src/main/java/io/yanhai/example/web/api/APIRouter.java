package io.yanhai.example.web.api;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jdbc.JDBCAuth;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.UserSessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.yanhai.example.common.RestResourceManagerRouter;
import io.yanhai.example.user.jdbc.JdbcUserManager;
import io.yanhai.example.web.auth.AuthorizeRequestHandler;
import io.yanhai.example.web.login.JsonLoginHandler;
import io.yanhai.example.web.login.LogoutHandler;

/**
 * @author yanhai
 */
public interface APIRouter {

  static Router create(Vertx vertx, JDBCAuth authProvider, JDBCClient jdbcClient) {
    Router router = Router.router(vertx);

    // router.route().handler(LoggerHandler.create());
    router.route().handler(BodyHandler.create());
    router.route().handler(CookieHandler.create());
    // TODO SessionHandler 如果返回状态码不是 2xx or 3xx，会自动删除cookie（应该是出于安全考虑）
    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
    router.route().handler(UserSessionHandler.create(authProvider));

    // 登录授权
    router.post("/login").handler(JsonLoginHandler.create(authProvider));
    router.post("/logout").handler(LogoutHandler.create());

    // 用户管理路由
    Router userManagerRouter = RestResourceManagerRouter.router(vertx,
        JdbcUserManager.create(authProvider, jdbcClient),
        s -> s == null || s.isEmpty() ? null : new JsonObject(s));
    router.route("/users").handler(AuthorizeRequestHandler.create("role:admin"));
    router.mountSubRouter("/users", userManagerRouter);

    return router;
  }

}
