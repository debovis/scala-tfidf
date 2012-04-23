// $Id$
package net.sf.jtmt.inferencing.jena;

import java.io.File;
import java.io.FileInputStream;

import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * TODO: Class level Javadocs
 * @author Sujit Pal
 * @version $Revision$
 */
public class JenaInferencer {

  private String n3data;
  
  public void setN3Data(String n3data) {
    this.n3data = n3data;
  }
  
  public void run() throws Exception {
    Reasoner reasoner = PelletReasonerFactory.theInstance().create();
    Model infModel = ModelFactory.createInfModel(reasoner, 
      ModelFactory.createDefaultModel());
    OntModel model = ModelFactory.createOntologyModel(
      OntModelSpec.OWL_DL_MEM, infModel);
    model.read(new FileInputStream(new File(n3data)), null, "N3");
    ExtendedIterator<Individual> eit = model.listIndividuals();
    while (eit.hasNext()) {
      Individual ind = eit.next();
      printIndividual(ind);
    }
    eit.close();
    model.close();
  }

  private void printIndividual(Individual ind) {
    System.out.println("name=" + ind.getLocalName());
    StmtIterator sit = ind.listProperties();
    while (sit.hasNext()) {
      Statement s = sit.next();
      System.out.println("  " + s.getPredicate().getLocalName() + 
        " " + s.getObject().toString() + ";");
    }
    sit.close();
  }
}
