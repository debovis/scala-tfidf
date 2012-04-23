// $Id$
// $Source$
package net.sf.jtmt.postaggers.lucene;

import net.sf.jtmt.postaggers.Pos;

import org.apache.lucene.util.Attribute;

/**
 * Part of Speech Attribute.
 * @author Sujit Pal (spal@healthline.com)
 * @version $Revision$
 */
public interface PosAttribute extends Attribute {
  public void setPos(Pos pos);
  public Pos getPos();
}
