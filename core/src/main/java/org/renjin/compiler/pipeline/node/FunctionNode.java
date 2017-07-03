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
package org.renjin.compiler.pipeline.node;

import org.renjin.compiler.pipeline.specialization.SpecializationKey;
import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.primitives.vector.MemoizedComputation;
import org.renjin.repackaged.asm.Type;
import org.renjin.sexp.*;

/**
 * Node that applies a function to one or more vector operands.
 */
public class FunctionNode extends DeferredNode implements Runnable {

  private DeferredComputation vector;
  private Vector result;

  public FunctionNode(DeferredComputation vector) {
    super();
    this.vector = vector;
  }

  public void replaceVector(DeferredComputation vector) {
    this.vector = vector;
  }

  @Override
  public String getDebugLabel() {
    return vector.getComputationName();
  }

  @Override
  public Vector getVector() {
    return vector;
  }

  @Override
  public NodeShape getShape() {
    if(vector instanceof MemoizedComputation) {
      return NodeShape.ELLIPSE;
    } else {
      return NodeShape.PARALLELOGRAM;
    }
  }

  @Override
  public Type getResultVectorType() {
    if(vector instanceof DoubleVector) {
      return Type.getType(DoubleArrayVector.class);
    } else if(vector instanceof IntArrayVector) {
      return Type.getType(IntArrayVector.class);
    } else if(vector instanceof LogicalVector) {
      return Type.getType(LogicalArrayVector.class);
    } else {
      throw new UnsupportedOperationException("TODO: " + vector.getClass().getName());
    }
  }


  public SpecializationKey jitKey() {
//    List<DeferredNode> nodes = flatten();
//    Class[] classes = new Class[nodes.size()];
//    for(int i=0;i!=classes.length;++i) {
//      classes[i] = nodes.get(i).getVector().getClass();
//    }
//    return new SpecializationKey(classes);
    throw new UnsupportedOperationException("TODO");
  }


  public String getComputationName() {
    return vector.getComputationName();
  }

  @Override
  public void run() {
    if(vector instanceof MemoizedComputation) {
      this.result = ((MemoizedComputation) vector).forceResult();
    } else if(vector instanceof DoubleVector) {
      this.result = DoubleArrayVector.unsafe(vector.toDoubleArray());
    } else if(vector instanceof IntVector) {
      this.result = IntArrayVector.unsafe(vector.toIntArray());
    } else if(vector instanceof LogicalVector) {
      this.result = LogicalArrayVector.unsafe(vector.toIntArray());
    } else {
      throw new UnsupportedOperationException("vector: " + vector.getClass().getName());
    }
  }
}
