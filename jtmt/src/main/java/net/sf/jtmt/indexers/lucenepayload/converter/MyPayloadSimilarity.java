package net.sf.jtmt.indexers.lucenepayload.converter;

import org.apache.lucene.analysis.payloads.PayloadHelper;
import org.apache.lucene.search.DefaultSimilarity;

/**
 * Custom similarity to use payload scores during search
 * @author Sujit Pal
 * @version $Revision$
 */
public class MyPayloadSimilarity extends DefaultSimilarity {

  private static final long serialVersionUID = -2402909220013794848L;

  @Override
  public float scorePayload(int docId, String fieldName,
      int start, int end, byte[] payload, int offset, int length) {
    if (payload != null) {
      return PayloadHelper.decodeFloat(payload, offset);
    } else {
      return 1.0F;
    }
  }
}
