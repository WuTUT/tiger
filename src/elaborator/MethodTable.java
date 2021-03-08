package elaborator;

import java.util.LinkedList;

import ast.Ast.Dec;
import ast.Ast.Type;

public class MethodTable {
  private java.util.Hashtable<String, Type.T> table;
  private java.util.Hashtable<String, Boolean> usedtable;
  private java.util.Hashtable<String, Integer> linetable;

  public MethodTable() {
    this.table = new java.util.Hashtable<String, Type.T>();
    this.usedtable = new java.util.Hashtable<>();
    this.linetable = new java.util.Hashtable<>();

  }

  // Duplication is not allowed
  public void put(LinkedList<Dec.T> formals, LinkedList<Dec.T> locals) {
    for (Dec.T dec : formals) {
      Dec.DecSingle decc = (Dec.DecSingle) dec;
      if (this.table.get(decc.id) != null) {
        System.out.println("Error: duplicated parameter: " + decc.id);
        // System.exit(1);
      }
      this.table.put(decc.id, decc.type);
      this.usedtable.put(decc.id, false);
      this.linetable.put(decc.id, decc.lineNum);
    }

    for (Dec.T dec : locals) {
      Dec.DecSingle decc = (Dec.DecSingle) dec;
      if (this.table.get(decc.id) != null) {
        System.out.println("Error: duplicated variable: " + decc.id);
        // System.exit(1);
      }
      this.table.put(decc.id, decc.type);
      this.usedtable.put(decc.id, false);
      this.linetable.put(decc.id, decc.lineNum);
    }

  }

  // return null for non-existing keys
  public Type.T get(String id) {
    return this.table.get(id);
  }

  public void dump() {
    // new Todo();
    for (String id : this.table.keySet()) {
      System.out.println("    " + this.table.get(id).toString() + "  " + id);
    }

  }

  @Override
  public String toString() {
    return this.table.toString();
  }

  public void setused(String id) {
    this.usedtable.put(id, true);
  }

  public void checkused() {
    for (String id : this.usedtable.keySet()) {
      if (!this.usedtable.get(id)) {
        System.out
            .println("Warning: variable \"" + id + "\" declared at line " + this.linetable.get(id) + " never used");
      }
    }
  }
}
