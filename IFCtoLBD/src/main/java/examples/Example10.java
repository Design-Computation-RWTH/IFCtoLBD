package examples;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.linkedbuildingdata.ifc2lbd.IFCtoLBDConverter;

public class Example10 {
	static final private int props_level = 1;
	static final private boolean hasBuildingElements = true;
	static final private boolean hasBuildingProperties = true;

	static final boolean hasSeparateBuildingElementsModel = false;
	static final boolean hasPropertiesBlankNodes = false;
	static final boolean hasSeparatePropertiesModel = false;

	static final boolean hasGeolocation = false;

	static final boolean hasGeometry = true;
	static final boolean exportIfcOWL = false;
	static final boolean hasUnits = false;

	static final boolean hasPerformanceBoost = false;
	static final boolean hasBoundingBoxWKT = true;

	public static void main(String[] args) {
		URL ifc_file_url = ClassLoader.getSystemResource("Duplex_A.ifc");
		try {
			File ifc_file = new File(ifc_file_url.toURI());

			try (IFCtoLBDConverter converter = new IFCtoLBDConverter("https://example.com/", hasPropertiesBlankNodes,
					props_level);) {
				converter.convert_read_in_phase(ifc_file.getAbsolutePath(), null, hasGeometry, hasPerformanceBoost,
						exportIfcOWL,hasBuildingElements,hasBuildingProperties,hasBoundingBoxWKT,hasUnits);
				
				Model m =converter.convert_LBD_phase(hasBuildingElements, hasSeparateBuildingElementsModel,
						hasBuildingProperties, hasSeparatePropertiesModel, hasGeolocation, hasGeometry, exportIfcOWL,
						hasUnits, hasBoundingBoxWKT, true);

				if (m != null) {
					Query query = QueryFactory.create("""
							PREFIX bot: <https://w3id.org/bot#>\r
							\r
							JSON { 'element' : ?element } WHERE {\r
							  ?element a bot:Element .\r
							}\s""", Syntax.syntaxARQ);
					
					try (QueryExecution queryExecution = QueryExecutionFactory.create(query, m)) {
						JsonArray jsonArray = queryExecution.execJson();
						System.out.println("jsonArray:"+jsonArray);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}