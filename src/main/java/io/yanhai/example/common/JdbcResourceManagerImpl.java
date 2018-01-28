package io.yanhai.example.common;

import java.util.List;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;

/**
 * @author yanhai
 */
public abstract class JdbcResourceManagerImpl implements JdbcResourceManager {

  protected final JDBCClient jdbcClient;

  protected String findOneSQL;
  protected String findAllSQL;
  protected String createSQL;
  protected String updateSQL;
  protected String deleteSQL;

  @Override
  public JdbcResourceManagerImpl setFindOneSQL(String findOneSQL) {
    this.findOneSQL = findOneSQL;
    return this;
  }

  @Override
  public JdbcResourceManagerImpl setFindAllSQL(String findAllSQL) {
    this.findAllSQL = findAllSQL;
    return this;
  }

  @Override
  public JdbcResourceManagerImpl setCreateSQL(String createSQL) {
    this.createSQL = createSQL;
    return this;
  }

  @Override
  public JdbcResourceManagerImpl setUpdateSQL(String updateSQL) {
    this.updateSQL = updateSQL;
    return this;
  }

  @Override
  public JdbcResourceManagerImpl setDeleteSQL(String deleteSQL) {
    this.deleteSQL = deleteSQL;
    return this;
  }

  public JdbcResourceManagerImpl(JDBCClient jdbcClient, String findOneSQL, String findAllSQL, String createSQL,
      String updateSQL, String deleteSQL) {
    this.jdbcClient = jdbcClient;
    this.findOneSQL = findOneSQL;
    this.findAllSQL = findAllSQL;
    this.createSQL = createSQL;
    this.updateSQL = updateSQL;
    this.deleteSQL = deleteSQL;
  }

  @Override
  public void findOne(String id, Handler<AsyncResult<JsonObject>> handler) {
    jdbcClient.getConnection(ar -> {
      if (ar.failed()) {
        handler.handle(Future.failedFuture(ar.cause()));
        return;
      }

      SQLConnection conn = ar.result();
      conn.queryWithParams(findOneSQL, new JsonArray().add(id), res -> {
        if (res.failed()) {
          handler.handle(Future.failedFuture(res.cause()));
        } else {
          ResultSet rs = res.result();
          if (rs == null) {
            handler.handle(Future.succeededFuture());
          } else {
            List<JsonArray> results = rs.getResults();

            if (results == null || results.isEmpty()) {
              handler.handle(Future.succeededFuture());
            } else {
              JsonArray result = results.get(0);
              handler.handle(Future.succeededFuture(mapRow(result)));
            }
          }
        }

        conn.close();
      });
    });
  }

  @Override
  public void findAll(Handler<AsyncResult<JsonArray>> handler) {
    jdbcClient.getConnection(ar -> {
      if (ar.failed()) {
        handler.handle(Future.failedFuture(ar.cause()));
        return;
      }

      SQLConnection conn = ar.result();
      conn.query(findAllSQL, res -> {
        if (res.failed()) {
          handler.handle(Future.failedFuture(res.cause()));
        } else {
          ResultSet rs = res.result();
          JsonArray result = new JsonArray();

          if (rs != null) {
            rs.getResults().stream()
                .map(this::mapRow)
                .forEach(row -> result.add(row));
          }

          handler.handle(Future.succeededFuture(result));
        }

        conn.close();
      });
    });
  }

  @Override
  public void create(JsonObject t) {
    this.create(t, null);
  }

  @Override
  public void create(JsonObject t, Handler<AsyncResult<JsonObject>> handler) {
    jdbcClient.getConnection(ar -> {
      if (ar.failed()) {
        handler.handle(Future.failedFuture(ar.cause()));
        return;
      }

      String id = generateId(t);
      JsonArray params = convertToCreateParams(t);

      SQLConnection conn = ar.result();
      conn.updateWithParams(createSQL, params, res -> {
        if (handler != null) {
          if (res.failed()) {
            handler.handle(Future.failedFuture(res.cause()));
          } else {
            UpdateResult result = res.result();
            if (result.getUpdated() == 1) {
              findOne(id, handler);
            } else {
              handler.handle(Future.succeededFuture());
            }
          }
        }
        conn.close();
      });
    });
  }

  @Override
  public void update(String id, JsonObject t) {
    this.update(id, t, null);
  }

  @Override
  public void update(String id, JsonObject t, Handler<AsyncResult<JsonObject>> handler) {
    jdbcClient.getConnection(ar -> {
      if (ar.failed()) {
        handler.handle(Future.failedFuture(ar.cause()));
        return;
      }

      JsonArray params = convertToUpdateParams(id, t);

      SQLConnection conn = ar.result();
      conn.updateWithParams(updateSQL, params, res -> {
        if (handler != null) {
          if (res.failed()) {
            handler.handle(Future.failedFuture(res.cause()));
          } else {
            UpdateResult result = res.result();
            if (result.getUpdated() == 1) {
              findOne(id, handler);
            } else {
              handler.handle(Future.succeededFuture());
            }
          }
        }
        conn.close();
      });
    });
  }

  @Override
  public void delete(String id) {
    this.delete(id, null);
  }

  @Override
  public void delete(String id, Handler<AsyncResult<JsonObject>> handler) {
    findOne(id, ar -> {
      if (ar.failed()) {
        handler.handle(Future.failedFuture(ar.cause()));
        return;
      }

      JsonObject t = ar.result();

      if (t != null) {
        jdbcClient.getConnection(connAr -> {
          if (connAr.failed()) {
            handler.handle(Future.failedFuture(connAr.cause()));
            return;
          }

          SQLConnection conn = connAr.result();
          conn.updateWithParams(deleteSQL, new JsonArray().add(id), res -> {
            if (handler != null) {
              if (res.failed()) {
                handler.handle(Future.failedFuture(res.cause()));
              } else {
                handler.handle(Future.succeededFuture(t));
              }
            }
            conn.close();
          });
        });
      } else {
        handler.handle(Future.succeededFuture());
      }
    });
  }

  abstract protected JsonObject mapRow(JsonArray json);

  abstract protected JsonArray convertToCreateParams(JsonObject json);

  abstract protected JsonArray convertToUpdateParams(String id, JsonObject json);

  abstract protected String generateId(JsonObject json);

}