package io.yanhai.example;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.jdbc.JDBCAuth;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BasicAuthHandler;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.UserSessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.yanhai.example.common.RestResourceManagerRouter;
import io.yanhai.example.user.jdbc.JdbcUserManager;
import io.yanhai.example.web.login.JsonLoginHandler;
import io.yanhai.example.web.login.LogoutHandler;

public class MainVerticle extends AbstractVerticle {

  static final Logger log = LoggerFactory.getLogger(MainVerticle.class);

  JDBCClient jdbcClient;

  JDBCAuth authProvider;

  @Override
  public void start(Future<Void> startFuture) {
    JsonObject config = new JsonObject()
        .put("url", "jdbc:postgresql:account")
        .put("driver_class", "org.postgresql.Driver")
        .put("user", "root")
        .put("password", "root");

    jdbcClient = JDBCClient.createShared(vertx, config);
    authProvider = JDBCAuth.create(vertx, jdbcClient);

    authProvider.setAuthenticationQuery("SELECT PASSWORD, PASSWORD_SALT FROM \"USER\" WHERE USERNAME = ?");

    initDatabase(config.getString("url"), config.getString("user"), config.getString("password"))
        .compose(this::existsAdminUser)
        .compose(this::initAdminUser)
        .compose(this::startHttpServer)
        .setHandler(ar -> {
          if (ar.succeeded()) {
            log.info("start successed, http://localhost:" + ar.result().actualPort());
            startFuture.complete();
          } else {
            startFuture.fail(ar.cause());
          }
        });
  }

  private Future<Void> initDatabase(String url, String user, String password) {
    Future<Void> fut = Future.future();

    try {
      log.info("initializing database...");
      Flyway flyway = new Flyway();
      flyway.setDataSource(url, user, password);
      flyway.migrate();
      fut.complete();
    } catch (FlywayException e) {
      fut.fail(e);
    }

    return fut;
  }

  private Future<Boolean> existsAdminUser(Void v) {
    final Future<Boolean> fut = Future.future();
    final JsonArray queryParams = new JsonArray().add("admin");

    jdbcClient.getConnection(ar -> {
      if (ar.succeeded()) {
        SQLConnection conn = ar.result();

        conn.querySingleWithParams("SELECT COUNT(0) FROM \"USER\" WHERE USERNAME=?", queryParams, res -> {
          if (res.succeeded()) {
            fut.complete(res.result().getInteger(0) > 0);
          } else {
            fut.fail(res.cause());
          }

          conn.close();
        });
      } else {
        fut.fail(ar.cause());
      }
    });

    return fut;
  }

  private Future<Void> initAdminUser(Boolean exists) {
    if (exists) {
      log.info("admin user is already exists!!!");
      return Future.succeededFuture();
    }

    log.info("initializing admin user...");
    final Future<Void> fut = Future.future();

    final String salt = authProvider.generateSalt();
    final String hash = authProvider.computeHash("admin", salt);
    final JsonArray insertParams = new JsonArray().add("admin").add(hash).add(salt);

    jdbcClient.getConnection(ar -> {
      if (ar.succeeded()) {
        SQLConnection conn = ar.result();

        conn.updateWithParams("INSERT INTO \"USER\" VALUES (?, ?, ?)", insertParams, res -> {
          if (res.succeeded()) {
            fut.complete();
          } else {
            fut.fail(res.cause());
          }

          conn.close();
        });
      } else {
        fut.fail(ar.cause());
      }
    });

    return fut;
  }

  private Future<HttpServer> startHttpServer(Void v) {
    log.info("starting http server...");
    final Future<HttpServer> fut = Future.future();

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
    // TODO 自定义授权失败 handler 替换 BasicAuthHandler
    router.route().handler(BasicAuthHandler.create(authProvider));

    // user manager router
    JdbcUserManager userManager = JdbcUserManager.create(authProvider, jdbcClient);
    Router userManagerRouter = RestResourceManagerRouter.router(vertx, userManager,
        s -> s == null || s.isEmpty() ? null : new JsonObject(s));
    // TODO 模块化控制路由权限，删除全局授权失败 handler
    router.mountSubRouter("/users", userManagerRouter);

    router.get().handler(req -> {
      final String msg;
      User user = req.user();
      if (user != null) {
        msg = user.principal().getString("username") + "!";
      } else {
        msg = "Vert.x!";
      }
      req.response().end("Hello " + msg);
    });

    vertx.createHttpServer()
        .requestHandler(router::accept)
        .listen(8080, fut.completer());

    return fut;
  }

}
