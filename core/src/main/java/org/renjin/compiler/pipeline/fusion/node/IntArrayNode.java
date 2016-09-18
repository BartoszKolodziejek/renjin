package org.renjin.compiler.pipeline.fusion.node;

import org.renjin.compiler.pipeline.ComputeMethod;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.MethodVisitor;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Optional;

import static org.renjin.repackaged.asm.Opcodes.*;

public class IntArrayNode extends LoopNode {

  /**
   * The local variable where we're storing the
   * raw array, double[]
   */
  protected int arrayLocalIndex;
  protected int operandIndex;
  private String vectorType;

  public IntArrayNode(int operandIndex, Type vectorType) {
    this.operandIndex = operandIndex;
    this.vectorType = vectorType.getInternalName();
  }

  public void init(ComputeMethod method) {

    arrayLocalIndex = method.reserveLocal(1);

    MethodVisitor mv = method.getVisitor();
    mv.visitVarInsn(ALOAD, method.getOperandsLocalIndex());
    pushIntConstant(mv, operandIndex);
    mv.visitInsn(AALOAD);
    mv.visitTypeInsn(CHECKCAST, vectorType);
    mv.visitMethodInsn(INVOKEVIRTUAL, vectorType, "toIntArrayUnsafe", "()[I", false);
    mv.visitVarInsn(ASTORE, arrayLocalIndex);
  }

  @Override
  public void pushLength(ComputeMethod method) {
    MethodVisitor mv = method.getVisitor();
    mv.visitVarInsn(ALOAD, arrayLocalIndex);
    mv.visitInsn(ARRAYLENGTH);
  }

  @Override
  public void pushElementAsDouble(ComputeMethod method, Optional<Label> integerNaLabel) {
    pushElementAsInt(method, integerNaLabel);
    MethodVisitor mv = method.getVisitor();
    mv.visitInsn(I2D);
  }

  @Override
  public void pushElementAsInt(ComputeMethod method, Optional<Label> integerNaLabel) {
    MethodVisitor mv = method.getVisitor();
    mv.visitVarInsn(ALOAD, arrayLocalIndex);
    mv.visitInsn(SWAP);
    mv.visitInsn(IALOAD);
    doIntegerNaCheck(mv, integerNaLabel);
  }

  @Override
  public boolean mustCheckForIntegerNAs() {
    return true;
  }

  @Override
  public String toString() {
    return "x" + operandIndex;
  }
}
