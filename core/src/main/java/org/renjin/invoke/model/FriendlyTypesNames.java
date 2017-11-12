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
package org.renjin.invoke.model;

import org.renjin.sexp.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Converts Java class to "R-friendly" names
 */
class FriendlyTypesNames {

  private static FriendlyTypesNames INSTANCE = null;

  private Map<Class, String> names;

  private FriendlyTypesNames() {
    names = new HashMap<Class, String>();
    names.put(SEXP[].class, "...");
    names.put(SEXP.class, "any");
    names.put(LogicalVector.class, SEXPType.LGLSXP.typeName());
    names.put(Logical.class, SEXPType.LGLSXP.typeName());
    names.put(Boolean.class, SEXPType.LGLSXP.typeName());
    names.put(Boolean.TYPE, SEXPType.LGLSXP.typeName());
    names.put(IntVector.class, SEXPType.INTSXP.typeName());
    names.put(Integer.class, SEXPType.INTSXP.typeName());
    names.put(Integer.TYPE, SEXPType.INTSXP.typeName());
    names.put(DoubleVector.class, SEXPType.REALSXP.typeName());
    names.put(Double.class,  SEXPType.REALSXP.typeName());
    names.put(Double.TYPE, SEXPType.REALSXP.typeName());
    names.put(String.class,  SEXPType.STRSXP.typeName());
    names.put(StringVector.class, SEXPType.STRSXP.typeName());
    names.put(ListVector.class, SEXPType.VECSXP.typeName());
    names.put(PairList.Node.class, SEXPType.LISTSXP.typeName());
  }

  public static FriendlyTypesNames get() {
    if(INSTANCE == null) {
      INSTANCE = new FriendlyTypesNames();
    }
    return INSTANCE;
  }

  public String format(Class clazz) {
    if(names.containsKey(clazz)) {
      return names.get(clazz);
    } else {
      return clazz.getSimpleName();
    }
  }
}
