package org.renjin.gcc.codegen.field;

import org.objectweb.asm.ClassVisitor;
import org.renjin.gcc.codegen.RecordClassGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

public class RecordArrayFieldGenerator implements FieldGenerator {

  private GimpleArrayType arrayType;
  private String className;
  private String fieldName;
  private RecordClassGenerator recordGenerator;

  public RecordArrayFieldGenerator(String className, String fieldName, 
                                   RecordClassGenerator recordGenerator, GimpleArrayType arrayType) {
    this.className = className;
    this.fieldName = fieldName;
    this.recordGenerator = recordGenerator;
    this.arrayType = arrayType;
  }
  @Override
  public void emitStaticField(ClassVisitor cv) {
    emitField(ACC_PUBLIC | ACC_STATIC, cv);
  }

  @Override
  public void emitInstanceField(ClassVisitor cv) {
    emitField(ACC_PUBLIC, cv);
  }

  private void emitField(int access, ClassVisitor cv) {
    cv.visitField(access, fieldName, "[" + recordGenerator.getDescriptor(), null, null).visitEnd();
  }

  @Override
  public ExprGenerator staticExprGenerator() {
    return new StaticArrayExpr();
  }

  @Override
  public ExprGenerator memberExprGenerator(ExprGenerator instanceGenerator) {
    throw new UnsupportedOperationException();
  }
  
  private class StaticArrayExpr extends AbstractExprGenerator {

    @Override
    public GimpleType getGimpleType() {
      return arrayType;
    }
    
  }
  
}
