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
package org.renjin.compiler.pipeline.specialization;

import java.util.Arrays;

/**
 * Uniquely identifies a function specialized by its the classes of its operands
 */
public class SpecializationKey {

  private Class[] classes;
  private int hash;

  public SpecializationKey(Class[] classes) {
    this.classes = classes;
    this.hash = Arrays.hashCode(classes);
  }

  @Override
  public int hashCode() {
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if(!(obj instanceof SpecializationKey)) {
      return false;
    }
    SpecializationKey other = (SpecializationKey)obj;
    return Arrays.equals(classes, other.classes);
  }

  @Override
  public String toString() {
    return "Specialization{" + Arrays.toString(classes) + "}";
  }
}
