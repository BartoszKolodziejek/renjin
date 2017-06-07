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
package org.renjin.invoke.codegen;

import org.renjin.eval.Context;
import org.renjin.sexp.*;


/**
 * Iterates over an argument list, nesting into ... as necessary.
 * 
 * <p>For example:
 * <p>
 * <code>
 * f&lt;-function(...) pow(..., 2)
 * f(8)
 * </code>
 * 
 * <p>When pow(...,2) is reached, the symbol '...` (Symbol.ELIPSES) must be evaluated
 * and merged into the list of arguments. 
 * 
 */
public class ArgumentIterator {

  private Context context;
  private Environment rho;
  private PairList args;
  private PairList ellipses = Null.INSTANCE;
  private String currentName;

  public ArgumentIterator(Context context, Environment rho, PairList args) {
    super();
    this.context = context;
    this.rho = rho;
    this.args = args;
  }
  
  public SEXP evalNext() {  
    PairList.Node node;
    if(ellipses != Null.INSTANCE) {
      node = ((PairList.Node) ellipses);
      ellipses = node.getNext();
      
    } else if(args != Null.INSTANCE){
      node = ((PairList.Node)args);
      args = node.getNext();
      
    } else {
      // we've run out of arguments!
      throw new ArgumentException("too few arguments");
    }
  
    SEXP value = node.getValue();

    if(Symbols.ELLIPSES.equals(value)) {
      ellipses = (PromisePairList) context.evaluate( value, rho);
      return evalNext();

    } else {
      this.currentName = node.getName();
      return context.evaluate(value, rho);
    } 
  }
  
  public SEXP next() {  
    PairList.Node node;
    if(ellipses != Null.INSTANCE) {
      node = ((PairList.Node) ellipses);
      ellipses = node.getNext();
      
    } else if(args != Null.INSTANCE){
      node = ((PairList.Node)args);
      args = node.getNext();
      
    } else {
      // we've run out of arguments!
      throw new ArgumentException("too few arguments");
    }

    this.currentName = node.getName();
    return node.getValue(); 
  }
  
  public PairList.Node nextNode() {  
    PairList.Node node;
    if(ellipses != Null.INSTANCE) {
      node = ((PairList.Node) ellipses);
      ellipses = node.getNext();
      
    } else if(args != Null.INSTANCE){
      node = ((PairList.Node)args);
      args = node.getNext();
      
    } else {
      // we've run out of arguments!
      throw new ArgumentException("too few arguments");
    }
  
    SEXP arg = node.getValue();
    
    if(Symbols.ELLIPSES.equals(arg)) {
      PromisePairList dotdot = (PromisePairList) context.evaluate(arg, rho);
      ellipses = dotdot;
      return nextNode();

    } else {
     
      return node;
    } 
  }

  
  public boolean hasNext() {
    if(ellipses != Null.INSTANCE) {
      return true;
    }
    
    if(args != Null.INSTANCE) {
      SEXP arg = ((PairList.Node)args).getValue();
      if(Symbols.ELLIPSES.equals(arg)) {
        PromisePairList dotdot = (PromisePairList) context.evaluate(arg, rho);
        ellipses = dotdot;
        args = ((PairList.Node)args).getNext();

        return hasNext();
        
      } else {
        return true;
      }
    }
    
    return false;
  }
}
