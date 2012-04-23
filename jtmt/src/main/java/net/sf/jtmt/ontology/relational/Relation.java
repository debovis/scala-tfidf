package net.sf.jtmt.ontology.relational;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Holder class to represent an edge in the graph.
 * @author Sujit Pal
 * @version $Revision$
 */
public class Relation implements Serializable {

  private static final long serialVersionUID = 8521110824988338681L;
  
  private long relationId;
  private String relationName;
  
  public Relation() {
    super();
  }

  public long getId() {
    return relationId;
  }

  public void setRelationId(long relationId) {
    this.relationId = relationId;
  }

  public String getName() {
    return relationName;
  }

  public void setRelationName(String relationName) {
    this.relationName = relationName;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.reflectionToString(this, ToStringStyle.NO_FIELD_NAMES_STYLE);
  }
}
