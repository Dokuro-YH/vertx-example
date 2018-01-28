package io.yanhai.example.common;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * @author yanhai
 */
@VertxGen
public interface ResourceManager {

  void findOne(String id, Handler<AsyncResult<JsonObject>> handler);

  void findAll(Handler<AsyncResult<JsonArray>> handler);

  void create(JsonObject t);

  void create(JsonObject t, Handler<AsyncResult<JsonObject>> handler);

  void update(String id, JsonObject t);

  void update(String id, JsonObject t, Handler<AsyncResult<JsonObject>> handler);

  void delete(String id);

  void delete(String id, Handler<AsyncResult<JsonObject>> handler);
}
