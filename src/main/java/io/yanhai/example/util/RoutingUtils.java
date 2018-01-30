package io.yanhai.example.util;

import java.util.function.Function;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
 * @author yanhai
 */
public interface RoutingUtils {

  static <T> Handler<AsyncResult<T>> withSuccess(RoutingContext ctx, Function<T, Handler<RoutingContext>> func) {
    return ar -> {
      if (ar.succeeded()) {
        func.apply(ar.result())
            .handle(ctx);
      } else {
        ctx.fail(ar.cause());
      }
    };
  }

  static <T> Function<T, Handler<RoutingContext>> created() {
    return payload -> ctx -> {
      if (payload == null) {
        ctx.response()
            .setStatusCode(201)
            .end();
      } else {
        json(payload, ctx);
      }
    };
  }

  static <T> Function<T, Handler<RoutingContext>> noContent() {
    return payload -> ctx -> noContent(ctx);
  }

  static <T> Function<T, Handler<RoutingContext>> json() {
    return payload -> ctx -> {
      if (payload == null) {
        notFound(ctx);
      } else {
        json(payload, ctx);
      }
    };
  }

  static <T> void json(T payload, RoutingContext ctx) {
    ctx.response()
        .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
        .end(Json.encode(payload));
  }

  static void noContent(RoutingContext ctx) {
    ctx.response()
        .setStatusCode(204)
        .end();
  }

  static void notFound(RoutingContext ctx) {
    ctx.response()
        .setStatusCode(404)
        .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
        .end(message("not_found").encode());
  }

  static JsonObject message(String message) {
    return new JsonObject().put("message", message);
  }

}
