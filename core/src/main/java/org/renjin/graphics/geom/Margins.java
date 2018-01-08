/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
package org.renjin.graphics.geom;

import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

public class Margins {
  
  private double bottom;
  private double left;
  private double top;
  private double right;
  
  public Margins() {
    
  }
  
  public Margins(double bottom, double left, double top, double right) {
    super();
    this.bottom = bottom;
    this.left = left;
    this.top = top;
    this.right = right;
  }
  
   public static Margins fromExp(SEXP exp) {
     if(!(exp instanceof Vector)) {
       throw new IllegalArgumentException("vector required");
     } 
     Vector vector = (Vector)exp;
     if(exp.length() != 4) {
       throw new IllegalArgumentException("vector of length 4 required");
     }
     return new Margins(
         vector.getElementAsDouble(0),
         vector.getElementAsDouble(1),
         vector.getElementAsDouble(2),
         vector.getElementAsDouble(3));
   }

  public double getBottom() {
    return bottom;
  }

  public double getLeft() {
    return left;
  }

  public double getTop() {
    return top;
  }

  public double getRight() {
    return right;
  }
  
  public Vector toVector() {
    return new DoubleArrayVector(bottom, left, top, right);
  }

  public Margins multiplyBy(Dimension size) {
    return new Margins(
        bottom * size.getHeight(), 
        left * size.getWidth(), 
        top * size.getHeight(), 
        right * size.getWidth());
  }
}
