package io.yanhai.example.util;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;

/**
 * @author yanhai
 */
public interface JdbcClientUtils {

  static <K> Future<Optional<JsonObject>> querySingle(JDBCClient client, String sql, K key) {
    return query(client, sql, new JsonArray().add(key))
        .map(rows -> rows == null || rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0)));
  }

  static Future<List<JsonObject>> query(JDBCClient client, String sql, JsonArray params) {
    return getConnection(client)
        .compose(queryWithParams(sql, params))
        .map(ResultSet::getRows);
  }

  static Future<Void> updateNoResult(JDBCClient client, String sql, JsonArray params) {
    return update(client, sql, params)
        .compose(Helpers.fconst(Future.succeededFuture()));
  }

  static Future<Integer> update(JDBCClient client, String sql, JsonArray params) {
    return getConnection(client)
        .compose(updateWithParams(sql, params))
        .map(UpdateResult::getUpdated);
  }

  static Function<SQLConnection, Future<UpdateResult>> updateWithParams(String sql, JsonArray params) {
    return conn -> {
      Future<UpdateResult> fut = Future.future();
      conn.updateWithParams(sql, params, ar -> {
        if (ar.succeeded()) {
          fut.complete(ar.result());
        } else {
          fut.fail(ar.cause());
        }
        conn.close();
      });
      return fut;
    };
  }

  static Function<SQLConnection, Future<ResultSet>> queryWithParams(String sql, JsonArray params) {
    return conn -> {
      Future<ResultSet> fut = Future.future();
      conn.queryWithParams(sql, params, ar -> {
        if (ar.succeeded()) {
          fut.complete(ar.result());
        } else {
          fut.fail(ar.cause());
        }
        conn.close();
      });
      return fut;
    };
  }

  static Future<SQLConnection> getConnection(JDBCClient client) {
    Future<SQLConnection> future = Future.future();
    client.getConnection(future.completer());
    return future;
  }
}
