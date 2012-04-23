package net.sf.jtmt.ontology.relational;

import java.util.Set;

import javax.sql.DataSource;

import net.sf.jtmt.ontology.relational.daos.EntityDao;
import net.sf.jtmt.ontology.relational.daos.FactDao;
import net.sf.jtmt.ontology.relational.daos.RelationDao;
import net.sf.jtmt.ontology.relational.loaders.Mysql2OntologyLoader;
import net.sf.jtmt.ontology.relational.transactions.AttributeAddTransaction;
import net.sf.jtmt.ontology.relational.transactions.AttributeDeleteTransaction;
import net.sf.jtmt.ontology.relational.transactions.AttributeUpdateTransaction;
import net.sf.jtmt.ontology.relational.transactions.EntityAddTransaction;
import net.sf.jtmt.ontology.relational.transactions.EntityDeleteTransaction;
import net.sf.jtmt.ontology.relational.transactions.EntityUpdateTransaction;
import net.sf.jtmt.ontology.relational.transactions.FactAddTransaction;
import net.sf.jtmt.ontology.relational.transactions.FactDeleteTransaction;
import net.sf.jtmt.ontology.relational.transactions.RelationAddTransaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgrapht.Graph;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.prevayler.Prevayler;
import org.prevayler.PrevaylerFactory;
import org.prevayler.foundation.serialization.XStreamSerializer;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

public class RelationalOntologyTest {

  private final Log log = LogFactory.getLog(getClass());
  
  private static final String CACHE_DIR = "/tmp/prevaylordb";
  
  private static Ontology ontology;
  private static Prevayler prevalentOntology;
  
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  
    DataSource dataSource = new DriverManagerDataSource(
      "com.mysql.jdbc.Driver", "jdbc:mysql://localhost:3306/ontodb", "root", "orange");
    
    EntityDao entityDao = new EntityDao();
    entityDao.setDataSource(dataSource);
    
    RelationDao relationDao = new RelationDao();
    relationDao.setDataSource(dataSource);
    
    FactDao factDao = new FactDao();
    factDao.setDataSource(dataSource);
    factDao.setEntityDao(entityDao);
    factDao.setRelationDao(relationDao);
    
    Mysql2OntologyLoader loader = new Mysql2OntologyLoader();
    loader.setEntityDao(entityDao);
    loader.setRelationDao(relationDao);
    loader.setFactDao(factDao);

    ontology = loader.load();
    
    PrevaylerFactory factory = new PrevaylerFactory();
    XStreamSerializer xstreamSerializer = new XStreamSerializer();
    factory.configureJournalSerializer(xstreamSerializer);
    factory.configureSnapshotSerializer(xstreamSerializer);
    factory.configurePrevalenceDirectory(CACHE_DIR);
    factory.configurePrevalentSystem(ontology);
    prevalentOntology = factory.create();
  }
  
  @Test
  public void testLoad() throws Exception {
    Graph<Entity,RelationEdge> ontologyGraph = ontology.ontology;
    // We should have 237 vertices and about 500 edges
    log.info("# vertices =" + ontologyGraph.vertexSet().size());
    Assert.assertTrue("#-vertices test failed", ontologyGraph.vertexSet().size() == 237);
    log.info("# edges = " + ontologyGraph.edgeSet().size());
    Assert.assertTrue("#-edges test failed", ontologyGraph.edgeSet().size() == 500);
  }
  
  @Test
  public void testWhereIsLoireRegion() throws Exception {
    Entity loireRegion = ontology.getEntityById(26);
    long locatedInRelationId = 2L;
    Set<Entity> entities = ontology.getEntitiesRelatedById(loireRegion, locatedInRelationId);
    log.info("query> where is Loire Region?");
    for (Entity entity : entities) {
      log.info("..." + entity.getName());
    }
  }
  
  @Test
  public void testWhatRegionsAreInUSRegion() throws Exception {
    Entity usRegion = ontology.getEntityById(23);
    long reverseLocatedInRelationId = -2L;
    Set<Entity> entities = ontology.getEntitiesRelatedById(usRegion, reverseLocatedInRelationId);
    log.info("query> what regions are in US Region?");
    for (Entity entity : entities) {
      log.info("..." + entity.getName());
    }
  }
  
  @Test
  public void testWhatAreSweetWines() throws Exception {
    Entity sweetWinesEntity = ontology.getEntityById(125);
    long reverseOfHasSugarRelationId = -4L;
    Set<Entity> entities = ontology.getEntitiesRelatedById(
      sweetWinesEntity, reverseOfHasSugarRelationId);
    log.info("query> what are sweet wines?");
    for (Entity entity : entities) {
      log.info("..." + entity.getName());
    }
  }
  
  @Test
  public void testTransactionsWithPrevalence() throws Exception {
    prevalentOntology.execute(new EntityAddTransaction(-1L, "foo"));
    prevalentOntology.execute(new EntityUpdateTransaction(-1L, "bar"));
    prevalentOntology.execute(new EntityAddTransaction(-2L, "baz"));
    prevalentOntology.execute(new AttributeAddTransaction(-1L, "name", "barname"));
    prevalentOntology.execute(new AttributeUpdateTransaction(-1L, "name", "fooname"));
    prevalentOntology.execute(new AttributeDeleteTransaction(-1L, "name", "fooname"));
    prevalentOntology.execute(new RelationAddTransaction(-100L, "some relation"));
    prevalentOntology.execute(new FactAddTransaction(-1L, -2L, -100L));
    prevalentOntology.execute(new FactDeleteTransaction(-1L, -2L, -100L));
    prevalentOntology.execute(new EntityDeleteTransaction(-1L, "bar"));
    prevalentOntology.execute(new EntityDeleteTransaction(-2L, "baz"));
  }
}
