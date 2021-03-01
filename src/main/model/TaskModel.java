package main.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.ArrayList;
import java.util.List;

public class TaskModel {

    private final SimpleIntegerProperty id;
    private final SimpleStringProperty qbase64;
    private final SimpleStringProperty url;
    private final SimpleStringProperty status;
    public List<ItemModel> result = new ArrayList<ItemModel>();

    public TaskModel(Integer id,String qbase64, String url,String status) {
        this.id = new SimpleIntegerProperty(id);
        this.qbase64 = new SimpleStringProperty(qbase64);
        this.url= new SimpleStringProperty(url);
        this.status = new SimpleStringProperty(status);
    }

    public Integer getId() {
        return this.id.get();
    }

    public void setId(Integer id) {
        this.id.set(id);
    }

    public String getQbase64() {
        return this.qbase64.get();
    }

    public void setQbase64(String qbase64) {
        this.qbase64.set(qbase64);
    }

    public String getUrl() {
        return this.url.get();
    }

    public void setUrl(String url) {
        this.url.set(url);
    }

    public String getStatus() {
        return this.status.get();
    }

    public void setStatus(String status) {
        this.status.set(status);
    }



}