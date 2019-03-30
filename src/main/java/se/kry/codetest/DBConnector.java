package se.kry.codetest;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;
import java.util.ArrayList;
import java.util.List;

public class DBConnector {

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
      if (conn.failed()) {
        System.err.println(conn.cause().getMessage());
        return;
      }
      connection = conn.result();
    });
  }

  public List<Service> getAll() {
    List<Service> services = new ArrayList<>();
    connection.query("SELECT * from SERVICES", res -> {
      if (res.succeeded()) {
        ResultSet resultSet = res.result();
        for (JsonObject jsonObject : resultSet.getRows()) {
          services.add(new Service(jsonObject));
        }
      } else {
        System.out.println("Failed to fetch services");
      }
    });

    return services;
  }

  public void save(Service service) {
    JsonObject jsonObject = service.toJsonObject();
    jsonObject.put("_id", service.getId());
    String insertQuery = "INSERT INTO SERVICES VALUES (?, ?, ?, ?, ?)";
    JsonArray params = new JsonArray()
                              .add(service.getId())
                              .add(service.getName())
                              .add(service.getURL())
                              .add(service.getStatus())
                              .add(service.getCreated());

    connection.updateWithParams(insertQuery,params, res -> {
      if (res.succeeded()) {
        UpdateResult result = res.result();
        System.out.println("Service created successfully");
      } else {
        System.out.println("Failed to created Service URL: "+ service.getURL());
      }
    });
  }

  public void delete(String id) {
    String deleteQuery = "DELETE FROM services WHERE id=?";
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
    // TODO
  }


}
