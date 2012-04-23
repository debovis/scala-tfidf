// $Id$
package net.sf.jtmt.indexers.nutch.plugins.indexing;

import net.sf.jtmt.indexers.nutch.plugins.parsing.TagExtractorParseFilter;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.log4j.Logger;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.crawl.Inlinks;
import org.apache.nutch.indexer.IndexingException;
import org.apache.nutch.indexer.IndexingFilter;
import org.apache.nutch.indexer.NutchDocument;
import org.apache.nutch.indexer.lucene.LuceneWriter;
import org.apache.nutch.indexer.lucene.LuceneWriter.INDEX;
import org.apache.nutch.indexer.lucene.LuceneWriter.STORE;
import org.apache.nutch.parse.Parse;

/**
 * The indexing portion of the TagExtractor module. Retrieves the
 * tag information stuffed into the ParseResult object by the parse
 * portion of this module.
 * @author Sujit Pal
 * @version $Revision$
 */
public class TagExtractorIndexFilter implements IndexingFilter {

  private static final Logger LOGGER = Logger.getLogger(TagExtractorIndexFilter.class);
  
  private Configuration conf;
  
  public void addIndexBackendOptions(Configuration conf) {
    LuceneWriter.addFieldOptions(
      TagExtractorParseFilter.TAG_KEY, STORE.YES, INDEX.UNTOKENIZED, conf);
  }

  public NutchDocument filter(NutchDocument doc, Parse parse, Text url,
      CrawlDatum datum, Inlinks inlinks) throws IndexingException {
    String[] tags = 
      parse.getData().getParseMeta().getValues(TagExtractorParseFilter.TAG_KEY);
    if (tags == null || tags.length == 0) {
      return doc;
    }
    // add to the nutch document, the properties of the field are set in
    // the addIndexBackendOptions method.
    for (String tag : tags) {
      LOGGER.debug("Adding tag: [" + tag + "] for URL: " + url.toString());
      doc.add(TagExtractorParseFilter.TAG_KEY, tag);
    }
    return doc;
  }

  public Configuration getConf() {
    return this.conf;
  }

  public void setConf(Configuration conf) {
    this.conf = conf;
  }
}
