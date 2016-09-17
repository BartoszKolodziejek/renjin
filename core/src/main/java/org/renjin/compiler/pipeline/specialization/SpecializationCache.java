package org.renjin.compiler.pipeline.specialization;

import org.renjin.compiler.pipeline.fusion.kernel.CompiledKernel;
import org.renjin.compiler.pipeline.node.DeferredNode;
import org.renjin.repackaged.guava.util.concurrent.SettableFuture;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * Maintains a cache of recently used JITted classes.
 */
public class SpecializationCache {

  public static final SpecializationCache INSTANCE = new SpecializationCache();

  private final ConcurrentHashMap<SpecializationKey, Future<CompiledKernel>> cache;

  private SpecializationCache() {
    cache = new ConcurrentHashMap<SpecializationKey, Future<CompiledKernel>>();
  }

  public CompiledKernel compile(DeferredNode node) {
    SpecializationKey key = node.jitKey();
    try {

      Future<CompiledKernel> existingSpecialization = cache.get(key);
      if(existingSpecialization != null) {
        return existingSpecialization.get();
      }
      // Immediately set the Future so that other threads that need this specialization
      // wait for this compilation to finish before starting compilation on their own
      SettableFuture<CompiledKernel> newlyCompiledSpecialization = SettableFuture.create();
      existingSpecialization = cache.putIfAbsent(key, newlyCompiledSpecialization);
      if (existingSpecialization != null) {
        return existingSpecialization.get();
      }

      throw new UnsupportedOperationException();
//      
//      //
//      LoopKernelCompiler jitter = new LoopKernelCompiler();
//      newlyCompiledSpecialization.set(jitter.compile(node));
//
//      return newlyCompiledSpecialization.get();
    } catch (Exception e) {
      throw new RuntimeException("Failed to compile " + key, e);
    }
  }

}
