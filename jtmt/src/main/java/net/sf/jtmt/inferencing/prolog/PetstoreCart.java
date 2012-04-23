// $Id$
package net.sf.jtmt.inferencing.prolog;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Simple bean to hold shopping cart information for PrologPetstoreClient.
 * @author Sujit Pal
 * @version $Revision$
 */
public class PetstoreCart {

  private int numFish;
  private int numFood;
  private int numTank;

  private String question;
  private Map<String,String> answers = new HashMap<String,String>();
  private int numFreeFood;
  private float discount;
  private boolean inProgress;
  
  public PetstoreCart(int numFish, int numFood, int numTank) {
    this.numFish = numFish;
    this.numFood = numFood;
    this.numTank = numTank;
  }

  @Override
  public PetstoreCart clone() {
    PetstoreCart clone = new PetstoreCart(numFish, numFood, numTank);
    clone.setAnswers(getAnswers());
    return clone;
  }

  public String prettyPrint() {
    StringBuilder buf = new StringBuilder();
    buf.append("[").append(String.valueOf(getNumFish())).
      append(", ").append(String.valueOf(getNumFood())).
      append(", ").append(String.valueOf(getNumTank())).
      append(", ").append(String.valueOf(getNumFreeFood())).
      append(", ").append(String.valueOf(getDiscount())).
      append("]");
    return buf.toString();
  }

  public int getNumFish() {
    return numFish;
  }

  public void setNumFish(int numFish) {
    this.numFish = numFish;
  }

  public int getNumFood() {
    return numFood;
  }

  public void setNumFood(int numFood) {
    this.numFood = numFood;
  }

  public int getNumTank() {
    return numTank;
  }

  public void setNumTank(int numTank) {
    this.numTank = numTank;
  }

  public String getQuestion() {
    return question;
  }

  public void setQuestion(String question) {
    this.question = question;
  }

  public Map<String, String> getAnswers() {
    return answers;
  }

  public void setAnswers(Map<String, String> answers) {
    this.answers = answers;
  }

  public int getNumFreeFood() {
    return numFreeFood;
  }

  public void setNumFreeFood(int numFreeFood) {
    this.numFreeFood = numFreeFood;
  }

  public float getDiscount() {
    return discount;
  }

  public void setDiscount(float discount) {
    this.discount = discount;
  }

  public boolean isInProgress() {
    return inProgress;
  }

  public void setInProgress(boolean inProgress) {
    this.inProgress = inProgress;
  }

  public String toString() {
    return ReflectionToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
  }
}
