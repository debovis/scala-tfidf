package net.sf.jtmt.ontology.relational;

import org.jgrapht.graph.DefaultEdge;

/**
 * Extends DefaultEdge to add a label to the graph. The label is the
 * relationId that relates the entities the edge connects.
 * @author Sujit Pal
 * @version $Revision$
 */
public class RelationEdge extends DefaultEdge {
  
  private static final long serialVersionUID = 1994877217677659613L;

  private long relationId;

  public RelationEdge() {
    super();
  }
  
  public RelationEdge(long relationId) {
    this();
    setRelationId(relationId);
  }
  
  public long getRelationId() {
    return relationId;
  }

  public void setRelationId(long relationId) {
    this.relationId = relationId;
  }
}
