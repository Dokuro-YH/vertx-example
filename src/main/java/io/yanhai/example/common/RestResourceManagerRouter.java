package io.yanhai.example.common;

import java.util.function.Function;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

/**
 * @author yanhai
 */
public interface RestResourceManagerRouter {

  String DEFAULT_CONTENT_TYPE = "application/json";

  static Router router(Vertx vertx, ResourceManager resourceManager, Function<String, JsonObject> requestBodyConvert) {
    Router router = Router.router(vertx);

    router.get("/:id").handler(ctx -> {
      @Nullable String id = ctx.pathParam("id");

      resourceManager.findOne(id, ar -> {
        if (ar.failed()) {
          ctx.fail(ar.cause());
          return;
        }

        if (ar.result() == null) {
          ctx.fail(404);
          return;
        }

        ctx.response()
            .putHeader(HttpHeaders.CONTENT_TYPE, DEFAULT_CONTENT_TYPE)
            .end(ar.result().encode());
      });
    });

    router.get().handler(ctx -> {

      resourceManager.findAll(ar -> {
        if (ar.failed()) {
          ctx.fail(ar.cause());
          return;
        }

        ctx.response()
            .putHeader(HttpHeaders.CONTENT_TYPE, DEFAULT_CONTENT_TYPE)
            .end(ar.result().encode());
      });
    });

    router.post().handler(ctx -> {
      @Nullable JsonObject body = requestBodyConvert.apply(ctx.getBodyAsString());

      if (body == null) {
        ctx.fail(400);
        return;
      }

      resourceManager.create(body, ar -> {
        if (ar.failed()) {
          if (ar.cause().getMessage().contains("duplicate")) {
            ctx.fail(409);
          } else {
            ctx.fail(ar.cause());
          }
          return;
        }

        ctx.response()
            .putHeader(HttpHeaders.CONTENT_TYPE, DEFAULT_CONTENT_TYPE)
            .end(ar.result().encode());
      });
    });

    router.put("/:id").handler(ctx -> {
      @Nullable String id = ctx.pathParam("id");
      @Nullable JsonObject body = requestBodyConvert.apply(ctx.getBodyAsString());

      if (body == null) {
        ctx.fail(400);
        return;
      }

      resourceManager.update(id, body, ar -> {
        if (ar.failed()) {
          if (ar.cause().getMessage().contains("duplicate")) {
            ctx.fail(409);
          } else {
            ctx.fail(ar.cause());
          }
          return;
        }

        if (ar.result() == null) {
          ctx.fail(404);
          return;
        }

        ctx.response()
            .putHeader(HttpHeaders.CONTENT_TYPE, DEFAULT_CONTENT_TYPE)
            .end(ar.result().encode());
      });
    });

    router.delete("/:id").handler(ctx -> {
      @Nullable String id = ctx.pathParam("id");

      resourceManager.delete(id, ar -> {
        if (ar.failed()) {
          if (ar.cause().getMessage().contains("duplicate")) {
            ctx.fail(409);
          } else {
            ctx.fail(ar.cause());
          }
          return;
        }

        if (ar.result() == null) {
          ctx.response().end();
        } else {
          ctx.response()
              .putHeader(HttpHeaders.CONTENT_TYPE, DEFAULT_CONTENT_TYPE)
              .end(ar.result().encode());
        }
      });
    });

    return router;
  }

}
