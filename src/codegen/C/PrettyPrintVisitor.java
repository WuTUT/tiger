package codegen.C;

import codegen.C.Ast.Class.ClassSingle;

import java.util.HashMap;

import codegen.C.Ast.Dec;
import codegen.C.Ast.Dec.DecSingle;
import codegen.C.Ast.Exp;
import codegen.C.Ast.Exp.Add;
import codegen.C.Ast.Exp.And;
import codegen.C.Ast.Exp.ArraySelect;
import codegen.C.Ast.Exp.Call;
import codegen.C.Ast.Exp.Id;
import codegen.C.Ast.Exp.Length;
import codegen.C.Ast.Exp.Lt;
import codegen.C.Ast.Exp.NewIntArray;
import codegen.C.Ast.Exp.NewObject;
import codegen.C.Ast.Exp.Not;
import codegen.C.Ast.Exp.Num;
import codegen.C.Ast.Exp.Sub;
import codegen.C.Ast.Exp.This;
import codegen.C.Ast.Exp.Times;
import codegen.C.Ast.MainMethod.MainMethodSingle;
import codegen.C.Ast.Method;
import codegen.C.Ast.Method.MethodSingle;
import codegen.C.Ast.Program.ProgramSingle;
import codegen.C.Ast.Stm;
import codegen.C.Ast.Type;
import codegen.C.Ast.Stm.Assign;
import codegen.C.Ast.Stm.AssignArray;
import codegen.C.Ast.Stm.Block;
import codegen.C.Ast.Stm.If;
import codegen.C.Ast.Stm.Print;
import codegen.C.Ast.Stm.While;
import codegen.C.Ast.Type.ClassType;
import codegen.C.Ast.Type.Int;
import codegen.C.Ast.Type.IntArray;
import codegen.C.Ast.Vtable;
import codegen.C.Ast.Vtable.VtableSingle;
import control.Control;

public class PrettyPrintVisitor implements Visitor {
  private int indentLevel;
  private java.io.BufferedWriter writer;
  private HashMap<String, Boolean> localid = new HashMap<>();

  public PrettyPrintVisitor() {
    this.indentLevel = 2;
  }

  private void indent() {
    this.indentLevel += 2;
  }

  private void unIndent() {
    this.indentLevel -= 2;
  }

  private void printSpaces() {
    int i = this.indentLevel;
    while (i-- != 0)
      this.say(" ");
  }

  private void sayln(String s) {
    say(s);
    try {
      this.writer.write("\n");
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private void say(String s) {
    try {
      this.writer.write(s);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  // /////////////////////////////////////////////////////
  // expressions
  @Override
  public void visit(Add e) {

    e.left.accept(this);
    this.say(" + ");
    e.right.accept(this);
  }

  @Override
  public void visit(And e) {
    e.left.accept(this);
    this.say(" && ");
    e.right.accept(this);
  }

  @Override
  public void visit(ArraySelect e) {
    e.array.accept(this);
    this.say("[");
    e.index.accept(this);
    this.say("]");
  }

  @Override
  public void visit(Call e) {
    if (this.localid.containsKey(e.assign)) {
      this.say("(frame." + e.assign + "=");
    } else
      this.say("(" + e.assign + "=");
    e.exp.accept(this);
    this.say(", ");
    if (localid.containsKey(e.assign)) {

      this.say("frame." + e.assign + "->vptr->" + e.id + "(frame." + e.assign);
    } else
      this.say(e.assign + "->vptr->" + e.id + "(" + e.assign);
    int size = e.args.size();
    if (size == 0) {
      this.say("))");
      return;
    }
    for (Exp.T x : e.args) {
      this.say(", ");
      x.accept(this);
    }
    this.say("))");
    return;
  }

  @Override
  public void visit(Id e) {

    if (localid.containsKey(e.id)) {
      this.say("frame.");
    }
    this.say(e.id);
  }

  @Override
  public void visit(Length e) {
    this.say("*((int*)(");
    e.array.accept(this);
    this.say(")-2)");
  }

  private boolean isLtPrintBrackets(Exp.T e) {
    if (e instanceof Exp.Sub || e instanceof Exp.Times || e instanceof Exp.Add) {
      return true;
    }
    return false;
  }

  @Override
  public void visit(Lt e) {
    if (isLtPrintBrackets(e.left)) {
      this.say("(");
    }
    e.left.accept(this);
    if (isLtPrintBrackets(e.left)) {
      this.say(")");
    }
    this.say(" < ");

    if (isLtPrintBrackets(e.right)) {
      this.say("(");
    }
    e.right.accept(this);
    if (isLtPrintBrackets(e.right)) {
      this.say(")");
    }
    return;
  }

  @Override
  public void visit(NewIntArray e) {
    this.say("(int*)(Tiger_new_array(");
    e.exp.accept(this);
    this.say("))");
  }

  @Override
  public void visit(NewObject e) {
    this.say("((struct " + e.id + "*)(Tiger_new (&" + e.id + "_vtable_, sizeof(struct " + e.id + "))))");
    return;
  }

  private boolean isNotPrintBrackets(Exp.T e) {
    if (e instanceof Exp.And || e instanceof Exp.Lt)
      return true;
    return false;

  }

  @Override
  public void visit(Not e) {
    this.say("!");
    if (isNotPrintBrackets(e.exp)) {
      this.say("(");
      e.exp.accept(this);
      this.say(")");
    } else
      e.exp.accept(this);
  }

  @Override
  public void visit(Num e) {
    this.say(Integer.toString(e.num));
    return;
  }

  @Override
  public void visit(Sub e) {
    e.left.accept(this);
    this.say(" - ");
    e.right.accept(this);
    return;
  }

  @Override
  public void visit(This e) {
    this.say("this");
  }

  private boolean isTimesPrintBrackets(Exp.T e) {
    if (e instanceof Exp.Add || e instanceof Exp.Sub) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void visit(Times e) {

    if (isTimesPrintBrackets(e.left)) {
      this.say("(");
    }
    e.left.accept(this);
    if (isTimesPrintBrackets(e.left)) {
      this.say(")");
    }
    this.say(" * ");
    if (isTimesPrintBrackets(e.right)) {
      this.say("(");
    }
    e.right.accept(this);
    if (isTimesPrintBrackets(e.right)) {
      this.say(")");
    }
    return;
  }

  // statements
  @Override
  public void visit(Assign s) {
    this.printSpaces();
    if (this.localid.containsKey(s.id)) {
      this.say("frame.");
    }
    this.say(s.id + " = ");
    s.exp.accept(this);
    this.sayln(";");
    return;
  }

  @Override
  public void visit(AssignArray s) {
    this.printSpaces();

    if (localid.containsKey(s.id)) {
      this.say("frame.");
    }
    this.say(s.id + "[");
    s.index.accept(this);
    this.say("] = ");
    s.exp.accept(this);
    this.sayln(";");
  }

  @Override
  public void visit(Block s) {
    this.printSpaces();
    this.sayln("{");
    this.indent();
    for (Stm.T stm : s.stms) {
      stm.accept(this);
    }
    this.unIndent();
    this.printSpaces();
    this.sayln("}");
  }

  @Override
  public void visit(If s) {

    this.printSpaces();
    this.say("if (");
    s.condition.accept(this);
    this.sayln(")");
    if (!(s.thenn instanceof Stm.Block))
      this.indent();
    s.thenn.accept(this);
    if (!(s.thenn instanceof Stm.Block))
      this.unIndent();
    this.sayln("");
    this.printSpaces();
    this.sayln("else");
    if (!(s.elsee instanceof Stm.Block) && !(s.elsee instanceof Stm.If))
      this.indent();
    s.elsee.accept(this);
    if (!(s.elsee instanceof Stm.Block) && !(s.elsee instanceof Stm.If))
      this.unIndent();
    this.sayln("");
    return;
  }

  @Override
  public void visit(Print s) {
    this.printSpaces();
    this.say("System_out_println (");
    s.exp.accept(this);
    this.sayln(");");
    return;
  }

  @Override
  public void visit(While s) {
    this.printSpaces();
    this.say("while (");
    s.condition.accept(this);
    this.sayln(")");
    if (!(s.body instanceof Stm.Block))
      this.indent();
    s.body.accept(this);
    if (!(s.body instanceof Stm.Block))
      this.unIndent();
  }

  // type
  @Override
  public void visit(ClassType t) {
    this.say("struct " + t.id + " *");
  }

  @Override
  public void visit(Int t) {
    this.say("int");
  }

  @Override
  public void visit(IntArray t) {
    this.say("int *");
  }

  // dec
  @Override
  public void visit(DecSingle d) {
    d.type.accept(this);
    this.sayln(" " + d.id);
  }

  // method
  @Override
  public void visit(MethodSingle m) {
    StringBuffer arguments_gc_maps = new StringBuffer();
    StringBuffer locals_gc_maps = new StringBuffer();
    for (Dec.T d : m.formals) {
      DecSingle dec = (DecSingle) d;
      if (dec.type instanceof Type.ClassType || dec.type instanceof Type.IntArray) {
        arguments_gc_maps.append("1");
      } else
        arguments_gc_maps.append("0");
    }
    for (Dec.T d : m.locals) {
      DecSingle dec = (DecSingle) d;
      if (dec.type instanceof Type.ClassType || dec.type instanceof Type.IntArray) {
        locals_gc_maps.append("1");
        localid.put(dec.id, true);
      }
    }
    this.sayln("char *" + m.classId + "_" + m.id + "_arguments_gc_map=\"" + arguments_gc_maps.toString() + "\";");
    this.sayln("char *" + m.classId + "_" + m.id + "_locals_gc_map=\"" + locals_gc_maps.toString() + "\";");
    this.sayln("struct " + m.classId + "_" + m.id
        + "_gc_frame{\n  void* __prev;\n  char *arguments_gc_map;\n  int *arguments_base_address;\n  char *locals_gc_map;");

    for (Dec.T d : m.locals) {
      DecSingle dec = (DecSingle) d;
      if ((dec.type instanceof Type.ClassType) || (dec.type instanceof Type.IntArray)) {
        this.say("  ");
        dec.type.accept(this);
        this.say(" " + dec.id + ";\n");
      }
    }
    this.sayln("};");
    m.retType.accept(this);
    this.say(" " + m.classId + "_" + m.id + "(");
    int size = m.formals.size();
    for (Dec.T d : m.formals) {
      DecSingle dec = (DecSingle) d;
      size--;
      dec.type.accept(this);
      this.say(" " + dec.id);
      if (size > 0)
        this.say(", ");
    }
    this.sayln(")");
    this.sayln("{");
    this.sayln("  struct " + m.classId + "_" + m.id + "_gc_frame frame;");
    this.sayln("  frame.__prev = __prev;");
    this.sayln("  __prev = &frame;");
    this.sayln("  frame.arguments_gc_map = " + m.classId + "_" + m.id + "_arguments_gc_map;");
    this.sayln("  frame.arguments_base_address = (int*)&this;");
    this.sayln("  frame.locals_gc_map = " + m.classId + "_" + m.id + "_locals_gc_map;");
    for (Dec.T d : m.locals) {
      DecSingle dec = (DecSingle) d;
      if (!(dec.type instanceof Type.ClassType) && !(dec.type instanceof Type.IntArray)) {
        this.say("  ");
        dec.type.accept(this);
        this.say(" " + dec.id + ";\n");
      } else {
        this.sayln("  frame." + dec.id + "=0;");
      }
    }
    this.sayln("");
    for (Stm.T s : m.stms)
      s.accept(this);
    this.sayln("  __prev = frame.__prev;");
    this.say("  return ");
    m.retExp.accept(this);
    this.sayln(";");
    this.sayln("}");
    localid = new HashMap<>();
    return;
  }

  @Override
  public void visit(MainMethodSingle m) {
    StringBuffer arguments_gc_maps = new StringBuffer();
    StringBuffer locals_gc_maps = new StringBuffer();

    for (Dec.T d : m.locals) {
      DecSingle dec = (DecSingle) d;
      if (dec.type instanceof Type.ClassType || dec.type instanceof Type.IntArray) {
        locals_gc_maps.append("1");
        localid.put(dec.id, true);
      }
    }
    this.sayln("char *" + "main" + "_arguments_gc_map=\"" + arguments_gc_maps.toString() + "\";");
    this.sayln("char *" + "main" + "_locals_gc_map=\"" + locals_gc_maps.toString() + "\";");
    this.sayln("struct " + "main"
        + "_gc_frame{\n  void* __prev;\n  char *arguments_gc_map;\n  int *arguments_base_address;\n  char *locals_gc_map;");
    for (Dec.T d : m.locals) {
      DecSingle dec = (DecSingle) d;
      if ((dec.type instanceof Type.ClassType) || (dec.type instanceof Type.IntArray)) {
        this.say("  ");
        dec.type.accept(this);
        this.say(" " + dec.id + ";\n");
      }
    }
    this.sayln("};");
    this.sayln("int Tiger_main ()");
    this.sayln("{");
    this.sayln("  struct " + "main" + "_gc_frame frame;");
    this.sayln("  frame.__prev = __prev;");
    this.sayln("  __prev = &frame;");
    this.sayln("  frame.arguments_gc_map = " + "main" + "_arguments_gc_map;");
    // this.sayln(" frame.arguments_base_address = &this;");
    this.sayln("  frame.locals_gc_map = " + "main" + "_locals_gc_map;");
    for (Dec.T d : m.locals) {
      DecSingle dec = (DecSingle) d;
      if (!(dec.type instanceof Type.ClassType) && !(dec.type instanceof Type.IntArray)) {
        this.say("  ");
        dec.type.accept(this);
        this.say(" " + dec.id + ";\n");
      }
    }

    m.stm.accept(this);
    this.sayln("  __prev = frame.__prev;");
    this.sayln("}\n");
    return;
  }

  // vtables
  @Override
  public void visit(VtableSingle v) {
    this.sayln("struct " + v.id + "_vtable");
    this.sayln("{");
    this.sayln("  char **" + v.id + "_gc_map;");
    for (codegen.C.Ftuple t : v.ms) {
      this.say("  ");
      t.ret.accept(this);
      this.sayln(" (*" + t.id + ")();");
    }
    this.sayln("};\n");
    return;
  }

  private void outputVtable(VtableSingle v) {
    this.sayln("struct " + v.id + "_vtable " + v.id + "_vtable_ = ");
    this.sayln("{");
    this.sayln(" &" + v.id + "_gc_map,");
    for (codegen.C.Ftuple t : v.ms) {
      this.say("  ");
      this.sayln(t.classs + "_" + t.id + ",");
    }
    this.sayln("};\n");
    return;
  }

  // class
  @Override
  public void visit(ClassSingle c) {
    this.say("char *" + c.id + "_gc_map = \"");
    for (codegen.C.Tuple t : c.decs) {
      if (t.type instanceof Ast.Type.ClassType || t.type instanceof Ast.Type.IntArray)
        this.say("1");
      else
        this.say("0");
    }
    this.sayln("\";");
    this.sayln("struct " + c.id);
    this.sayln("{");
    this.sayln("  struct " + c.id + "_vtable *vptr;");
    for (codegen.C.Tuple t : c.decs) {
      this.say("  ");
      t.type.accept(this);
      this.say(" ");
      this.sayln(t.id + ";");
    }
    this.sayln("};\n");
    return;
  }

  // program
  @Override
  public void visit(ProgramSingle p) {
    // we'd like to output to a file, rather than the "stdout".
    try {
      String outputName = null;
      if (Control.ConCodeGen.outputName != null)
        outputName = Control.ConCodeGen.outputName;
      else if (Control.ConCodeGen.fileName != null)
        outputName = Control.ConCodeGen.fileName + ".c";
      else
        outputName = "a.c.c";

      this.writer = new java.io.BufferedWriter(
          new java.io.OutputStreamWriter(new java.io.FileOutputStream(outputName)));
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }

    this.sayln("// This is automatically generated by the Tiger compiler.");
    this.sayln("// Do NOT modify!\n");

    this.sayln("void *Tiger_new(void *vtable, int size);");

    this.sayln("void *Tiger_new_array(int length);");

    this.sayln("int System_out_println(int i);");

    this.sayln("// a global pointer");
    this.sayln("extern void* __prev;");

    this.sayln("// structures");
    for (codegen.C.Ast.Class.T c : p.classes) {
      c.accept(this);
    }

    this.sayln("// vtables structures");
    for (Vtable.T v : p.vtables) {
      v.accept(this);
      this.sayln("struct " + ((Vtable.VtableSingle) v).id + "_vtable " + ((Vtable.VtableSingle) v).id + "_vtable_;");
    }
    this.sayln("");

    this.sayln("// methods");
    for (Method.T m : p.methods) {
      m.accept(this);
    }
    this.sayln("");

    this.sayln("// vtables");
    for (Vtable.T v : p.vtables) {
      outputVtable((VtableSingle) v);
    }
    this.sayln("");

    this.sayln("// main method");
    p.mainMethod.accept(this);
    this.sayln("");

    this.say("\n\n");

    try {
      this.writer.close();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }

  }

}
