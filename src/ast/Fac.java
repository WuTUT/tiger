package ast;

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
import ast.Ast.Stm.If;
import ast.Ast.Stm.Print;
import ast.Ast.Stm.While;
import ast.Ast.Type;
import ast.Ast.Stm.Block;

public class Fac {
        // Lab2, exercise 2: read the following code and make
        // sure you understand how the sample program "test/Fac.java" is represented.

        // /////////////////////////////////////////////////////
        // To represent the "Fac.java" program in memory manually
        // this is for demonstration purpose only, and
        // no one would want to do this in reality (boring and error-prone).
        /*
         * class Factorial { public static void main(String[] a) {
         * System.out.println(new Fac().ComputeFac(10)); } } class Fac { public int
         * ComputeFac(int num) { int num_aux; if (num < 1) num_aux = 1; else num_aux =
         * num * (this.ComputeFac(num-1)); return num_aux; } }
         */
        static int lineNum = 0;
        // // main class: "Factorial"
        static MainClass.T factorial = new MainClassSingle("Factorial", "a",
                        new Print(new Call(new NewObject("Fac", lineNum), "ComputeFac",
                                        new util.Flist<Exp.T>().list(new Num(10, lineNum)), lineNum), lineNum),
                        lineNum);

        // // class "Fac"
        static ast.Ast.Class.T fac = new ast.Ast.Class.ClassSingle("Fac", null, new util.Flist<Dec.T>().list(),
                        new util.Flist<Method.T>().list(new Method.MethodSingle(new Type.Int(), "ComputeFac",
                                        new util.Flist<Dec.T>().list(new Dec.DecSingle(new Type.Int(), "num", lineNum)),
                                        new util.Flist<Dec.T>()
                                                        .list(new Dec.DecSingle(new Type.Int(), "num_aux", lineNum)),
                                        new util.Flist<Stm.T>().list(new If(
                                                        new Lt(new Id("num", lineNum), new Num(1, lineNum), lineNum),
                                                        new Assign("num_aux", new Num(1, lineNum), lineNum),
                                                        new Assign("num_aux", new Times(new Id("num", lineNum),
                                                                        new Call(new This(), "ComputeFac",
                                                                                        new util.Flist<Exp.T>().list(
                                                                                                        new Sub(new Id("num",
                                                                                                                        lineNum),
                                                                                                                        new Num(1, lineNum),
                                                                                                                        lineNum)),
                                                                                        lineNum),
                                                                        lineNum), lineNum),
                                                        lineNum)),
                                        new Id("num_aux", lineNum), lineNum)),
                        lineNum);

        // program
        public static Program.T prog = new ProgramSingle(factorial, new util.Flist<ast.Ast.Class.T>().list(fac));

        // Lab2, exercise 2: you should write some code to
        // represent the program "test/Sum.java".
        // Your code here:
        static MainClass.T sum = new MainClassSingle("Sum", "a",
                        new Print(new Call(new NewObject("Doit", lineNum), "doit",
                                        new util.Flist<Exp.T>().list(new Num(101, lineNum)), lineNum), lineNum),
                        lineNum);
        static ast.Ast.Class.T doit = new ast.Ast.Class.ClassSingle("Doit", null, new util.Flist<Dec.T>().list(),
                        new util.Flist<Method.T>().list(new Method.MethodSingle(new Type.Int(), "doit",
                                        new util.Flist<Dec.T>().list(new Dec.DecSingle(new Type.Int(), "n", lineNum)),
                                        new util.Flist<Dec.T>().list(new Dec.DecSingle(new Type.Int(), "sum", lineNum),
                                                        new Dec.DecSingle(new Type.Int(), "i", lineNum)),
                                        new util.Flist<Stm.T>().list(new Assign("i", new Num(0, lineNum), lineNum),
                                                        new Assign("sum", new Num(0, lineNum), lineNum),
                                                        new While(new Lt(new Id("i", lineNum), new Id("n", lineNum),
                                                                        lineNum),
                                                                        new Block(new util.Flist<Stm.T>().list(
                                                                                        new Assign("sum", new Add(
                                                                                                        new Id("sum", lineNum),
                                                                                                        new Id("i", lineNum),
                                                                                                        lineNum),
                                                                                                        lineNum),
                                                                                        new Assign("i", new Add(new Id(
                                                                                                        "i", lineNum),
                                                                                                        new Num(1, lineNum),
                                                                                                        lineNum),
                                                                                                        lineNum)),
                                                                                        lineNum),
                                                                        lineNum)),
                                        new Id("sum", lineNum), lineNum)

                        ), lineNum);
        public static Program.T sumprog = new ProgramSingle(sum, new util.Flist<ast.Ast.Class.T>().list(doit));

}
