// $Id$
// $Source$
package net.sf.jtmt.crawling.gdata;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections15.Bag;
import org.apache.commons.collections15.bag.HashBag;
import org.apache.commons.lang.StringUtils;

/**
 * TODO: Class level Javadocs
 * @author Sujit Pal
 * @version $Revision$
 */
public class CategoryCounter {

  public void count() throws Exception {
    BufferedReader reader = new BufferedReader(new FileReader("src/test/resources/data/blog/category.txt"));
    final Bag<String> categoryBag = new HashBag<String>();
    String line;
    while ((line = reader.readLine()) != null) {
      String[] parts = StringUtils.split(line, "|");
      categoryBag.add(parts[1]);
    }
    reader.close();
    List<String> categories = new ArrayList<String>();
    categories.addAll(categoryBag.uniqueSet());
    Collections.sort(categories, new Comparator<String>() {
      public int compare(String s1, String s2) {
        int count1 = categoryBag.getCount(s1);
        int count2 = categoryBag.getCount(s2);
        if (count1 == count2) {
          return 0;
        } else {
          return (count2 > count1 ? 1 : -1);
        }
      }
    });
    for (String category : categories) {
      System.out.println(category + " => " + categoryBag.getCount(category));
    }
  }
}
