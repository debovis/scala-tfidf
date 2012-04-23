package net.sf.jtmt.ontology.relational.loaders;

import java.io.FileInputStream;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import net.sf.jtmt.ontology.relational.Attribute;
import net.sf.jtmt.ontology.relational.Entity;
import net.sf.jtmt.ontology.relational.daos.EntityDao;
import net.sf.jtmt.ontology.relational.daos.FactDao;

import org.apache.commons.collections15.Closure;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;

/**
 * Parse OWL files representing external ontologies and loads them 
 * into local database.
 * @author Sujit Pal
 * @version $Revision$
 */
public class Owl2MysqlLoader {
  
  private final static String RDF_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
  
  private String owlFileLocation;
  private EntityDao entityDao;
  private FactDao factDao;
  
  private static String parentTagName = null;
  
  public void setOwlFileLocation(String owlFileLocation) {
    this.owlFileLocation = owlFileLocation;
  }

  @Required
  public void setEntityDao(EntityDao entityDao) {
    this.entityDao = entityDao;
  }

  @Required
  public void setFactDao(FactDao factDao) {
    this.factDao = factDao;
  }

  /**
   * These parsing rules were devised by physically looking at the OWL file
   * and figuring out what goes where. This should by no means be considered
   * a generalized way to parse OWL files.
   * 
   * Parsing rules:
   * 
   * owl:Class@rdf:ID = entity (1), type=Wine
   * optional:
   *   owl:Class/rdfs:subClassOf@rdf:resource = entity (2), type=Wine
   *   (2) -- parent --> (1)
   * if owl:Class/rdfs:subClassOf has no attributes, ignore
   * if no owl:Class/rdfs:subClassOf entity, ignore it
   * owl:Class/owl:Restriction/owl:onProperty@rdf:resource related to
   *   owl:Class/owl:Restriction/owl:hasValue@rdf:resource
   *  
   * Region@rdf:ID = entity, type=Region
   * optional:
   *   Region/locatedIn@rdf:resource=entity (2), type=Region
   *   (2) -- parent -- (1)
   * owl:Class/rdfs:subClassOf/owl:Restriction - ignore
   * 
   * WineBody@rdf:ID = entity, type=WineBody
   * WineColor@rdf:ID = entity, type=WineColor
   * WineFlavor@rdf:ID = entity, type=WineFlavor
   * WineSugar@rdf:ID = entity, type=WineSugar
   * Winery@rdf:ID = entity, type=Winery
   * WineGrape@rdf:ID = entity, type=WineGrape
   * 
   * Else if no namespace, this must be a wine itself, capture as entity:
   * ?@rdf:ID = entity, type=Wine
   *   all subtags are relations:
   *     tagname = relation_name
   *     tag@rdf:resource = target entity
   */
  public void parseAndLoadData() throws Exception {
    XMLInputFactory factory = XMLInputFactory.newInstance();
    XMLStreamReader parser = factory.createXMLStreamReader(
      new FileInputStream(owlFileLocation));
    int depth = 0;
    for (;;) {
      int event = parser.next();
      if (event == XMLStreamConstants.END_DOCUMENT) {
        break;
      }
      switch (event) {
        case XMLStreamConstants.START_ELEMENT:
          depth++;
          String tagName = formatTag(parser.getName());
          if (tagName.equals("owl:Class")) {
            processTag(parser, new Closure<XMLStreamReader>() {
              public void execute(XMLStreamReader parser) {
                // relations are not being persisted because value of child
                // entity cannot be persisted.
                String tagName = formatTag(parser.getName());
                if (tagName.equals("owl:Class")) {
                  String name = parser.getAttributeValue(RDF_URI, "ID");
                  if (name != null) {
                    Entity classEntity = new Entity();
                    parentTagName = name;
                    classEntity.setName(parentTagName);
                    classEntity.addAttribute(new Attribute("Type", "Class"));
                    entityDao.save(classEntity);
                  }
                } else if (tagName.equals("rdfs:subClassOf")) {
                  String name = parser.getAttributeValue(RDF_URI, "resource");
                  if (name != null) {
                    Entity superclassEntity = new Entity();
                    if (name.startsWith("http://")) {
                      superclassEntity.setName(name.substring(name.lastIndexOf('#') + 1));
                      superclassEntity.addAttribute(new Attribute("Type", 
                        name.substring(name.lastIndexOf('/') + 1, 
                        name.lastIndexOf('#')) + ":Class"));
                    } else if (name.startsWith("#")) {
                      superclassEntity.setName(name.substring(1));
                      superclassEntity.addAttribute(new Attribute("Type", "Class"));
                    } else {
                      superclassEntity.setName(name);
                      superclassEntity.addAttribute(new Attribute("Type", "Class"));
                    }
                    entityDao.save(superclassEntity);
                    factDao.save(superclassEntity.getName(), parentTagName, "subclassOf");
                    parentTagName = null;
                  }
                }
              }
            });
          } else if (tagName.equals("Region")) {
            processTag(parser, new Closure<XMLStreamReader>() {
              public void execute(XMLStreamReader parser) {
                String tagName = formatTag(parser.getName());
                if (tagName.equals("Region")) {
                  Entity classEntity = new Entity();
                  parentTagName = parser.getAttributeValue(RDF_URI, "ID");
                  classEntity.setName(parentTagName);
                  classEntity.addAttribute(new Attribute("Type", "Region"));
                  entityDao.save(classEntity);
                } else if (tagName.equals("locatedIn")) {
                  Entity superclassEntity = new Entity();
                  String locationEntityName = parser.getAttributeValue(RDF_URI, "resource");
                  if (locationEntityName.startsWith("#")) {
                    locationEntityName = locationEntityName.substring(1);
                  }
                  superclassEntity.setName(locationEntityName);
                  superclassEntity.addAttribute(new Attribute("Type", "Region"));
                  entityDao.save(superclassEntity);
                  factDao.save(parentTagName, locationEntityName, "locatedIn");
                  parentTagName = null;
                }
              }
            });
          } else if (tagName.equals("WineBody") || 
              tagName.equals("WineColor") ||
              tagName.equals("WineFlavor") ||
              tagName.equals("WineSugar") ||
              tagName.equals("WineGrape")) {
            processTag(parser, new Closure<XMLStreamReader>() {
              public void execute(XMLStreamReader parser) {
                Entity entity = new Entity();
                String name = parser.getAttributeValue(RDF_URI, "ID");
                if (name != null) {
                  entity.setName(name);
                  String tagName = parser.getLocalName();
                  Attribute attribute = null;
                  if (tagName.equals("WineBody")) {
                    attribute = new Attribute("Type", "Body");
                  } else if (tagName.equals("WineColor")) {
                    attribute = new Attribute("Type", "Color");
                  } else if (tagName.equals("WineFlavor")) {
                    attribute = new Attribute("Type", "Flavor");
                  } else if (tagName.equals("WineSugar")) {
                    attribute = new Attribute("Type", "Sugar");
                  } else if (tagName.equals("WineGrape")) {
                    attribute = new Attribute("Type", "Grape");
                  }
                  entity.addAttribute(attribute);
                  entityDao.save(entity);
                }
              }
            });
          } else if (tagName.equals("vin:Winery")) {
            processTag(parser, new Closure<XMLStreamReader>() {
              public void execute(XMLStreamReader parser) {
                String wineryName = parser.getAttributeValue(RDF_URI, "about");
                if (wineryName.startsWith("#")) {
                  wineryName = wineryName.substring(1);
                }
                Entity entity = new Entity();
                entity.setName(wineryName);
                entity.addAttribute(new Attribute("Type", "Winery"));
                entityDao.save(entity);
              }
            });
          } else if (! tagName.startsWith("owl:")) {
            Entity parentEntity = entityDao.getByName(tagName);
            if (parentEntity != null) {
              processTag(parser, new Closure<XMLStreamReader>() {
                public void execute(XMLStreamReader parser) {
                  String tagName = formatTag(parser.getName());
                  String id = parser.getAttributeValue(RDF_URI, "ID");
                  if (StringUtils.isNotBlank(id)) {
                    // this is the entity
                    Entity entity = new Entity();
                    entity.setName(id);
                    entity.addAttribute(new Attribute("Type", "Wine"));
                    parentTagName = entity.getName();
                    entityDao.save(entity);
                  } else {
                    // these are the relations
                    String relationName = tagName;
                    String targetEntityName = parser.getAttributeValue(RDF_URI, "resource");
                    if (targetEntityName != null && targetEntityName.startsWith("#")) {
                      targetEntityName = targetEntityName.substring(1);
                    }
                    if (targetEntityName != null) {
                      factDao.save(parentTagName, targetEntityName, relationName);
                    }
                  }
                }
              });
            }
          }
          break;
        case XMLStreamConstants.END_ELEMENT:
          depth--;
          break;
        default:
          break;
      }
      parser.close();
    }
  }

  /**
   * A tag processor template method which takes as input a closure that is
   * responsible for extracting the information from the tag and saving it
   * to the database. The contents of the closure is called inside the
   * START_DOCUMENT case of the template code.
   * @param parser a reference to our StAX XMLStreamReader.
   * @param tagProcessor a reference to the Closure to process the tag.
   * @throws Exception if one is thrown.
   */
  private void processTag(XMLStreamReader parser, Closure<XMLStreamReader> tagProcessor) 
      throws Exception {
    int depth = 0;
    int event = parser.getEventType();
    String startTag = formatTag(parser.getName());
    FOR_LOOP:
    for (;;) {
      switch(event) {
        case XMLStreamConstants.START_ELEMENT:
          String tagName = formatTag(parser.getName());
          tagProcessor.execute(parser);
          depth++;
          break;
        case XMLStreamConstants.END_ELEMENT:
          tagName = formatTag(parser.getName());
          depth--;
          if (tagName.equals(startTag) && depth == 0) {
            break FOR_LOOP;
          }
          break;
        default:
          break;
      }
      event = parser.next();
    }
  }
  
  /**
   * Format the XML tag. Takes as input the QName of the tag, and formats
   * it to a namespace:tagname format.
   * @param qname the QName for the tag.
   * @return the formatted QName for the tag.
   */
  private String formatTag(QName qname) {
    String prefix = qname.getPrefix();
    String suffix = qname.getLocalPart();
    if (StringUtils.isBlank(prefix)) {
      return suffix;
    } else {
      return StringUtils.join(new String[] {prefix, suffix}, ":");
    }
  }
}
