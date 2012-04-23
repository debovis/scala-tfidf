// $Id$
// $Source$
package net.sf.jtmt.spelling;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.swabunga.spell.engine.SpellDictionary;
import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.engine.Word;

/**
 * Uses probability of word-collocations to determine best phrases to be
 * returned from a spellchecker for multi-word misspelled queries.
 * @author Sujit Pal
 * @version $Revision$
 */
public class SpellingCorrector {

  private static final int SCORE_THRESHOLD = 200;
  private static final String DICTIONARY_FILENAME = "src/main/resources/english.0";
  
  private long occurASumWords = 1L;
  private JdbcTemplate jdbcTemplate;
  
  @SuppressWarnings("unchecked")
  public String getSuggestion(String input) throws Exception {
    // initialize Jazzy spelling dictionary
    SpellDictionary dictionary = new SpellDictionaryHashMap(
      new File(DICTIONARY_FILENAME));
    // initialize database connection
    DataSource dataSource = new DriverManagerDataSource(
      "com.mysql.jdbc.Driver", "jdbc:mysql://localhost:3306/spelldb", 
      "root", "orange");
    jdbcTemplate = new JdbcTemplate(dataSource);
    occurASumWords = jdbcTemplate.queryForLong("select sum(n_words) from occur_a");
    if (occurASumWords == 0L) {
      occurASumWords = 1L;
    }
    // set up graph and create root vertex
    final SimpleDirectedWeightedGraph<SuggestedWord,DefaultWeightedEdge> g = 
      new SimpleDirectedWeightedGraph<SuggestedWord,DefaultWeightedEdge>(
      new ClassBasedEdgeFactory<SuggestedWord,DefaultWeightedEdge>(
      DefaultWeightedEdge.class));
    SuggestedWord startVertex = new SuggestedWord("START", 0);
    g.addVertex(startVertex);
    // set up variables to hold results of previous iteration
    List<SuggestedWord> prevVertices = new ArrayList<SuggestedWord>();
    List<SuggestedWord> currentVertices = new ArrayList<SuggestedWord>();
    int tokenId = 1;
    prevVertices.add(startVertex);
    // parse the string
    String[] tokens = input.toLowerCase().split("[ -]");
    for (String token : tokens) {
      // build up spelling suggestions for individual word
      List<String> possibleTokens = new ArrayList<String>();
      if (token.trim().length() <= 2) {
        // people usually don't make mistakes for words 2 words or less,
        // just pass it back unchanged
        possibleTokens.add(token);
      } else if (dictionary.isCorrect(token)) {
        // no need to find suggestions, token is recognized as valid spelling
        possibleTokens.add(token);
      } else {
        possibleTokens.add(token);
        List<Word> words = dictionary.getSuggestions(token, SCORE_THRESHOLD);
        for (Word word : words) {
          possibleTokens.add(StringUtils.lowerCase(word.getWord()));
        }
      }
      // populate the graph with these values
      for (String possibleToken : possibleTokens) {
        SuggestedWord currentVertex = new SuggestedWord(possibleToken, tokenId); 
        g.addVertex(currentVertex);
        currentVertices.add(currentVertex);
        for (SuggestedWord prevVertex : prevVertices) {
          DefaultWeightedEdge edge = new DefaultWeightedEdge();
          double weight = computeEdgeWeight(prevVertex.token, currentVertex.token);
          g.setEdgeWeight(edge, weight);
          g.addEdge(prevVertex, currentVertex, edge);
        }
      }
      prevVertices.clear();
      prevVertices.addAll(currentVertices);
      currentVertices.clear();
      tokenId++;
    } // for token : tokens
    // finally set the end vertex
    SuggestedWord endVertex = new SuggestedWord("END", tokenId);
    g.addVertex(endVertex);
    for (SuggestedWord prevVertex : prevVertices) {
      DefaultWeightedEdge edge = new DefaultWeightedEdge();
      g.setEdgeWeight(edge, 1.0D);
      g.addEdge(prevVertex, endVertex, edge);
    }
    // find shortest path between START and END
    DijkstraShortestPath<SuggestedWord,DefaultWeightedEdge> dijkstra =
      new DijkstraShortestPath<SuggestedWord, DefaultWeightedEdge>(g, startVertex, endVertex);
    List<DefaultWeightedEdge> edges = dijkstra.getPathEdgeList();
    List<String> bestMatch = new ArrayList<String>();
    for (DefaultWeightedEdge edge : edges) {
      if (startVertex.equals(g.getEdgeSource(edge))) {
        // skip the START vertex
        continue;
      }
      bestMatch.add(g.getEdgeSource(edge).token);
    }
    return StringUtils.join(bestMatch.iterator(), " ");
  }

  private Double computeEdgeWeight(String prevToken, String currentToken) {
    if (prevToken.equals("START")) {
      // this is the first word, return 1-P(B)
      try {
        double nb = (Double) jdbcTemplate.queryForObject(
          "select n_words/? from occur_a where word = ?", 
          new Object[] {occurASumWords, currentToken}, Double.class);
        return 1.0D - nb;
      } catch (IncorrectResultSizeDataAccessException e) {
        // in case there is no match, then we should return weight of 1
        return 1.0D;
      }
    }
    double na = 0.0D;
    try {
      na = (Double) jdbcTemplate.queryForObject(
        "select n_words from occur_a where word = ?", 
        new String[] {prevToken}, Double.class);
    } catch (IncorrectResultSizeDataAccessException e) {
      // no match, should be 0
      na = 0.0D;
    }
    if (na == 0.0D) {
      // if N(A) == 0, A does not exist, and hence N(A ^ B) == 0 too,
      // so we guard against a DivideByZero and an additional useless
      // computation.
      return 1.0D;
    }
    // for the A^B lookup, alphabetize so A is lexically ahead of B
    // since that is the way we store it in the database
    String[] tokens = new String[] {prevToken, currentToken};
    Arrays.sort(tokens); // alphabetize before lookup
    double nba = 0.0D;
    try {
      nba = (Double) jdbcTemplate.queryForObject(
        "select n_words from occur_ab where word_a = ? and word_b = ?",
        tokens, Double.class);
    } catch (IncorrectResultSizeDataAccessException e) {
      // no result found so N(B^A) = 0
      nba = 0.0D;
    }
    return 1.0D - (nba / na);
  }

  private class SuggestedWord {
    public String token;
    public int id;
    
    public SuggestedWord(String token, int id) {
      this.token = token;
      this.id = id;
    }
    
    @Override
    public int hashCode() {
      return toString().hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof SuggestedWord)) {
        return false;
      }
      SuggestedWord that = (SuggestedWord) obj;
      return (this.id == that.id && 
        this.token.equals(that.token));
    }
    
    @Override
    public String toString() {
      return id + ":" + token;
    }
  };
}
