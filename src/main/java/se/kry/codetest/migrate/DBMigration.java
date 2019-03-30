package se.kry.codetest.migrate;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import se.kry.codetest.DBConnector;
import se.kry.codetest.Service;

public class DBMigration {

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    SQLConnection connection = new DBConnector(vertx).getConnection();

    connection.query("CREATE TABLE IF NOT EXISTS service (id integer PRIMARY KEY, name TEXT NOT NULL,url VARCHAR(128) NOT NULL,status TEXT NOT NULL,created TEXT NOT NULL);",res -> {
      if(res.succeeded()){
        System.out.println("completed db migrations");
      } else {
        res.cause().printStackTrace();
      }
    });
  }
}
