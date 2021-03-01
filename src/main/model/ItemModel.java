package main.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class ItemModel {

    private final SimpleIntegerProperty id;
    private final SimpleStringProperty host;
    private final SimpleStringProperty ip;
    private final SimpleStringProperty port;
    private final SimpleStringProperty server;
    private final SimpleStringProperty title;


    public ItemModel(Integer id,String host, String ip,String port,String server,String title) {
        this.id = new SimpleIntegerProperty(id);
        this.host = new SimpleStringProperty(host);
        this.ip= new SimpleStringProperty(ip);
        this.port = new SimpleStringProperty(port);
        this.server = new SimpleStringProperty(server);
        this.title = new SimpleStringProperty(title);
    }

    public Integer getId() {
        return this.id.get();
    }

    public void setId(Integer id) {
        this.id.set(id);
    }

    public String getHost() {
        return this.host.get();
    }

    public void setHost(String host) {
        this.host.set(host);
    }

    public String getIp() {
        return this.ip.get();
    }

    public void setIp(String ip) {
        this.ip.set(ip);
    }

    public String getPort() {
        return this.port.get();
    }

    public void setPort(String port) {
        this.port.set(port);
    }

    public String getServer() {
        return this.server.get();
    }

    public void setServer(String server) {
        this.server.set(server);
    }

    public String getTitle() {
        return this.title.get();
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

}
