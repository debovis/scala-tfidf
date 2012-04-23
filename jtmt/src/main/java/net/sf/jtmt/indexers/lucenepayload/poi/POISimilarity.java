// $Id$
// $Source$
package net.sf.jtmt.indexers.lucenepayload.poi;

import org.apache.lucene.analysis.payloads.PayloadHelper;
import org.apache.lucene.search.DefaultSimilarity;

/**
 * Custom similarity for POI.
 * @author Sujit Pal
 * @version $Revision$
 */
public class POISimilarity extends DefaultSimilarity {

  private static final long serialVersionUID = -909003452363957475L;

  @Override
  public float scorePayload(int docId, String fieldName,
      int start, int end, byte[] payload, int offset, int length) {
    if (payload != null) {
      float score = PayloadHelper.decodeFloat(payload, offset);
      return score;
    } else {
      return 1.0F;
    }
  }
}
