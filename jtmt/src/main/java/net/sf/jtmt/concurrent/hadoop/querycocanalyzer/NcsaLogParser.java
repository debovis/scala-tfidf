// $Id$
// $Source$
package net.sf.jtmt.concurrent.hadoop.querycocanalyzer;

import java.util.ArrayList;
import java.util.List;

/**
 * A stripped down version of the NCSA Log parser. There is no attempt
 * to identify the fields here. The log line is tokenized by whitespace,
 * unless the current token begins with a quote (ie multi word fields
 * such as user_agent) or with a square bracket (ie datetime group). 
 * Caller is responsible for identifying which token to use. Using this
 * approach simplifies the logic a lot, and also ensures that only the
 * processing that is absolutely necessary get done, ie we are not 
 * spending cycles to parse out the contents of the datetime or the 
 * request group unless we want to. If we do, then the parsing is done
 * in the caller code - at this point, the extra parsing can be as simple 
 * as calling String.split() with the appropriate delimiters.
 * 
 * @author Sujit Pal
 * @version $Revision$
 */
public class NcsaLogParser {
  
  public static List<String> parse(String logline) {
    List<String> tokens = new ArrayList<String>();
    StringBuilder buf = new StringBuilder();
    char[] lc = logline.toCharArray();
    boolean inQuotes = false;
    boolean inBrackets = false;
    for (int i = 0; i < lc.length; i++) {
      if (lc[i] == '"') {
        inQuotes = inQuotes ? false : true;
      } else if (lc[i] == '[') {
        inBrackets = true;
      } else if (lc[i] == ']') {
        if (inBrackets) {
          inBrackets = false;
        }
      } else if (lc[i] == ' ' && (! inQuotes) && (! inBrackets)) {
        tokens.add(buf.toString());
        buf = new StringBuilder();
      } else {
        buf.append(lc[i]);
      }
    }
    if (buf.length() > 0) {
      tokens.add(buf.toString());
    }
    return tokens;
  }
}
