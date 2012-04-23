package net.sf.jtmt.ontology.relational;

/**
 * Custom Exception thrown by Ontology Update operations.
 * @author Sujit Pal
 * @version $Revision$
 */
public class OntologyException extends Exception {

  private static final long serialVersionUID = 8631563841769982676L;
  
  public OntologyException(String message) {
    super(message);
  }
  
  public OntologyException(String message, Throwable t) {
    super(message, t);
  }
}
