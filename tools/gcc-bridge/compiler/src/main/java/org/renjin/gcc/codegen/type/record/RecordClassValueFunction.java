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
package org.renjin.gcc.codegen.type.record;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ArrayElement;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrPair;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.fatptr.WrappedFatPtrExpr;
import org.renjin.gcc.codegen.type.primitive.ConstantValue;
import org.renjin.gcc.codegen.var.LocalVarAllocator;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Optional;

import java.util.Collections;
import java.util.List;

import static org.renjin.gcc.codegen.expr.Expressions.constantInt;


/**
 * Translates a pointer array and offset to a Record value represented by a JVM Class.
 */
public class RecordClassValueFunction implements ValueFunction {
  
  private RecordClassTypeStrategy strategy;

  public RecordClassValueFunction(RecordClassTypeStrategy strategy) {
    this.strategy = strategy;
  }

  @Override
  public Type getValueType() {
    return strategy.getJvmType();
  }

  @Override
  public GimpleType getGimpleValueType() {
    return strategy.getGimpleType();
  }

  @Override
  public int getElementLength() {
    return 1;
  }

  @Override
  public int getArrayElementBytes() {
    return strategy.getRecordTypeDef().getSize() / 8;
  }

  @Override
  public RecordValue dereference(JExpr array, JExpr offset) {
    JExpr castedElement = elementAt(array, offset);
    FatPtrPair address = new FatPtrPair(this, array, offset);
    
    return new RecordValue(castedElement, address);
  }

  @Override
  public GExpr dereference(WrappedFatPtrExpr wrapperInstance) {
    return new RecordValue(wrapperInstance.valueExpr(), wrapperInstance);
  }

  private JExpr elementAt(JExpr array, JExpr offset) {
    ArrayElement element = Expressions.elementAt(array, offset);
    return Expressions.cast(element, strategy.getJvmType());
  }

  @Override
  public List<JExpr> toArrayValues(GExpr expr) {
    return Collections.singletonList(((RecordValue) expr).getRef());
  }

  @Override
  public void memoryCopy(MethodGenerator mv, 
                         JExpr destinationArray, JExpr destinationOffset, 
                         JExpr sourceArray, JExpr sourceOffset, 
                         JExpr valueCount) {
    
    // If we have a small, fixed number of records to copy,
    // unroll the loop
    if(valueCount instanceof ConstantValue) {
      int length = ((ConstantValue) valueCount).getIntValue();
      if(length < 3) {
        for(int i=0;i<length;++i) {
          copyElement(mv, destinationArray, destinationOffset, sourceArray, sourceOffset, constantInt(i));
        }
        return;
      }
    }
    
    // Otherwise,
    // Loop over each element and invoke the set() method
    LocalVarAllocator.LocalVar counter = mv.getLocalVarAllocator().reserve(Type.INT_TYPE);
    counter.store(mv, constantInt(0));

    Label loopHead = new Label();
    Label loopBody = new Label();

    // Initialize our loop counter
    mv.goTo(loopHead);

    // Loop body
    mv.visitLabel(loopBody);

    // Copy record
    copyElement(mv, destinationArray, destinationOffset, sourceArray, sourceOffset, counter);
    
    mv.iinc(counter.getIndex(), 1);

    // Loop head
    mv.visitLabel(loopHead);
    counter.load(mv);
    valueCount.load(mv);
    mv.ificmplt(loopBody);

  }

  @Override
  public void memorySet(MethodGenerator mv, JExpr array, JExpr offset, JExpr byteValue, JExpr length) {
    
    // Call the record's class static memset(record[], offset, byteValue, length) method
    
    array.load(mv);
    offset.load(mv);
    byteValue.load(mv);
    length.load(mv);
    mv.invokestatic(strategy.getJvmType(), "memset", Type.getMethodDescriptor(Type.VOID_TYPE,
        array.getType(), Type.INT_TYPE, Type.INT_TYPE, Type.INT_TYPE));
  }

  private void copyElement(MethodGenerator mv, 
                           JExpr destinationArray, JExpr destinationOffset, 
                           JExpr sourceArray, JExpr sourceOffset, 
                           JExpr index) {
    
    JExpr destRef = elementAt(destinationArray, Expressions.sum(destinationOffset, index));
    JExpr sourceRef = elementAt(sourceArray, Expressions.sum(sourceOffset, index));

    destRef.load(mv);
    sourceRef.load(mv);
    mv.invokevirtual(getValueType(), "set", Type.getMethodDescriptor(Type.VOID_TYPE, getValueType()), false);
  }

  @Override
  public Optional<JExpr> getValueConstructor() {
    return Optional.<JExpr>of(new RecordConstructor(strategy));
  }

  @Override
  public String toString() {
    return "RecordClass[" + strategy.getRecordTypeDef().getName() + "]";
  }

}
