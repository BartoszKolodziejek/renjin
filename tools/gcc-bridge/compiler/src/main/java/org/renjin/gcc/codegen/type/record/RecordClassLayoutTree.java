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

import org.renjin.gcc.gimple.type.GimpleField;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.repackaged.guava.base.Strings;
import org.renjin.repackaged.guava.collect.Sets;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;


public class RecordClassLayoutTree {

  public class Node {

    /**
     * Offset from the beginning of the record, in bits
     */
    private int start;
    

    /**
     * The end of this field, in number of bits from beginning of record
     */
    private int end;
    
    private Set<GimpleField> fields = new HashSet<>();
    
    private Set<GimpleType> types = Sets.newHashSet();
    
    private boolean addressable = false;

    public Node(GimpleField field, int start) {
      this.start = start;
      this.end = this.start + field.getType().getSize();
      addField(field);
    }
    
    public int getSize() {
      return end - start;
    }
    
    private void addField(GimpleField field) {
      fields.add(field);
      types.add(field.getType());
      if(field.isAddressed()) {
        addressable = true;
      }
      if(field.getOffset() + field.getSize() > end) {
        end = field.getOffset() + field.getSize();
      }
    }

    public boolean isAddressable() {
      return addressable;
    }

    public FieldTypeSet typeSet() {
      return new FieldTypeSet(fields);
    }

    public int getOffset() {
      return start;
    }
    
    public String name() {
      String name = null;
      for (GimpleField field : fields) {
        if(field.getName() != null) {
          if(name == null) {
            name = field.getName();
          } else {
            name = Strings.commonPrefix(name, field.getName());
          }
        }
      }
      return Strings.nullToEmpty(name);
    }
    
    public boolean overlap(Node other) {
      if(this.end <= other.start) {
        return false;
      }
      if(this.start >= other.end) {
        return false;
      }
      return true;
    }

    public void addFrom(Node adjacent) {
      this.fields.addAll(adjacent.fields);
      this.types.addAll(adjacent.types);
      this.start = Math.min(this.start, adjacent.start);
      this.end = Math.max(this.end, adjacent.end);
    }

    public Set<GimpleField> getFields() {
      return fields;
    }
  }

  private LinkedList<Node> tree = new LinkedList<>();

  public RecordClassLayoutTree(UnionSet unionSet) {

    // Add the NON-RECORD fields from the union
    for (GimpleRecordTypeDef unionDef : unionSet.getUnions()) {
      for (GimpleField field : unionDef.getFields()) {
        if(!(field.getType() instanceof GimpleRecordType)) {
          if(field.isReferenced()) {
            addField(field);
          }
        }
      }
    }

    // Now add the fields from the records that are members of the union
    for (GimpleRecordTypeDef recordDef : unionSet.getRecords()) {
      for (GimpleField field : recordDef.getFields()) {
        if(field.isReferenced()) {
          addField(field);
        }
      }
    }
  }

  private void addField(GimpleField field) {

    // Bit fields may not begin on a byte boundary,
    // but for our purposes we need to put them in a box
    int fieldStart = (field.getOffset() / 8) * 8;

    ListIterator<Node> it = tree.listIterator();
    while(it.hasNext()) {
      Node node = it.next();
      
      if(node.start > fieldStart) {
        tree.add(it.previousIndex(), new Node(field, fieldStart));
        mergeNodes();
        return;

      } else if(node.start == fieldStart) {
        int oldSize = node.getSize();
        node.addField(field);
        if(node.getSize() != oldSize) {
          mergeNodes();
          return;
        }
      }
    }
    // Add to end
    tree.add(new Node(field, fieldStart));
    mergeNodes();
  }

  private void mergeNodes() {
    ListIterator<Node> it = tree.listIterator();
    Node node = it.next();
    
    while(it.hasNext()) {
      Node adjacent = it.next();
      if(node.overlap(adjacent)) {
        node.addFrom(adjacent);
        it.remove();
      } else {
        node = adjacent;
      }
    }
    
  }

  public LinkedList<Node> getTree() {
    return tree;
  }
}
