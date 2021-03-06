package slp;

import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;

import slp.Slp.Exp;
import slp.Slp.Exp.Eseq;
import slp.Slp.Exp.Id;
import slp.Slp.Exp.Num;
import slp.Slp.Exp.Op;
import slp.Slp.ExpList;
import slp.Slp.Stm;
import util.Bug;
import util.Todo;
import control.Control;

public class Main {
  // ///////////////////////////////////////////
  // maximum number of args

  private int maxArgsExp(Exp.T exp) {
    // new Todo();
    if (exp instanceof Exp.Eseq) {
      Exp.Eseq eseq = (Exp.Eseq) exp;
      int n1 = maxArgsExp(eseq.exp);
      int n2 = maxArgsStm(eseq.stm);
      return n1 >= n2 ? n1 : n2;
    }
    return 0;
  }

  private int maxArgsStm(Stm.T stm) {
    if (stm instanceof Stm.Compound) {
      Stm.Compound s = (Stm.Compound) stm;
      int n1 = maxArgsStm(s.s1);
      int n2 = maxArgsStm(s.s2);

      return n1 >= n2 ? n1 : n2;
    } else if (stm instanceof Stm.Assign) {
      // new Todo();
      return maxArgsExp(((Stm.Assign) stm).exp);
    } else if (stm instanceof Stm.Print) {
      // new Todo();
      Stm.Print s = (Stm.Print) stm;
      int n1 = 0;
      int n3 = 0;
      ExpList.T expList = s.explist;
      while (expList instanceof ExpList.Pair) {
        int n2 = maxArgsExp(((ExpList.Pair) expList).exp);
        n1 = n1 > n2 ? n1 : n2;
        n3++;
        expList = ((ExpList.Pair) expList).list;
      }
      int n2 = maxArgsExp(((ExpList.Last) expList).exp);
      n3++;
      n1 = n1 > n2 ? n1 : n2;
      return n1 > n3 ? n1 : n3;
    } else
      new Bug();
    return 0;
  }

  // ////////////////////////////////////////
  // interpreter
  HashMap<String, Integer> table = new HashMap<>();

  private int interpExp(Exp.T exp) {
    // new Todo();
    if (exp instanceof Exp.Num) {
      return ((Exp.Num) exp).num;
    } else if (exp instanceof Exp.Op) {
      Exp.Op op = (Exp.Op) exp;
      switch (op.op) {
        case ADD:
          return interpExp(op.left) + interpExp(op.right);
        case SUB:
          return interpExp(op.left) - interpExp(op.right);
        case TIMES:
          return interpExp(op.left) * interpExp(op.right);
        case DIVIDE:
          return interpExp(op.left) / interpExp(op.right);
        default:
          new Bug();
      }
    } else if (exp instanceof Exp.Eseq) {
      Exp.Eseq eseq = (Exp.Eseq) exp;
      interpStm(eseq.stm);
      return interpExp(eseq.exp);
    } else if (exp instanceof Exp.Id) {
      return table.get(((Exp.Id) exp).id);
    }
    return -1;
  }

  private void interpStm(Stm.T prog) {
    if (prog instanceof Stm.Compound) {
      // new Todo();Automatic merge failed; fix conflicts and then commit the result.
      Stm.Compound compound = (Stm.Compound) prog;
      interpStm(compound.s1);
      interpStm(compound.s2);
    } else if (prog instanceof Stm.Assign) {
      // new Todo();
      Stm.Assign assign = (Stm.Assign) prog;
      table.put(assign.id, interpExp(assign.exp));

    } else if (prog instanceof Stm.Print) {
      // new Todo();
      Stm.Print print = (Stm.Print) prog;
      ExpList.T expList = print.explist;
      while (expList instanceof ExpList.Pair) {
        ExpList.Pair pair = (ExpList.Pair) expList;
        System.out.print(interpExp(pair.exp));
        System.out.print(' ');
        expList = pair.list;
      }
      ExpList.Last last = (ExpList.Last) expList;
      System.out.println(interpExp(last.exp));

    } else
      new Bug();
  }

  // ////////////////////////////////////////
  // compile
  HashSet<String> ids;
  StringBuffer buf;

  private void emit(String s) {
    buf.append(s);
  }

  private void compileExp(Exp.T exp) {
    if (exp instanceof Id) {
      Exp.Id e = (Exp.Id) exp;
      String id = e.id;

      emit("\tmovl\t" + id + ", %eax\n");
    } else if (exp instanceof Num) {
      Exp.Num e = (Exp.Num) exp;
      int num = e.num;

      emit("\tmovl\t$" + num + ", %eax\n");
    } else if (exp instanceof Op) {
      Exp.Op e = (Exp.Op) exp;
      Exp.T left = e.left;
      Exp.T right = e.right;
      Exp.OP_T op = e.op;

      switch (op) {
        case ADD:
          compileExp(left);
          emit("\tpushl\t%eax\n");
          compileExp(right);
          emit("\tpopl\t%edx\n");
          emit("\taddl\t%edx, %eax\n");
          break;
        case SUB:
          compileExp(left);
          emit("\tpushl\t%eax\n");
          compileExp(right);
          emit("\tpopl\t%edx\n");
          emit("\tsubl\t%eax, %edx\n");
          emit("\tmovl\t%edx, %eax\n");
          break;
        case TIMES:
          compileExp(left);
          emit("\tpushl\t%eax\n");
          compileExp(right);
          emit("\tpopl\t%edx\n");
          emit("\timul\t%edx\n");
          break;
        case DIVIDE:
          compileExp(left);
          emit("\tpushl\t%eax\n");
          compileExp(right);
          emit("\tpopl\t%edx\n");
          emit("\tmovl\t%eax, %ecx\n");
          emit("\tmovl\t%edx, %eax\n");
          emit("\tcltd\n");
          emit("\tdiv\t%ecx\n");
          break;
        default:
          new Bug();
      }
    } else if (exp instanceof Eseq) {
      Eseq e = (Eseq) exp;
      Stm.T stm = e.stm;
      Exp.T ee = e.exp;

      compileStm(stm);
      compileExp(ee);
    } else
      new Bug();
  }

  private void compileExpList(ExpList.T explist) {
    if (explist instanceof ExpList.Pair) {
      ExpList.Pair pair = (ExpList.Pair) explist;
      Exp.T exp = pair.exp;
      ExpList.T list = pair.list;

      compileExp(exp);
      emit("\tpushl\t%eax\n");
      emit("\tpushl\t$slp_format\n");
      emit("\tcall\tprintf\n");
      emit("\taddl\t$4, %esp\n");
      compileExpList(list);
    } else if (explist instanceof ExpList.Last) {
      ExpList.Last last = (ExpList.Last) explist;
      Exp.T exp = last.exp;

      compileExp(exp);
      emit("\tpushl\t%eax\n");
      emit("\tpushl\t$slp_format\n");
      emit("\tcall\tprintf\n");
      emit("\taddl\t$4, %esp\n");
    } else
      new Bug();
  }

  private void compileStm(Stm.T prog) {
    if (prog instanceof Stm.Compound) {
      Stm.Compound s = (Stm.Compound) prog;
      Stm.T s1 = s.s1;
      Stm.T s2 = s.s2;

      compileStm(s1);
      compileStm(s2);
    } else if (prog instanceof Stm.Assign) {
      Stm.Assign s = (Stm.Assign) prog;
      String id = s.id;
      Exp.T exp = s.exp;

      ids.add(id);
      compileExp(exp);
      emit("\tmovl\t%eax, " + id + "\n");
    } else if (prog instanceof Stm.Print) {
      Stm.Print s = (Stm.Print) prog;
      ExpList.T explist = s.explist;

      compileExpList(explist);
      emit("\tpushl\t$newline\n");
      emit("\tcall\tprintf\n");
      emit("\taddl\t$4, %esp\n");
    } else
      new Bug();
  }

  // ////////////////////////////////////////
  public void doit(Stm.T prog) {
    // return the maximum number of arguments
    if (Control.ConSlp.action == Control.ConSlp.T.ARGS) {
      int numArgs = maxArgsStm(prog);
      System.out.println(numArgs);
    }

    // interpret a given program
    if (Control.ConSlp.action == Control.ConSlp.T.INTERP) {
      interpStm(prog);
    }

    // compile a given SLP program to x86
    if (Control.ConSlp.action == Control.ConSlp.T.COMPILE) {
      ids = new HashSet<String>();
      buf = new StringBuffer();

      compileStm(prog);
      try {
        // FileOutputStream out = new FileOutputStream();
        FileWriter writer = new FileWriter("slp_gen.s");
        writer.write("// Automatically generated by the Tiger compiler, do NOT edit.\n\n");
        writer.write("\t.data\n");
        writer.write("slp_format:\n");
        writer.write("\t.string \"%d \"\n");
        writer.write("newline:\n");
        writer.write("\t.string \"\\n\"\n");
        for (String s : this.ids) {
          writer.write(s + ":\n");
          writer.write("\t.int 0\n");
        }
        writer.write("\n\n\t.text\n");
        writer.write("\t.globl main\n");
        writer.write("main:\n");
        writer.write("\tpushl\t%ebp\n");
        writer.write("\tmovl\t%esp, %ebp\n");
        writer.write(buf.toString());
        writer.write("\tleave\n\tret\n\n");
        writer.close();
        Process child = Runtime.getRuntime().exec("gcc -m32 slp_gen.s");
        child.waitFor();
        if (!Control.ConSlp.keepasm)
          Runtime.getRuntime().exec("rm -rf slp_gen.s");
      } catch (Exception e) {
        e.printStackTrace();
        System.exit(0);
      }
      // System.out.println(buf.toString());
    }
  }
}
