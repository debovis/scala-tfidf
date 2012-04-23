package net.sf.jtmt.ontology.relational.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.jtmt.ontology.relational.Entity;
import net.sf.jtmt.ontology.relational.Fact;
import net.sf.jtmt.ontology.relational.Relation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

/**
 * DAO for the Fact table.
 * @author Sujit Pal
 * @version $Revision$
 */
public class FactDao extends JdbcDaoSupport {

  private final Log log = LogFactory.getLog(getClass());
  
  private EntityDao entityDao;
  private RelationDao relationDao;
  
  public void setEntityDao(EntityDao entityDao) {
    this.entityDao = entityDao;
  }
  
  public void setRelationDao(RelationDao relationDao) {
    this.relationDao = relationDao;
  }

  @SuppressWarnings("unchecked")
  public List<Fact> getAllFacts() {
    List<Fact> facts = new ArrayList<Fact>();
    List<Map<String,Integer>> rows = getJdbcTemplate().queryForList(
      "select f.src_entity_id, f.trg_entity_id, f.relation_id " +
      "from facts f, relations r " +
      "where f.relation_id = r.id");
    for (Map<String,Integer> row : rows) {
      Fact fact = new Fact();
      fact.setSourceEntityId(row.get("SRC_ENTITY_ID"));
      fact.setTargetEntityId(row.get("TRG_ENTITY_ID"));
      fact.setRelationId(row.get("RELATION_ID"));
      facts.add(fact);
    }
    return facts;
  }

  public void save(Fact fact) {
    Entity sourceEntity = entityDao.getById(fact.getSourceEntityId());
    Entity targetEntity = entityDao.getById(fact.getTargetEntityId());
    if (sourceEntity == null || targetEntity == null) {
      log.error("Cannot relate null entities");
      return;
    }
    Relation relation = relationDao.getById(fact.getRelationId());
    if (relation == null) {
      log.error("Unknown relation, cannot save fact");
      return;
    }
    save(sourceEntity.getName(), targetEntity.getName(), relation.getName());
  }
  
  public void save(final String sourceEntityName, final String targetEntityName, 
      final String relationName) {
    // get the entity ids for source and target
    Entity sourceEntity = entityDao.getByName(sourceEntityName);
    Entity targetEntity = entityDao.getByName(targetEntityName);
    if (sourceEntity == null || targetEntity == null) {
      log.error("Cannot save relation: " + relationName + "(" + 
        sourceEntityName + "," + targetEntityName + ")"); 
      return;
    }
    log.debug("Saving relation: " + relationName + "(" + sourceEntityName + "," + targetEntityName + ")");
    // get the relation id
    long relationTypeId = 0L;
    try {
      relationTypeId = getJdbcTemplate().queryForInt(
        "select id from relations where name = ?", 
        new String[] {relationName});
    } catch (IncorrectResultSizeDataAccessException e) {
      KeyHolder keyholder = new GeneratedKeyHolder();
      getJdbcTemplate().update(new PreparedStatementCreator() {
        public PreparedStatement createPreparedStatement(Connection conn) 
            throws SQLException {
          PreparedStatement ps = conn.prepareStatement(
            "insert into relations(name) values (?)", 
            Statement.RETURN_GENERATED_KEYS);
          ps.setString(1, relationName);
          return ps;
        }
      }, keyholder);
      relationTypeId = keyholder.getKey().longValue();
    }
    // save it
    getJdbcTemplate().update(
      "insert into facts(src_entity_id, trg_entity_id, relation_id) values (?, ?, ?)", 
      new Long[] {sourceEntity.getId(), targetEntity.getId(), relationTypeId});
  }
}
