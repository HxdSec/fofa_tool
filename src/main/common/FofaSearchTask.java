package main.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.kevinsawicki.http.HttpRequest;
import javafx.concurrent.Task;
import main.model.ItemModel;
import main.model.TaskModel;



public  class FofaSearchTask extends Task<TaskModel> {

    private TaskModel model;
    private Integer totalSize = 0;
    private Integer pageCount = 0;
    private Integer page = 1;
    private Integer pageSize = 500;

    public FofaSearchTask(TaskModel model) {
        this.model = model ;
    }

    @Override
    public TaskModel call() throws Exception {

        try {

            HttpRequest request =  HttpRequest.get(model.getUrl()+"&size="+pageSize);
            String result = request.body();
            JSONObject json = JSON.parseObject(result);
            boolean error = json.getBoolean("error");
            if (error){
                model.setStatus("error");
                return model;
            }else{
                totalSize = json.getInteger("size");
                pageCount = (totalSize-1)/pageSize + 1;

                this.setData(json);

                while (page<pageCount){

                    page++;
                    Thread.sleep(100);
                    if (isCancelled()) {
                        break ;
                    }

                    HttpRequest request2 =  HttpRequest.get(model.getUrl()+"&size="+pageSize+"&page="+page);
                    String result2 = request2.body();
                    JSONObject json2 = JSON.parseObject(result2);
                    boolean error2 = json2.getBoolean("error");
                    if (error2){
                        model.setStatus("error");
                        return model;
                    }else{

                        this.setData(json2);
                    }
                }

            }

            return model;

        } catch (HttpRequest.HttpRequestException exception) {
            model.setStatus("error");
            return model;
        }
    }

    private void setData(JSONObject json){
        JSONArray list = json.getJSONArray("results");
        for(int i=0;i<list.size();i++) {
            JSONArray arr = list.getJSONArray(i);
            ItemModel item = new ItemModel(i,arr.getString(0),arr.getString(1),arr.getString(2),arr.getString(3),arr.getString(4));
            this.model.result.add(item);
        }

        this.model.setStatus("["+page+"/"+pageCount+"]");
        this.updateValue(model);
        this.updateProgress(page,pageCount);

    }
}
