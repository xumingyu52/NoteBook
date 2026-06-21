package com.formal.notebook;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
        //第四界面：异常界面
        GridPane error_scene_root = new GridPane();

        // 让整个内容距离窗口边缘有 20 像素的“呼吸空间”
        error_scene_root.setPadding(new Insets(20));
        // 让左边图标和右边表单之间横向隔开 20 像素
        error_scene_root.setHgap(25); 
        error_scene_root.setAlignment(Pos.CENTER_LEFT); // 靠左居中对齐

        InputStream error_icon = getClass().getResourceAsStream("/icons/错误_error.png");
        if (error_icon != null) {
            ImageView error_icon_view = new ImageView(new Image(error_icon));
            error_icon_view.setFitWidth(80);  // 🌟 稍微缩小一点，更精致
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

        Stage error_stage = new Stage();
        error_stage.setTitle("程序异常");
        error_stage.setScene(error_scene);
        error_stage.setResizable(false);
        error_stage.initOwner(primaryStage);
        error_stage.initModality(Modality.WINDOW_MODAL);
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
            }//后续加上笔记本主界面
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



    }

    public static void main(String[] args) {
        launch(args);
    }

    
}
