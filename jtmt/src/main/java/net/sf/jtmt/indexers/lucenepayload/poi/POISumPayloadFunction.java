// $Id$
// $Source$
package net.sf.jtmt.indexers.lucenepayload.poi;

import org.apache.lucene.search.payloads.AveragePayloadFunction;

/**
 * Returns the sum of the matched custom POI scores instead of averaging
 * them, since we want the scores to be additive for our ranking.
 * @author Sujit Pal
 * @version $Revision$
 */
public class POISumPayloadFunction extends AveragePayloadFunction {

  private static final long serialVersionUID = -3478867768985954830L;

  @Override
  public float docScore(int docId, String field, int numPayloadsSeen, 
      float payloadScore) {
    return numPayloadsSeen > 0 ? payloadScore : 1;
  }
}
