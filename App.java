package com.csc190.finalproject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.stream.Stream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Scanner;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Polyline;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class App extends Application{
  public static final double WIDTH = 960;
  public static final double HEIGHT = 540;
  private void handle(KeyEvent e, Graph g, TextField equation,TextField minX,TextField maxX,TextField minY,TextField maxY, TextField deltaX){
    if(e == null || e.getCode() != KeyCode.ENTER) return;
    try{
      String eq = equation.getText();
      if(eq.equals("")) throw new Exception("Empty Equation");
      double mnx = Double.valueOf(minX.getText());
      double mxx = Double.valueOf(maxX.getText());
      double mny = Double.valueOf(minY.getText());
      double mxy = Double.valueOf(maxY.getText());
      double dx = Double.valueOf(deltaX.getText());
      g.updateSpecs(eq,mnx,mxx,mny,mxy,dx);
    } catch (NumberFormatException ex) {
      //Do nothing
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  @Override
  public void start(Stage stage){
    stage.setTitle("Final Project — Graph");
    Graph graph = new Graph();
    BorderPane root = new BorderPane(graph);
    GridPane controls = new GridPane();
    controls.setVgap(15);
    controls.setHgap(10);
//    FileChooser
    FileChooser fileChooser = new FileChooser();
    fileChooser.setInitialDirectory(new File(".\\graphs"));
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files", "*.dat"));
//    Make TextFields
    TextField equationField = new TextField();
    TextField minXField = new TextField();
    TextField maxXField = new TextField();
    TextField minYField = new TextField();
    TextField maxYField = new TextField();
    TextField deltaXField = new TextField();
//    Add EventListeners
    equationField.setOnKeyReleased(e->handle(e,graph,equationField,minXField,maxXField,minYField,maxYField,deltaXField));
    minXField.setOnKeyReleased(e->handle(e,graph,equationField,minXField,maxXField,minYField,maxYField,deltaXField));
    maxXField.setOnKeyReleased(e->handle(e,graph,equationField,minXField,maxXField,minYField,maxYField,deltaXField));
    minYField.setOnKeyReleased(e->handle(e,graph,equationField,minXField,maxXField,minYField,maxYField,deltaXField));
    maxYField.setOnKeyReleased(e->handle(e,graph,equationField,minXField,maxXField,minYField,maxYField,deltaXField));
    deltaXField.setOnKeyReleased(e->handle(e,graph,equationField,minXField,maxXField,minYField,maxYField,deltaXField));
//    Add TextFields
    controls.add(equationField, 1, 0);
    controls.add(minXField, 1, 1);
    controls.add(maxXField, 1, 2);
    controls.add(minYField, 1, 3);
    controls.add(maxYField, 1, 4);
    controls.add(deltaXField, 1, 5);
//    Add Open
    var openFileBtn = new Button("Open");
    openFileBtn.setOnAction(e->{
      try{
        fileChooser.setTitle("Open Graph File");
        File file = fileChooser.showOpenDialog(null);
        if(file == null) return;
        var scanner = new Scanner(file);
        ArrayList<String> lines = new ArrayList<>();
        while(scanner.hasNextLine())
          lines.add(scanner.nextLine());
        scanner.close();
        ArrayList<Number> points = new ArrayList<>();
        try(Stream<String> stream = lines.stream()){
          stream.filter(line->line.contains(",")).forEach(pair->{
            for(String number : pair.split(","))
              points.add(Double.valueOf(number));
          });
        } catch (Exception ex) {
          System.out.println(ex.getClass().getName());
        }
        graph.setSpecs("",0,0,0,0,0);
        ArrayList<Object> list = new ArrayList<>();
        TextField[] fields = {minXField, maxXField, minYField, maxYField, deltaXField, equationField};
        for(int i = 0; i < fields.length; fields[i].setText(lines.get(i)), i++);
//        graph.setSpecs(lines.get(5),lines.get(0),lines.get(1),lines.get(2),lines.get(3),lines.get(4),points.toArray(new Number[0]));
        String eq = equationField.getText();
        if(eq.equals("")) throw new Exception("Empty equation field");
        double mnx = Double.valueOf(minXField.getText());
        double mxx = Double.valueOf(maxXField.getText());
        double mny = Double.valueOf(minYField.getText());
        double mxy = Double.valueOf(maxYField.getText());
        double dx = Double.valueOf(deltaXField.getText());
        if(points.size() == 0){
          graph.setSpecs(eq,mnx,mxx,mny,mxy,dx);
          graph.updatePoints();
          graph.plot();
        } else {
          graph.updateSpecs(eq, minXField.getText(), maxXField.getText(), minYField.getText(), maxYField.getText(), deltaXField.getText(), points.toArray(new Number[0]));
        }
      } catch (FileNotFoundException ex){
        System.out.println(ex);
        Stage errorPopup = new Stage();
        errorPopup.setTitle("Error");
        var text = new Text("Unable to access File");
        text.setFont(new Font(20));
        var pane = new StackPane(text);
        errorPopup.setScene(new Scene(pane,250,175));
        errorPopup.show();
        } catch (Exception ex){
        System.out.println(ex);
        Stage errorPopup = new Stage();
        errorPopup.setTitle("Error");
        var text = new Text("Unable to access File");
        text.setFont(new Font(20));
        var pane = new StackPane(text);
        errorPopup.setScene(new Scene(pane,250,175));
        errorPopup.show();
      }
    });
    var saveFileBtn = new Button("Save");
    saveFileBtn.setOnAction(e->{
      try{
        fileChooser.setTitle("Save Graph File");
        File file = fileChooser.showSaveDialog(null);
        if(file == null) return;
        file.createNewFile();
        var writer = new FileWriter(file);
        writer.write(graph.getMinX() + "\n" + graph.getMaxX() + "\n" + graph.getMinY() + "\n" + graph.getMaxY() + "\n" + graph.getDeltaX() + "\n"+ graph.getEquation());
        Number[] points = graph.getPoints();
        for(int i = 0; i < points.length; writer.write("\n" + points[i] + "," + points[i+1]), i += 2);
        writer.close();
      } catch(Exception ex){
        ex.printStackTrace();
      }
    });
    HBox fileButtons = new HBox(15,openFileBtn,saveFileBtn);
    HBox.setHgrow(openFileBtn,Priority.ALWAYS);
    HBox.setHgrow(saveFileBtn,Priority.ALWAYS);
    openFileBtn.setMaxWidth(App.WIDTH);
    saveFileBtn.setMaxWidth(App.WIDTH);
    controls.add(fileButtons,1,6);
    
    var howToBtn = new Button("How To Use App");
    howToBtn.setOnAction(e->{
      new HowToApp().start(new Stage());
    });
    GridPane.setFillWidth(howToBtn, true);
    GridPane.setFillHeight(howToBtn, true);
    howToBtn.setMaxWidth(App.WIDTH);
    controls.add(howToBtn, 1, 7);
    
//    Make labels
    Label equationLabel = new Label("Equation: (y=)");
    Label minXLabel = new Label("MinX =");
    Label maxXLabel = new Label("MaxX =");
    Label minYLabel = new Label("MinY =");
    Label maxYLabel = new Label("MaxY =");
    Label deltaXLabel = new Label("Δx =");
//    Bind Labels
    equationLabel.setLabelFor(equationField);
    minXLabel.setLabelFor(minXField);
    maxXLabel.setLabelFor(maxXField);
    minYLabel.setLabelFor(minYField);
    maxYLabel.setLabelFor(maxYField);
    deltaXLabel.setLabelFor(deltaXField);
//    Add Labels
    controls.add(equationLabel, 0, 0);
    controls.add(minXLabel, 0, 1);
    controls.add(maxXLabel, 0, 2);
    controls.add(minYLabel, 0, 3);
    controls.add(maxYLabel, 0, 4);
    controls.add(deltaXLabel, 0, 5);
    var tableBtn = new Button("Table");
    tableBtn.setOnAction(e->{
      new TableApp().start(new Stage(),graph);
    });
    controls.add(tableBtn, 0, 6);
    controls.setPadding(new Insets(10));
    root.setRight(controls);
    Scene scene = new Scene(root,WIDTH,HEIGHT);
    scene.setOnKeyPressed(e -> {
      if(e.isControlDown()){
        switch(e.getCode()){
          case R:
            stage.close();
            Platform.runLater( () -> new App().start( new Stage() ) );
            break;
          case W:
            Platform.exit();
        }
      }
    } );
    stage.setScene(scene);
    stage.show();
  }
  public static void main(String[] args){
    launch(args);
  }
}

class HowToApp extends Application implements EventHandler<ActionEvent>{
  private Text content;
  public HowToApp(){
    this.content = new Text("This application is here as a guide for using the graphing application");
    this.content.setFont(new Font(13));
  }
  @Override
  public void start(Stage stage){
    stage.setTitle("");
    var titleText = new Text("How To Use App");
    titleText.setTextAlignment(TextAlignment.CENTER);
    titleText.setFont(Font.font(25));
    StackPane title = new StackPane(titleText);
    title.setAlignment(Pos.CENTER);
    StackPane pane = new StackPane(this.content);
    pane.setPadding(new Insets(7));
    pane.setAlignment(Pos.TOP_LEFT);
    BorderPane bp = new BorderPane(pane);
    Button domainBtn = new Button("Domain");
    Button rangeBtn = new Button("Range");
    Button deltaXBtn = new Button("Δx");
    Button opsBtn = new Button("Supported Operations");
    Button fileBtn = new Button("Opening and Saving Files");
    VBox navbar = new VBox(10,domainBtn,rangeBtn,deltaXBtn,opsBtn,fileBtn);
    for(Object child : navbar.getChildren()){
      if(child instanceof Button){
        ((Button)child).setOnAction(this);
        ((Button)child).setMaxWidth(Double.MAX_VALUE);
      }
    }
    bp.setTop(title);
    bp.setLeft(navbar);
    Scene scene = new Scene(bp,App.WIDTH*3/4,App.HEIGHT*5/6);
    stage.setScene(scene);
    stage.show();
  }
  @Override
  public void handle(ActionEvent e){
    String text = "";
    switch(((Button)e.getSource()).getText()){
      case "Domain":
        text = "The minX and maxX fields control the window and the domain of points being calculated.";
        break;
      case "Range":
        text = "The minY and maxY fields control the graph window and scale the graph accordingly.";
        break;
      case "Δx":
        text = "Δx is the change in x between two points being graphed.";
        break;
      case "Supported Operations":
        String[] operations = {"Addition (+)", "Subtraction (-)", "Multiplication (*)", "Division (/)", "Exponentiation (^)", "Factorials (!)"};
        for(int i = 0; i < operations.length; operations[i] = "• " + operations[i], i++);
        text = "The application's equation field accepts the following operations:\n"+ String.join("\n",operations);
        break;
      case "Opening and Saving Files":
        text = "The 'Open' button is opens a menu for you to select and display a previously saved graph.\n\nThe 'Save' button saves the current graph to a file of your choice.";
        break;
    }
    this.content.setText(text);
  }
}

class Graph extends Pane{
  private static DecimalFormat formatter = new DecimalFormat("0.0000");
  private double scalerY;
  private double scalerX;
  private double minX;
  private double minY;
  private double maxY;
  private double maxX;
  private double deltaX;
  private String equation;
  private Number[] points;
  private double majorlines = 5;
  private double minorlines = 1;
  private static final double axisWeight = 3;
  private static final double majorWeight = 1;
  private static final double minorWeight = 0.1;
  public Graph(){}
  public Graph(String equation, double minX, double maxX, double minY, double maxY, double deltaX){
    this.setEquation(equation);
    this.setMinX(minX);
    this.setMaxX(maxX);
    this.setMinY(minY);
    this.setMaxY(maxY);
    this.setDeltaX(deltaX);
    this.updateScalerX();
    this.updateScalerY();
    this.updatePoints();
  }
  public void updateScalers(){
    this.updateScalerX();
    this.updateScalerY();
  }
  public void updateScalerX(){
    this.scalerX = App.HEIGHT / (this.maxX-this.minX);
  }
  public void updateScalerY(){
    this.scalerY = App.HEIGHT / (this.maxY-this.minY);
  }
  public void setEquation(String eq){
    this.equation = eq;
    this.updateScalers();
  }
  public void setMinX(double minX){
    this.minX = minX;
    this.updateScalers();
  }
  public void setMaxX(double maxX){
    this.maxX = maxX;
    this.updateScalers();
  }
  public void setMinY(double minY){
    this.minY = minY;
    this.updateScalers();
  }
  public void setMaxY(double maxY){
    this.maxY = maxY;
    this.updateScalers();
  }
  public void setDeltaX(double deltaX){
    this.deltaX = deltaX;
  }
  public void setPoints(Number[] points){
    this.points = points;
  }
  public String getEquation(){
    return this.equation;
  }
  public double getMinX(){
    return this.minX;
  }
  public double getMaxX(){
    return this.maxX;
  }
  public double getMinY(){
    return this.minY;
  }
  public double getMaxY(){
    return this.maxY;
  }
  public double getDeltaX(){
    return this.deltaX;
  }
  public Number[] getPoints(){
    return this.points;
  }
  private static double format(double n, DecimalFormat format){
    return Double.valueOf(format.format(n));
  }
  public void updatePoints(){
    var mathParser = new ParseMath(this.equation);
    ArrayList<Number> points = new ArrayList<>();
    try{
      for(double x = this.minX; x <= this.maxX; x += this.deltaX){
        points.add(format(x,formatter));
        points.add(mathParser.forX(format(x,formatter)));
      }
    } catch(ParseException ex) {
      points = new ArrayList<>();
      ex.printStackTrace();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    this.points = points.toArray(new Number[0]);
  }
  private void mapPointsToLine(Polyline line, Number[] points){
    for(int i = 0; i < points.length; i++)
      if(i % 2 == 0)
        line.getPoints().add((points[i].doubleValue()-this.minX)*this.scalerX);
      else
        line.getPoints().add((this.maxY-points[i].doubleValue())*this.scalerY);
  }
  private void paint(){
    this.getChildren().clear();
//    Make line
    var line = new Polyline();
    mapPointsToLine(line,this.points);
//    Make axes
    var xaxis = new Polyline();
    xaxis.setStrokeWidth(axisWeight);
    mapPointsToLine(xaxis,new Double[]{this.minX,0.0,this.maxX,0.0});
    var yaxis = new Polyline();
    yaxis.setStrokeWidth(axisWeight);
    mapPointsToLine(yaxis,new Double[]{0.0,this.minY,0.0,this.maxY});
//    TODO: Add major and minor lines
    var border = new Polyline();
    border.setStrokeWidth(majorWeight);
    mapPointsToLine(border, new Double[]{this.maxX,this.minY,this.maxX,this.maxY});
    this.getChildren().add(border);
    Group majorLinesY = new Group();
    {
      for(double i = 0; i <= this.maxX; i += this.majorlines){
        var majorline = new Polyline();
        majorline.setStrokeWidth(majorWeight);
        mapPointsToLine(majorline,new Double[]{i,this.minY,i,this.maxY});
        majorLinesY.getChildren().add(majorline);
      }
      for(double i = 0; i >= this.minX; i -= this.majorlines){
        var majorline = new Polyline();
        majorline.setStrokeWidth(majorWeight);
        mapPointsToLine(majorline, new Double[]{i,this.minY,i,this.maxY});
        majorLinesY.getChildren().add(majorline);
      }
    }
    Group majorLinesX = new Group();
    {
      for(double i = 0; i <= this.maxY; i += this.majorlines){
        var majorline = new Polyline();
        majorline.setStrokeWidth(majorWeight);
        mapPointsToLine(majorline, new Double[]{this.minX,i,this.maxX,i});
        majorLinesX.getChildren().add(majorline);
      }
      for(double i = 0; i >= this.minY; i -= this.majorlines){
        var majorline = new Polyline();
        majorline.setStrokeWidth(majorWeight);
        mapPointsToLine(majorline, new Double[]{this.minX,i,this.maxX,i});
        majorLinesX.getChildren().add(majorline);
      }
    }
    Group minorLinesY = new Group();
    {
      for(double i = 0; i <= this.maxX; i += this.minorlines){
        var minorline = new Polyline();
        minorline.setStrokeWidth(minorWeight);
        mapPointsToLine(minorline,new Double[]{i,this.minY,i,this.maxY});
        minorLinesY.getChildren().add(minorline);
      }
      for(double i = 0; i >= this.minX; i -= this.minorlines){
        var minorline = new Polyline();
        minorline.setStrokeWidth(minorWeight);
        mapPointsToLine(minorline, new Double[]{i,this.minY,i,this.maxY});
        minorLinesY.getChildren().add(minorline);
      }
    }
    Group minorLinesX = new Group();
    {
      for(double i = 0; i <= this.maxY; i += this.minorlines){
        var minorline = new Polyline();
        minorline.setStrokeWidth(minorWeight);
        mapPointsToLine(minorline, new Double[]{this.minX,i,this.maxX,i});
        minorLinesX.getChildren().add(minorline);
      }
      for(double i = 0; i >= this.minY; i -= this.minorlines){
        var minorline = new Polyline();
        minorline.setStrokeWidth(minorWeight);
        mapPointsToLine(minorline, new Double[]{this.minX,i,this.maxX,i});
        minorLinesX.getChildren().add(minorline);
      }
    }
    this.getChildren().addAll(minorLinesX,majorLinesX,minorLinesY,majorLinesY);
//    Add axis
    this.getChildren().addAll(xaxis,yaxis);
    //Add main line
    this.getChildren().add(line);
  }
  public void plot(){
    this.updatePoints();
    this.paint();
  }
  public void setSpecs(String equation, double minX, double maxX, double minY, double maxY, double deltaX){
    this.setEquation(equation);
    this.setMinX(minX);
    this.setMaxX(maxX);
    this.setMinY(minY);
    this.setMaxY(maxY);
    this.setDeltaX(deltaX);
  }
  public void updateSpecs(String equation, double minX, double maxX, double minY, double maxY, double deltaX){
    this.setSpecs(equation, minX, maxX, minY, maxY, deltaX);
    this.plot();
  }
  public void updateSpecs(String equation, String minX, String maxX, String minY, String maxY, String deltaX, Number[] points){
    this.setPoints(points);
    this.updateSpecs(equation, Double.valueOf(minX), Double.valueOf(maxX), Double.valueOf(minY), Double.valueOf(maxY), Double.valueOf(deltaX));
  }
}

class ReusableText{
  private Text text;
  private Font font;
  public ReusableText(Font f){
    this.font = f;
  }
  public Font getFont(){
    return this.font;
  }
  public void setFont(Font f){
    this.font = f;
  }
  public Text withText(String s){
    this.text = new Text();
    this.text.setFont(this.font);
    return this.text;
  }
  public Text withText(){
    return this.withText(this.text != null ? this.text.getText() : "");
  }
}

class Table extends Graph{
  private Graph graph;
  public Table(Graph graph){
    this.graph = graph;
  }
  private void paint(){
    this.getChildren().clear();
    GridPane gp = new GridPane();
    ColumnConstraints column = new ColumnConstraints();
    column.setPercentWidth(50);
    gp.getColumnConstraints().add(column);
    gp.getColumnConstraints().add(column);
    var text = new ReusableText(new Font(15)); 
    gp.add(text.withText("1"), 0, 0);
    gp.add(text.withText("2"), 1, 0);
    gp.add(text.withText("2"), 0, 1);
    gp.add(text.withText("4"), 1, 1);
    gp.add(text.withText("3"), 0, 2);
    gp.add(text.withText("6"), 1, 2);
    gp.add(text.withText("4"), 0, 3);
    gp.add(text.withText("8"), 1, 3);
    gp.add(text.withText("5"), 0, 4);
    gp.add(text.withText("10"), 1, 4);
    gp.add(text.withText("6"), 0, 4);
    gp.add(text.withText("12"), 1, 5);
    this.getChildren().add(gp);
  }
}

class TableApp extends Application{
  public static final double HEIGHT = App.HEIGHT*4/3;
  public static final double WIDTH = 500;
  Table table;
  public void start(Stage s, Graph g){
    this.table = new Table(g);
    this.start(s);
  }
  @Override
  public void start(Stage stage){
    if(this.table == null){
      return;
    }
    stage.setTitle("Table");
    Scene scene = new Scene(this.table);
    stage.setScene(scene);
    stage.show();
  }
}