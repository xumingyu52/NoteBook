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
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class NoteBook_fx extends Application{
    private int last_notebook_id = -1;
    private Stage primaryStage;
    private Scene guide_scene;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
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
        //error_stage.setResizable(false);
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

        this.guide_scene = new Scene(guide_scene_root, 800, 500);

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
        //整体为splitpane，左侧为VBox（工具栏，笔记列表），右侧为笔记编辑区
        SplitPane main_splitpane = new SplitPane();
        main_splitpane.setPadding(new Insets(0));
        main_splitpane.setDividerPositions(0.35);
        main_splitpane.setStyle("-fx-background-color: #f8f9fa; -fx-box-border: transparent;");

        // 左侧：工具栏 + 笔记列表
        VBox left_panel = new VBox();
        left_panel.setSpacing(0);
        left_panel.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e9ecef; -fx-border-width: 0 1 0 0;");

        // 顶部工具栏：笔记本操作 + 新建笔记
        HBox left_toolbar = new HBox();
        left_toolbar.setSpacing(8);
        left_toolbar.setPadding(new Insets(12, 16, 12, 16));
        left_toolbar.setAlignment(Pos.CENTER_LEFT);
        left_toolbar.setStyle("-fx-background-color: #ffffff;");

        // 新建笔记本按钮（工具栏左侧）
        //Button new_notebook_button_main = new Button();
        //new_notebook_button_main.setTooltip(new javafx.scene.control.Tooltip("新建笔记本"));
        //setIcon(new_notebook_button_main, "/icons/笔记本_notebook-one.png", 20);
        //new_notebook_button_main.setStyle(getToolbarButtonStyle());

        javafx.scene.layout.Region toolbar_spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(toolbar_spacer, Priority.ALWAYS);

        // 新建笔记按钮（工具栏右侧）
        Button new_note_button = new Button();
        new_note_button.setTooltip(new javafx.scene.control.Tooltip("新建笔记"));
        setIcon(new_note_button, "/icons/文件添加_file-addition.png", 20);
        new_note_button.setStyle(getToolbarButtonStyle());

        left_toolbar.getChildren().addAll(
            //new_notebook_button_main,
            //toolbar_spacer,
            new_note_button
        );

        // 笔记本选择下拉菜单
        HBox notebook_selector_bar = new HBox();
        notebook_selector_bar.setPadding(new Insets(0, 16, 8, 16));
        notebook_selector_bar.setAlignment(Pos.CENTER_LEFT);
        notebook_selector_bar.setStyle("-fx-background-color: #ffffff;");
        MenuButton select_notebook_button = new MenuButton("选择笔记本");
        select_notebook_button.setStyle("-fx-background-color: transparent; -fx-font-size: 13px; -fx-text-fill: #495057;");
        notebook_selector_bar.getChildren().add(select_notebook_button);

        // 笔记列表
        ListView<String> note_list_view = new ListView<>();
        Label empty_title = new Label("暂无笔记");
        empty_title.setStyle("-fx-text-fill: #adb5bd; -fx-font-style: italic;");
        note_list_view.setPlaceholder(empty_title);
        note_list_view.setPrefWidth(300);
        note_list_view.setStyle("-fx-background-color: #ffffff; -fx-border-color: transparent; -fx-padding: 8;");
        VBox.setVgrow(note_list_view, Priority.ALWAYS);

        // 笔记列表右键菜单
        ContextMenu note_context_menu = new ContextMenu();
        note_context_menu.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e9ecef;");
        MenuItem rename_note_item = new MenuItem("重命名笔记");
        MenuItem delete_note_item = new MenuItem("删除笔记");
        note_context_menu.getItems().addAll(rename_note_item, delete_note_item);
        note_list_view.setContextMenu(note_context_menu);

        // 右键菜单事件：重命名笔记
        rename_note_item.setOnAction(event -> {
            String selectedTitle = note_list_view.getSelectionModel().getSelectedItem();
            if (selectedTitle == null || selectedTitle.isEmpty()) return;
            TextInputDialog rename_dialog = new TextInputDialog(selectedTitle);
            rename_dialog.setTitle("重命名笔记");
            rename_dialog.setHeaderText(null);
            rename_dialog.setContentText("请输入新的笔记标题：");
            rename_dialog.showAndWait().ifPresent(newTitle -> {
                if (!newTitle.trim().isEmpty() && !newTitle.equals(selectedTitle)) {
                    try {
                        DB_Opearte.update_title(last_notebook_id, selectedTitle, newTitle.trim());
                        refresh_title_list(last_notebook_id, note_list_view);
                    } catch (SQLException e) {
                        e.printStackTrace();
                        error_stackTrace.setText(e.getMessage());
                        error_stage.show();
                    }
                }
            });
        });

        // 右键菜单事件：删除笔记
        delete_note_item.setOnAction(event -> {
            String selectedTitle = note_list_view.getSelectionModel().getSelectedItem();
            if (selectedTitle == null || selectedTitle.isEmpty()) return;
            try {
                DB_Opearte.delete_title(last_notebook_id, selectedTitle);
                refresh_title_list(last_notebook_id, note_list_view);
            } catch (SQLException e) {
                e.printStackTrace();
                error_stackTrace.setText(e.getMessage());
                error_stage.show();
            }
        });

        left_panel.getChildren().addAll(left_toolbar, notebook_selector_bar, note_list_view);

        // 右侧：笔记编辑区
        VBox right_panel = new VBox();
        right_panel.setStyle("-fx-background-color: #ffffff;");
        right_panel.setPrefWidth(500);

        // 创建 Typora 编辑器
        TyporaEditorNode editor = new TyporaEditorNode("");
        VBox.setVgrow(editor, Priority.ALWAYS);
        right_panel.getChildren().add(editor);

        // 记录当前编辑器打开的是哪个标题（用于切换前保存）
        final String[] currentEditingTitle = {null};

        // 笔记列表点击事件：打开笔记内容到编辑器
        note_list_view.setOnMouseClicked(event -> {
            String selectedTitle = note_list_view.getSelectionModel().getSelectedItem();
            if (selectedTitle == null || selectedTitle.isEmpty()) return;
            // 如果点击的还是同一个标题，不重复加载
            if (selectedTitle.equals(currentEditingTitle[0])) return;

            // 先保存当前编辑的内容
            editor.saveNow();

            try {
                Title_and_Content content = DB_Opearte.query_title_and_content(last_notebook_id, selectedTitle);
                if (content != null) {
                    editor.setNoteInfo(last_notebook_id, selectedTitle);
                    editor.setMarkdown(content.getContent());
                    currentEditingTitle[0] = selectedTitle;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                error_stackTrace.setText(e.getMessage());
                error_stage.show();
            }
        });

        main_splitpane.getItems().addAll(left_panel, right_panel);
        Scene main_scene = new Scene(main_splitpane, 900, 600);
        
        //-----------------------------------------------------------------//
        //第五界面：新建笔记界面

        // 第五界面用于新的stage，创建笔记
        GridPane new_note_scene_root = new GridPane();

        // 让整个内容距离窗口边缘有 20 像素的“呼吸空间”
        new_note_scene_root.setPadding(new Insets(20));
        // 让左边图标和右边表单之间横向隔开 20 像素
        new_note_scene_root.setHgap(25);
        new_note_scene_root.setAlignment(Pos.CENTER_LEFT); // 靠左居中对齐

        InputStream new_note_icon = getClass().getResourceAsStream("/icons/笔记本_notebook-one.png");
        if (new_note_icon != null) {
            ImageView new_note_icon_view = new ImageView(new Image(new_note_icon));
            new_note_icon_view.setFitWidth(80);  // 🌟 稍微缩小一点，更精致
            new_note_icon_view.setFitHeight(80);
            new_note_scene_root.add(new_note_icon_view, 0, 0);
        }

        VBox note_label_and_textarea = new VBox();
        // 控制右侧内部的整体垂直间距
        note_label_and_textarea.setSpacing(10);

        Label new_note_label = new Label("新建笔记");
        new_note_label.setStyle("-fx-font-weight: bold; -fx-font-size: 20px;"); // 用CSS改字号更方便

        Label note_tip_label = new Label("请不要输入已有的笔记名！不可超过50个字");
        note_tip_label.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;"); // 把提示文字改成高级灰，突出重点

        // 把大卡、粗鲁的 TextArea 换成优雅的单行 TextField
        TextField note_name_field = new TextField();
        note_name_field.setPromptText("请输入笔记名称..."); // 增加灰色占位提示词
        note_name_field.setPrefWidth(260); // 调整更合适的宽度
        //确认按钮Hbox右对齐
        HBox note_confirm_button_hbox = new HBox();
        note_confirm_button_hbox.setAlignment(Pos.CENTER_RIGHT);
        Button note_confirm_button = new Button(" 创建 ");
        note_confirm_button.setStyle("-fx-background-color: #bcf3d3ff; -fx-text-fill: black; -fx-font-weight: bold;"
                                +"-fx-background-radius: 32px; -fx-border-radius: 32px;");
        note_confirm_button_hbox.getChildren().add(note_confirm_button);

        // 按顺序塞入右侧
        note_label_and_textarea.getChildren().addAll(new_note_label, note_tip_label, note_name_field, note_confirm_button_hbox);

        new_note_scene_root.add(note_label_and_textarea, 1, 0);

        // 🌟 5. 稍微宽裕一点的舞台尺寸，让布局展开
        Scene new_note_scene = new Scene(new_note_scene_root, 420, 160);

        //-------------------------------------------------------------------//
        //stage配置区
        //-------------------------------------------------------------------//
        primaryStage.setScene(guide_scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(500);
        
        // 窗口关闭逻辑（保存配置 + 保存笔记 + 退出程序）在下方统一处理
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
        
        //new_note_stage.show();
        Stage new_note_stage = new Stage();
        new_note_stage.setTitle("新建笔记");
        new_note_stage.setScene(new_note_scene);
        new_note_stage.setResizable(false);
        new_note_stage.initOwner(primaryStage);
        new_note_stage.initModality(Modality.WINDOW_MODAL);
        new_note_stage.setOnCloseRequest(event -> {
            note_name_field.clear();
        });

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
                this.last_notebook_id = Integer.parseInt(properties.getProperty("last_notebook_id"));
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
                //刷新笔记本下拉菜单（让新笔记本出现）
                add_notebook_list(select_notebook_button, note_list_view);
                // 创建后重新显示当前笔记本名（add_notebook_list 会 clear，需恢复）
                set_select_notebook_button_text(notebook_name, select_notebook_button);
                primaryStage.setScene(main_scene);
                new_notebook_stage.close();
            }catch(SQLException e){
                error_stackTrace.setText(e.getMessage());
                error_stage.show();
                e.printStackTrace();
            }
        });

        // 将 new_notebook_stage 存入 MenuButton 的 userData，供 add_notebook_list 内部的"新建笔记本"项使用
        select_notebook_button.setUserData(new_notebook_stage);

        // 创建成功后也刷新笔记本菜单列表（让新笔记本出现在下拉里）
        // （已在 confirm_button.setOnAction 末尾追加刷新逻辑）

        //添加笔记本到菜单（含"新建笔记本"首项，右键重命名/删除）
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
                // 保存当前编辑的笔记
                if (editor != null) {
                    editor.shutdown();
                }
                primaryStage.close();
                javafx.application.Platform.exit();
                System.exit(0);
            }
            
        });
        //--------------------------------------------------------------//
        //新建笔记的判断逻辑
        //输入框实时监听是否笔记名重复
        //设置提示label
        Label note_warning_label_1 = new Label("笔记名最多50个字符");
        note_warning_label_1.setStyle("-fx-font-size: 11px; -fx-text-fill: #ff4d4f;"); // 把提示文字改成红色
        note_warning_label_1.setWrapText(true);
        note_label_and_textarea.getChildren().add(3, note_warning_label_1);
        note_warning_label_1.setManaged(false);

        Label note_warning_label_2 = new Label("笔记名已存在");
        note_warning_label_2.setStyle("-fx-font-size: 11px; -fx-text-fill: #ff4d4f;"); // 把提示文字改成红色
        note_warning_label_2.setWrapText(true);
        note_label_and_textarea.getChildren().add(4, note_warning_label_2);
        note_warning_label_2.setManaged(false);

        Label note_empty_warnning = new Label("笔记名不能为空");
        note_empty_warnning.setStyle("-fx-font-size: 11px; -fx-text-fill: #ff4d4f;"); // 把提示文字改成红色
        note_empty_warnning.setWrapText(true);
        note_label_and_textarea.getChildren().add(5, note_empty_warnning);
        note_empty_warnning.setManaged(false);

        new_note_button.setOnAction(event -> {
            new_note_stage.show();
        });
        note_name_field.textProperty().addListener((obs, oldValue, newValue) -> {
            //限制字数逻辑
            int max_length = 50;
            //如果输入的字符数超过最大长度且不为空字符串
            if(newValue.length() > max_length && !newValue.isEmpty()){
                String truncated_name = newValue.substring(0, max_length);
                int postition = note_name_field.getCaretPosition();
                note_name_field.setText(truncated_name);
                note_name_field.positionCaret(Math.min(postition, max_length));
                //显示警告标签
                note_warning_label_1.setVisible(true);
                note_warning_label_1.setManaged(true);
            }else{
                //隐藏警告标签
                note_warning_label_1.setVisible(false);
                note_warning_label_1.setManaged(false);
            }

            //判断是否重复
            try{
                boolean is_exists = DB_Opearte.is_title_exists(last_notebook_id, newValue);
                if(is_exists){
                    note_warning_label_2.setVisible(true);
                    note_warning_label_2.setManaged(true);
                }else{
                    note_warning_label_2.setVisible(false);
                    note_warning_label_2.setManaged(false);
                }
            }catch(SQLException e){
                error_stackTrace.setText(e.getMessage());
                error_stage.show();
                e.printStackTrace();
            }
        });

        note_confirm_button.setOnAction(event ->{
            String note_name = note_name_field.getText();
            //判断是否为空
            if(note_name.isEmpty()){
                note_empty_warnning.setVisible(true);
                note_empty_warnning.setManaged(true);
                return;
            }
            // 检查是否已选择笔记本
            if (last_notebook_id <= 0) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.WARNING,
                    "请先选择一个笔记本！"
                );
                alert.setTitle("提示");
                alert.setHeaderText(null);
                alert.showAndWait();
                return;
            }
            //判断是否重复
            //判断是否重复
            try{
                boolean is_exists = DB_Opearte.is_title_exists(last_notebook_id, note_name);
                if(is_exists){
                    note_warning_label_2.setVisible(true);
                    note_warning_label_2.setManaged(true);
                }else{
                    note_warning_label_2.setVisible(false);
                    note_warning_label_2.setManaged(false);
                }
            }catch(SQLException e){
                error_stackTrace.setText(e.getMessage());
                error_stage.show();
                e.printStackTrace();
            }
            //创建笔记
            try{
                // 先保存当前编辑的内容
                System.out.println("[DEBUG] 新建笔记前 saveNow, currentEditingTitle=" + currentEditingTitle[0]);
                editor.saveNow();

                System.out.println("[DEBUG] 创建笔记: notebook_id=" + last_notebook_id + ", title=" + note_name);
                DB_Opearte.create_new_title(last_notebook_id, note_name);

                //刷新笔记列表
                refresh_title_list(last_notebook_id, note_list_view);

                // 滚动到新创建的笔记并选中
                note_list_view.getSelectionModel().select(note_name);
                note_list_view.scrollTo(note_name);

                // 初始化编辑器：设置笔记信息并清空内容（新建笔记为空）
                editor.setNoteInfo(last_notebook_id, note_name);
                editor.setMarkdown("# 请输入标题");
                currentEditingTitle[0] = note_name;
                System.out.println("[DEBUG] 新建笔记完成: title=" + note_name + ", currentEditingTitle=" + currentEditingTitle[0]);

                primaryStage.setScene(main_scene);
                new_note_stage.close();
            }catch(SQLException e){
                error_stackTrace.setText(e.getMessage());
                error_stage.show();
                e.printStackTrace();
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

        public void add_notebook_list(MenuButton menuButton, ListView<String> note_list_view) throws SQLException {
        try {
            ArrayList<Notebook> notebooks = DB_Opearte.query_all_notebooks();
            menuButton.getItems().clear();

            // ① 始终保留"新建笔记本"作为第一项
            MenuItem new_notebook_item_refresh = new MenuItem("✚ 新建笔记本");
            new_notebook_item_refresh.setOnAction(event -> {
                // 触发外部已存储的新建笔记本 stage，通过 userData 传递
                Object stageObj = menuButton.getUserData();
                if (stageObj instanceof Stage) {
                    ((Stage) stageObj).show();
                }
            });
            menuButton.getItems().add(new_notebook_item_refresh);

            // ② 用 CustomMenuItem 包裹真实 Label Node，使右键可正常触发
            for (Notebook notebook : notebooks) {

                // 右键上下文菜单：重命名 / 删除
                ContextMenu notebook_ctx = new ContextMenu();

                MenuItem ctx_rename = new MenuItem("重命名笔记本");
                ctx_rename.setOnAction(event -> {
                    TextInputDialog dlg = new TextInputDialog(notebook.getName());
                    dlg.setTitle("重命名笔记本");
                    dlg.setHeaderText(null);
                    dlg.setContentText("请输入新的笔记本名称：");
                    dlg.showAndWait().ifPresent(newName -> {
                        String trimmed = newName.trim();
                        if (trimmed.isEmpty() || trimmed.equals(notebook.getName())) return;
                        try {
                            if (DB_Opearte.is_notebook_name_exists(trimmed)) {
                                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                                    javafx.scene.control.Alert.AlertType.WARNING,
                                    "笔记本名称\"" + trimmed + "\"已存在，请换一个名称。"
                                );
                                alert.setHeaderText(null);
                                alert.showAndWait();
                                return;
                            }
                            DB_Opearte.update_notebook_name(notebook.getId(), trimmed);
                            if (notebook.getId() == last_notebook_id) {
                                set_select_notebook_button_text(trimmed, menuButton);
                            }
                            add_notebook_list(menuButton, note_list_view);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });
                });

                MenuItem ctx_delete = new MenuItem("删除笔记本");
                ctx_delete.setOnAction(event -> {
                    javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.CONFIRMATION,
                        "确定要删除笔记本\"" + notebook.getName() + "\"及其所有笔记吗？此操作不可撤销。",
                        javafx.scene.control.ButtonType.YES,
                        javafx.scene.control.ButtonType.NO
                    );
                    confirm.setTitle("删除笔记本");
                    confirm.setHeaderText(null);
                    confirm.showAndWait().ifPresent(btn -> {
                        if (btn != javafx.scene.control.ButtonType.YES) return;
                        try {
                            DB_Opearte.delete_notebook(notebook.getId());
                            if (notebook.getId() == last_notebook_id) {
                                last_notebook_id = -1;
                                menuButton.setText("选择笔记本");
                                note_list_view.getItems().clear();
                            }
                            add_notebook_list(menuButton, note_list_view);
                            // 检查是否还有笔记本，如果没有则回到引导界面
                            if (menuButton.getItems().size() <= 1) { // 只有"新建笔记本"项
                                primaryStage.setScene(guide_scene);
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });
                });

                notebook_ctx.getItems().addAll(ctx_rename, ctx_delete);

                // CustomMenuItem 内包真实 Node，鼠标事件完整可用
                javafx.scene.control.Label item_label = new javafx.scene.control.Label(notebook.getName());
                item_label.setPrefWidth(200);
                item_label.setPadding(new Insets(2, 4, 2, 4));

                // 左键：切换笔记本
                item_label.setOnMouseClicked(e -> {
                    if (e.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                        last_notebook_id = notebook.getId();
                        set_select_notebook_button_text(notebook.getName(), menuButton);
                        menuButton.hide();
                        try {
                            refresh_title_list(last_notebook_id, note_list_view);
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                });

                // 右键：弹出重命名/删除菜单
                item_label.setOnContextMenuRequested(e -> {
                    //menuButton.hide();
                    notebook_ctx.show(item_label, e.getScreenX(), e.getScreenY());
                    e.consume();
                });

                javafx.scene.control.CustomMenuItem custom_item =
                    new javafx.scene.control.CustomMenuItem(item_label, false);
                custom_item.setHideOnClick(false);

                menuButton.getItems().add(custom_item);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void set_select_notebook_button_text(String notebook_name, MenuButton menuButton){
        menuButton.setText(notebook_name);
    }

    // 辅助方法：为按钮设置图标
    private void setIcon(Button button, String iconPath, int size) {
        try (InputStream is = getClass().getResourceAsStream(iconPath)) {
            if (is != null) {
                ImageView iconView = new ImageView(new Image(is));
                iconView.setFitWidth(size);
                iconView.setFitHeight(size);
                button.setGraphic(iconView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 辅助方法：获取工具栏按钮统一样式
    private String getToolbarButtonStyle() {
        return "-fx-background-color: transparent; "
             + "-fx-padding: 6; "
             + "-fx-cursor: hand; "
             + "-fx-background-radius: 6px; "
             + "-fx-border-radius: 6px; "
             + "-fx-border-color: transparent;";
    }

    public static void main(String[] args) {
        launch(args);
    }

    
}
