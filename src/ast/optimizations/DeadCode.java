package ast.optimizations;

import ast.Ast.Class.ClassSingle;
import ast.Ast.Dec.DecSingle;
import ast.Ast.MainClass.MainClassSingle;
import ast.Ast.Method.MethodSingle;
import ast.Ast.Program.ProgramSingle;
import ast.Ast.Type.Boolean;
import ast.Ast.Type.ClassType;
import ast.Ast.Type.Int;
import ast.Ast.Type.IntArray;
import cfg.Cfg.MainMethod;
import ast.Ast.Exp.*;

import java.util.LinkedList;

import ast.Ast.*;
import ast.Ast.Stm.*;

// Dead code elimination optimizations on an AST.

public class DeadCode implements ast.Visitor {
  private ast.Ast.Class.T newClass;
  private ast.Ast.MainClass.T mainClass;
  public ast.Ast.Program.T program;
  private ast.Ast.Exp.T exp;
  private ast.Ast.Stm.T stm;
  private ast.Ast.Dec.T dec;
  private ast.Ast.Type.T type;
  private Method.T method;
  public boolean ischanged;
  private LinkedList<Method.T> methods;

  public DeadCode() {
    this.newClass = null;
    this.mainClass = null;
    this.program = null;
    this.exp = null;
    this.stm = null;
    this.dec = null;
    this.type = null;
    this.methods = new LinkedList<Method.T>();
    this.ischanged = false;
  }

  // //////////////////////////////////////////////////////
  //
  public String genId() {
    return util.Temp.next();
  }

  // /////////////////////////////////////////////////////
  // expressions
  @Override
  public void visit(Add e) {
    e.left.accept(this);
    Exp.T left = this.exp;
    e.right.accept(this);
    Exp.T right = this.exp;
    this.exp = new Exp.Add(left, right, e.lineNum);
  }

  @Override
  public void visit(And e) {
    e.left.accept(this);
    Exp.T left = this.exp;
    e.right.accept(this);
    Exp.T right = this.exp;
    this.exp = new Exp.And(left, right, e.lineNum);
  }

  @Override
  public void visit(ArraySelect e) {
    e.array.accept(this);
    Exp.T array = this.exp;
    e.index.accept(this);
    Exp.T index = this.exp;
    this.exp = new Exp.ArraySelect(array, index, e.lineNum);
  }

  @Override
  public void visit(Call e) {
    e.exp.accept(this);
    Exp.T exp = this.exp;
    LinkedList<Exp.T> args = new LinkedList<Exp.T>();
    LinkedList<Type.T> at = e.at;// just assign is ok
    for (Exp.T x : e.args) {
      x.accept(this);
      args.add(this.exp);

    }

    e.rt.accept(this);

    Call cl = new Call(exp, e.id, args, e.lineNum);
    cl.rt = this.type;
    cl.at = at;
    this.exp = cl;
    return;
  }

  @Override
  public void visit(False e) {
    this.exp = new Exp.False(e.lineNum);
  }

  @Override
  public void visit(Id e) {
    Id newid = new Id(e.id, e.lineNum);
    newid.isField = e.isField;
    newid.type = e.type;
    this.exp = newid;
    return;
  }

  @Override
  public void visit(Length e) {
    e.array.accept(this);
    this.exp = new Exp.Length(this.exp, e.lineNum);
  }

  @Override
  public void visit(Lt e) {
    e.left.accept(this);
    Exp.T left = this.exp;

    e.right.accept(this);
    Exp.T right = this.exp;

    this.exp = new Lt(left, right, e.lineNum);
    return;
  }

  @Override
  public void visit(NewIntArray e) {
    e.exp.accept(this);
    this.exp = new NewIntArray(this.exp, e.lineNum);
  }

  @Override
  public void visit(NewObject e) {
    this.exp = new NewObject(e.id, e.lineNum);
    return;
  }

  @Override
  public void visit(Not e) {
    e.exp.accept(this);

    this.exp = new Exp.Not(this.exp, e.lineNum);
  }

  @Override
  public void visit(Num e) {
    this.exp = new Num(e.num, e.lineNum);
    return;
  }

  @Override
  public void visit(Sub e) {
    e.left.accept(this);
    Exp.T left = this.exp;
    e.right.accept(this);
    Exp.T right = this.exp;
    this.exp = new Sub(left, right, e.lineNum);
    return;
  }

  @Override
  public void visit(This e) {
    this.exp = new This(e.lineNum);
    return;
  }

  @Override
  public void visit(Times e) {
    e.left.accept(this);
    Exp.T left = this.exp;
    e.right.accept(this);
    Exp.T right = this.exp;
    this.exp = new Times(left, right, e.lineNum);
    return;
  }

  @Override
  public void visit(True e) {
    this.exp = new Exp.True(e.lineNum);
  }

  // statements
  @Override
  public void visit(Assign s) {
    s.exp.accept(this);
    s.type.accept(this);
    // a=a???// a=a???

    if (this.exp instanceof Exp.Id) {
      Exp.Id expid = (Exp.Id) this.exp;
      if (expid.isField == s.isField) {
        // a = a if multi-thread?
        if (expid.id.equals(s.id)) {
          this.ischanged = true;
          this.stm = null;
          return;
        }
      }
    }
    this.stm = new Assign(s.id, this.exp, s.lineNum, s.isField, this.type);
    return;
  }

  @Override
  public void visit(AssignArray s) {
    s.index.accept(this);
    Exp.T index = this.exp;
    s.exp.accept(this);
    Exp.T exp = this.exp;
    this.stm = new Stm.AssignArray(s.id, index, exp, s.lineNum, s.isField);
  }

  @Override
  public void visit(Block s) {
    LinkedList<Stm.T> stms = new LinkedList<>();
    for (ast.Ast.Stm.T stm : s.stms) {
      stm.accept(this);
      if (this.stm != null)
        stms.add(this.stm);
    }
    this.stm = new Stm.Block(stms, s.lineNum);
  }

  @Override
  public void visit(If s) {
    s.condition.accept(this);
    if (this.exp instanceof Exp.False) {
      // just return the object of previous AST,no deep copy seems ....ok??
      this.ischanged = true;
      s.elsee.accept(this);
      return;
    } else if (this.exp instanceof Exp.True) {
      this.ischanged = true;
      s.thenn.accept(this);
      // just return the object of previous AST,no deep copy seems ....ok??
      return;
    }
    Exp.T condition = this.exp;
    s.thenn.accept(this);
    Stm.T thenn = this.stm;
    s.elsee.accept(this);
    Stm.T elsee = this.stm;
    this.stm = new If(condition, thenn, elsee, s.lineNum);
    return;
  }

  @Override
  public void visit(Print s) {
    s.exp.accept(this);
    this.stm = new Print(this.exp, s.lineNum);
    return;
  }

  @Override
  public void visit(While s) {
    s.condition.accept(this);
    Exp.T condition = this.exp;
    if (condition instanceof Exp.False) {
      this.ischanged = true;
      this.stm = null;
      return;
    }
    s.body.accept(this);
    Stm.T body = this.stm;
    this.stm = new Stm.While(condition, body, s.lineNum);
  }

  // type
  @Override
  public void visit(Boolean t) {
    this.type = new Type.Boolean();
  }

  @Override
  public void visit(ClassType t) {
    this.type = new Type.ClassType(t.id);
  }

  @Override
  public void visit(Int t) {

    this.type = new Type.Int();
  }

  @Override
  public void visit(IntArray t) {
    this.type = new Type.IntArray();
  }

  // dec
  @Override
  public void visit(DecSingle d) {
    d.type.accept(this);
    this.dec = new Dec.DecSingle(this.type, d.id, d.lineNum);
    return;
  }

  // method
  @Override
  public void visit(MethodSingle m) {

    m.retType.accept(this);
    Type.T newRetType = this.type;
    LinkedList<Dec.T> newFormals = new LinkedList<Dec.T>();

    for (ast.Ast.Dec.T d : m.formals) {
      d.accept(this);
      if (this.dec != null)
        newFormals.add(this.dec);
    }

    LinkedList<Dec.T> locals = new LinkedList<Dec.T>();
    for (ast.Ast.Dec.T d : m.locals) {
      d.accept(this);
      if (this.dec != null)
        locals.add(this.dec);
    }

    LinkedList<Stm.T> newStm = new LinkedList<Stm.T>();
    for (ast.Ast.Stm.T s : m.stms) {
      s.accept(this);
      if (this.stm != null)
        newStm.add(this.stm);
    }

    m.retExp.accept(this);
    Exp.T retExp = this.exp;

    this.method = new MethodSingle(newRetType, m.id, newFormals, locals, newStm, retExp, m.lineNum);

    return;
  }

  // class
  @Override
  public void visit(ClassSingle c) {
    LinkedList<Dec.T> newdec = new LinkedList<>();
    for (Dec.T d : c.decs) {
      d.accept(this);
      newdec.add(this.dec);
    }
    for (Method.T m : c.methods) {
      m.accept(this);
      this.methods.add(this.method);
    }
    this.newClass = new ClassSingle(c.id, c.extendss, newdec, this.methods, c.lineNum);
    this.methods = new LinkedList<Method.T>();
    return;
  }

  // main class
  @Override
  public void visit(MainClassSingle c) {
    c.stm.accept(this);
    this.mainClass = new MainClassSingle(c.id, c.arg, this.stm, c.lineNum);
    return;
  }

  // program
  @Override
  public void visit(ProgramSingle p) {

    // You should comment out this line of code:
    // this.program = p;
    p.mainClass.accept(this);
    LinkedList<ast.Ast.Class.T> newclasses = new LinkedList<>();
    for (ast.Ast.Class.T cla : p.classes) {
      cla.accept(this);
      newclasses.add(this.newClass);
    }
    this.program = new ProgramSingle(this.mainClass, newclasses);
    for (String itemString : control.Control.trace) {
      if (itemString.equals("ast.DeadCode")) {

        System.out.println("before optimization:");
        ast.PrettyPrintVisitor pp = new ast.PrettyPrintVisitor();
        p.accept(pp);
        System.out.println("after optimization:");
        this.program.accept(pp);
        break;
      }
    }

    return;
  }
}
