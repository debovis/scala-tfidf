// $Id$
package net.sf.jtmt.indexers.nutch.plugins.parsing;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.Logger;
import org.apache.nutch.metadata.Metadata;
import org.apache.nutch.parse.HTMLMetaTags;
import org.apache.nutch.parse.HtmlParseFilter;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.parse.ParseResult;
import org.apache.nutch.protocol.Content;
import org.w3c.dom.DocumentFragment;

/**
 * The parse portion of the Tag Extractor module. Parses out blog tags 
 * from the body of the document and sets it into the ParseResult object.
 * @author Sujit Pal
 * @version $Revision$
 */
public class TagExtractorParseFilter implements HtmlParseFilter {

  public static final String TAG_KEY = "labels";
  
  private static final Logger LOG = Logger.getLogger(TagExtractorParseFilter.class);
  
  private static final Pattern tagPattern = Pattern.compile(">(\\w+)<");
  
  private Configuration conf;

  /**
   * We use regular expressions to parse out the Labels section from
   * the section snippet shown below:
   * <pre>
   * Labels:
   * <a href='http://sujitpal.blogspot.com/search/label/ror' rel='tag'>ror</a>,
   * ...
   * </span>
   * </pre>
   * Accumulate the tag values into a List, then stuff the list into the
   * parseResult with a well-known key (exposed as a public static variable
   * here, so the indexing filter can pick it up from here).
   */
  public ParseResult filter(Content content, ParseResult parseResult,
      HTMLMetaTags metaTags, DocumentFragment doc) {
    LOG.debug("Parsing URL: " + content.getUrl());
    BufferedReader reader = new BufferedReader(
      new InputStreamReader(new ByteArrayInputStream(content.getContent())));
    String line;
    boolean inTagSection = false;
    List<String> tags = new ArrayList<String>();
    try {
      while ((line = reader.readLine()) != null) {
        if (line == null) {
          continue;
        }
        if (line.contains("Labels:")) {
          inTagSection = true;
          continue;
        }
        if (inTagSection && line.contains("</span>")) {
          inTagSection = false;
          break;
        }
        if (inTagSection) {
          Matcher m = tagPattern.matcher(line);
          if (m.find()) {
            LOG.debug("Adding tag=" + m.group(1));
            tags.add(m.group(1));
          }
        }
      }
      reader.close();
    } catch (IOException e) {
      LOG.warn("IOException encountered parsing file:", e);
    }
    Parse parse = parseResult.get(content.getUrl());
    Metadata metadata = parse.getData().getParseMeta();
    for (String tag : tags) {
      metadata.add(TAG_KEY, tag);
    }
    return parseResult;
  }

  public Configuration getConf() {
    return conf;
  }

  public void setConf(Configuration conf) {
    this.conf = conf;
  }
}
