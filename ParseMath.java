package com.csc190.finalproject;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.lang.reflect.InvocationTargetException;
//Temp:
import java.util.Arrays;
public class ParseMath{
  public String equation;
  public ParseMath(String equation){
    this.equation = equation.replaceAll("\\s+","").replaceAll("\\*\\*","^").replaceAll("(\\d+)(x|\\()","$1*x").replaceAll("\\)\\(",")*(");
  }
  public Number forX(double x){
    Number n = null;
    try{
      n = Interpreter.interpret(Parser.parse(Lexer.lex(this.equation.replaceAll("x", String.format("(%s)",x)))));
    } catch (ParseException ex){
      //Ignore ParseException
    } catch (Exception ex){
      ex.printStackTrace();
    }
    return n;
  }
  public static enum TokenType{
    NUM,
    LPAREN,
    RPAREN,
    MINUS,
    PLUS,
    MUL,
    DIV,
    EXP,
    MOD
  }
  static class Lexer{
    public static final Map<String,TokenType> CHARACTER_MAPPING = makeCharacterMap();
    private static final Map<String,TokenType> makeCharacterMap(){
      Map<String,TokenType> map = new HashMap<>();
      map.put("(",TokenType.LPAREN);
      map.put(")",TokenType.RPAREN);
      map.put("-",TokenType.MINUS);
      map.put("+",TokenType.PLUS);
      map.put("*",TokenType.MUL);
      map.put("/",TokenType.DIV);
      map.put("^",TokenType.EXP);
      return map;
    }
    public static final Set<String> NUMBERS = Set.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
    public static class Token{
      private TokenType type;
      private String value;
      public Token(String type){
        this.type = TokenType.valueOf(type);
      }
      public Token(TokenType type){
        this.type = type;
      }
      public Token(TokenType type, Object value){
        this.type = type;
        this.value = value.toString();
      }
      public String toString(){
        if(this.value == null)
          return this.type.toString();
        return this.type.toString()+":"+this.value.toString();
      }
      public boolean equals(Object o){
        if(o instanceof Token)
          if(this.type.equals(((Token)o).type))
            return this.value == ((Token)o).value || this.value.equals(((Token)o).value);
        return false;
      }
    }
    private String text;
    private int index;
    private String currentChar;
    public Lexer(String s){
      this.text = s;
      this.index = -1;
      this.advance();
    }
    private void advance(){
      this.currentChar = ++this.index >= this.text.length() ? null : String.valueOf(this.text.charAt(this.index));
    }
    public static Token[] lex(String s){
      return new Lexer(s).makeTokens();
    }
    public Token[] makeTokens(){
      ArrayList<Token> tokens = new ArrayList<>();
      while(currentChar != null){
        if(NUMBERS.contains(this.currentChar)){
          tokens.add(this.makeNumberToken());
        } else if (CHARACTER_MAPPING.containsKey(this.currentChar)){
          tokens.add(new Token(CHARACTER_MAPPING.get(this.currentChar)));
          this.advance();
        } else {
          throw new ParseException("Invalid character: " + this.currentChar + " — " + this.text);
        }
      }
      return tokens.toArray(new Token[tokens.size()]);
    }
    private Token makeNumberToken(){
      String str = "";
      boolean decimal = false;
      while(this.currentChar != null && (NUMBERS.contains(this.currentChar) || this.currentChar.equals("."))){
        if(this.currentChar.equals(".")){
          if(decimal)
            throw new ArithmeticException("Double decimal point");
          decimal = true;
        }
        str += this.currentChar;
        this.advance();
      }
      return new Token(TokenType.NUM, Double.valueOf(str));
    }
  }
  static class Parser{
    private Lexer.Token[] tokens;
    private int index;
    private Lexer.Token currentToken;
    static abstract class ASTNode {}
    static class BinOpNode extends ASTNode{
      private ASTNode left;
      private Lexer.Token op_token;
      private ASTNode right;
      public BinOpNode(ASTNode left, Lexer.Token op_token, ASTNode right){
        this.left = left;
        this.op_token = op_token;
        this.right = right;
      }
      public ASTNode getLeft() {
        return left;
      }
      public Lexer.Token getOpToken() {
        return op_token;
      }
      public ASTNode getRight() {
        return right;
      }
      public String toString() {
        return String.format("(%s, %s, %s)", this.left,  this.op_token, this.right);
      }
    }
    static class NumberNode extends ASTNode {
      private double value;
      public NumberNode(double n){
        this.value = n;
      }
      public double getValue(){
        return this.value;
      }
      public String toString(){
        return String.valueOf(this.value);
      }
    }
    static class UnaryOpNode extends ASTNode {
      private Lexer.Token op_token;
      private ASTNode node;
      public UnaryOpNode(Lexer.Token token, ASTNode node){
        this.op_token = token;
        this.node = node;
      }
      public Lexer.Token getOpToken(){
        return op_token;
      }
      public ASTNode getNode(){
        return node;
      }
      public String toString(){
        return String.format("(%s, %s)",this.op_token.toString(),this.node.toString());
      }
    }
    public Parser(Lexer.Token[] tokens){
      this.tokens = tokens;
      this.index = -1;
      this.advance();
    }
    public static ASTNode parse(Lexer.Token[] tokens) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException{
      return new Parser(tokens).parse();
    }
    private ASTNode makebinopnode(String left_method, Set<TokenType> tokenTypes, String right_method) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException{
      ASTNode left = (ASTNode)Parser.class.getMethod(left_method).invoke(this);
      while(this.currentToken != null && tokenTypes.contains(this.currentToken.type)){
        Lexer.Token op_tok = this.currentToken;
        this.advance();
        ASTNode right = (ASTNode)Parser.class.getMethod(right_method).invoke(this);
        left = new BinOpNode(left, op_tok, right);
      }
      return left;
    }
    private ASTNode makebinopnode(String methodname, Set<TokenType> tokenTypes) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException{
      return this.makebinopnode(methodname, tokenTypes, methodname);
    }
    private void advance(){
      this.currentToken = ++this.index >= this.tokens.length ? null : this.tokens[this.index];
    }
    public ASTNode parse() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException{
      return this.one();
    }
    public ASTNode one() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException{
      return makebinopnode("two",Set.<TokenType>of(TokenType.PLUS,TokenType.MINUS));
    }
    public ASTNode two() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException{
      return makebinopnode("three",Set.<TokenType>of(TokenType.MUL,TokenType.DIV));
    }
    public ASTNode three() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException{
      if(this.currentToken.type == TokenType.MINUS){
        Lexer.Token token = this.currentToken;
        this.advance();
        return new UnaryOpNode(token,this.four());
      }
      return this.four();
    }
    public ASTNode four() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException{
      return makebinopnode("five",Set.<TokenType>of(TokenType.EXP));
    }
    public ASTNode five() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException{
      if(this.currentToken.type.equals(TokenType.LPAREN)){
        this.advance();
        ASTNode node = this.one();
        if(!this.currentToken.type.equals(TokenType.RPAREN))
          throw new ParseException("')' expected");
        this.advance();
        return node;
      } else if (this.currentToken.type.equals(TokenType.NUM)){
        double num = Double.valueOf(this.currentToken.value);
        this.advance();
        return new NumberNode(num);
      }
      return null;
    }
  }
  static class Interpreter{
    static class Value extends Number {
      private String value;
      public Value(Parser.NumberNode node){
        this.value = String.valueOf(node.value);
      }
      public Value(double value){
        this.value = String.valueOf(value);
      }
      public Value add(Value other){
        return new Value(this.doubleValue() + other.doubleValue());
      }
      public Value subtract(Value other){
        return new Value(this.doubleValue() - other.doubleValue());
      }
      public Value multiply(Value other){
        return new Value(this.doubleValue() * other.doubleValue());
      }
      public Value divide(Value other){
        return new Value(this.doubleValue() / other.doubleValue());
      }
      public Value exp(Value other){
        return new Value(Math.pow(this.doubleValue(),other.doubleValue()));
      }
      public Value mod(Value other){
        return new Value(this.doubleValue() % other.doubleValue());
      }

      @Override
      public double doubleValue(){
        return Double.valueOf(this.value);
      }
      @Override
      public int intValue(){
        return Integer.valueOf(this.value);
      }
      @Override
      public long longValue(){
        return Long.valueOf(this.value);
      }
      @Override
      public float floatValue(){
        return Float.valueOf(this.value);
      }
      public String toString(){
        return this.value;
      }
    }
    private Parser.ASTNode root;
    private Value value;
    public Interpreter(Parser.ASTNode parent){
      this.root = parent;
      this.value = this.interpret();
    }
    public Value interpret(){
      return this.visit(this.root);
    }
    public static Value interpret(Parser.ASTNode node){
      return new Interpreter(node).interpret();
    }
    private Value visit(Parser.ASTNode node){
      switch(node.getClass().getSimpleName()){
        case "BinOpNode":
          return this.visit_BinOpNode((Parser.BinOpNode)node);
        case "NumberNode":
          return this.visit_NumberNode((Parser.NumberNode)node);
        case "UnaryOpNode":
          return this.visit_UnaryOpNode((Parser.UnaryOpNode)node);
        default:
          throw new ParseException("Unknown Node type: " + node.getClass().getSimpleName());
      }
    }
    private Value visit_BinOpNode(Parser.BinOpNode node) {
      Value left = visit(node.left);
      switch(node.op_token.type){
        case PLUS:
          return left.add(visit(node.right));
        case MINUS:
          return left.subtract(visit(node.right));
        case MUL:
          return left.multiply(visit(node.right));
        case DIV:
          return left.divide(visit(node.right));
        case EXP:
          return left.exp(visit(node.right));
        case MOD:
          return left.mod(visit(node.right));
      }
      throw new UnsupportedOperationException();
    }
    private Value visit_NumberNode(Parser.NumberNode node){
      return new Value(node.value);
    }
    private Value visit_UnaryOpNode(Parser.UnaryOpNode node){
      Value value = visit(node.node);
      switch(node.op_token.type){
        case MINUS:
          return value.multiply(new Value(-1));
        default:
          throw new UnsupportedOperationException();
      }
    }
    public String toString(){
      return this.root.toString()+" = "+this.value;
    }
  }
}

class ParseException extends RuntimeException {
  ParseException(){
    super();
  }
  ParseException(String s){
    super(s);
  }
}
