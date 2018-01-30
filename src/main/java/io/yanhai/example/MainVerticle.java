package io.yanhai.example;

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
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.UserSessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.yanhai.example.login.JsonLoginHandler;
import io.yanhai.example.login.LogoutHandler;
import io.yanhai.example.user.UserService;
import io.yanhai.example.util.FlywayDatabase;

public class MainVerticle extends AbstractVerticle {

  static final Logger log = LoggerFactory.getLogger(MainVerticle.class);

  public static final int DEFAULT_PORT = 8080;

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

    authProvider.setAuthenticationQuery("select password, password_salt from \"user\" where username = ?");

    FlywayDatabase.migrate(config.getString("url"), config.getString("user"), config.getString("password"))
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

  private Future<Boolean> existsAdminUser(Void v) {
    final Future<Boolean> fut = Future.future();
    final JsonArray queryParams = new JsonArray().add("admin");

    jdbcClient.getConnection(ar -> {
      if (ar.succeeded()) {
        SQLConnection conn = ar.result();

        conn.querySingleWithParams("select count(0) from \"user\" where username=?", queryParams, res -> {
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

        conn.updateWithParams("insert into \"user\" values (?, ?, ?)", insertParams, res -> {
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
    router.mountSubRouter("/api", apiRouter());

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
        .listen(config().getInteger("http.port", DEFAULT_PORT), fut.completer());

    return fut;
  }

  private Router apiRouter() {
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

    // 用户管理
    UserService userService = UserService.create(authProvider, jdbcClient);
    router.mountSubRouter("/users", UserService.router(vertx, userService));

    return router;
  }
}
