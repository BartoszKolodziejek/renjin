package org.renjin.compiler.pipeline;

import org.renjin.compiler.pipeline.fusion.LoopKernels;
import org.renjin.compiler.pipeline.fusion.kernel.CompiledKernel;
import org.renjin.compiler.pipeline.node.ComputationNode;
import org.renjin.compiler.pipeline.specialization.SpecializationCache;
import org.renjin.primitives.vector.MemoizedComputation;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.Vector;


/**
 * Fully computes a node and stores its value
 */
public class DeferredNodeComputer implements Runnable {

  private final org.renjin.compiler.pipeline.node.DeferredNode node;

  public DeferredNodeComputer(org.renjin.compiler.pipeline.node.DeferredNode node) {
    this.node = node;
  }

  @Override
  public void run() {

    long start = System.nanoTime();

    // TODO: at the moment, we can compile only a small number of summary
    // function, eventually we want to generate bytecode on the fly based
    // on their implementations elsewhere.
    if(LoopKernels.INSTANCE.supports(((ComputationNode) node))) {
      try {
        Vector[] operands = node.flattenVectors();
        CompiledKernel computer = SpecializationCache.INSTANCE.compile(node);

        Vector result = DoubleArrayVector.unsafe(computer.compute(operands));

        ((MemoizedComputation)node.getVector()).setResult(result);
        node.setResult(result);
      } catch(Throwable e) {
        throw new RuntimeException("Exception compiling node " + node, e);
      }
    } else if(node.getVector() instanceof MemoizedComputation) {
      
      node.setResult(((MemoizedComputation) node.getVector()).forceResult());
    }
    
    if(VectorPipeliner.DEBUG) {
      long time = System.nanoTime() - start;
      System.out.println("Computed " + node + " in " + (time/1e6) + "ms");
    }
  }
}
