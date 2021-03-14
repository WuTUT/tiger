package cfg.optimizations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import cfg.Cfg.Block;
import cfg.Cfg.Block.BlockSingle;
import cfg.Cfg.Class.ClassSingle;
import cfg.Cfg.Dec.DecSingle;
import cfg.Cfg.MainMethod.MainMethodSingle;
import cfg.Cfg.Method;
import cfg.Cfg.Method.MethodSingle;
import cfg.Cfg.Operand;
import cfg.Cfg.Operand.ArraySelect;
import cfg.Cfg.Operand.Int;
import cfg.Cfg.Operand.Length;
import cfg.Cfg.Operand.Var;
import cfg.Cfg.Program.ProgramSingle;
import cfg.Cfg.Stm;
import cfg.Cfg.Stm.Add;
import cfg.Cfg.Stm.And;
import cfg.Cfg.Stm.InvokeVirtual;
import cfg.Cfg.Stm.Lt;
import cfg.Cfg.Stm.Move;
import cfg.Cfg.Stm.MoveArray;
import cfg.Cfg.Stm.NewIntArray;
import cfg.Cfg.Stm.NewObject;
import cfg.Cfg.Stm.Not;
import cfg.Cfg.Stm.Print;
import cfg.Cfg.Stm.Sub;
import cfg.Cfg.Stm.Times;
import cfg.Cfg.Transfer;
import cfg.Cfg.Transfer.Goto;
import cfg.Cfg.Transfer.If;
import cfg.Cfg.Transfer.Return;
import cfg.Cfg.Type.ClassType;
import cfg.Cfg.Type.IntArrayType;
import cfg.Cfg.Type.IntType;
import cfg.Cfg.Vtable.VtableSingle;
import util.Graph;
import util.Label;
import util.Graph.Edge;
import util.Graph.Node;

public class LivenessVisitor implements cfg.Visitor {
  // gen, kill for one statement
  private HashSet<String> oneStmGen;
  private HashSet<String> oneStmKill;

  // gen, kill for one transfer
  private HashSet<String> oneTransferGen;
  private HashSet<String> oneTransferKill;

  // gen, kill for statements
  private HashMap<Stm.T, HashSet<String>> stmGen;
  private HashMap<Stm.T, HashSet<String>> stmKill;

  // gen, kill for transfers
  private HashMap<Transfer.T, HashSet<String>> transferGen;
  private HashMap<Transfer.T, HashSet<String>> transferKill;

  // gen, kill for blocks
  private HashMap<Block.T, HashSet<String>> blockGen;
  private HashMap<Block.T, HashSet<String>> blockKill;

  // liveIn, liveOut for blocks
  private HashMap<Block.T, HashSet<String>> blockLiveIn;
  private HashMap<Block.T, HashSet<String>> blockLiveOut;

  // liveIn, liveOut for statements
  public HashMap<Stm.T, HashSet<String>> stmLiveIn;
  public HashMap<Stm.T, HashSet<String>> stmLiveOut;

  // liveIn, liveOut for transfer
  public HashMap<Transfer.T, HashSet<String>> transferLiveIn;
  public HashMap<Transfer.T, HashSet<String>> transferLiveOut;

  // As you will walk the tree for many times, so
  // it will be useful to recored which is which:
  enum Liveness_Kind_t {
    None, StmGenKill, BlockGenKill, BlockInOut, StmInOut,
  }

  private Liveness_Kind_t kind = Liveness_Kind_t.None;

  public LivenessVisitor() {
    this.oneStmGen = new HashSet<>();
    this.oneStmKill = new java.util.HashSet<>();

    this.oneTransferGen = new java.util.HashSet<>();
    this.oneTransferKill = new java.util.HashSet<>();

    this.stmGen = new java.util.HashMap<>();
    this.stmKill = new java.util.HashMap<>();

    this.transferGen = new java.util.HashMap<>();
    this.transferKill = new java.util.HashMap<>();

    this.blockGen = new java.util.HashMap<>();
    this.blockKill = new java.util.HashMap<>();

    this.blockLiveIn = new java.util.HashMap<>();
    this.blockLiveOut = new java.util.HashMap<>();

    this.stmLiveIn = new java.util.HashMap<>();
    this.stmLiveOut = new java.util.HashMap<>();

    this.transferLiveIn = new java.util.HashMap<>();
    this.transferLiveOut = new java.util.HashMap<>();

    this.kind = Liveness_Kind_t.None;
  }

  // /////////////////////////////////////////////////////
  // utilities

  private java.util.HashSet<String> getOneStmGenAndClear() {
    java.util.HashSet<String> temp = this.oneStmGen;
    this.oneStmGen = new java.util.HashSet<>();
    return temp;
  }

  private java.util.HashSet<String> getOneStmKillAndClear() {
    java.util.HashSet<String> temp = this.oneStmKill;
    this.oneStmKill = new java.util.HashSet<>();
    return temp;
  }

  private java.util.HashSet<String> getOneTransferGenAndClear() {
    java.util.HashSet<String> temp = this.oneTransferGen;
    this.oneTransferGen = new java.util.HashSet<>();
    return temp;
  }

  private java.util.HashSet<String> getOneTransferKillAndClear() {
    java.util.HashSet<String> temp = this.oneTransferKill;
    this.oneTransferKill = new java.util.HashSet<>();
    return temp;
  }

  // /////////////////////////////////////////////////////
  // operand
  @Override
  public void visit(Int operand) {
    return;
  }

  @Override
  public void visit(Var operand) {
    this.oneStmGen.add(operand.id);
    return;
  }

  // statements
  @Override
  public void visit(Add s) {
    this.oneStmKill.add(s.dst);
    // Invariant: accept() of operand modifies "gen"
    s.left.accept(this);
    s.right.accept(this);
    return;
  }

  @Override
  public void visit(InvokeVirtual s) {
    this.oneStmKill.add(s.dst);
    this.oneStmGen.add(s.obj);
    for (Operand.T arg : s.args) {
      arg.accept(this);
    }
    return;
  }

  @Override
  public void visit(Lt s) {
    this.oneStmKill.add(s.dst);
    // Invariant: accept() of operand modifies "gen"
    s.left.accept(this);
    s.right.accept(this);
    return;
  }

  @Override
  public void visit(Move s) {
    this.oneStmKill.add(s.dst);
    // Invariant: accept() of operand modifies "gen"
    s.src.accept(this);
    return;
  }

  @Override
  public void visit(NewObject s) {
    this.oneStmKill.add(s.dst);
    return;
  }

  @Override
  public void visit(Print s) {
    s.arg.accept(this);
    return;
  }

  @Override
  public void visit(Sub s) {
    this.oneStmKill.add(s.dst);
    // Invariant: accept() of operand modifies "gen"
    s.left.accept(this);
    s.right.accept(this);
    return;
  }

  @Override
  public void visit(Times s) {
    this.oneStmKill.add(s.dst);
    // Invariant: accept() of operand modifies "gen"
    s.left.accept(this);
    s.right.accept(this);
    return;
  }

  @Override
  public void visit(ArraySelect s) {
    s.array.accept(this);
    s.index.accept(this);

  }

  @Override
  public void visit(And s) {
    this.oneStmKill.add(s.dst);
    // Invariant: accept() of operand modifies "gen"
    s.left.accept(this);
    s.right.accept(this);
    return;
  }

  @Override
  public void visit(NewIntArray s) {
    this.oneStmKill.add(s.dst);
    s.len.accept(this);
  }

  @Override
  public void visit(Length s) {
    s.array.accept(this);
  }

  @Override
  public void visit(Not s) {
    this.oneStmKill.add(s.dst);
    s.src.accept(this);

  }

  @Override
  public void visit(MoveArray s) {
    this.oneStmKill.add(s.dst);
    s.src.accept(this);
    s.index.accept(this);
  }

  // transfer
  @Override
  public void visit(If s) {
    // Invariant: accept() of operand modifies "gen"
    if (s.operand instanceof Operand.Var) {
      this.oneTransferGen.add(s.operand.toString());
    }
    return;
  }

  @Override
  public void visit(Goto s) {
    return;
  }

  @Override
  public void visit(Return s) {
    // Invariant: accept() of operand modifies "gen"
    if (s.operand instanceof Operand.Var) {
      this.oneTransferGen.add(s.operand.toString());
    }

    return;
  }

  // type
  @Override
  public void visit(ClassType t) {
  }

  @Override
  public void visit(IntType t) {
  }

  @Override
  public void visit(IntArrayType t) {
  }

  // dec
  @Override
  public void visit(DecSingle d) {
  }

  // utility functions:
  private void calculateStmTransferGenKill(BlockSingle b) {
    for (Stm.T s : b.stms) {
      this.oneStmGen = new java.util.HashSet<>();
      this.oneStmKill = new java.util.HashSet<>();
      s.accept(this);
      this.stmGen.put(s, this.oneStmGen);
      this.stmKill.put(s, this.oneStmKill);
      if (control.Control.isTracing("liveness.step1")) {
        System.out.print("\ngen, kill for statement:");
        System.out.println(s.toString());
        System.out.print("\ngen is:");
        for (String str : this.oneStmGen) {
          System.out.print(str + ", ");
        }
        System.out.print("\nkill is:");
        for (String str : this.oneStmKill) {
          System.out.print(str + ", ");
        }
      }
    }

    this.oneTransferGen = new java.util.HashSet<>();
    this.oneTransferKill = new java.util.HashSet<>();
    b.transfer.accept(this);
    this.transferGen.put(b.transfer, this.oneTransferGen);
    this.transferKill.put(b.transfer, this.oneTransferKill);
    if (control.Control.isTracing("liveness.step1")) {
      System.out.print("\ngen, kill for transfer:");
      System.out.println(b.transfer.toString());
      System.out.print("\ngen is:");
      for (String str : this.oneTransferGen) {
        System.out.print(str + ", ");
      }
      System.out.println("\nkill is:");
      for (String str : this.oneTransferKill) {
        System.out.print(str + ", ");
      }
    }
    return;
  }

  // block
  @Override
  public void visit(BlockSingle b) {
    switch (this.kind) {
    case StmGenKill:
      calculateStmTransferGenKill(b);
      break;
    case BlockGenKill:
      calculateBlkTransferGenKill(b);
      break;
    case StmInOut:
      calculateStmInOut(b);
      break;
    default:
      // Your code here:
      return;
    }
  }

  private void calculateStmInOut(BlockSingle b) {
    HashSet<String> transout = new HashSet<>();
    transout.addAll(this.blockLiveOut.get(b));
    this.transferLiveOut.put(b.transfer, transout);
    HashSet<String> transin = new HashSet<>();
    transin.addAll(transout);
    transin.removeAll(this.transferKill.get(b.transfer));
    transin.addAll(this.transferGen.get(b.transfer));
    this.transferLiveIn.put(b.transfer, transin);

    HashSet<String> tmp = new HashSet<>();
    tmp.addAll(transin);
    for (int i = b.stms.size() - 1; i >= 0; i--) {
      Stm.T stm = b.stms.get(i);
      HashSet<String> instm = new HashSet<>();
      HashSet<String> outstm = new HashSet<>();
      outstm.addAll(tmp);
      this.stmLiveOut.put(stm, outstm);
      instm.addAll(outstm);
      instm.removeAll(this.stmKill.get(stm));
      instm.addAll(this.stmGen.get(stm));
      this.stmLiveIn.put(stm, instm);
      tmp = new HashSet<>();
      tmp.addAll(instm);
    }

  }

  private void calculateBlkTransferGenKill(BlockSingle b) {
    HashSet<String> oneBlockGen = new HashSet<>();
    HashSet<String> oneBlockKill = new HashSet<>();
    HashSet<String> tmp = null;
    for (Stm.T s : b.stms) {
      tmp = this.stmGen.get(s);
      tmp.removeAll(oneBlockKill);
      oneBlockKill.addAll(this.stmKill.get(s));
      oneBlockGen.addAll(tmp);
    }
    tmp = this.transferGen.get(b.transfer);
    tmp.removeAll(oneBlockKill);
    oneBlockKill.addAll(this.transferKill.get(b.transfer));
    oneBlockGen.addAll(tmp);
    this.blockGen.put(b, oneBlockGen);
    this.blockKill.put(b, oneBlockKill);
    if (control.Control.isTracing("liveness.step2")) {
      System.out.print("\ngen, kill for statement:");
      System.out.println(b.label.toString());
      System.out.print("\ngen is:");
      for (String str : oneBlockGen) {
        System.out.print(str + ", ");
      }
      System.out.print("\nkill is:");
      for (String str : oneBlockKill) {
        System.out.print(str + ", ");
      }
    }
  }

  // method
  @Override
  public void visit(MethodSingle m) {
    // Four steps:
    // Step 1: calculate the "gen" and "kill" sets for each
    // statement and transfer
    this.kind = Liveness_Kind_t.StmGenKill;
    for (Block.T block : m.blocks) {
      block.accept(this);
    }

    // Step 2: calculate the "gen" and "kill" sets for each block.
    // For this, you should visit statements and transfers in a
    // block in a reverse order.
    // Your code here:
    this.kind = Liveness_Kind_t.BlockGenKill;
    for (Block.T block : m.blocks) {
      block.accept(this);
    }
    // Step 3: calculate the "liveIn" and "liveOut" sets for each block
    // Note that to speed up the calculation, you should first
    // calculate a reverse topo-sort order of the CFG blocks, and
    // crawl through the blocks in that order.
    // And also you should loop until a fix-point is reached.
    // Your code here:
    Graph<Block.T> g = new Graph<Block.T>(m.id + "liveness");
    HashMap<Label, Block.T> labelMap = new HashMap<>();
    Block.T startBlk = null;
    for (Block.T block : m.blocks) {
      Block.BlockSingle blk = (Block.BlockSingle) block;
      g.addNode(blk);
      labelMap.put(blk.label, blk);
    }
    for (Block.T block : m.blocks) {
      Block.BlockSingle blk = (Block.BlockSingle) block;
      if (blk.transfer instanceof Transfer.Goto) {
        Transfer.Goto transgo = (Transfer.Goto) blk.transfer;
        g.addEdge(labelMap.get(transgo.label), blk);
      } else if (blk.transfer instanceof Transfer.If) {
        Transfer.If transif = (Transfer.If) blk.transfer;
        g.addEdge(labelMap.get(transif.falsee), blk);
        g.addEdge(labelMap.get(transif.truee), blk);
      } else if (blk.transfer instanceof Transfer.Return) {
        startBlk = blk;
      }
    }
    g.visualize();
    // Init return Transfer
    boolean ischanged = true;
    this.blockLiveOut.put(startBlk, new HashSet<>());
    HashSet<String> inblk = new HashSet<>();
    inblk.addAll(this.blockGen.get(startBlk));

    // Init all in[B]/return transfer
    for (Block.T block : m.blocks) {
      this.blockLiveIn.put(block, new HashSet<String>());
    }
    this.blockLiveIn.put(startBlk, inblk);
    while (ischanged) {
      ischanged = false;
      Queue<Block.T> queue = new LinkedList<>();
      queue.add(startBlk);
      HashSet<Block.T> vis = new HashSet<>();
      vis.add(startBlk);
      while (queue.size() > 0) {
        Block.T blk = queue.poll();
        Graph<Block.T>.Node node = g.lookupNode(blk);
        LinkedList<Graph<Block.T>.Edge> edges = node.edges;
        for (Graph<Block.T>.Edge edge : edges) {
          Block.T to = edge.getTo().getData();
          if (!vis.contains(to)) {
            vis.add(to);
            queue.add(to);
            HashSet<String> outb = new HashSet<>();
            for (Graph<Block.T>.Edge edge_to : g.lookupNode(to).backEdges) {
              Block.T succ = edge_to.getTo().getData();
              outb.addAll(this.blockLiveIn.get(succ));
            }
            this.blockLiveOut.put(to, outb);
            HashSet<String> caloutb = new HashSet<>();
            caloutb.addAll(outb);
            caloutb.removeAll(this.blockKill.get(to));
            caloutb.addAll(this.blockGen.get(to));
            if (!caloutb.equals(this.blockLiveIn.get(to))) {
              ischanged = true;
            }
            this.blockLiveIn.put(to, caloutb);
          }
        }
      }
      // System.out.println("LOOP============");

      for (Block.T block : m.blocks) {
        BlockSingle b = (Block.BlockSingle) block;
        if (control.Control.isTracing("liveness.step3")) {

          System.out.print("\ngen, kill for statement:");
          System.out.println(b.label.toString());
          System.out.print("\nin is:");
          for (String str : this.blockLiveIn.get(b)) {
            System.out.print(str + ", ");
          }
          System.out.print("\nout is:");
          for (String str : this.blockLiveOut.get(b)) {
            System.out.print(str + ", ");
          }
          System.out.println();
        }
      }
      // System.out.println("LOOPEND=============");
    }

    // Step 4: calculate the "liveIn" and "liveOut" sets for each
    // statement and transfer
    // Your code here:
    this.kind = Liveness_Kind_t.StmInOut;
    for (Block.T block : m.blocks) {
      block.accept(this);
    }
    for (Block.T block : m.blocks) {
      BlockSingle b = (Block.BlockSingle) block;

      // if (control.Control.isTracing("liveness.step4")) {
      for (Stm.T stm : b.stms) {
        System.out.print("\nin, out for statement:");
        System.out.println(stm.toString());
        System.out.print("\nin is:");
        for (String str : this.stmLiveIn.get(stm)) {
          System.out.print(str + ", ");
        }
        System.out.print("\nout is:");
        for (String str : this.stmLiveOut.get(stm)) {
          System.out.print(str + ", ");
        }
      }

    }
  }

  @Override
  public void visit(MainMethodSingle m) {
    // Four steps:
    // Step 1: calculate the "gen" and "kill" sets for each
    // statement and transfer
    this.kind = Liveness_Kind_t.StmGenKill;
    for (Block.T block : m.blocks) {
      block.accept(this);
    }

    // Step 2: calculate the "gen" and "kill" sets for each block.
    // For this, you should visit statements and transfers in a
    // block in a reverse order.
    // Your code here:

    // Step 3: calculate the "liveIn" and "liveOut" sets for each block
    // Note that to speed up the calculation, you should first
    // calculate a reverse topo-sort order of the CFG blocks, and
    // crawl through the blocks in that order.
    // And also you should loop until a fix-point is reached.
    // Your code here:

    // Step 4: calculate the "liveIn" and "liveOut" sets for each
    // statement and transfer
    // Your code here:
  }

  // vtables
  @Override
  public void visit(VtableSingle v) {
  }

  // class
  @Override
  public void visit(ClassSingle c) {
  }

  // program
  @Override
  public void visit(ProgramSingle p) {
    p.mainMethod.accept(this);
    for (Method.T mth : p.methods) {
      mth.accept(this);
    }
    return;
  }

}
