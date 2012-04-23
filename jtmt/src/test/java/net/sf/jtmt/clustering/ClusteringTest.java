package net.sf.jtmt.clustering;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.jtmt.indexers.matrix.IdfIndexer;
import net.sf.jtmt.indexers.matrix.VectorGenerator;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.math.linear.RealMatrix;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * Test for clustering algorithms.
 * @author Sujit Pal
 * @version $Revision$
 */
public class ClusteringTest {

  private RealMatrix tdMatrix;
  private String[] documentNames;
  
  private DocumentCollection documentCollection;
  
  @Before
  public void setUp() throws Exception {
    VectorGenerator vectorGenerator = new VectorGenerator();
    vectorGenerator.setDataSource(new DriverManagerDataSource(
      "com.mysql.jdbc.Driver", "jdbc:mysql://localhost:3306/tmdb", "root", "orange"));
    Map<String,Reader> documents = new LinkedHashMap<String,Reader>();
    BufferedReader reader = new BufferedReader(
      new FileReader("src/test/resources/data/indexing_sample_data.txt"));
    String line = null;
    while ((line = reader.readLine()) != null) {
      String[] docTitleParts = StringUtils.split(line, ";");
      documents.put(docTitleParts[0], new StringReader(docTitleParts[1]));
    }
    vectorGenerator.generateVector(documents);
    IdfIndexer indexer = new IdfIndexer();
    tdMatrix = indexer.transform(vectorGenerator.getMatrix());
    documentNames = vectorGenerator.getDocumentNames();
    documentCollection = new DocumentCollection(tdMatrix, documentNames);
  }

  @Test
  public void testPcaClusterVisualization() throws Exception {
    PcaClusterVisualizer visualizer = new PcaClusterVisualizer();
    visualizer.reduce(tdMatrix, documentNames);
  }
  
  @Test
  public void testKMeansClustering() throws Exception {
    KMeansClusterer clusterer = new KMeansClusterer();
    clusterer.setInitialClusterAssignments(new String[] {"D1", "D3"});
    List<Cluster> clusters = clusterer.cluster(documentCollection);
    System.out.println("=== Clusters from K-Means algorithm ===");
    for (Cluster cluster : clusters) {
      System.out.println(cluster.toString());
    }
  }

  @Test
  public void testQtClustering() throws Exception {
    QtClusterer clusterer = new QtClusterer();
    clusterer.setMaxRadius(0.40D);
    clusterer.setRandomizeDocuments(true);
    List<Cluster> clusters = clusterer.cluster(documentCollection);
    System.out.println("=== Clusters from QT Algorithm ===");
    for (Cluster cluster : clusters) {
      System.out.println(cluster.toString());
    }
  }

  @Test
  public void testSimulatedAnnealingClustering() throws Exception {
    SimulatedAnnealingClusterer clusterer = new SimulatedAnnealingClusterer();
    clusterer.setRandomizeDocs(false);
    clusterer.setNumberOfLoops(5);
    clusterer.setInitialTemperature(100.0D);
    clusterer.setFinalTemperature(1.0D);
    clusterer.setDownhillProbabilityCutoff(0.7D);
    List<Cluster> clusters = clusterer.cluster(documentCollection);
    System.out.println("=== Clusters from Simulated Annealing Algorithm ===");
    for (Cluster cluster : clusters) {
      System.out.println(cluster.toString());
    }
  }
  
  @Test
  public void testNearestNeighborClustering() throws Exception {
    NearestNeighborClusterer clusterer = new NearestNeighborClusterer();
    clusterer.setNumNeighbors(2);
    clusterer.setSimilarityThreshold(0.25);
    List<Cluster> clusters = clusterer.cluster(documentCollection);
    System.out.println("=== Clusters from Nearest Neighbor Algorithm ===");
    for (Cluster cluster : clusters) {
      System.out.println(cluster.toString());
    }
  }
  
  @Test
  public void testGeneticAlgorithmClustering() throws Exception {
    GeneticClusterer clusterer = new GeneticClusterer();
    clusterer.setNumberOfCrossoversPerMutation(5);
    clusterer.setMaxGenerations(500);
    clusterer.setRandomizeData(false);
    List<Cluster> clusters = clusterer.cluster(documentCollection);
    System.out.println("=== Clusters from Genetic Algorithm ===");
    for (Cluster cluster : clusters) {
      System.out.println(cluster.toString());
    }
  }
}
