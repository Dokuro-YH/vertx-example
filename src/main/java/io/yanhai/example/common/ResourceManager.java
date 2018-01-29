package io.yanhai.example.common;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;

/**
 * @author yanhai
 */
@VertxGen
public interface ResourceManager<T> {

  void findOne(String id, Handler<AsyncResult<T>> handler);

  void findAll(Handler<AsyncResult<JsonArray>> handler);

  void create(T t);

  void create(T t, Handler<AsyncResult<T>> handler);

  void update(String id, T t);

  void update(String id, T t, Handler<AsyncResult<T>> handler);

  void delete(String id);

  void delete(String id, Handler<AsyncResult<T>> handler);
}
