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
package org.renjin.gcc.codegen.type.primitive;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.array.ArrayExpr;
import org.renjin.gcc.codegen.array.ArrayTypeStrategies;
import org.renjin.gcc.codegen.array.ArrayTypeStrategy;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.fatptr.*;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.type.primitive.op.CastGenerator;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.codegen.vptr.VPtrStrategy;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Optional;
import org.renjin.repackaged.guava.base.Preconditions;

import java.lang.reflect.Field;

/**
 * Strategy for dealing with primitive types.
 * 
 * <p>This is the easiest case, because there is (mostly) a one-to-one correspondence between primitive
 * types in {@code Gimple} and those of the JVM.</p>
 */
public class PrimitiveTypeStrategy implements SimpleTypeStrategy<PrimitiveValue> {
  
  private GimplePrimitiveType type;

  public PrimitiveTypeStrategy(GimplePrimitiveType type) {
    this.type = type;
  }

  public PrimitiveTypeStrategy(Class<?> type) {
    Preconditions.checkArgument(type.isPrimitive());
    this.type = GimplePrimitiveType.fromJvmType(Type.getType(type));
  }

  public GimplePrimitiveType getType() {
    return type;
  }

  @Override
  public ParamStrategy getParamStrategy() {
    return new PrimitiveParamStrategy(type.jvmType());
  }

  @Override
  public ReturnStrategy getReturnStrategy() {
    return new SimpleReturnStrategy(this);
  }

  public ValueFunction getValueFunction() {
    return valueFunction();
  }

  @Override
  public FieldStrategy addressableFieldGenerator(Type className, String fieldName) {
    return new AddressableField(className, fieldName, valueFunction());
  }

  @Override
  public FieldStrategy fieldGenerator(Type className, String fieldName) {
    return new PrimitiveFieldStrategy(className, fieldName, type);
  }

  @Override
  public PrimitiveValue variable(GimpleVarDecl decl, VarAllocator allocator) {
    if(decl.isAddressable()) {
      JLValue unitArray = allocator.reserveUnitArray(decl.getNameIfPresent(), type.jvmType(), Optional.<JExpr>absent());
      FatPtrPair address = new FatPtrPair(valueFunction(), unitArray);
      JExpr value = Expressions.elementAt(address.getArray(), 0);
      return new PrimitiveValue(value, address);
      
    } else {
      return new PrimitiveValue(allocator.reserve(decl.getNameIfPresent(), type.jvmType()));
    }
  }

  @Override
  public PrimitiveValue providedGlobalVariable(GimpleVarDecl decl, Field javaField) {
    Type javaType = Type.getType(javaField.getType());
    if(!javaType.equals(this.type.jvmType())) {
      throw new UnsupportedOperationException("Cannot map global variable " + decl + " to JVM field of type " + javaField + ". " +
          "Expected static field of type " + this.type.jvmType());
    }

    return new PrimitiveValue(Expressions.staticField(javaField));
  }

  @Override
  public PrimitiveValue constructorExpr(ExprFactory exprFactory, MethodGenerator mv, GimpleConstructor value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public PointerTypeStrategy pointerTo() {
    return new VPtrStrategy();
  }

  @Override
  public ArrayTypeStrategy arrayOf(GimpleArrayType arrayType) {
    return ArrayTypeStrategies.of(arrayType, valueFunction());
  }

  @Override
  public PrimitiveValue cast(MethodGenerator mv, GExpr value, TypeStrategy typeStrategy) throws UnsupportedCastException {
    
    if(value instanceof ArrayExpr) {
      GExpr first = ((ArrayExpr) value).first();
      if(first instanceof PrimitiveValue) {
        return (PrimitiveValue) first;
      }
    }
    
    if(typeStrategy instanceof PrimitiveTypeStrategy) {
      // Handle casts between primitive types and signed/unsigned
      GimplePrimitiveType valueType = ((PrimitiveTypeStrategy) typeStrategy).getType();
      PrimitiveValue primitiveValue = (PrimitiveValue) value;
      return new PrimitiveValue(new CastGenerator(primitiveValue.unwrap(), valueType, this.type));
    }
    
    if(typeStrategy instanceof FatPtrStrategy) {
      return ((FatPtrStrategy) typeStrategy).toInt(mv, (FatPtr) value);
    
    } else if(value instanceof RefPtrExpr) {
      RefPtrExpr ptrExpr = (RefPtrExpr) value;
      return new PrimitiveValue(Expressions.identityHash(ptrExpr.unwrap()));
    }
    
    throw new UnsupportedCastException();
  }
  
  public PrimitiveValue zero() {
    return new PrimitiveValue(new ConstantValue(type.jvmType(), 0));
  }

  private PrimitiveValueFunction valueFunction() {
    return new PrimitiveValueFunction(type);
  }

  @Override
  public String toString() {
    return "PrimitiveTypeStrategy[" + type + "]";
  }

  @Override
  public Type getJvmType() {
    return type.jvmType();
  }

  @Override
  public PrimitiveValue wrap(JExpr expr) {
    Preconditions.checkArgument(expr.getType().equals(getJvmType()));
    
    return new PrimitiveValue(expr);
  }
}
