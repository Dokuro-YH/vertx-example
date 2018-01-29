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
import io.yanhai.example.web.api.APIRouter;

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

    router.mountSubRouter("/api", APIRouter.create(vertx, authProvider, jdbcClient));

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
}
