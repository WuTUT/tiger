package parser;

import java.util.ArrayList;
import java.util.LinkedList;

import ast.Ast.Dec;
import ast.Ast.Exp;
import ast.Ast.Exp.Call;
import ast.Ast.Exp.Id;
import ast.Ast.Exp.Lt;
import ast.Ast.Exp.NewObject;
import ast.Ast.Exp.Num;
import ast.Ast.Exp.Sub;
import ast.Ast.Exp.Add;
import ast.Ast.Exp.This;
import ast.Ast.Exp.Times;
import ast.Ast.MainClass;
import ast.Ast.MainClass.MainClassSingle;
import ast.Ast.Method;
import ast.Ast.Program;
import ast.Ast.Program.ProgramSingle;
import ast.Ast.Stm;
import ast.Ast.Stm.Assign;
import ast.Ast.Stm.AssignArray;
import ast.Ast.Stm.If;
import ast.Ast.Stm.Print;
import ast.Ast.Stm.While;
import ast.Ast.Type;
import ast.Ast.Class.ClassSingle;
import ast.Ast.Stm.Block;
import ast.Ast.Method.MethodSingle;
import ast.Ast.Class;

import lexer.Lexer;
import lexer.Token;
import lexer.Token.Kind;

public class Parser {
  Lexer lexer;
  Token current;

  public Parser(String fname, java.io.InputStream fstream) {
    lexer = new Lexer(fname, fstream);
    current = lexer.nextToken();
  }

  // /////////////////////////////////////////////
  // utility methods to connect the lexer
  // and the parser.
  ArrayList<Token> tokenBuf = new ArrayList<>();

  private void advance() {
    if (tokenBuf.size() != 0) {
      current = tokenBuf.remove(0);
    } else
      current = lexer.nextToken();
  }

  private void eatToken(Kind kind) {
    if (kind == current.kind)
      advance();
    else {
      System.out.println("Expects: " + kind.toString());
      System.out.println("But got: " + current.kind.toString());
      System.out.println("at line: " + current.lineNum.toString());
      System.exit(1);
    }
  }

  private void error() {
    System.out.println("Syntax error: compilation aborting at line: " + current.lineNum);
    System.exit(1);
    return;
  }

  // ////////////////////////////////////////////////////////////
  // below are method for parsing.

  // A bunch of parsing methods to parse expressions. The messy
  // parts are to deal with precedence and associativity.

  // ExpList -> Exp ExpRest*
  // ->
  // ExpRest -> , Exp
  private LinkedList<Exp.T> parseExpList() {
    LinkedList<Exp.T> expList = new LinkedList<>();
    if (current.kind == Kind.TOKEN_RPAREN)
      return expList;
    Exp.T exp = parseExp();
    expList.add(exp);
    while (current.kind == Kind.TOKEN_COMMER) {
      advance();
      exp = parseExp();
      expList.add(exp);
    }
    return expList;
  }

  // AtomExp -> (exp)
  // -> INTEGER_LITERAL
  // -> true
  // -> false
  // -> this
  // -> id
  // -> new int [exp]
  // -> new id ()
  private Exp.T parseAtomExp() {
    switch (current.kind) {
      case TOKEN_LPAREN:
        advance();
        Exp.T exp = parseExp();
        eatToken(Kind.TOKEN_RPAREN);
        return exp;
      case TOKEN_NUM:
        int num = Integer.parseInt(current.lexeme);
        advance();
        return new Exp.Num(num);
      case TOKEN_TRUE:
        advance();
        return new Exp.True();
      case TOKEN_FALSE:
        advance();
        return new Exp.False();
      case TOKEN_THIS:
        advance();
        return new Exp.This();
      case TOKEN_ID:
        String id = current.lexeme;
        advance();
        return new Exp.Id(id);
      case TOKEN_NEW: {
        advance();
        switch (current.kind) {
          case TOKEN_INT:
            advance();
            eatToken(Kind.TOKEN_LBRACK);
            exp = parseExp();
            eatToken(Kind.TOKEN_RBRACK);
            return new Exp.NewIntArray(exp);
          case TOKEN_ID:
            id = current.lexeme;
            advance();
            eatToken(Kind.TOKEN_LPAREN);
            eatToken(Kind.TOKEN_RPAREN);
            return new Exp.NewObject(id);
          default:
            error();
            return null;
        }
      }
      default:
        error();
        return null;
    }
  }

  // NotExp -> AtomExp
  // -> AtomExp .id (expList)
  // -> AtomExp [exp]
  // -> AtomExp .length
  private Exp.T parseNotExp() {
    Exp.T atomexp = parseAtomExp();
    while (current.kind == Kind.TOKEN_DOT || current.kind == Kind.TOKEN_LBRACK) {
      if (current.kind == Kind.TOKEN_DOT) {
        advance();
        if (current.kind == Kind.TOKEN_LENGTH) {
          advance();
          return new Exp.Length(atomexp);
        }
        String id = current.lexeme;
        eatToken(Kind.TOKEN_ID);
        eatToken(Kind.TOKEN_LPAREN);
        LinkedList<Exp.T> args = parseExpList();
        eatToken(Kind.TOKEN_RPAREN);
        atomexp = new Exp.Call(atomexp, id, args);
      } else {
        advance();
        Exp.T index = parseExp();
        eatToken(Kind.TOKEN_RBRACK);
        atomexp = new Exp.ArraySelect(atomexp, index);
      }
    }
    return atomexp;
  }

  // TimesExp -> ! TimesExp
  // -> NotExp
  private Exp.T parseTimesExp() {
    int cnt = 0;
    while (current.kind == Kind.TOKEN_NOT) {
      advance();
      cnt++;
    }
    Exp.T notexp = parseNotExp();
    while (cnt > 0) {
      cnt--;
      notexp = new Exp.Not(notexp);
    }

    return notexp;
  }

  // AddSubExp -> TimesExp * TimesExp
  // -> TimesExp
  private Exp.T parseAddSubExp() {
    Exp.T firstexp = parseTimesExp();
    while (current.kind == Kind.TOKEN_TIMES) {
      advance();
      Exp.T restexp = parseTimesExp();
      firstexp = new Exp.Times(firstexp, restexp);
    }
    return firstexp;
  }

  // LtExp -> AddSubExp + AddSubExp
  // -> AddSubExp - AddSubExp
  // -> AddSubExp
  private Exp.T parseLtExp() {
    Exp.T firstexp = parseAddSubExp();
    while (current.kind == Kind.TOKEN_ADD || current.kind == Kind.TOKEN_SUB) {
      boolean isadd = (current.kind == Kind.TOKEN_ADD);
      advance();
      Exp.T restexp = parseAddSubExp();
      if (isadd) {
        firstexp = new Exp.Add(firstexp, restexp);
      } else {
        firstexp = new Exp.Sub(firstexp, restexp);
      }
    }
    return firstexp;
  }

  // AndExp -> LtExp < LtExp
  // -> LtExp
  private Exp.T parseAndExp() {
    Exp.T firstexp = parseLtExp();
    while (current.kind == Kind.TOKEN_LT) {
      advance();
      Exp.T restexp = parseLtExp();
      firstexp = new Exp.Lt(firstexp, restexp);
    }
    return firstexp;
  }

  // Exp -> AndExp && AndExp
  // -> AndExp
  private Exp.T parseExp() {
    Exp.T firstexp = parseAndExp();
    while (current.kind == Kind.TOKEN_AND) {
      advance();
      Exp.T restexp = parseAndExp();
      firstexp = new Exp.And(firstexp, restexp);
    }
    return firstexp;
  }

  // Statement -> { Statement* }
  // -> if ( Exp ) Statement else Statement
  // -> while ( Exp ) Statement
  // -> System.out.println ( Exp ) ;
  // -> id = Exp ;
  // -> id [ Exp ]= Exp ;
  private Stm.T parseStatement() {
    // Lab1. Exercise 4: Fill in the missing code
    // to parse a statement.
    // new util.Todo();
    switch (current.kind) {
      case TOKEN_LBRACE:
        advance();
        LinkedList<Stm.T> stmList = parseStatements();
        eatToken(Kind.TOKEN_RBRACE);
        return new Stm.Block(stmList);
      case TOKEN_IF:
        advance();
        eatToken(Kind.TOKEN_LPAREN);
        Exp.T condition = parseExp();
        eatToken(Kind.TOKEN_RPAREN);
        Stm.T thenn = parseStatement();
        eatToken(Kind.TOKEN_ELSE);
        Stm.T elsee = parseStatement();
        return new Stm.If(condition, thenn, elsee);
      case TOKEN_WHILE:
        advance();
        eatToken(Kind.TOKEN_LPAREN);
        condition = parseExp();
        eatToken(Kind.TOKEN_RPAREN);
        Stm.T body = parseStatement();
        return new While(condition, body);
      case TOKEN_SYSTEM:
        advance();
        eatToken(Kind.TOKEN_DOT);
        eatToken(Kind.TOKEN_OUT);
        eatToken(Kind.TOKEN_DOT);
        eatToken(Kind.TOKEN_PRINTLN);
        eatToken(Kind.TOKEN_LPAREN);
        Exp.T printexp = parseExp();
        eatToken(Kind.TOKEN_RPAREN);
        eatToken(Kind.TOKEN_SEMI);
        return new Print(printexp);
      case TOKEN_ID:
        String id = current.lexeme;
        advance();
        switch (current.kind) {
          case TOKEN_ASSIGN:
            advance();
            Exp.T exp = parseExp();
            eatToken(Kind.TOKEN_SEMI);
            return new Assign(id, exp);
          case TOKEN_LBRACK:
            advance();
            Exp.T index = parseExp();
            eatToken(Kind.TOKEN_RBRACK);
            eatToken(Kind.TOKEN_ASSIGN);
            exp = parseExp();
            eatToken(Kind.TOKEN_SEMI);
            return new AssignArray(id, index, exp);
          default:
            error();
            return null;
        }
      default:
        error();
        return null;
    }
  }

  // Statements -> Statement Statements
  // ->
  private LinkedList<Stm.T> parseStatements() {
    LinkedList<Stm.T> stmList = new LinkedList<>();
    while (current.kind == Kind.TOKEN_LBRACE || current.kind == Kind.TOKEN_IF || current.kind == Kind.TOKEN_WHILE
        || current.kind == Kind.TOKEN_SYSTEM || current.kind == Kind.TOKEN_ID) {
      Stm.T stm = parseStatement();
      stmList.add(stm);
    }
    return stmList;
  }

  // Type -> int []
  // -> boolean
  // -> int
  // -> id
  private Type.T parseType() {
    // Lab1. Exercise 4: Fill in the missing code
    // to parse a type.
    // new util.Todo();

    switch (current.kind) {
      case TOKEN_INT:
        advance();
        if (current.kind == Kind.TOKEN_LBRACK) {
          advance();
          eatToken(Kind.TOKEN_RBRACK);
          return new Type.IntArray();
        } else {
          return new Type.Int();
        }
      case TOKEN_BOOLEAN:
        advance();
        return new Type.Boolean();
      case TOKEN_ID:
        String id = current.lexeme;
        advance();
        return new Type.ClassType(id);
      default:
        error();
        return null;
    }
  }

  // VarDecl -> Type id ;
  private Dec.T parseVarDecl() {
    // to parse the "Type" nonterminal in this method, instead of writing
    // a fresh one.
    Type.T type = parseType();
    String id = current.lexeme;
    eatToken(Kind.TOKEN_ID);
    eatToken(Kind.TOKEN_SEMI);
    return new Dec.DecSingle(type, id);
  }

  // VarDecls -> VarDecl VarDecls
  // ->
  private LinkedList<Dec.T> parseVarDecls() {
    LinkedList<Dec.T> decList = new LinkedList<>();
    while (current.kind == Kind.TOKEN_INT || current.kind == Kind.TOKEN_BOOLEAN || current.kind == Kind.TOKEN_ID) {
      if (current.kind == Kind.TOKEN_ID) {
        Token token2 = lexer.nextToken();
        tokenBuf.add(token2);
        if (token2.kind != Kind.TOKEN_ID) {
          return decList;
        }
      }
      Dec.T dec = parseVarDecl();
      decList.add(dec);
    }
    return decList;
  }

  // FormalList -> Type id FormalRest*
  // ->
  // FormalRest -> , Type id
  private LinkedList<Dec.T> parseFormalList() {
    LinkedList<Dec.T> formalList = new LinkedList<>();
    if (current.kind == Kind.TOKEN_INT || current.kind == Kind.TOKEN_BOOLEAN || current.kind == Kind.TOKEN_ID) {
      Type.T type = parseType();
      String id = current.lexeme;
      eatToken(Kind.TOKEN_ID);
      formalList.add(new Dec.DecSingle(type, id));
      while (current.kind == Kind.TOKEN_COMMER) {
        advance();
        type = parseType();
        id = current.lexeme;
        eatToken(Kind.TOKEN_ID);
        formalList.add(new Dec.DecSingle(type, id));
      }
    }
    return formalList;
  }

  // Method -> public Type id ( FormalList )
  // { VarDecl* Statement* return Exp ;}
  private Method.T parseMethod() {
    // Lab1. Exercise 4: Fill in the missing code
    // to parse a method.
    // new util.Todo();
    eatToken(Kind.TOKEN_PUBLIC);
    Type.T retType = parseType();
    String id = current.lexeme;
    eatToken(Kind.TOKEN_ID);
    eatToken(Kind.TOKEN_LPAREN);
    LinkedList<Dec.T> formals = parseFormalList();
    eatToken(Kind.TOKEN_RPAREN);
    eatToken(Kind.TOKEN_LBRACE);
    LinkedList<Dec.T> locals = parseVarDecls();
    LinkedList<Stm.T> stms = parseStatements();
    eatToken(Kind.TOKEN_RETURN);
    Exp.T retExp = parseExp();
    eatToken(Kind.TOKEN_SEMI);
    eatToken(Kind.TOKEN_RBRACE);
    return new MethodSingle(retType, id, formals, locals, stms, retExp);
  }

  // MethodDecls -> MethodDecl MethodDecls
  // ->
  private LinkedList<Method.T> parseMethodDecls() {
    LinkedList<Method.T> methods = new LinkedList<>();
    while (current.kind == Kind.TOKEN_PUBLIC) {
      Method.T method = parseMethod();
      methods.add(method);
    }
    return methods;
  }

  // ClassDecl -> class id { VarDecl* MethodDecl* }
  // -> class id extends id { VarDecl* MethodDecl* }
  private Class.T parseClassDecl() {
    eatToken(Kind.TOKEN_CLASS);
    String extendss = null;
    String id = current.lexeme;
    eatToken(Kind.TOKEN_ID);
    if (current.kind == Kind.TOKEN_EXTENDS) {
      eatToken(Kind.TOKEN_EXTENDS);
      extendss = current.lexeme;
      eatToken(Kind.TOKEN_ID);
    }
    eatToken(Kind.TOKEN_LBRACE);
    LinkedList<Dec.T> decs = parseVarDecls();
    LinkedList<Method.T> methods = parseMethodDecls();
    eatToken(Kind.TOKEN_RBRACE);
    return new ClassSingle(id, extendss, decs, methods);
  }

  // ClassDecls -> ClassDecl ClassDecls
  // ->
  private LinkedList<Class.T> parseClassDecls() {
    LinkedList<Class.T> classList = new LinkedList<>();
    while (current.kind == Kind.TOKEN_CLASS) {
      Class.T classa = parseClassDecl();
      classList.add(classa);
    }
    return classList;
  }

  // MainClass -> class id
  // {
  // public static void main ( String [] id )
  // {
  // Statement
  // }
  // }
  private ast.Ast.MainClass.T parseMainClass() {
    // Lab1. Exercise 4: Fill in the missing code
    // to parse a main class as described by the
    // grammar above.
    // new util.Todo();
    eatToken(Kind.TOKEN_CLASS);
    String id = current.lexeme;
    eatToken(Kind.TOKEN_ID);
    eatToken(Kind.TOKEN_LBRACE);
    eatToken(Kind.TOKEN_PUBLIC);
    eatToken(Kind.TOKEN_STATIC);
    eatToken(Kind.TOKEN_VOID);
    eatToken(Kind.TOKEN_MAIN);
    eatToken(Kind.TOKEN_LPAREN);
    eatToken(Kind.TOKEN_STRING);
    eatToken(Kind.TOKEN_LBRACK);
    eatToken(Kind.TOKEN_RBRACK);
    String arg = current.lexeme;
    eatToken(Kind.TOKEN_ID);
    eatToken(Kind.TOKEN_RPAREN);
    eatToken(Kind.TOKEN_LBRACE);
    Stm.T stm = parseStatement();
    eatToken(Kind.TOKEN_RBRACE);
    eatToken(Kind.TOKEN_RBRACE);
    return new MainClassSingle(id, arg, stm);
  }

  // Program -> MainClass ClassDecl*
  private Program.T parseProgram() {
    MainClass.T mainClass = parseMainClass();
    LinkedList<ast.Ast.Class.T> classes = parseClassDecls();
    eatToken(Kind.TOKEN_EOF);
    return new Program.ProgramSingle(mainClass, classes);
  }

  public Program.T parse() {
    return parseProgram();

  }
}
