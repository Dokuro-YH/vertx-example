package io.yanhai.example.common;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;

/**
 * @author yanhai
 */
@VertxGen
public interface JdbcResourceManager<T> extends ResourceManager<T> {

  @Fluent
  JdbcResourceManager<T> setFindOneSQL(String findOneSQL);

  @Fluent
  JdbcResourceManager<T> setFindAllSQL(String findAllSQL);

  @Fluent
  JdbcResourceManager<T> setCreateSQL(String createSQL);

  @Fluent
  JdbcResourceManager<T> setUpdateSQL(String updateSQL);

  @Fluent
  JdbcResourceManager<T> setDeleteSQL(String deleteSQL);

}