// $Id$
// $Source$
package net.sf.jtmt.postaggers.lucene;

import net.sf.jtmt.postaggers.Pos;

import org.apache.lucene.util.AttributeImpl;

/**
 * TODO: class level javadocs
 * @author Sujit Pal (spal@healthline.com)
 * @version $Revision$
 */
public class PosAttributeImpl extends AttributeImpl implements PosAttribute {

  private static final long serialVersionUID = -8416041956010464591L;

  private Pos pos = Pos.OTHER;
  
  public Pos getPos() {
    return pos;
  }

  public void setPos(Pos pos) {
    this.pos = pos;
  }

  @Override
  public void clear() {
    this.pos = Pos.OTHER;
  }

  @Override
  public void copyTo(AttributeImpl target) {
    ((PosAttributeImpl) target).setPos(pos);
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if (other instanceof PosAttributeImpl) {
      return pos == ((PosAttributeImpl) other).getPos();
    }
    return false;
  }

  @Override
  public int hashCode() {
    return pos.ordinal();
  }
}
