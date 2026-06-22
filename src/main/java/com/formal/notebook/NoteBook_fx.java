package com.formal.notebook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class NoteBook_fx extends Application{
    private int last_notebook_id = -1;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 应用程序启动时执行的代码
        System.out.println("NoteBook_fx 应用程序启动");
        //设置primaryStage图标
        InputStream imageStream_icon_document = getClass().getResourceAsStream("/icons/文档架子_document-folder.png");

        if (imageStream_icon_document != null) {
            primaryStage.getIcons().add(new Image(imageStream_icon_document));
        }

        primaryStage.setTitle("NoteBooK");

        /**
         * 图形界面包括三个场景，两个stage
         * 1.当无任何笔记本时，加载引导界面创建新笔记本
         * 2.创建笔记本会打开一个新窗口用于输入笔记本名字
         * 3.创建好笔记本后，为splitpane，左侧栏有gridpane，顶上有笔记本选择，中间是笔记标题，最下面有操作栏，有新建笔记本，新建笔记，右侧栏为笔记内容
         * 4.数据库错误窗口，用于显示数据库错误信息，包括错误类型，错误信息，错误位置
         */

        //-----------------------------------------------------------------//
        //场景制作区
        //-----------------------------------------------------------------//
        //第四界面：异常界面贯穿全代码，需提前
        GridPane error_scene_root = new GridPane();

        // 让整个内容距离窗口边缘有 20 像素的“呼吸空间”
        error_scene_root.setPadding(new Insets(20));
        // 让左边图标和右边表单之间横向隔开 20 像素
        error_scene_root.setHgap(25); 
        error_scene_root.setAlignment(Pos.CENTER_LEFT); // 靠左居中对齐

        InputStream error_icon = getClass().getResourceAsStream("/icons/错误_error.png");
        if (error_icon != null) {
            ImageView error_icon_view = new ImageView(new Image(error_icon));
            error_icon_view.setFitWidth(80);  //稍微缩小一点，更精致
            error_icon_view.setFitHeight(80);
            error_scene_root.add(error_icon_view, 0, 0);
        }

        VBox error_label_box = new VBox();
        // 控制右侧内部的整体垂直间距
        error_label_box.setSpacing(10); 

        Label error_label = new Label("程序异常！");
        error_label.setStyle("-fx-font-weight: bold; -fx-font-size: 20px;"); // 用CSS改字号更方便

        Label error_tip_label = new Label("请确认数据库存在且启动mysql服务，如果为在线服务，请确认网路连接。");
        error_tip_label.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;"); // 把提示文字改成高级灰，突出重点
        error_tip_label.setWrapText(true);
        //*请记得绑定这个label的文字属性到异常信息
        Label error_stackTrace = new Label("");

        // 按顺序塞入右侧
        error_label_box.getChildren().addAll(error_label, error_tip_label, error_stackTrace);

        error_scene_root.add(error_label_box, 1, 0);

        // 🌟 5. 稍微宽裕一点的舞台尺寸，让布局展开
        Scene error_scene = new Scene(error_scene_root, 420, 160);
        
        Stage error_stage = new Stage();
        error_stage.setTitle("程序异常");
        error_stage.setScene(error_scene);
        error_stage.setResizable(false);
        error_stage.initOwner(primaryStage);
        error_stage.initModality(Modality.WINDOW_MODAL);
        
        //-----------------------------------------------------------------//
        //第一界面为无笔记本下的场景
        VBox guide_scene_root = new VBox();
        InputStream no_notebook_icon = getClass().getResourceAsStream("/icons/收件箱_inbox.png");
        if (no_notebook_icon != null) {
            ImageView no_notebook_icon_view = new ImageView(new Image(no_notebook_icon));
            no_notebook_icon_view.setFitWidth(100);
            no_notebook_icon_view.setFitHeight(100);
            guide_scene_root.getChildren().add(no_notebook_icon_view);
            Label no_notebook_label = new Label("暂无笔记本");
            no_notebook_label.setFont(new Font("System", 20));
            guide_scene_root.getChildren().add(no_notebook_label);
            guide_scene_root.spacingProperty().set(20);
            guide_scene_root.setAlignment(Pos.CENTER);
        }
        //创建一个用于新建笔记本的按钮
        Button create_notebook_button = new Button("新建笔记本");
        guide_scene_root.getChildren().add(create_notebook_button);

        Scene guide_scene = new Scene(guide_scene_root, 800, 500);

        //-----------------------------------------------------------------//
        //第二界面用于新的stage，创建笔记本
        // 第二界面用于新的stage，创建笔记本
        GridPane new_notebook_scene_root = new GridPane();

        // 让整个内容距离窗口边缘有 20 像素的“呼吸空间”
        new_notebook_scene_root.setPadding(new Insets(20));
        // 让左边图标和右边表单之间横向隔开 20 像素
        new_notebook_scene_root.setHgap(25); 
        new_notebook_scene_root.setAlignment(Pos.CENTER_LEFT); // 靠左居中对齐

        InputStream new_notebook_icon = getClass().getResourceAsStream("/icons/笔记本_notebook-one.png");
        if (new_notebook_icon != null) {
            ImageView new_notebook_icon_view = new ImageView(new Image(new_notebook_icon));
            new_notebook_icon_view.setFitWidth(80);  // 🌟 稍微缩小一点，更精致
            new_notebook_icon_view.setFitHeight(80);
            new_notebook_scene_root.add(new_notebook_icon_view, 0, 0);
        }

        VBox label_and_textarea = new VBox();
        // 控制右侧内部的整体垂直间距
        label_and_textarea.setSpacing(10); 

        Label new_notebook_label = new Label("新建笔记本");
        new_notebook_label.setStyle("-fx-font-weight: bold; -fx-font-size: 20px;"); // 用CSS改字号更方便

        Label tip_label = new Label("请不要输入已有的笔记本名！不可超过50个字");
        tip_label.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;"); // 把提示文字改成高级灰，突出重点

        // 把大卡、粗鲁的 TextArea 换成优雅的单行 TextField
        TextField notebook_name_field = new TextField();
        notebook_name_field.setPromptText("请输入笔记本名称..."); // 增加灰色占位提示词
        notebook_name_field.setPrefWidth(260); // 调整更合适的宽度
        //确认按钮Hbox右对齐
        HBox confirm_button_hbox = new HBox();
        confirm_button_hbox.setAlignment(Pos.CENTER_RIGHT);
        Button confirm_button = new Button(" 创建 ");
        confirm_button.setStyle("-fx-background-color: #bcf3d3ff; -fx-text-fill: black; -fx-font-weight: bold;"
                                +"-fx-background-radius: 32px; -fx-border-radius: 32px;");
        confirm_button_hbox.getChildren().add(confirm_button);

        // 按顺序塞入右侧
        label_and_textarea.getChildren().addAll(new_notebook_label, tip_label, notebook_name_field, confirm_button_hbox);

        new_notebook_scene_root.add(label_and_textarea, 1, 0);

        // 🌟 5. 稍微宽裕一点的舞台尺寸，让布局展开
        Scene new_notebook_scene = new Scene(new_notebook_scene_root, 420, 160); 

        //-----------------------------------------------------------------//
        //第三界面：主笔记界面 
        //整体为splitpane，左侧为gridpane（笔记列表，笔记本选择），右侧为gridpane（标题，内容）
        SplitPane main_splitpane = new SplitPane();

        // 让整个内容距离窗口边缘有 20 像素的“呼吸空间”
        main_splitpane.setPadding(new Insets(20));
        main_splitpane.setDividerPositions(0.5);
        //GridPane（笔记列表，笔记本选择）
        GridPane note_list_scene_root = new GridPane();
        ListView<String> note_list_view = new ListView<>();
        Label empty_title = new Label("暂无笔记");
        empty_title.setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic;");
        note_list_view.setPlaceholder(empty_title);
        
        note_list_view.setPrefWidth(400);
        //下方创建新笔记，切换笔记本菜单
        //icon加载
        ImageView new_note_icon_view = null;
        try (InputStream is = getClass().getResourceAsStream("/icons/文件添加_file-addition.png")){
            new_note_icon_view = new ImageView(new Image(is));
        } catch (Exception e) {
            e.printStackTrace();
            error_stackTrace.setText(e.getMessage());
            error_stage.show();
        }
        Button new_note_button = new Button();
        if (new_note_icon_view != null) {
            new_note_button.setGraphic(new_note_icon_view);
        }
        new_note_button.setPrefWidth(40);
        new_note_button.setPrefHeight(40);
        //底部笔记本选择菜单
        MenuButton select_notebook_button = new MenuButton();//后续绑定文本到当前笔记本名
        //无论如何，笔记本选择菜单中有一个新建笔记本的选项
        MenuItem new_notebook_item = new MenuItem("新建笔记本");
        select_notebook_button.getItems().add(new_notebook_item);
        


        note_list_scene_root.add(new_note_button, 0, 0);
        note_list_scene_root.add(note_list_view, 0, 1);
        note_list_scene_root.add(select_notebook_button, 0, 2);

        note_list_scene_root.setMinWidth(400);

        main_splitpane.getItems().add(note_list_scene_root);
        Scene main_scene = new Scene(main_splitpane, 800, 500);
        
        //-----------------------------------------------------------------//

        //-------------------------------------------------------------------//
        //stage配置区
        //-------------------------------------------------------------------//
        primaryStage.setScene(guide_scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(500);
        //primaryStage.show();

        Stage new_notebook_stage = new Stage();
        new_notebook_stage.setTitle("新建笔记本");
        new_notebook_stage.setScene(new_notebook_scene);
        new_notebook_stage.setResizable(false);
        new_notebook_stage.initOwner(primaryStage);
        new_notebook_stage.initModality(Modality.WINDOW_MODAL);
        new_notebook_stage.setOnCloseRequest(event -> {
            notebook_name_field.clear();
        });
        //new_notebook_stage.show();
        
        //new_notebook_stage.show();


        //error_stage.show();   

        //-----------------------------------------------------------//
        //事件，判断，监听，事件处理
        //-----------------------------------------------------------//
        
        //首次启动判断是否有笔记本
        try{
            ArrayList<Notebook> notebook_list = new ArrayList<>();
            notebook_list = DB_Opearte.query_all_notebooks();
            if(notebook_list.isEmpty()){
                primaryStage.show();
            }else{
                //从config中读取上次使用的笔记本ID
                Properties properties = new Properties();
                try(FileInputStream config_load = new FileInputStream("Config.properties")){
                    properties.load(config_load);
                }catch(Exception e){
                    e.printStackTrace();
                    error_stackTrace.setText(e.getMessage());
                    error_stage.show();
                }
                int last_notebook_id = Integer.parseInt(properties.getProperty("last_notebook_id"));
                //根据上次使用的笔记本ID刷新标题列表
                refresh_title_list(last_notebook_id, note_list_view);
                String notebook_name = DB_Opearte.get_notebook_name(last_notebook_id);
                set_select_notebook_button_text(notebook_name, select_notebook_button);
                primaryStage.setScene(main_scene);
                primaryStage.show();
            }
        }catch(SQLException e){
            error_stackTrace.setText(e.getMessage());
            error_stage.show();
            e.printStackTrace();
        }
        create_notebook_button.setOnAction(event -> {
            new_notebook_stage.show();
        });

        //新建笔记本的判断逻辑
        //输入框实时监听是否笔记本名重复
        //设置提示label
        Label warning_label_1 = new Label("笔记本名最多50个字符");
        warning_label_1.setStyle("-fx-font-size: 11px; -fx-text-fill: #ff4d4f;"); // 把提示文字改成红色
        warning_label_1.setWrapText(true);
        label_and_textarea.getChildren().add(3, warning_label_1);
        warning_label_1.setManaged(false);

        Label warning_label_2 = new Label("笔记本名已存在");
        warning_label_2.setStyle("-fx-font-size: 11px; -fx-text-fill: #ff4d4f;"); // 把提示文字改成红色
        warning_label_2.setWrapText(true);
        label_and_textarea.getChildren().add(4, warning_label_2);
        warning_label_2.setManaged(false);

        Label empty_warnning = new Label("笔记本名不能为空");
        empty_warnning.setStyle("-fx-font-size: 11px; -fx-text-fill: #ff4d4f;"); // 把提示文字改成红色
        empty_warnning.setWrapText(true);
        label_and_textarea.getChildren().add(5, empty_warnning);
        empty_warnning.setManaged(false);

        notebook_name_field.textProperty().addListener((observable, oldValue, newValue) -> {
            //限制字数逻辑
            int max_length = 50;
            //如果输入的字符数超过最大长度且不为空字符串
            if(newValue.length() > max_length && !newValue.isEmpty()){
                String truncated_name = newValue.substring(0, max_length);
                int postition = notebook_name_field.getCaretPosition();
                notebook_name_field.setText(truncated_name);
                notebook_name_field.positionCaret(Math.min(postition, max_length));
                //显示警告标签
                warning_label_1.setVisible(true);
                warning_label_1.setManaged(true);
            }else{
                //隐藏警告标签
                warning_label_1.setVisible(false);
                warning_label_1.setManaged(false);
            }

            //判断是否重复
            try{
                boolean is_exists = DB_Opearte.is_notebook_name_exists(newValue);
                if(is_exists){
                    warning_label_2.setVisible(true);
                    warning_label_2.setManaged(true);
                }else{
                    warning_label_2.setVisible(false);
                    warning_label_2.setManaged(false);
                }
            }catch(SQLException e){
                error_stackTrace.setText(e.getMessage());
                error_stage.show();
                e.printStackTrace();
            }
        });

        //确认创建按钮
        confirm_button.setOnAction(event -> {
            String notebook_name = notebook_name_field.getText();
            //判断是否为空
            if(notebook_name.isEmpty()){
                empty_warnning.setVisible(true);
                empty_warnning.setManaged(true);
                return;
            }
            //判断是否重复
            try{
                boolean is_exists = DB_Opearte.is_notebook_name_exists(notebook_name);
                if(is_exists){
                    warning_label_2.setVisible(true);
                    warning_label_2.setManaged(true);
                    return;
                }
            }catch(SQLException e){
                error_stackTrace.setText(e.getMessage());
                error_stage.show();
                e.printStackTrace();
            }
            //创建笔记本
            try{
                DB_Opearte.create_new_notebook(notebook_name);
                int notebook_id = DB_Opearte.get_notebook_id(notebook_name);
                //更新上次使用的笔记本ID
                last_notebook_id = notebook_id;
                //刷新笔记列表
                refresh_title_list(notebook_id,note_list_view);
                //设置select_notebook_button的文本为新创建的笔记本名
                set_select_notebook_button_text(notebook_name, select_notebook_button);
                primaryStage.setScene(main_scene);
                new_notebook_stage.close();
            }catch(SQLException e){
                error_stackTrace.setText(e.getMessage());
                error_stage.show();
                e.printStackTrace();
            }
        });

        //对menu_item进行设置
        new_notebook_item.setOnAction(event -> {
            new_notebook_stage.show();
        });

        //添加笔记本到菜单
        try{
            add_notebook_list(select_notebook_button, note_list_view);
        }catch(SQLException e){
            e.printStackTrace();
            error_stackTrace.setText(e.getMessage());
            error_stage.show();
        }

        primaryStage.setOnCloseRequest(event -> {
            //保存上一次使用的笔记本ID
            try {
                FileInputStream config_load = new FileInputStream("Config.properties");
                if (last_notebook_id != -1) {
                    // 保存上一次使用的笔记本ID
                    Properties properties = new Properties();
                    properties.setProperty("last_notebook_id", String.valueOf(last_notebook_id));
                    try(FileOutputStream config_save = new FileOutputStream("Config.properties")){
                        properties.store(config_save, "Last Notebook ID");
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
                config_load.close();
            } catch (Exception e) {
                e.printStackTrace();
                error_stackTrace.setText(e.getMessage());
                error_stage.show();
            }
            finally{
                primaryStage.close();
            }
            
        });

    }

    public void refresh_title_list(int notebook_id, ListView<String> title_list)throws SQLException{
        try {
            ArrayList<String> titles = DB_Opearte.query_all_titles(notebook_id);
            title_list.setItems(FXCollections.observableArrayList(titles));
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
    }

    public void add_notebook_list(MenuButton menuButton, ListView<String> note_list_view)throws SQLException{
        try {
            ArrayList<Notebook> notebooks = DB_Opearte.query_all_notebooks();
            menuButton.getItems().clear();
            for (Notebook notebook : notebooks) {
                MenuItem item = new MenuItem(notebook.getName());
                menuButton.getItems().add(item);
                //对menu_item进行设置
                item.setOnAction(event -> {
                    //更新上次使用的笔记本ID
                    last_notebook_id = notebook.getId();
                    set_select_notebook_button_text(notebook.getName(),menuButton);
                    //刷新笔记列表
                    try{
                        refresh_title_list(last_notebook_id,note_list_view);
                    }catch(SQLException e){
                        e.printStackTrace();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void set_select_notebook_button_text(String notebook_name, MenuButton menuButton){
        menuButton.setText(notebook_name);
    }

    public static void main(String[] args) {
        launch(args);
    }

    
}
