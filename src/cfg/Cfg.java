package cfg;

import java.util.LinkedList;

public class Cfg {
  // //////////////////////////////////////////////////
  // type
  public static class Type {
    public static abstract class T implements cfg.Acceptable {
    }

    public static class ClassType extends T {
      public String id;

      public ClassType(String id) {
        this.id = id;
      }

      @Override
      public String toString() {
        return this.id;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class IntType extends T {
      public IntType() {
      }

      @Override
      public String toString() {
        return "@int";
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

    public static class IntArrayType extends T {
      public IntArrayType() {
      }

      @Override
      public String toString() {
        return "@int[]";
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

  }// end of type

  // //////////////////////////////////////////////////
  // dec
  public static class Dec {
    public static abstract class T implements cfg.Acceptable {
    }

    public static class DecSingle extends T {
      public Type.T type;
      public String id;

      public DecSingle(Type.T type, String id) {
        this.type = type;
        this.id = id;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }

      @Override
      public String toString() {
        return type.toString() + " " + id + ";";
      }
    }

  }// end of dec

  // //////////////////////////////////////////////////
  // Operand
  public static class Operand {
    public static abstract class T implements cfg.Acceptable {
    }

    public static class Int extends T {
      public int i;

      public Int(int i) {
        this.i = i;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }

      @Override
      public String toString() {
        return String.valueOf(i);
      }
    }

    public static class Var extends T {
      public String id;

      public Var(String id) {
        this.id = id;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }

      @Override
      public String toString() {
        return id;
      }
    }

    public static class ArraySelect extends T {

      public T array;
      public T index;

      public ArraySelect(T array, T index) {
        this.array = array;
        this.index = index;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
        return;
      }

      @Override
      public String toString() {
        return array.toString() + "[" + index.toString() + "]";
      }
    }

    public static class Length extends T {
      public Operand.T array;

      public Length(Operand.T array) {
        this.array = array;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }

      @Override
      public String toString() {
        return array.toString() + ".length";
      }
    }

  }// end of operand

  // //////////////////////////////////////////////////
  // statement
  public static class Stm {
    public static abstract class T implements cfg.Acceptable {
    }

    public static class Add extends T {
      public String dst;
      // type of the destination variable
      public Type.T ty;
      public Operand.T left;
      public Operand.T right;

      public Add(String dst, Type.T ty, Operand.T left, Operand.T right) {
        this.dst = dst;
        this.ty = ty;
        this.left = left;
        this.right = right;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }

      @Override
      public String toString() {
        return dst + " = " + left.toString() + " + " + right.toString() + ";";
      }
    }

    public static class InvokeVirtual extends T {
      public String dst;
      public String obj;
      public String f;
      // type of the destination variable
      public java.util.LinkedList<Operand.T> args;

      public InvokeVirtual(String dst, String obj, String f, LinkedList<Operand.T> args) {
        this.dst = dst;
        this.obj = obj;
        this.f = f;
        this.args = args;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }

      @Override
      public String toString() {
        StringBuilder sb = new StringBuilder(dst + " = " + obj + "." + f + "(");
        for (Operand.T arg : args) {
          sb.append(arg.toString());
          if (arg != args.getLast()) {
            sb.append(", ");
          }

        }
        sb.append(");");
        return sb.toString();
      }
    }

    public static class Lt extends T {
      public String dst;
      // type of the destination variable
      public Type.T ty;
      public Operand.T left;
      public Operand.T right;

      public Lt(String dst, Type.T ty, Operand.T left, Operand.T right) {
        this.dst = dst;
        this.ty = ty;
        this.left = left;
        this.right = right;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }

      @Override
      public String toString() {
        return dst + " = " + left.toString() + " < " + right.toString() + ";";
      }
    }

    public static class And extends T {
      public String dst;
      // type of the destination variable
      public Type.T ty;
      public Operand.T left;
      public Operand.T right;

      public And(String dst, Type.T ty, Operand.T left, Operand.T right) {
        this.dst = dst;
        this.ty = ty;
        this.left = left;
        this.right = right;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }

      @Override
      public String toString() {
        return dst + " = " + left.toString() + " && " + right.toString() + ";";
      }
    }

    public static class Move extends T {
      public String dst;
      // type of the destination variable
      public Type.T ty;
      public Operand.T src;

      public Move(String dst, Type.T ty, Operand.T src) {
        this.dst = dst;
        this.ty = ty;
        this.src = src;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }

      @Override
      public String toString() {
        return dst + " = " + src.toString() + ";";
      }
    }

    public static class MoveArray extends T {
      public String dst;
      // type of the destination variable
      public Operand.T index;
      public Type.T ty;
      public Operand.T src;

      public MoveArray(String dst, Type.T ty, Operand.T index, Operand.T src) {
        this.dst = dst;
        this.ty = ty;
        this.index = index;
        this.src = src;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }

      @Override
      public String toString() {
        return dst + "[" + index.toString() + "]" + " = " + src.toString() + ";";
      }
    }

    public static class Not extends T {
      public String dst;
      // type of the destination variable
      public Type.T ty;
      public Operand.T src;

      public Not(String dst, Type.T ty, Operand.T src) {
        this.dst = dst;
        this.ty = ty;
        this.src = src;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }

      @Override
      public String toString() {
        return dst + " = !" + src.toString() + ";";
      }
    }

    public static class NewObject extends T {
      public String dst;
      // type of the destination variable
      public String c;

      public NewObject(String dst, String c) {
        this.dst = dst;
        this.c = c;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }

      @Override
      public String toString() {
        return dst + " = new " + c + "();";
      }
    }

    public static class NewIntArray extends T {
      public String dst;
      // type of the destination variable
      public Operand.T len;

      public NewIntArray(String dst, Operand.T len) {
        this.dst = dst;
        this.len = len;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }

      @Override
      public String toString() {
        return dst + " = new int[" + len.toString() + "];";
      }
    }

    public static class Print extends T {
      public Operand.T arg;

      public Print(Operand.T arg) {
        this.arg = arg;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }

      @Override
      public String toString() {
        return "System.out.println(" + arg.toString() + ");";
      }
    }

    public static class Sub extends T {
      public String dst;
      // type of the destination variable
      public Type.T ty;
      public Operand.T left;
      public Operand.T right;

      public Sub(String dst, Type.T ty, Operand.T left, Operand.T right) {
        this.dst = dst;
        this.ty = ty;
        this.left = left;
        this.right = right;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }

      @Override
      public String toString() {
        return dst + " = " + left.toString() + " - " + right.toString();
      }
    }

    public static class Times extends T {
      public String dst;
      // type of the destination variable
      public Type.T ty;
      public Operand.T left;
      public Operand.T right;

      public Times(String dst, Type.T ty, Operand.T left, Operand.T right) {
        this.dst = dst;
        this.ty = ty;
        this.left = left;
        this.right = right;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }

      @Override
      public String toString() {
        return dst + " = " + left.toString() + " * " + right.toString() + ";";
      }
    }

  }// end of statement

  // //////////////////////////////////////////////////
  // transfer
  public static class Transfer {
    public static abstract class T implements cfg.Acceptable {
    }

    public static class Goto extends T {
      public util.Label label;

      public Goto(util.Label label) {
        this.label = label;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }

      @Override
      public String toString() {
        return "goto " + label.toString() + ";\n";
      }
    }

    public static class If extends T {
      public Operand.T operand;
      public util.Label truee;
      public util.Label falsee;

      public If(Operand.T operand, util.Label truee, util.Label falsee) {
        this.operand = operand;
        this.truee = truee;
        this.falsee = falsee;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }

      @Override
      public String toString() {
        return "if (" + operand.toString() + ") then " + truee.toString() + "else " + falsee.toString() + ";\n";
      }
    }

    public static class Return extends T {
      public Operand.T operand;

      public Return(Operand.T operand) {
        this.operand = operand;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }

      @Override
      public String toString() {
        return "return " + operand.toString() + ";\n";
      }
    }

  }// end of transfer

  // //////////////////////////////////////////////////
  // block
  public static class Block {
    public static abstract class T implements cfg.Acceptable {
    }

    public static class BlockSingle extends T {
      public util.Label label;
      public LinkedList<Stm.T> stms;
      public Transfer.T transfer;

      public BlockSingle(util.Label label, LinkedList<Stm.T> stms, Transfer.T transfer) {
        this.label = label;
        this.stms = stms;
        this.transfer = transfer;
      }

      @Override
      public boolean equals(Object o) {
        if (o == null)
          return false;

        if (!(o instanceof BlockSingle))
          return false;

        BlockSingle ob = (BlockSingle) o;
        return this.label.equals(ob.label);
      }

      @Override
      public String toString() {
        StringBuffer strb = new StringBuffer();
        strb.append(this.label.toString() + ":\\n");
        // Lab5. Your code here:
        for (Stm.T stm : stms) {
          strb.append(stm.toString() + "\n");
        }
        strb.append(transfer.toString());
        return strb.toString();
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }
  }// end of block

  // //////////////////////////////////////////////////
  // method
  public static class Method {
    public static abstract class T implements cfg.Acceptable {
    }

    public static class MethodSingle extends T {
      public Type.T retType;
      public String id;
      public String classId;
      public LinkedList<Dec.T> formals;
      public LinkedList<Dec.T> locals;
      public LinkedList<Block.T> blocks;
      public util.Label entry;
      public util.Label exit;
      public Operand.T retValue;

      public MethodSingle(Type.T retType, String id, String classId, LinkedList<Dec.T> formals,
          LinkedList<Dec.T> locals, LinkedList<Block.T> blocks, util.Label entry, util.Label exit, Operand.T retValue) {
        this.retType = retType;
        this.id = id;
        this.classId = classId;
        this.formals = formals;
        this.locals = locals;
        this.blocks = blocks;
        this.entry = null;
        this.exit = null;
        this.retValue = null;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

  }// end of method

  // //////////////////////////////////////////////////
  // main method
  public static class MainMethod {
    public static abstract class T implements cfg.Acceptable {
    }

    public static class MainMethodSingle extends T {
      public LinkedList<Dec.T> locals;
      public LinkedList<Block.T> blocks;

      public MainMethodSingle(LinkedList<Dec.T> locals, LinkedList<Block.T> blocks) {
        this.locals = locals;
        this.blocks = blocks;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
        return;
      }

    }

  }// end of main method

  // //////////////////////////////////////////////////
  // vtable
  public static class Vtable {
    public static abstract class T implements cfg.Acceptable {
    }

    public static class VtableSingle extends T {
      public String id; // name of the class
      public LinkedList<cfg.Ftuple> ms; // all methods

      public VtableSingle(String id, LinkedList<cfg.Ftuple> ms) {
        this.id = id;
        this.ms = ms;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }
    }

  }

  // //////////////////////////////////////////////////
  // class
  public static class Class {
    public static abstract class T implements cfg.Acceptable {
    }

    public static class ClassSingle extends T {
      public String id;
      public LinkedList<cfg.Tuple> decs;

      public ClassSingle(String id, LinkedList<cfg.Tuple> decs) {
        this.id = id;
        this.decs = decs;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
      }

    }

  }// end of class

  // //////////////////////////////////////////////////
  // program
  public static class Program {
    public static abstract class T implements cfg.Acceptable {
    }

    public static class ProgramSingle extends T {
      public LinkedList<Class.T> classes;
      public LinkedList<Vtable.T> vtables;
      public LinkedList<Method.T> methods;
      public MainMethod.T mainMethod;

      public ProgramSingle(LinkedList<Class.T> classes, LinkedList<Vtable.T> vtables, LinkedList<Method.T> methods,
          MainMethod.T mainMethod) {
        this.classes = classes;
        this.vtables = vtables;
        this.methods = methods;
        this.mainMethod = mainMethod;
      }

      @Override
      public void accept(Visitor v) {
        v.visit(this);
        return;
      }
    }
  }// end of program
}
