// $Id$
package net.sf.jtmt.inferencing.prolog;

import java.io.Console;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jpl.Atom;
import jpl.JPL;
import jpl.Query;
import jpl.Term;
import jpl.Util;
import jpl.Variable;
import jpl.fli.Prolog;

/**
 * A JPL client to the Prolog rules for the Drools Petstore Example.
 * The basic idea is that the client calls the checkout goal repeatedly
 * until it succeeds. If it fails, the checkout goal will assert a 
 * question which needs to be answered before the goal can proceed
 * further, which the Java client reads and answers.
 * @author Sujit Pal
 * @version $Revision$
 */
public class PrologPetstoreClient {

  private final String RULES_FILE = 
    "src/main/prolog/net/sf/jtmt/inferencing/prolog/petstore2.pro";
  
  /**
   * Connects to the engine and injects the rules file into it. This is
   * done once during the lifetime of the engine.
   * @throws Exception if thrown.
   */
  public void init() throws Exception {
    JPL.init();
    Query consultQuery = new Query("consult", new Term[] {new Atom(RULES_FILE)});
    if (! consultQuery.hasSolution()) {
      throw new Exception("File not found: " + RULES_FILE);
    }
    consultQuery.close();
  }
  
  /**
   * Stops the prolog engine.
   * @throws Exception if thrown.
   */
  public void destroy() throws Exception {
    Query haltQuery = new Query("halt");
    haltQuery.hasSolution();
    haltQuery.close();
  }
  
  /**
   * Called by client after the PetstoreCart reaches the checkout phase.
   * This method is unsynchronized because it will go back and forth for
   * user input. This is the version that is expected to be called from
   * the client.
   * @param cart the Petstore cart object.
   * @return the processed cart object.
   */
  public PetstoreCart checkout(PetstoreCart cart) {
    return checkout(cart, null);
  }
  
  /**
   * Overloaded version for testing. This contains a hook to supply the
   * answers to the questions, so we don't have to enter them at the 
   * command prompt.  
   * @param cart the PetstoreCart object to checkout.
   * @param answers a Map of question and answer.
   * @return the processed PetstoreCart object.
   */
  protected PetstoreCart checkout(PetstoreCart cart, Map<String,String> answers) {
    PetstoreCart newCart = null;
    for (;;) {
      newCart = _checkout(cart);
      if (newCart.isInProgress()) {
        String question = newCart.getQuestion();
        String answer = prompt(question, answers);
        newCart.getAnswers().put(question, answer);
      } else {
        break;
      }
    }
    return newCart;
  }
  
  /**
   * Prompt the user for the question and waits for an answer, or
   * gets the answer from the answers map, if provided.
   * @param question the question to answer.
   * @param answers a Map of question and answer.
   * @return the answer to the question.
   */
  private String prompt(String question, Map<String,String> answers) {
    if (answers == null) {
      Console console = System.console();
      if (console != null) {
        return console.readLine(">> " + question + "?");  
      } else {
        throw new RuntimeException("No console, start client on OS prompt");
      }
    } else {
      // run using a map of predefined answers (for testing).
      if (answers.containsKey(question)) {
        String answer = answers.get(question);
        System.out.println(">> " + question + "? " + answer + ".");
        return answer;
      } else {
        throw new RuntimeException("No answer defined for question:[" + 
          question + "]");
      }
    }
  }

  /**
   * Called from checkout. This is the method that actually issues the
   * checkout query to the Prolog engine, and in case of failure, retrieves
   * the question to be answered, and in case of success, retrieves back
   * the Cart object from the Prolog engine. 
   * @param cart the current form of the PetstoreCart object.
   * @return the processed PetstoreCart object.
   */
  private synchronized PetstoreCart _checkout(PetstoreCart cart) {
    Query checkoutQuery = new Query("checkout", 
      new Term[] {buildPrologCart(cart)});
    boolean checkoutSuccessful = checkoutQuery.hasSolution();
    checkoutQuery.close();
    if (checkoutSuccessful) {
      // succeeded, get cart from factbase
      Variable X = new Variable("X");
      Query cartQuery = new Query("cart", new Term[] {X});
      Term prologCart = (Term) cartQuery.oneSolution().get("X");
      PetstoreCart newCart = parsePrologCart(prologCart);
      newCart.setInProgress(false);
      cartQuery.close();
      return newCart;
    } else {
      // failed, get question, and stick it into question
      PetstoreCart newCart = cart.clone();
      Variable X = new Variable("X");
      Query questionQuery = new Query("question", new Term[] {X});
      newCart.setQuestion(String.valueOf(questionQuery.oneSolution().get("X")));
      newCart.setInProgress(true);
      questionQuery.close();
      return newCart;
    }
  }
  
  /**
   * Builds a Term representing a Prolog List object from the contents 
   * of a PetstoreCart. The List is passed to the checkout goal. 
   * @param cart the PetstoreCart object.
   * @return a Term to pass to the checkout goal.
   */
  private Term buildPrologCart(PetstoreCart cart) {
    StringBuilder prologCart = new StringBuilder();
    prologCart.append("[").
      append(String.valueOf(cart.getNumFish())).
      append(",").
      append(String.valueOf(cart.getNumFood())).
      append(",").
      append(String.valueOf(cart.getNumTank()));
    Map<String,String> answers = cart.getAnswers();
    for (String question : answers.keySet()) {
      prologCart.append(",").
        append("answer(").
        append(question).append(",").
        append(answers.get(question)).
        append(")");
    }
    prologCart.append("]");
    return Util.textToTerm(prologCart.toString());
  }

  /**
   * Parses the returned Term object representing a Prolog List that
   * represents the processed contents of the cart. The Term is 
   * returned as a compound term of nested "." (concat) functions.
   * @param prologCart the Term representing the Cart, returned from
   *        querying Prolog with cart(X).
   * @return a PetstoreCart object.
   */
  private PetstoreCart parsePrologCart(Term prologCart) {
    List<String> elements = new ArrayList<String>();
    Term term = prologCart;
    for (;;) {
      if (term.type() == Prolog.COMPOUND) {
        elements.add(term.arg(1).toString());
        term = term.arg(2);
      } else if (term.type() == Prolog.ATOM) {
        break;
      }
    }
    PetstoreCart cart = new PetstoreCart(
      Integer.valueOf(elements.get(0)),
      Integer.valueOf(elements.get(1)), 
      Integer.valueOf(elements.get(2)));
    cart.setNumFreeFood(Integer.valueOf(elements.get(3)));
    cart.setDiscount(Float.valueOf(elements.get(4)));
    return cart;
  }
}
