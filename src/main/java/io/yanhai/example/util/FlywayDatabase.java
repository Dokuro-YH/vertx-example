package io.yanhai.example.util;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;

import io.vertx.core.Future;

/**
 * @author yanhai
 */
public interface FlywayDatabase {

  static Future<Void> migrate(String url, String user, String password) {
    Future<Void> fut = Future.future();

    try {
      Flyway flyway = new Flyway();
      flyway.setDataSource(url, user, password);
      flyway.migrate();
      fut.complete();
    } catch (FlywayException e) {
      fut.fail(e);
    }

    return fut;
  }

}
