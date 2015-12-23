package org.renjin.gcc.codegen.type;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.renjin.gcc.codegen.UnimplementedException;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Generates field definitions, loads and stores for global variables
 */
public abstract class FieldGenerator {
  
  public abstract GimpleType getType();

  public void emitInstanceInit(MethodVisitor mv) {
  }

  public abstract void emitInstanceField(ClassVisitor cv);

  /**
   *
   * @param instanceGenerator an {@code ExprGenerator} that can read the record's instance 
   * @return an {@code ExprGenerator} that can generate loads/stores for this field.
   */
  public abstract ExprGenerator memberExprGenerator(ExprGenerator instanceGenerator);

  /**
   * Emits the bytecode to store a value to the record currently on the stack.
   */
  public void emitStoreMember(MethodVisitor mv, ExprGenerator valueGenerator) {
    throw new UnimplementedException(getClass(), "emitStoreMember");
  }

}