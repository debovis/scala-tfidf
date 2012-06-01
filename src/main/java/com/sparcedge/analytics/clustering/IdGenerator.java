package com.sparcedge.analytics.clustering;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Wrapper over Random to ensure unique ids over a series of invocations.
 * @author Sujit Pal
 * @version $Revision$
 */
public class IdGenerator {

  private int upperBound;
  
  private Random randomizer;
  private Set<Integer> ids = new HashSet<Integer>();
  
  public IdGenerator(int upperBound) {
    this.upperBound = upperBound;
    randomizer = new Random();
  }
  
  public int getNextId() {
    if (ids.size() == upperBound) {
      ids.clear();
    }
    for (;;) {
      int id = randomizer.nextInt(upperBound);
      if (ids.contains(id)) {
        continue;
      }
      ids.add(id);
      return id;
    }
  }
}
