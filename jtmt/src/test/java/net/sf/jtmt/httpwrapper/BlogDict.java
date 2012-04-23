// $Id$
package net.sf.jtmt.httpwrapper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * Simple testing java object that will be exposed by Joh.
 * @author Sujit Pal
 * @version $Revision$
 */
public class BlogDict {

  private String file;
  
  private Set<String> labels;
  private Map<String,Set<String>> synonyms;
  private Map<String,Set<String>> categories;
  
  public BlogDict(String file) {
    this.file = file;
    init();
  }
  
  public Set<String> getLabels() {
    return labels;
  }
  
  public Set<String> getSynonyms(String label) {
    if (synonyms.containsKey(label)) {
      return synonyms.get(label);
    } else {
      return Collections.emptySet();
    }
  }
  
  public Set<String> getCategories(String label) {
    if (categories.containsKey(label)) {
      return categories.get(label);
    } else {
      return Collections.emptySet();
    }
  }
  
  protected void init() {
    this.labels = new HashSet<String>();
    this.synonyms = new HashMap<String,Set<String>>();
    this.categories = new HashMap<String,Set<String>>();
    try {
      BufferedReader reader = new BufferedReader(new FileReader(file));
      String line = null;
      while ((line = reader.readLine()) != null) {
        if (line.startsWith("#")) {
          continue;
        }
        String[] cols = StringUtils.splitPreserveAllTokens(line, ":");
        this.labels.add(cols[0]);
        this.synonyms.put(cols[0], new HashSet<String>(
          Arrays.asList(StringUtils.split(cols[1], ","))));
        this.categories.put(cols[0], new HashSet<String>(
          Arrays.asList(StringUtils.split(cols[2], ","))));
      }
      reader.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
