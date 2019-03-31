package se.kry.codetest;

import io.vertx.core.json.JsonObject;

import java.time.LocalDateTime;
import java.util.UUID;
import org.apache.commons.validator.routines.UrlValidator;


public class Service {
    private String id;
    private String name;
    private String URL;
    private String status;
    private LocalDateTime created;

    public Service(String name, String URL) {
        this.name = name;
        this.id = getUniqueIdentifier();
        this.URL = URL;
        this.status = "UNKNOWN";
        this.created = LocalDateTime.now();
    }

    public Service(String name, String id, String URL, String status) {
        this.name = name;
        this.id = id;
        this.URL = URL;
        this.status = status;
        this.created=LocalDateTime.now();
    }
    public Service(String name, String id, String URL, String status, LocalDateTime created) {
        this.name = name;
        this.id = id;
        this.URL = URL;
        this.status = status;
        this.created=created;
    }

    public Service(JsonObject jsonObject) {
        this.name = jsonObject.getString("name");
        this.id = jsonObject.getString("id");
        this.URL = jsonObject.getString("url");
        this.status = jsonObject.getString("status");
        this.created=LocalDateTime.now();
    }

    private String getUniqueIdentifier() {
        return UUID.randomUUID().toString();
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getURL() {
        return URL;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public JsonObject toJsonObject(){
        JsonObject json = new JsonObject();
        json.put("name", this.getName());
        json.put("id", this.getId());
        json.put("url", this.getURL());
        json.put("status", this.getStatus());
        json.put("created", this.getCreated());
        return json;
    }

    public static boolean isValidURL(String url) {
        String[] schemes = {"http", "https"};
        UrlValidator urlValidator = new UrlValidator(schemes);
        return urlValidator.isValid(url);
    }
}
