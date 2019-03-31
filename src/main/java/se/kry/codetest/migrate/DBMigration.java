package se.kry.codetest.migrate;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import se.kry.codetest.DBConnector;
import se.kry.codetest.Service;

public class DBMigration {

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    JsonObject config = new JsonObject()
            .put("url", "jdbc:sqlite:" + "poller.db")
            .put("driver_class", "org.sqlite.JDBC")
            .put("max_pool_size", 30);
    SQLClient client = JDBCClient.createShared(vertx, config);
    client.getConnection(conn -> {
      if (conn.succeeded()) {
        conn.result().query("DROP TABLE service;",res -> {
          if(res.succeeded()){
            System.out.println("Dropped db migrations");
          } else {
            res.cause().printStackTrace();
          }
        });
      }
    });


  }
}
