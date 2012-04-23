package net.sf.jtmt.ontology.relational.loaders;

import java.util.List;

import net.sf.jtmt.ontology.relational.Entity;
import net.sf.jtmt.ontology.relational.Fact;
import net.sf.jtmt.ontology.relational.Ontology;
import net.sf.jtmt.ontology.relational.OntologyException;
import net.sf.jtmt.ontology.relational.Relation;
import net.sf.jtmt.ontology.relational.daos.EntityDao;
import net.sf.jtmt.ontology.relational.daos.FactDao;
import net.sf.jtmt.ontology.relational.daos.RelationDao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Loads the Ontology object from database data.
 * @author Sujit Pal
 * @version $Revision$
 */
public class Mysql2OntologyLoader {
  
  private final Log log = LogFactory.getLog(getClass());
  
  private EntityDao entityDao;
  private RelationDao relationDao;
  private FactDao factDao;
  
  public void setEntityDao(EntityDao entityDao) {
    this.entityDao = entityDao;
  }

  public void setRelationDao(RelationDao relationDao) {
    this.relationDao = relationDao;
  }

  public void setFactDao(FactDao factDao) {
    this.factDao = factDao;
  }

  public Ontology load() throws Exception {
    Ontology ontology = new Ontology();
    log.debug("Loading entities");
    List<Entity> entities = entityDao.getAllEntities();
    for (Entity entity : entities) {
      try {
        ontology.addEntity(entity);
      } catch (OntologyException e) {
        log.error(e.getMessage());
        continue;
      }
    }
    log.debug("Loading relations");
    List<Relation> relations = relationDao.getAllRelations();
    for (Relation relation : relations) {
      ontology.addRelation(relation);
      if (relationDao.isBidirectional(relation.getId())) {
        Relation reverseRelation = relationDao.getById(-1L * relation.getId());
        try {
          ontology.addRelation(reverseRelation);
        } catch (OntologyException e) {
          log.error(e.getMessage());
          continue;
        }
      }
    }
    log.debug("Loading facts");
    List<Fact> facts = factDao.getAllFacts();
    for (Fact fact : facts) {
      try {
        ontology.addFact(fact);
      } catch (OntologyException e) {
        log.error(e);
        continue;
      }
    }
    log.debug("Ontology load complete");
    return ontology;
  }
}
