package io.yanhai.example.web.login;

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;

/**
 * @author yanhai
 */
public class JsonLoginHandlerImpl implements JsonLoginHandler {

  private static final Logger log = LoggerFactory.getLogger(JsonLoginHandlerImpl.class);

  private final AuthProvider authProvider;

  private String usernameParam;
  private String passwordParam;

  @Override
  public JsonLoginHandler setUsernameParam(String usernameParam) {
    this.usernameParam = usernameParam;
    return this;
  }

  @Override
  public JsonLoginHandler setPasswordParam(String passwordParam) {
    this.passwordParam = passwordParam;
    return this;
  }

  public JsonLoginHandlerImpl(AuthProvider authProvider, String usernameParam, String passwordParam) {
    this.authProvider = authProvider;
    this.usernameParam = usernameParam;
    this.passwordParam = passwordParam;
  }

  @Override
  public void handle(RoutingContext context) {
    HttpServerRequest req = context.request();
    if (req.method() != HttpMethod.POST) {
      context.fail(405);
    } else {
      String body = context.getBodyAsString();
      if (body == null || body.isEmpty()) {
        log.warn("Json body not parsed - did you forget to include a BodyHandler?");
        context.fail(401);
      } else {
        JsonObject params = context.getBodyAsJson();
        String username = params.getString(usernameParam);
        String password = params.getString(passwordParam);
        if (username == null || password == null) {
          log.warn("No username or password provided in body - did you forget to include a BodyHandler?");
          context.fail(401);
        } else {
          Session session = context.session();
          JsonObject authInfo = new JsonObject().put("username", username).put("password", password);
          authProvider.authenticate(authInfo, res -> {
            if (res.succeeded()) {
              User user = res.result();
              context.setUser(user);
              if (session != null) {
                // 更新SessionID
                session.regenerateId();
              }
              // 返回 principal 信息
              context.response()
                  .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                  .end(user.principal().encode());
            } else {
              context.fail(403);
            }
          });
        }
      }
    }
  }

}
