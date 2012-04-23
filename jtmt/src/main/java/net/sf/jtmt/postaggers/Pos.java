package net.sf.jtmt.postaggers;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;
import org.apache.commons.lang.StringUtils;

import edu.mit.jwi.item.POS;

/**
 * Enumeration of Parts of Speech being considered. Conversions from
 * Brown Tags and Wordnet tags are handled by convenience methods.
 * @author Sujit Pal
 * @version $Revision$
 */
public enum Pos {

  NOUN, VERB, ADJECTIVE, ADVERB, OTHER;

  private static Map<String,Pos> bmap = null;
  private static BidiMap<Pos,POS> wmap = null;
  private static final String translationFile = "src/main/resources/brown_tags.txt";
  
  public static Pos fromBrownTag(String btag) throws Exception {
    if (bmap == null) {
      bmap = new HashMap<String,Pos>();
      BufferedReader reader = new BufferedReader(new InputStreamReader(
          new FileInputStream(translationFile)));
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.startsWith("#")) {
          continue;
        }
        String[] cols = StringUtils.split(line, "\t");
        bmap.put(StringUtils.lowerCase(cols[0]), Pos.valueOf(cols[1])); 
      }
      reader.close();
    }
    Pos pos = bmap.get(btag);
    if (pos == null) {
      return Pos.OTHER;
    }
    return pos;
  }

  public static Pos fromWordnetPOS(POS pos) {
    if (wmap == null) {
      wmap = buildPosBidiMap();
    }
    return wmap.getKey(pos);
  }
  
  public static POS toWordnetPos(Pos pos) {
    if (wmap == null) {
      wmap = buildPosBidiMap();
    }
    return wmap.get(pos);
  }

  private static BidiMap<Pos,POS> buildPosBidiMap() {
    wmap = new DualHashBidiMap<Pos, POS>();
    wmap.put(Pos.NOUN, POS.NOUN);
    wmap.put(Pos.VERB, POS.VERB);
    wmap.put(Pos.ADJECTIVE, POS.ADJECTIVE);
    wmap.put(Pos.ADVERB, POS.ADVERB);
    wmap.put(Pos.OTHER, null);
    return wmap;
  }
}
