package main.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import main.common.FofaSearchTask;
import main.model.ItemModel;
import main.model.TaskModel;
import main.utils.FileUtil;
import org.supercsv.cellprocessor.FmtBool;
import org.supercsv.cellprocessor.FmtDate;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.constraint.LMinMax;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.UniqueHashCode;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainController implements Initializable {

    private static final int MAX_THREADS = 4 ;
    private String FOFA_API = "https://fofa.so/api/v1/search/all?email=%s&key=%s&qbase64=%s&fields=host,ip,port,server,title";

    private final Executor exec = Executors.newFixedThreadPool(MAX_THREADS, runnable -> {
        Thread t = new Thread(runnable);
        t.setDaemon(true);
        return t ;
    });

    private ObservableList<TaskModel> taskList = FXCollections.observableArrayList();
    private ObservableList<ItemModel> resultList = FXCollections.observableArrayList();
    private Integer progress = 0;
    private List<FofaSearchTask> searchList = new ArrayList<FofaSearchTask>();


    private Stage stage;

    @FXML
    private AnchorPane root;

    @FXML
    private Button btn_search;

    @FXML
    private TextField txt_one;

    @FXML
    private Button btn_clear;

    @FXML
    private ListView<TaskModel> lv_target;

    @FXML
    private ProgressBar progress_bar;

    @FXML
    private Button btn_cancel;

    @FXML
    private TextField txt_key;

    @FXML
    private CheckBox cbx_remember;

    @FXML
    private TextField txt_email;

    @FXML
    private TableView<ItemModel> tb_result;

    @FXML
    private Button btn_export;

    @FXML
    private Button btn_add_one;

    @FXML
    private Button btn_add_more;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        txt_one.setText("domain=\"baidu.com\"");
        btn_search.setDisable(false);
        btn_cancel.setDisable(true);

        //properties
        Properties properties = new Properties();
        //加载配置文件信息到Properties中
        try {
            properties.load(new FileReader("config.properties"));
            if (properties.getProperty("email")!=null){
                txt_email.setText(properties.getProperty("email"));
            }
            if (properties.getProperty("key")!=null){
                txt_key.setText(properties.getProperty("key"));
            }
            if (properties.getProperty("remember").equals("1")){
                cbx_remember.setSelected(true);
            }else{
                cbx_remember.setSelected(false);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        //listView
        lv_target.setCellFactory(lv -> {
            ListCell<TaskModel> cell = new ListCell<TaskModel>() {
                @Override
                protected void updateItem(TaskModel item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setText("");
                    } else {
                        setText(item.getStatus()+" "+item.getQbase64());
                    }
                }
            };
            return cell;
        });
        lv_target.setItems(taskList);

        //tableview init
        TableColumn idCol = new TableColumn("ID");
        idCol.prefWidthProperty().bind(tb_result.widthProperty().multiply(0.05));
        idCol.setCellValueFactory(new PropertyValueFactory<ItemModel, String>("id"));

        TableColumn hostCol = new TableColumn("Host");
        hostCol.prefWidthProperty().bind(tb_result.widthProperty().multiply(0.20));
        hostCol.setCellValueFactory(new PropertyValueFactory<ItemModel, String>("host"));

        TableColumn ipportCol = new TableColumn("IP:Port");
        ipportCol.prefWidthProperty().bind(tb_result.widthProperty().multiply(0.25));
        ipportCol.setCellValueFactory(new PropertyValueFactory<ItemModel, String>("ip"));

        TableColumn serverCol = new TableColumn("Serer");
        serverCol.prefWidthProperty().bind(tb_result.widthProperty().multiply(0.20));
        serverCol.setCellValueFactory(new PropertyValueFactory<ItemModel, String>("server"));

        TableColumn titleCol = new TableColumn("Title");
        titleCol.prefWidthProperty().bind(tb_result.widthProperty().multiply(0.30));
        titleCol.setCellValueFactory(new PropertyValueFactory<ItemModel, String>("title"));


        tb_result.getColumns().addAll(idCol, hostCol,ipportCol,serverCol,titleCol);
        tb_result.setItems(resultList);

        tb_result.setRowFactory(new Callback<TableView<ItemModel>, TableRow<ItemModel>>()
        {
            @Override
            public TableRow<ItemModel> call(TableView<ItemModel> param)
            {
                TableRow<ItemModel> row = new TableRow<ItemModel>();
                row.setOnMouseClicked(new EventHandler<MouseEvent>()
                {
                    @Override
                    public void handle(MouseEvent event)
                    {
                        if (event.getClickCount() == 2 && (! row.isEmpty()) )
                        {
                            ItemModel item = row.getItem();
                            String url = item.getHost();
                            if(!url.contains("http")){
                                url = "http://" + url;
                            }
                            try {
                                Desktop.getDesktop().browse(new URI(url));
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (URISyntaxException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                return row;
            }
        });

    }


    @FXML
    void btn_remember_click(ActionEvent event) throws IOException {
        if (!cbx_remember.isSelected()){
            Properties properties = new Properties();
            properties.store(new FileWriter("config.properties"), "config");
        }else{
            Properties properties = new Properties();
            properties.setProperty("email", txt_email.getText());
            properties.setProperty("key", txt_key.getText());
            properties.setProperty("remember", cbx_remember.isSelected()?"1":"0");
            properties.store(new FileWriter("config.properties"), "config");

        }


    }

    @FXML
    void btn_search_click(ActionEvent event) {
        progress = 0;
        progress_bar.setProgress(0.0);
        searchList.clear();
        resultList.clear();

        if(taskList.stream().count()>0){
            btn_search.setDisable(true);
            btn_cancel.setDisable(false);
        }


        taskList.stream().map(n->{
            return  new TaskModel(taskList.indexOf(n),n.getQbase64(),n.getUrl(),n.getStatus());
        }).map(FofaSearchTask::new).peek(exec::execute).forEach(task-> {
            searchList.add(task);

                    task.setOnSucceeded(t -> {

                        TaskModel model = task.getValue();
                        for (ItemModel item:model.result) {
                            resultList.add(item);
                        }

                        progress++;
                        progress_bar.setProgress((double)progress/(double)taskList.stream().count());
                        if (progress == taskList.stream().count()){
                            btn_search.setDisable(false);
                            btn_cancel.setDisable(true);
                        }

                    });

                    task.setOnCancelled(t -> {

                        TaskModel model = task.getValue();
                        for (ItemModel item:model.result) {
                            resultList.add(item);
                        }

                    });


                    task.progressProperty().addListener((observable, oldValue, newValue)  -> {

                        TaskModel model = task.getValue();
                        taskList.set(model.getId(),model);

                    });
                }

        );

    }

    @FXML
    void btn_cancel_click(ActionEvent event) {
        searchList.stream().filter(Task::isRunning).forEach(Task::cancel);
        btn_search.setDisable(false);
        btn_cancel.setDisable(true);

    }

    private  CellProcessor[] getProcessors() {

        final CellProcessor[] processors = new CellProcessor[] {
                new NotNull(), // id
                new NotNull(), // host
                new NotNull(), // ip
                new NotNull(), // port
                new NotNull(), // server
                new NotNull()// title
        };

        return processors;
    }

    @FXML
    void btn_export_click(ActionEvent event) throws Exception {
        if (resultList.stream().count() == 0){
            return;
        }

        ICsvBeanWriter beanWriter = null;
        try {
            beanWriter = new CsvBeanWriter(new FileWriter("result.csv"),
                    CsvPreference.STANDARD_PREFERENCE);

            final CellProcessor[] processors = getProcessors();
            final String[] header = new String[] {"id", "host", "ip", "port", "server", "title" };

            // write the header
            beanWriter.writeHeader(header);

            // write the customer lists
            for(ItemModel model : resultList){
                beanWriter.write(model, header, processors);
            }

            new Alert(Alert.AlertType.INFORMATION, "数据已经导出到根目录result.csv文件", new ButtonType[]{ButtonType.CLOSE}).show();

        }
        finally {
            if( beanWriter != null ) {
                beanWriter.close();
            }
        }

    }

    @FXML
    void btn_add_one_click(ActionEvent event) {
        if(txt_one.getText().trim().equals("")){
            new Alert(Alert.AlertType.INFORMATION, "请添加查询目标", new ButtonType[]{ButtonType.CLOSE}).show();
            return;
        }
        String query = Base64.getEncoder().encodeToString(txt_one.getText().getBytes(StandardCharsets.UTF_8));
        String url = String.format(FOFA_API, txt_email.getText(),txt_key.getText(),query);
        taskList.add(new TaskModel((int)taskList.stream().count()+1,txt_one.getText(),url,""));
    }

    @FXML
    void btn_add_more_click(ActionEvent event) {

        if (stage == null) {
            stage = (Stage) root.getScene().getWindow();
        }

        final FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null){
            String filepath = file.getPath();
            List<String> list = FileUtil.Read(filepath);
            for (String str: list ) {
                String query = Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
                String url = String.format(FOFA_API, txt_email.getText(),txt_key.getText(),query);
                taskList.add(new TaskModel((int)taskList.stream().count()+1,str,url,""));
            }
        }
    }

    @FXML
    void btn_clear_click(ActionEvent event) {
        taskList.clear();
        resultList.clear();

    }




}

