package net.sf.jtmt.ontology.relational;

/**
 * A Fact joins two Entity objects with a Relation.
 * @author Sujit Pal 
 * @version $Revision$
 */
public class Fact {

  private long sourceEntityId;
  private long targetEntityId;
  private long relationId;
  
  public Fact() {
    super();
  }
  
  public Fact(long sourceEntityId, long targetEntityId, long relationId) {
    this();
    setSourceEntityId(sourceEntityId);
    setTargetEntityId(targetEntityId);
    setRelationId(relationId);
  }
  
  public long getSourceEntityId() {
    return sourceEntityId;
  }
  
  public void setSourceEntityId(long sourceEntityId) {
    this.sourceEntityId = sourceEntityId;
  }
  
  public long getTargetEntityId() {
    return targetEntityId;
  }
  
  public void setTargetEntityId(long targetEntityId) {
    this.targetEntityId = targetEntityId;
  }
  
  public long getRelationId() {
    return relationId;
  }

  public void setRelationId(long relationId) {
    this.relationId = relationId;
  }
}
