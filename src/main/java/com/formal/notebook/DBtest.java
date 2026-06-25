package com.formal.notebook;


public class DBtest {

    public static void main(String[] args) {
        try {
            // 先查看所有笔记本
            DB_Opearte.create_new_title(11,"JavaFx程序基本结构");
            DB_Opearte.update_content(11,"JavaFx程序基本结构","## 一、JavaFX层级结构（从外到内四层核心组件）\n" +
                    "整体层级：`Application(应用)` → `Stage(窗口)` → `Scene(场景)` → `SceneGraph场景图(根节点+各类控件Node)`\n" +
                    "![[截屏2026-06-02 21.26.45.png]]\n" +
                    "\n" +
                    "| 层级  | 组件名称                   | 作用说明                                            |\n" +
                    "| --- | ---------------------- | ----------------------------------------------- |\n" +
                    "| 最外层 | Application（应用程序）      | JavaFX程序入口，==一个Application应用可以创建**多个Stage窗口**== |\n" +
                    "| 第二层 | Stage（窗口）              | 操作系统级别的窗体（带最大化、最小化、关闭按钮），1个Stage绑定1个Scene场景     |\n" +
                    "| 第三层 | Scene（场景）              | 承载所有UI控件的画布容器，==1个Scene只能绑定1个根Parent节点==        |\n" +
                    "| 最内层 | SceneGraph场景图（Node节点树） | 由**根节点Parent** + 若干子Node控件组成树形布局                |\n" +
                    "\n" +
                    "## 二、Node节点分类（场景图构成）\n" +
                    "1. **Parent（父容器节点）**：可以包含多个子节点，是布局容器（VBox/HBox/BorderPane等），作为Scene的根节点\n" +
                    "2. **Node（普通控件节点）**：按钮、标签、输入框等基础UI组件，挂载在Parent容器内，不能再包含子控件\n" +
                    "\n" +
                    "> 结构特点：场景图是树形嵌套结构 → 容器里可以嵌套容器，容器内摆放各类UI控件。\n" +
                    "\n" +
                    "## 三、组件对应关系总结\n" +
                    "1. `Application` : 多个`Stage`（一个应用多窗口）\n" +
                    "2. `Stage` : ==单个`Scene`（一个窗口同一时间只显示一个场景，可切换Scene）==\n" +
                    "3. `Scene` : 单个根`Parent`（一个场景仅有一个顶层根容器）\n" +
                    "4. `Parent` : 多个子`Node/Parent`（容器嵌套、摆放控件，构成场景树Scene Graph）\n" +
                    "\n" +
                    "## 四、简易代码逻辑示意\n" +
                    "```java\n" +
                    "import javafx.application.Application;\n" +
                    "import javafx.scene.Scene;\n" +
                    "import javafx.scene.control.Button; //Button位于control包下\n" +
                    "import javafx.scene.layout.BorderPane; //BorderPane位于layout包下\n" +
                    "import javafx.stage.Stage;\n" +
                    "\n" +
                    "// JavaFX 应用主类\n" +
                    "public class DemoApp extends Application {\n" +
                    "\n" +
                    "    // 1. JavaFX 入口：重写 start 方法\n" +
                    "    @Override\n" +
                    "    public void start(Stage primaryStage) {\n" +
                    "        // 创建根容器 Parent\n" +
                    "        BorderPane root = new BorderPane();\n" +
                    "        \n" +
                    "        // 添加控件 Node\n" +
                    "        root.setCenter(new Button(\"我是一个按钮\"));\n" +
                    "\n" +
                    "        // 创建场景 Scene  ⚠\uFE0F 为scene绑定容器\n" +
                    "        Scene scene = new Scene(root, 400, 300);\n" +
                    "\n" +
                    "        // 窗口绑定场景\n" +
                    "        primaryStage.setScene(scene); //⚠\uFE0F 配置好场景要为窗口绑定\n" +
                    "        primaryStage.setTitle(\"JavaFX 极简程序\");\n" +
                    "        primaryStage.show(); // 显示窗口\n" +
                    "    }\n" +
                    "\n" +
                    "    // 2. 程序主入口 main 方法\n" +
                    "    public static void main(String[] args) {\n" +
                    "        // 启动 JavaFX 应用\n" +
                    "        Application.launch(args); \n" +
                    "    }\n" +
                    "}\n" +
                    "```\n" +
                    "1. `Application.launch(args)`：**JavaFX 启动核心方法**\n" +
                    "    \n" +
                    "    - 底层自动创建`DemoApp`实例；\n" +
                    "    - 自动执行生命周期：`init()初始化 → start(Stage)创建界面 → stop()关闭程序`；\n" +
                    "    - 自动生成主窗口`primaryStage`，传入 start 方法参数；\n" +
                    "\n" +
                    "⚠\uFE0F 注意：`launch()`只能调用 1 次，重复调用报错。\n" +
                    "\n" +
                    "2. start (Stage primaryStage) 界面构建方法\n" +
                    "    运行在 JavaFX 绘图线程，所有 UI 创建代码必须写在此方法内\n" +
                    "\n" +
                    "3. 根节点`BorderPane`\n" +
                    "    ![[60078dd64d9cde88873bf27371623b54~tplv-be4g95zd3a-image.jpeg]]\n" +
                    "\n" +
                    "\n" +
                    "## 五、简图概括\n" +
                    "```\n" +
                    "Application应用\n" +
                    "├─ Stage窗口1\n" +
                    "│  └─ Scene场景\n" +
                    "│     └─ Parent根容器（场景图起点）\n" +
                    "│        ├─ Node控件1\n" +
                    "│        ├─ Parent子容器\n" +
                    "│        │   └─ Node控件2、控件3...\n" +
                    "│        └─ Node控件4\n" +
                    "└─ Stage窗口2（可选，多窗口程序）\n" +
                    "   └─ Scene场景2...\n" +
                    "```");
            


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
