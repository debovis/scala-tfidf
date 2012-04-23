// $Id$
package net.sf.jtmt.ontology.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.ReturnableEvaluator;
import org.neo4j.api.core.TraversalPosition;

/**
 * Returnable Evaluator implementation that stores traversed nodes
 * in a data structure which is available to the client.
 * @author Sujit Pal
 * @version $Revision$
 */
public class BrowserReturnableEvaluator implements ReturnableEvaluator {

  private Node startNode;
  private TreeMap<String,ArrayList<WeightedNode>> neighbors;
  
  private class WeightedNode implements Comparable<WeightedNode> {
    public Node node;
    public Float weight;
    
    public WeightedNode(Node node, Float weight) {
      this.node = node;
      this.weight = weight;
    }
    
    public int compareTo(WeightedNode that) {
      return (that.weight.compareTo(this.weight));
    }
  };

  public BrowserReturnableEvaluator(Node startNode) {
    this.startNode = startNode;
    this.neighbors = new TreeMap<String,ArrayList<WeightedNode>>();
  }
  
  public boolean isReturnableNode(TraversalPosition pos) {
    // if related to self, don't include in traversal results
    Node currentNode = pos.currentNode();
    if (startNode.getProperty(NeoOntologyNavigator.FIELD_ENTITY_NAME).equals(
      currentNode.getProperty(NeoOntologyNavigator.FIELD_ENTITY_NAME))) {
      return false;
    }
    // if relationship weight is 0.0F, don't include in traversal results
    Relationship lastRel = pos.lastRelationshipTraversed();
    Float relWeight = (Float) lastRel.getProperty(
      NeoOntologyNavigator.FIELD_RELATIONSHIP_WEIGHT);
    if (relWeight <= 0.0F) {
      return false;
    }
    String relName = (String) lastRel.getProperty(
      NeoOntologyNavigator.FIELD_RELATIONSHIP_NAME);
    // accumulate into our neighbor data structure
    ArrayList<WeightedNode> nodes;
    if (neighbors.containsKey(relName)) {
      nodes = neighbors.get(relName);
    } else {
      nodes = new ArrayList<WeightedNode>();
    }
    nodes.add(new WeightedNode(currentNode, relWeight));
    neighbors.put(relName, nodes);
    // include in traversal results
    return true;
  }

  public Map<String,List<Node>> getNeighbors() {
    Map<String,List<Node>> neighborsMap = new LinkedHashMap<String,List<Node>>();
    for (String relName : neighbors.keySet()) {
      List<WeightedNode> weightedNodes = neighbors.get(relName);
      Collections.sort(weightedNodes);
      List<Node> relatedNodes = new ArrayList<Node>();
      for (WeightedNode weightedNode : weightedNodes) {
        relatedNodes.add(weightedNode.node);
      }
      neighborsMap.put(relName, relatedNodes);
    }
    return neighborsMap;
  }
}
