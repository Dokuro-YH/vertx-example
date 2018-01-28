package io.yanhai.example.common;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;

/**
 * @author yanhai
 */
@VertxGen
public interface JdbcResourceManager extends ResourceManager {

  @Fluent
  JdbcResourceManager setFindOneSQL(String findOneSQL);

  @Fluent
  JdbcResourceManager setFindAllSQL(String findAllSQL);

  @Fluent
  JdbcResourceManager setCreateSQL(String createSQL);

  @Fluent
  JdbcResourceManager setUpdateSQL(String updateSQL);

  @Fluent
  JdbcResourceManager setDeleteSQL(String deleteSQL);

}