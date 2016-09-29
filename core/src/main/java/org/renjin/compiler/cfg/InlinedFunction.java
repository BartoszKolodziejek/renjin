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
package org.renjin.compiler.cfg;

import org.renjin.compiler.NotCompilableException;
import org.renjin.compiler.TypeSolver;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.InlineParamExpr;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.exception.InternalCompilerException;
import org.renjin.compiler.ir.ssa.SsaTransformer;
import org.renjin.compiler.ir.tac.*;
import org.renjin.compiler.ir.tac.expressions.Constant;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.ReadParam;
import org.renjin.compiler.ir.tac.statements.ReturnStatement;
import org.renjin.compiler.ir.tac.statements.Statement;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.Closure;
import org.renjin.sexp.Function;
import org.renjin.sexp.Symbol;

import java.util.List;
import java.util.Map;
import java.util.Set;


public class InlinedFunction {

  private final RuntimeState runtimeState;

  private final SsaTransformer ssaTransformer;
  private final DominanceTree dTree;
  private final ControlFlowGraph cfg;
  private final UseDefMap useDefMap;
  private final TypeSolver types;
  private final List<ReadParam> params;

  private List<ReturnStatement> returnStatements = Lists.newArrayList();
  private Closure closure;

  /**
   * @param closure the closure to inline
   * @param arguments the names of the formals that will be supplied to this inline call
   */
  public InlinedFunction(RuntimeState parentState, Closure closure, Set<Symbol> arguments) {
    this.closure = closure;

    runtimeState = new RuntimeState(parentState, closure.getEnclosingEnvironment());
    
    IRBodyBuilder builder = new IRBodyBuilder(runtimeState);
    IRBody body = builder.buildFunctionBody(closure, arguments);

    cfg = new ControlFlowGraph(body);
    dTree = new DominanceTree(cfg);
    ssaTransformer = new SsaTransformer(cfg, dTree);
    ssaTransformer.transform();
    useDefMap = new UseDefMap(cfg);
    types = new TypeSolver(cfg, useDefMap);
    params = body.getParams();

    for (Statement statement : body.getStatements()) {
      if(statement instanceof ReturnStatement) {
        returnStatements.add((ReturnStatement) statement);
      }
    }
    
    System.out.println("+++ INLINED +++ ");
    System.out.println(cfg);
    System.out.println("+++++++++++++++ ");

  }

  public ControlFlowGraph getCfg() {
    return cfg;
  }

  public SsaTransformer getSsaTransformer() {
    return ssaTransformer;
  }

  public List<ReadParam> getParams() {
    return params;
  }


  public void updateParam(int i, ValueBounds argumentBounds) {
    params.get(i).updateBounds(argumentBounds);

  }
  
  public ValueBounds updateBounds(List<IRArgument> arguments, Map<Expression, ValueBounds> typeMap) {

    for (int i = 0; i < arguments.size(); i++) {
      Expression argumentExpr = arguments.get(i).getExpression();
      ValueBounds argumentBounds;
      if(argumentExpr instanceof Constant) {
        argumentBounds = argumentExpr.getValueBounds();
      } else {
        argumentBounds = typeMap.get(argumentExpr);
      }
      if(argumentBounds == null) {
        throw new IllegalStateException("No argument bounds for " + arguments.get(i).getName());
      }
      updateParam(i, argumentBounds);
    }
    
    return computeBounds();
  }
  
  public ValueBounds computeBounds() {
    
    types.execute();
    
    if(returnStatements.size() == 1) {
      return returnStatements.get(0).getRHS().getValueBounds();
    } else {
      throw new UnsupportedOperationException("TODO");
    }
  }
  
  public void writeInline(EmitContext emitContext, InstructionAdapter mv, IRMatchedArguments matching, List<IRArgument> arguments) {
 

    EmitContext inlineContext = emitContext.inlineContext(cfg, types);

    for (Map.Entry<Symbol, Integer> formal : matching.getMatchedFormals().entrySet()) {
      inlineContext.setInlineParameter(formal.getKey(),
          new InlineParamExpr(emitContext, arguments.get(formal.getValue()).getExpression()));
    }

    // Last check for assumption violations
    types.verifyFunctionAssumptions(runtimeState);

    Label exitLabel = new Label();
    
    for(BasicBlock basicBlock : cfg.getBasicBlocks()) {
      if(basicBlock != cfg.getEntry() && basicBlock != cfg.getExit()) {
        for(IRLabel label : basicBlock.getLabels()) {
          mv.visitLabel(inlineContext.getAsmLabel(label));
        }
        for(Statement stmt : basicBlock.getStatements()) {
          try {
            if (stmt instanceof ReturnStatement) {
              // Instead of returning, just push the return value on the stack
              // and jump to the exit point for the function.
              stmt.getRHS().load(inlineContext, mv);
              mv.goTo(exitLabel);

            } else {
              stmt.emit(inlineContext, mv);
            }
          } catch (NotCompilableException e) {
            throw e;
          } catch (Exception e) {
            throw new InternalCompilerException("Exception compiling statement " + stmt, e);
          }
        }
      }
    }
    mv.mark(exitLabel);
  }
  

  @Override
  public String toString() {
    return cfg.toString();
  }

  public TypeSolver getTypes() {
    return types;
  }

  public Function getClosure() {
    return closure;
  }
}
