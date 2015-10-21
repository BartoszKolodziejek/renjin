package org.renjin.gcc.codegen.param;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.LocalVarAllocator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.var.VarGenerator;

import java.util.List;

public abstract class ParamGenerator {


  /**
   * @return number of local variable slots occupied by this parameter
   */
  public abstract int numSlots();

  /**
   * 
   * @return one or more parameter types to which this parameter maps
   */
  public abstract List<Type> getParameterTypes();

  /**
   * Emits any bytecode necessary to initialize the parameter at the start of the generated method body, 
   * and returns an ExprGenerator which can be used to retrieve its value
   * 
   * @param methodVisitor MethodVisitor to write its value
   * @param startIndex the first index among the parameters
   * @param localVars an {@link LocalVarAllocator} which can be used to reserve additional local variable slots
   *                   if needed.
   * @return an {@code ExprGenerator} which can be used to access this parameter's value.
   */
  public abstract ExprGenerator emitInitialization(MethodVisitor methodVisitor, int startIndex, LocalVarAllocator localVars);

  public abstract void emitPushParameter(MethodVisitor mv, ExprGenerator parameterValueGenerator);
}
