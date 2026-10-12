// OneToManyTable.java

/*
** Copyright (c) 1998 by the Java Lobby
** <mailto:jfa@javalobby.org>  <http://www.javalobby.org>
** 
** This program is free software.
** 
** You may redistribute it and/or modify it under the terms of the JFA
** license as described in the LICENSE file included with this 
** distribution.  If the license is not included with this distribution,
** you may find a copy on the web at 'http://javalobby.org/jfa/license.html'
**
** THIS SOFTWARE IS PROVIDED AS-IS WITHOUT WARRANTY OF ANY KIND,
** NOT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY. THE AUTHOR
** OF THIS SOFTWARE, ASSUMES _NO_ RESPONSIBILITY FOR ANY
** CONSEQUENCE RESULTING FROM THE USE, MODIFICATION, OR
** REDISTRIBUTION OF THIS SOFTWARE. 
** 
*/

package org.openscience.nmrshiftdb.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

public class OneToManyTable<S,T> {

  protected Hashtable<S,ArrayList<T>> table = new Hashtable<S,ArrayList<T>>();

  public OneToManyTable() {}

  public boolean put(S one, T element) {
    if(table.containsKey(one)) {
      ArrayList<T> c = table.get(one);
      return c.add(element);
    }
    else {
      ArrayList<T> c = new ArrayList<T>();
      table.put(one, c);
      return c.add(element);
    }
  }

  public ArrayList<T> get(S one) {
    return table.get(one);
  }

  public boolean contains(S one) {
    return table.containsKey(one);
  }

  public void remove(S one) {
    table.remove(one);
  }

  public Enumeration<S> ones() {
    return table.keys();
  }

  public Iterator<T> many(Object one) {
    ArrayList<T> c = table.get(one);
    if(c != null)
      return c.iterator();

    return null;
  }
}
