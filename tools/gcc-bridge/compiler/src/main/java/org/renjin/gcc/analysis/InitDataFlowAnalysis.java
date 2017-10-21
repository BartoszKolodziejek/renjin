/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.gcc.analysis;

import org.renjin.gcc.gimple.GimpleBasicBlock;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.*;
import org.renjin.gcc.gimple.statement.GimpleAssignment;
import org.renjin.gcc.gimple.statement.GimpleCall;
import org.renjin.gcc.gimple.statement.GimpleStatement;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.repackaged.guava.base.Optional;
import org.renjin.repackaged.guava.collect.Maps;
import org.renjin.repackaged.guava.collect.Sets;

import java.util.*;

/**
 * Identifies uses of uninitialized variables
 */
public class InitDataFlowAnalysis {

  private ControlFlowGraph cfg;
  
  private Map<Long, GimpleVarDecl> localVariables = Maps.newHashMap();
  private Map<ControlFlowGraph.Node, Set<Long>> entryState = new HashMap<>();
  private Map<ControlFlowGraph.Node, Set<Long>> exitState = new HashMap<>();
  
  public InitDataFlowAnalysis(GimpleFunction function, ControlFlowGraph cfg) {
    this.cfg = cfg;
    for (GimpleVarDecl decl : function.getVariableDeclarations()) {
      localVariables.put(decl.getId(), decl);
    }
  }

  /**
   * Solves the dataflow equation at the boundaries of the basic blocks
   */
  public void solve() {
    
    // Step 1: initialize the state of all nodes
    
    // We know the initial state of all nodes includes
    // _at least_ those variables explicitly initialized
    
    Set<Long> initialState = new HashSet<>();
    for (GimpleVarDecl decl : localVariables.values()) {
      if(decl.getValue() != null) {
        initialState.add(decl.getId());
      }
      // we always have to allocate arrays and records explicitly, because our 
      // arrays are stored on the heap, not the stack
      if (decl.getType() instanceof GimpleArrayType ||
          decl.getType() instanceof GimpleRecordType) {

        initialState.add(decl.getId());
      }
    }
    
    for (ControlFlowGraph.Node node : cfg.getNodes()) {
      entryState.put(node, initialState);
      exitState.put(node, applyTransfer(initialState, node.getBasicBlock()));
    }
    
    // While (sets are still changing)
    boolean changed;
    do {
      changed = false;
      
      for (ControlFlowGraph.Node node : cfg.getBasicBlockNodes()) {
        // recalculate the node's input
        Set<Long> currentEntryState = entryState.get(node);
        Set<Long> updatedEntryState = applyJoin(node);

        if (!updatedEntryState.equals(currentEntryState)) {
          entryState.put(node, updatedEntryState);

          Set<Long> currentExitState = exitState.get(node);
          Set<Long> updatedExitState = applyTransfer(updatedEntryState, node.getBasicBlock());
          if (!currentExitState.equals(updatedExitState)) {
            changed = true;
            exitState.put(node, updatedExitState);
          }
        }
      }
    } while(changed);
  }

  public Set<GimpleVarDecl> getVariablesUsedWithoutInitialization() {

    Set<GimpleVarDecl> result = new HashSet<>();
    
    for (ControlFlowGraph.Node node : cfg.getBasicBlockNodes()) {
      // The set of variables that have been *definitely* initialized
      Set<Long> initialized = new HashSet<>(entryState.get(node));
      
      // Now go statement-by-statement to see if there are any possible
      // uses before definition
      for (GimpleStatement statement : node.getBasicBlock().getStatements()) {
        for (GimpleSymbolRef symbolRef : statement.findVariableUses()) {
          if(localVariables.containsKey(symbolRef.getId())) {
            // we're using a local variable. Are we sure it's been initialized?
            if(!initialized.contains(symbolRef.getId())) {
              result.add(localVariables.get(symbolRef.getId())); 
            }
          }
        }
        
        // update our set of definitely defined variables 
        updateInitializedSet(statement, initialized);
      }
    }
    return result;
  }

  /**
   * Updates a node's entry state as a function of all the incoming node's states.
   */
  private Set<Long> applyJoin(ControlFlowGraph.Node node) {
    
    // a local variable is known to be initialized if has been
    // initialized on all incoming paths
    Iterator<ControlFlowGraph.Node> incomingIt = node.getIncoming().iterator();

    Set<Long> state = new HashSet<>(exitState.get(incomingIt.next()));
    
    while(incomingIt.hasNext()) {
      state = Sets.intersection(state, exitState.get(incomingIt.next()));      
    }
    return state;
  }
  

  private Set<Long> applyTransfer(Set<Long> initialState, GimpleBasicBlock basicBlock) {
    Set<Long> exitState = new HashSet<>(initialState);

    if(basicBlock != null) {
      for (GimpleStatement ins : basicBlock.getStatements()) {
        updateInitializedSet(ins, exitState);
      }
    }
    return exitState;
  }

  /**
   * Updates the set of initialized variables with the given statement
   * @param statement the statement
   * @param initializedVariables the set of variableIds that have definitely been initialized
   */
  private void updateInitializedSet(GimpleStatement statement, Set<Long> initializedVariables) {
    Optional<GimpleVariableRef> variableRef = Optional.absent();
    if (statement instanceof GimpleAssignment) {
      variableRef = findVariableRef(((GimpleAssignment) statement).getLHS());
    } else if (statement instanceof GimpleCall) {
      variableRef = findVariableRef(((GimpleCall) statement).getLhs());
    }
    if (variableRef.isPresent()) {
      initializedVariables.add(variableRef.get().getId());
    }
  }

  private Optional<GimpleVariableRef> findVariableRef(GimpleExpr lhs) {
    if(lhs instanceof GimpleVariableRef) {
      GimpleVariableRef ref = (GimpleVariableRef) lhs;
      // is this a local variable or global variable?
      if(localVariables.containsKey(ref.getId())) {
        return Optional.of(ref);
      } else {
        return Optional.absent();
      }
    } else if(lhs instanceof GimpleMemRef) {
      return findVariableRef(((GimpleMemRef) lhs).getPointer());
    } else if(lhs instanceof GimpleAddressOf) {
      return findVariableRef(((GimpleAddressOf) lhs).getValue());
    } else if(lhs instanceof GimpleComponentRef) {
      return findVariableRef(((GimpleComponentRef) lhs).getValue());
    } else {
      return Optional.absent();
    }
  }

  public void dump() {
    for (ControlFlowGraph.Node node : cfg.getBasicBlockNodes()) {
      System.out.println(node.getId() + ": " + toString(entryState.get(node)) + " -> " + toString(exitState.get(node)));
    }
  }

  private String toString(Set<Long> variableIds) {
    StringBuilder sb = new StringBuilder("[");
    boolean needsComma = false;
    for (Long variableId : variableIds) {
      if(needsComma) {
        sb.append(", ");
      } 
      sb.append(localVariables.get(variableId).getName());
      needsComma = true;
    }
    sb.append("]");
    return sb.toString();
  }
}
