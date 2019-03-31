package se.kry.codetest;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DBConnector {

  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private final String DB_PATH = "poller.db";
  private final SQLClient client;
  private SQLConnection connection;

  public DBConnector(Vertx vertx){
    JsonObject config = new JsonObject()
        .put("url", "jdbc:sqlite:" + DB_PATH)
        .put("driver_class", "org.sqlite.JDBC")
        .put("max_pool_size", 30);

    client = JDBCClient.createShared(vertx, config);
    client.getConnection(conn -> {
      if (conn.succeeded()) {
        connection = conn.result();
        ensureTables(connection);
      }
      else{
        System.err.println(conn.cause().getMessage());
        return;
      }
    });
  }

  private void ensureTables(SQLConnection connection) {
    connection.query("CREATE TABLE IF NOT EXISTS service (id TEXT PRIMARY KEY, name TEXT NOT NULL,url TEXT NOT NULL,status TEXT NOT NULL,created TEXT NOT NULL);",res -> {
      if(res.succeeded()){
        System.out.println("db table set");
      } else {
        res.cause().printStackTrace();
      }
    });
  }

  public List<Service> getAll() {
    List<Service> services=new ArrayList<>();
    connection.query("SELECT * FROM service", res -> {
      if (res.succeeded()) {
        ResultSet resultSet = res.result();
        for (JsonArray row : resultSet.getResults()) {
          String id = row.getString(0);
          String name = row.getString(1);
          String URL = row.getString(2);
          String status = row.getString(3);
          LocalDateTime created = LocalDateTime.parse(row.getString(4),formatter);
          services.add(new Service(id,name,URL,status,created));
        }
      } else {
        System.out.println("Failed to fetch services");
      }
    });

    return services;
  }

  public void save(Service service) {
    String insertQuery = "INSERT INTO service VALUES (?, ?, ?, ?, ?)";
    String created=service.getCreated().format(formatter);
    JsonArray params = new JsonArray()
                              .add(service.getId())
                              .add(service.getName())
                              .add(service.getURL())
                              .add(service.getStatus())
                              .add(created);

    connection.updateWithParams(insertQuery,params, res -> {
      if (res.succeeded()) {
        System.out.println("Service created successfully");
      } else {
        System.out.println("Failed to created Service URL: "+ service.getURL());
      }
    });
  }

  public void delete(String id) {
    String deleteQuery = "DELETE FROM service WHERE id=?";
    JsonArray params = new JsonArray().add(id);
    connection.updateWithParams(deleteQuery, params,res -> {
      if (res.succeeded()) {
        UpdateResult result = res.result();
        System.out.println("deleted no. of rows: " + result.getUpdated());
      } else {
        System.out.println("failed to remove service id " + id);
      }
    });
  }

  public void reset() {
    connection.query("DROP TABLE service;",res -> {
      if(res.succeeded()){
        System.out.println("Dropped table service");
        ensureTables(connection);
      } else {
        res.cause().printStackTrace();
      }
    });
  }


}
