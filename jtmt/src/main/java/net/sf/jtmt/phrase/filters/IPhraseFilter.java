// $Id$
package net.sf.jtmt.phrase.filters;

/**
 * Interface for various phrase filtering mechanisms. Its isPhrase() method
 * is implemented differently in different phrase filters.
 * @author Sujit Pal
 * @version $Revision$
 */
public interface IPhraseFilter {

  public double getPhraseDeterminant();
}
