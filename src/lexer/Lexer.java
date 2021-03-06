package lexer;

import static control.Control.ConLexer.dump;

import java.io.InputStream;

import lexer.Token.Kind;
import util.Bug;
import util.Todo;

import java.util.HashMap;
import java.util.regex.Pattern;

public class Lexer {
  String fname; // the input file name to be compiled
  InputStream fstream; // input stream for the above file

  int lineNum;
  Pattern idPattern = Pattern.compile("^([a-zA-Z_$][a-zA-Z\\d_$]*)$");
  HashMap<String, Token.Kind> reserveID = new HashMap<>();

  public Lexer(String fname, InputStream fstream) {
    this.fname = fname;
    this.fstream = fstream;
    this.lineNum = 1;
    reserveID.put("boolean", Kind.TOKEN_BOOLEAN);
    reserveID.put("class", Kind.TOKEN_CLASS);
    reserveID.put("else", Kind.TOKEN_ELSE);
    reserveID.put("extends", Kind.TOKEN_EXTENDS);
    reserveID.put("false", Kind.TOKEN_FALSE);
    reserveID.put("if", Kind.TOKEN_IF);
    reserveID.put("int", Kind.TOKEN_INT);
    reserveID.put("length", Kind.TOKEN_LENGTH);
    reserveID.put("new", Kind.TOKEN_NEW);
    reserveID.put("main", Kind.TOKEN_MAIN);
    reserveID.put("out", Kind.TOKEN_OUT);
    reserveID.put("println", Kind.TOKEN_PRINTLN);
    reserveID.put("public", Kind.TOKEN_PUBLIC);
    reserveID.put("return", Kind.TOKEN_RETURN);
    reserveID.put("static", Kind.TOKEN_STATIC);
    reserveID.put("String", Kind.TOKEN_STRING);
    reserveID.put("System", Kind.TOKEN_SYSTEM);
    reserveID.put("this", Kind.TOKEN_THIS);
    reserveID.put("true", Kind.TOKEN_TRUE);
    reserveID.put("void", Kind.TOKEN_VOID);
    reserveID.put("while", Kind.TOKEN_WHILE);

  }

  // When called, return the next token (refer to the code "Token.java")
  // from the input stream.
  // Return TOKEN_EOF when reaching the end of the input stream.
  private Token nextTokenInternal() throws Exception {
    int c = this.fstream.read();
    if (-1 == c)
      // The value for "lineNum" is now "null",
      // you should modify this to an appropriate
      // line number for the "EOF" token.
      return new Token(Kind.TOKEN_EOF, null);

    // skip all kinds of "blanks"
    while (' ' == c || '\t' == c || '\n' == c) {
      if (c == '\n') {
        lineNum++;
      }
      c = this.fstream.read();
    }
    if (-1 == c)
      return new Token(Kind.TOKEN_EOF, null);

    while (c == '/') {
      this.fstream.mark(1);
      int c2 = this.fstream.read();
      if (c2 == '/') {
        // comment token
        while (c2 != '\n' && c2 != -1) {
          c2 = this.fstream.read();
        }
        if (c2 == -1)
          return new Token(Kind.TOKEN_EOF, null);
        lineNum++;
        c = this.fstream.read();
        while (' ' == c || '\t' == c || '\n' == c) {
          if (c == '\n') {
            lineNum++;
          }
          c = this.fstream.read();
        }
        if (-1 == c)
          return new Token(Kind.TOKEN_EOF, null);

      } else {
        this.fstream.reset();
        break;
      }
    }
    switch (c) {
      case '+':
        return new Token(Kind.TOKEN_ADD, lineNum);
      case '&':
        this.fstream.mark(1);
        int c2 = this.fstream.read();
        if (c2 == '&') {
          return new Token(Kind.TOKEN_AND, lineNum);
        } else {
          this.fstream.reset();
          new Bug();
        }
      case '=':
        return new Token(Kind.TOKEN_ASSIGN, lineNum);
      case ',':
        return new Token(Kind.TOKEN_COMMER, lineNum);
      case '.':
        return new Token(Kind.TOKEN_DOT, lineNum);
      case '-':
        return new Token(Kind.TOKEN_SUB, lineNum);
      case '*':
        return new Token(Kind.TOKEN_TIMES, lineNum);
      case '!':
        return new Token(Kind.TOKEN_NOT, lineNum);
      case '<':
        return new Token(Kind.TOKEN_LT, lineNum);
      case '{':
        return new Token(Kind.TOKEN_LBRACE, lineNum);
      case '[':
        return new Token(Kind.TOKEN_LBRACK, lineNum);
      case '(':
        return new Token(Kind.TOKEN_LPAREN, lineNum);
      case '}':
        return new Token(Kind.TOKEN_RBRACE, lineNum);
      case ']':
        return new Token(Kind.TOKEN_RBRACK, lineNum);
      case ')':
        return new Token(Kind.TOKEN_RPAREN, lineNum);
      case ';':
        return new Token(Kind.TOKEN_SEMI, lineNum);
      default:
        // Lab 1, exercise 2: supply missing code to
        // lex other kinds of tokens.
        // Hint: think carefully about the basic
        // data structure and algorithms. The code
        // is not that much and may be less than 50 lines. If you
        // find you are writing a lot of code, you
        // are on the wrong way.

        // new Todo();
        if (Character.isDigit((char) c)) {
          StringBuffer numBuf = new StringBuffer();
          numBuf.append((char) c);
          this.fstream.mark(1);
          c = this.fstream.read();
          while (Character.isDigit((char) c)) {
            numBuf.append((char) c);
            this.fstream.mark(1);
            c = this.fstream.read();
          }
          this.fstream.reset();
          return new Token(Kind.TOKEN_NUM, lineNum, numBuf.toString());
        } else if (Character.isJavaIdentifierStart(c)) {
          StringBuffer idBuf = new StringBuffer();
          idBuf.append((char) c);
          this.fstream.mark(1);
          c = this.fstream.read();
          while (Character.isJavaIdentifierPart((char) c)) {
            idBuf.append((char) c);
            this.fstream.mark(1);
            c = this.fstream.read();
          }
          this.fstream.reset();
          String id = idBuf.toString();
          // System.out.println(id);
          if (reserveID.containsKey(id))
            return new Token(reserveID.get(id), lineNum);
          return new Token(Kind.TOKEN_ID, lineNum, id);
        }
        return null;
    }
  }

  public Token nextToken() {
    Token t = null;

    try {
      t = this.nextTokenInternal();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
    if (dump)
      System.out.println(t.toString());
    return t;
  }
}
