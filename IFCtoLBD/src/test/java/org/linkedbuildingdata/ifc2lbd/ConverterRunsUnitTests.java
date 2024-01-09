package org.linkedbuildingdata.ifc2lbd;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.lib.ShLib;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.linkedbuildingdata.ifc2lbd.application_messaging.IFC2LBD_ApplicationEventBusService;
import org.linkedbuildingdata.ifc2lbd.core.IFCtoRDF;
import org.linkedbuildingdata.ifc2lbd.core.utils.IfcOWLUtils;
import org.linkedbuildingdata.ifc2lbd.core.utils.RDFUtils;

import com.github.davidmoten.rtreemulti.Entry;
import com.github.davidmoten.rtreemulti.RTree;
import com.github.davidmoten.rtreemulti.geometry.Geometry;
import com.github.davidmoten.rtreemulti.geometry.Rectangle;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;

public class ConverterRunsUnitTests {


	@DisplayName("Test the existence of the test data Duplex.ifc")
	@Test
	public void findTestData() {
		URL file_url = ClassLoader.getSystemResource("Duplex.ifc");
		try {
			File file = new File(file_url.toURI());
			if (!file.exists())
				fail("Test data not found/available");
		} catch (Exception e) {
			fail("Test data not found/available: " + e.getMessage());
		}
	}


	@DisplayName("Test the ontolgy read")
	@Test
	public void test_getOntology() {
		
		try {
			URL file_url = ClassLoader.getSystemResource("Duplex.ifc");
			Model ontology_model = ModelFactory.createDefaultModel();
			File file = new File(file_url.toURI());
			IfcOWLUtils.readIfcOWLOntology(file.getAbsolutePath(), ontology_model);
			EventBus eventBus = IFC2LBD_ApplicationEventBusService.getEventBus();
			RDFUtils.readInOntologyTTL(ontology_model, "prod.ttl", eventBus);
		} catch (Exception e) {
			fail("Test get test rilen and an ontology failed: " + e.getMessage());
		}
	}
	
	@DisplayName("Test ifcOWL 1")
    @Test
    public void test_ifcOWL_Conversion1() {
        URL file_url = ClassLoader.getSystemResource("Duplex.ifc");
        try {
            File ifc_file = new File(file_url.toURI());
            File temp_file = File.createTempFile("ifc2lbd", "test.ttl");
            IFCtoRDF rj = new IFCtoRDF();
            Optional<String>  ontURI1 = rj.convert_into_rdf(ifc_file.getAbsolutePath(), temp_file.getAbsolutePath(), "http://test.de/", false);
            
            if(ontURI1.isEmpty())
                fail("Should have an Ontology URI");

            Optional<String>  ontURI2 = rj.convert_into_rdf(ifc_file.getAbsolutePath(), temp_file.getAbsolutePath(), "http://test.de/", true);
            
            if(ontURI2.isEmpty())
                fail("Should have an Ontology URI");

            
        } catch (Exception e) {
            e.printStackTrace();
            fail("ifcOWL conversion had an error: " + e.getMessage());
        }
    }

	@DisplayName("Test ifcOWL 2")
    @Test
    public void test_ifcOWL_Conversion2() {
        URL file_url = ClassLoader.getSystemResource("Duplex.ifc");
        try {
            File ifc_file = new File(file_url.toURI());
            
            File temp_file = File.createTempFile("ifc2lbd", "test.ttl");
            IFCtoLBDConverter c1wb = new IFCtoLBDConverter("https://dot.dc.rwth-aachen.de/IFCtoLBDset#", true, Integer.valueOf(1));
            
            Model m3wb = c1wb.readAndConvertIFC2ifcOWL(ifc_file.getAbsolutePath(), "https://dot.dc.rwth-aachen.de/IFCtoLBDset#", false, temp_file.getAbsolutePath(),false);
            		
			ImmutableList<Resource> subjectList1 = ImmutableList.copyOf(m3wb.listSubjects());
			if(subjectList1.size()!=94539)
			{
				System.out.println("Converted subject count  should not be 94539. Was: "+subjectList1.size());
				fail("Converted subject count  should not be 94539. Was: "+subjectList1.size());
			}

            
        } catch (Exception e) {
            e.printStackTrace();
            fail("ifcOWL conversion had an error: " + e.getMessage());
        }
    }

	@SuppressWarnings("unused")
	@DisplayName("Two walls geometry conversion")
	@Test
	public void testTwoWallsConversion() {
		URL file_url = ClassLoader.getSystemResource("TWO WALLS.ifc");
		try {
			File ifc_file = new File(file_url.toURI());
			File temp_file = File.createTempFile("ifc2lbd", "test.ttl");
			new IFCtoLBDConverter(ifc_file.getAbsolutePath(), "https://dot.dc.rwth-aachen.de/IFCtoLBDset#",
					temp_file.getAbsolutePath(), 0, true, false, true, false, false, false);
			new IFCtoLBDConverter(ifc_file.getAbsolutePath(), "https://dot.dc.rwth-aachen.de/IFCtoLBDset",
					temp_file.getAbsolutePath(), 0, true, false, true, false, false, false);
			new IFCtoLBDConverter(ifc_file.getAbsolutePath(), null,
					temp_file.getAbsolutePath(), 0, true, false, true, false, false, false);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Conversion had an error: " + e.getMessage());
		}
	}
	
	@SuppressWarnings("unused")
	@DisplayName("Test basic conversion")
	@Test
	public void testBasicConversion() {
		URL file_url = ClassLoader.getSystemResource("Duplex.ifc");
		try {
			File ifc_file = new File(file_url.toURI());
			File temp_file = File.createTempFile("ifc2lbd", "test.ttl");
			new IFCtoLBDConverter(ifc_file.getAbsolutePath(), "https://dot.dc.rwth-aachen.de/IFCtoLBDset#",
					temp_file.getAbsolutePath(), 0, true, false, true, false, false, true);
			new IFCtoLBDConverter(ifc_file.getAbsolutePath(), "https://dot.dc.rwth-aachen.de/IFCtoLBDset#",
					temp_file.getAbsolutePath(), 0, true, false, true, false, false, false);
			new IFCtoLBDConverter(ifc_file.getAbsolutePath(), "https://dot.dc.rwth-aachen.de/IFCtoLBDset#",
					temp_file.getAbsolutePath(), 0, true, false, true, false, false, false, true);
			new IFCtoLBDConverter(ifc_file.getAbsolutePath(), "https://dot.dc.rwth-aachen.de/IFCtoLBDset#",
					temp_file.getAbsolutePath(), 0, true, false, true, false, false, false, false);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Conversion had an error: " + e.getMessage());
		}
	}

	
	@SuppressWarnings("unused")
	@DisplayName("Test old IFC version conversion")
	@Test
	public void testOldIFCVersionConversion() {
		URL file_url = ClassLoader.getSystemResource("05111002_IFCR2_Geo_Columns_1.ifc");
		try {
			File ifc_file = new File(file_url.toURI());
			File temp_file = File.createTempFile("ifc2lbd", "test.ttl");
			new IFCtoLBDConverter(ifc_file.getAbsolutePath(), "https://dot.dc.rwth-aachen.de/IFCtoLBDset#",
					temp_file.getAbsolutePath(), 0, true, false, true, false, false, false);
		} catch (Exception e) {
			fail("Conversion had an error: " + e.getMessage());
		}
	}
	
	@DisplayName("Test conversion test case 1")
	@Test
	public void testConversion1() {
		URL file_url = ClassLoader.getSystemResource("Duplex.ifc");
		URL example_url = ClassLoader.getSystemResource("testConversion1_list.txt");
		List<String> subject_samples = new ArrayList<>();
		Scanner scanner;
		try {
			scanner = new Scanner(new File(example_url.toURI()));
			while (scanner.hasNextLine()) {
				String line=scanner.nextLine().trim();
				if(line.length()>0)
				   subject_samples.add(line);
			}
		} catch (FileNotFoundException | URISyntaxException e1) {
			e1.printStackTrace();
		}

		
		try {
			File ifc_file = new File(file_url.toURI());
			IFCtoLBDConverter c1nb = new IFCtoLBDConverter("https://dot.dc.rwth-aachen.de/IFCtoLBDset#", false, Integer.valueOf(1));
			
			Model m1nb = c1nb.convert(ifc_file.getAbsolutePath());
			ImmutableList<Resource> subjectList1 = ImmutableList.copyOf(m1nb.listSubjects());
			if(subjectList1.size()!=295)
			{
				System.out.println("Converted subject count  should not be 295. Was: "+subjectList1.size());
				fail("Converted subject count  should not be 295. Was: "+subjectList1.size());
			}
			
			if(m1nb.size()==0)
			{
				System.out.println("Conversion size should not be zero.");
				fail("Conversion size should not be zero.");
			}

			
			IFCtoLBDConverter c1wb = new IFCtoLBDConverter("https://dot.dc.rwth-aachen.de/IFCtoLBDset#", true, Integer.valueOf(1));
			Model m1wb = c1wb.convert(ifc_file.getAbsolutePath());
			
            ImmutableList<Resource> subjectList2 = ImmutableList.copyOf(m1wb.listSubjects());
			
			if(subjectList2.size()!=295)
			{
				System.out.println("Converted subject count should not be 295. Was: "+subjectList2.size());
				fail("Converted subject count  should not be 295. Was: "+subjectList2.size());
			}
			
			IFCtoLBDConverter c2nb = new IFCtoLBDConverter("https://dot.dc.rwth-aachen.de/IFCtoLBDset#", false, Integer.valueOf(2));
			Model m2nb = c2nb.convert(ifc_file.getAbsolutePath());
			
            ImmutableList<Resource> subjectList3 = ImmutableList.copyOf(m2nb.listSubjects());

			if(subjectList3.size()!=6792)
			{
				System.out.println("Converted subject count should not be 6792. Was: "+subjectList3.size());
				fail("Converted subject count  should not be 6792. Was: "+subjectList3.size());
			}

			IFCtoLBDConverter c2wb = new IFCtoLBDConverter("https://dot.dc.rwth-aachen.de/IFCtoLBDset#", true, Integer.valueOf(2));
			
			Model m2wb = c2wb.convert(ifc_file.getAbsolutePath()); 
			
            ImmutableList<Resource> subjectList4 = ImmutableList.copyOf(m2wb.listSubjects());

			if(subjectList4.size()!=6799)
			{
				System.out.println("Converted subject count should not be 6799. Was2: "+subjectList4.size());
				fail("Converted subject count  should not be 6799. Was2: "+subjectList4.size());
			}

			IFCtoLBDConverter c3nb1 = new IFCtoLBDConverter("https://dot.dc.rwth-aachen.de/IFCtoLBDset#", false, Integer.valueOf(3));
			Model m3nb = c3nb1.convert(ifc_file.getAbsolutePath());  
			
            ImmutableList<Resource> subjectList51 = ImmutableList.copyOf(m3nb.listSubjects());
            System.out.println("s5");
            for(Resource r: subjectList51)
            {
            	if(!subject_samples.contains(r.getURI().split("_a")[0].split("_p")[0]))
            	{
            	    System.out.println(""+r);
            	    fail("Converted subjects: extras: ");
            	    
            	}
            }
            
            
			if(subjectList51.size()!=13296)
			{
				System.out.println("Converted subject count should not be 13296. Was: "+subjectList51.size());
				fail("Converted subject count  should not be 13296. Was: "+subjectList51.size());
			}
			
			
			IFCtoLBDConverter c3nb2 = new IFCtoLBDConverter("https://dot.dc.rwth-aachen.de/IFCtoLBDset#", false, Integer.valueOf(3));
			Model m3nb2 = c3nb2.convert(ifc_file.getAbsolutePath());  
			
            ImmutableList<Resource> subjectList52 = ImmutableList.copyOf(m3nb2.listSubjects());
            
            List<Resource> comparison = new ArrayList<>();
            comparison.addAll(subjectList52);
            comparison.removeAll(subjectList52);
            if(comparison.size()!=0)
			{
				System.out.println("Two comparison and different results. Was: "+comparison);
				fail("Two comparison and different results. Was: ");
			}
            
			if(subjectList52.size()!=13296)
			{
				System.out.println("Converted subject count should not be 13296. Was: "+subjectList52.size());
				fail("Converted subject count  should not be 13296. Was: "+subjectList52.size());
			}

			IFCtoLBDConverter c3wb = new IFCtoLBDConverter("https://dot.dc.rwth-aachen.de/IFCtoLBDset#", true, Integer.valueOf(3));

			Model m3wb = c3wb.convert(ifc_file.getAbsolutePath());
			
            ImmutableList<Resource> subjectList6 = ImmutableList.copyOf(m3wb.listSubjects());
            

			if(subjectList6.size()!=13303)
			{
				System.out.println("Converted subject count should not be 13303. Was: "+subjectList6.size());
				fail("Converted subject count  should not be 13303. Was: "+subjectList6.size());
			}

		} catch (Exception e) {
			fail("Conversion set 1 had an error: " + e.getMessage());
		}
	}
	
	@DisplayName("Test conversion test case 2: class variable text")
	@Test
	public void testConversion2() {
		URL file_url = ClassLoader.getSystemResource("Duplex.ifc");
		try {
			File ifc_file = new File(file_url.toURI());

			IFCtoLBDConverter c3nb1 = new IFCtoLBDConverter("https://dot.dc.rwth-aachen.de/IFCtoLBDset#", false, Integer.valueOf(3));
			Model m3nb1 = c3nb1.convert(ifc_file.getAbsolutePath());
			
            ImmutableList<Resource> subjectList51 = ImmutableList.copyOf(m3nb1.listSubjects());         
			System.out.println("Converted subject count  5 was: "+subjectList51.size());

			if(subjectList51.size()!=13293)
			{
				System.out.println("Converted subject count should not be 295. Was: "+subjectList51.size());
				fail("Converted subject count  should not be 295. Was: "+subjectList51.size());
			}
			
			IFCtoLBDConverter c3nb2 = new IFCtoLBDConverter("https://dot.dc.rwth-aachen.de/IFCtoLBDset#", true, 3);
			Model m3nb2 = c3nb2.convert(ifc_file.getAbsolutePath());
			m3nb2.write(System.out,"TTL");
            ImmutableList<Resource> subjectList52 = ImmutableList.copyOf(m3nb2.listSubjects());
            
			System.out.println("Converted subject count  5 was: "+subjectList52.size());

			if(subjectList52.size()!=13293)
			{
				System.out.println("Converted subject count should not be 295. Was: "+subjectList52.size());
				fail("Converted subject count  should not be 295. Was: "+subjectList52.size());
			}
			

		} catch (Exception e) {
			fail("Conversion set 1 had an error: " + e.getMessage());
		}
	}
	
	@DisplayName("Test conversion test case 3: repeated convert")
	@Test
	public void testConversion3() {
		URL file_url = ClassLoader.getSystemResource("Duplex.ifc");
		try {
			File ifc_file = new File(file_url.toURI());

			IFCtoLBDConverter c3nb1 = new IFCtoLBDConverter("https://dot.dc.rwth-aachen.de/IFCtoLBDset#", false, Integer.valueOf(3));
			Model m3nb1 = c3nb1.convert(ifc_file.getAbsolutePath());
			
            ImmutableList<Resource> subjectList51 = ImmutableList.copyOf(m3nb1.listSubjects());         
			System.out.println("Converted subject count  5 was: "+subjectList51.size());

			
			IFCtoLBDConverter c3nb2 = new IFCtoLBDConverter("https://dot.dc.rwth-aachen.de/IFCtoLBDset#", true, 3);
			Model m3nb21 = c3nb2.convert(ifc_file.getAbsolutePath());
			Model m3nb22 = c3nb2.convert(ifc_file.getAbsolutePath());
		    
			Model out =m3nb21.remove(m3nb22);
			
			if(out.size()!=0)
			{
				System.out.println("Converted result shouls not change when doining it repeteadly.");
				fail("Converted result shouls not change when doining it repeteadly.");
			}

		} catch (Exception e) {
			fail("Conversion set 1 had an error: " + e.getMessage());
		}
	}
	
	@DisplayName("Test conversion test case Level 3")
	@Test
	public void testConversionLevel3() {
		System.out.println("Start");
		URL ifc_file_url = ClassLoader.getSystemResource("Duplex.ifc");
		URL rule_file_url = ClassLoader.getSystemResource("SHACL_rulesetLevel3.ttl");
		try {
			File ifc_file = new File(ifc_file_url.toURI());

			IFCtoLBDConverter c1nb = new IFCtoLBDConverter("https://dot.dc.rwth-aachen.de/IFCtoLBDset#", false, Integer.valueOf(3));
			Model m1nb = c1nb.convert(ifc_file.getAbsolutePath());

			Graph graph_m1nb = m1nb.getGraph();

			File rule1_file = new File(rule_file_url.toURI());
			Graph shapesGraph = RDFDataMgr.loadGraph(rule1_file.getAbsolutePath());
			Shapes shapes = Shapes.parse(shapesGraph);

			ValidationReport report = ShaclValidator.get().validate(shapes, graph_m1nb);
			if (!report.conforms()) {
				System.out.println("false");

				ShLib.printReport(report);
				RDFDataMgr.write(System.out, report.getModel(), Lang.TTL);
				fail("Conversion output does not conform SHACL_rulesetLevel3");
			} else
				System.out.println("Actually ok");
		} catch (Exception e) {
			System.out.println("ERROR");
			e.printStackTrace();
			fail("Conversion using set SHACL_rulesetLevel3 had an error: " + e.getMessage());
		}
	}
	


	@DisplayName("Test IfcRelAggregates not converting #16")
	@Test
	public void testConversionBugIfcRelAggregatesNotConverting() {
		System.out.println("Start");
		URL ifc_file_url = ClassLoader.getSystemResource("SampleHouse.ifc");
		URL rule_file_url = ClassLoader.getSystemResource("SHACL_rulesetLevel2.ttl");
		try {
			File ifc_file = new File(ifc_file_url.toURI());

			IFCtoLBDConverter c1nb = new IFCtoLBDConverter("https://dot.dc.rwth-aachen.de/IFCtoLBDset#", false, Integer.valueOf(1));
			Model m1nb = c1nb.convert(ifc_file.getAbsolutePath());
			// m1nb.removeAll();
			m1nb.write(System.out, "TTL");
			Graph graph_m1nb = m1nb.getGraph();

			File rule1_file = new File(rule_file_url.toURI());
			Graph shapesGraph = RDFDataMgr.loadGraph(rule1_file.getAbsolutePath());
			Shapes shapes = Shapes.parse(shapesGraph);

			ValidationReport report = ShaclValidator.get().validate(shapes, graph_m1nb);
			if (!report.conforms()) {
				System.out.println("false");

				ShLib.printReport(report);
				RDFDataMgr.write(System.out, report.getModel(), Lang.TTL);
				fail("Conversion output does not conform SHACL_rulesetLevel3");
			} else
				System.out.println("Actually ok");

		} catch (Exception e) {
			System.out.println("ERROR");
			e.printStackTrace();
			fail("Conversion using set SHACL_rulesetLevel4 had an error: " + e.getMessage());
		}
	}

	@DisplayName("IFC Virtual Elements not found #4")
	@Test
	public void testConversionBugIfcVirtualElement() {
		System.out.println("Start");
		URL ifc_file_url = ClassLoader.getSystemResource("IFC_Schependomlaan.ifc");
		URL rule_file_url = ClassLoader.getSystemResource("SHACL_rulesetLevel2.ttl");
		try {
			File ifc_file = new File(ifc_file_url.toURI());

			IFCtoLBDConverter c1nb = new IFCtoLBDConverter("https://dot.dc.rwth-aachen.de/IFCtoLBDset#", false, Integer.valueOf(1));
			Model m1nb = c1nb.convert(ifc_file.getAbsolutePath());
			m1nb.write(System.out, "TTL");
			Graph graph_m1nb = m1nb.getGraph();

			File rule1_file = new File(rule_file_url.toURI());
			Graph shapesGraph = RDFDataMgr.loadGraph(rule1_file.getAbsolutePath());
			Shapes shapes = Shapes.parse(shapesGraph);

			ValidationReport report = ShaclValidator.get().validate(shapes, graph_m1nb);
			if (!report.conforms()) {
				System.out.println("false");

				ShLib.printReport(report);
				RDFDataMgr.write(System.out, report.getModel(), Lang.TTL);
				fail("Conversion output does not conform SHACL_rulesetLevel3");
			} else
				System.out.println("Actually ok");

		} catch (Exception e) {
			System.out.println("ERROR");
			e.printStackTrace();
			fail("Conversion using set SHACL_rulesetLevel4 had an error: " + e.getMessage());
		}
	}

	@DisplayName("IFC3 Duplex_A from Japan")
	@Test
	public void testDuplexAJapanBug() {
		System.out.println("Start");
		URL ifc_file_url = ClassLoader.getSystemResource("Duplex_AJ.ifc");
		URL rule_file_url = ClassLoader.getSystemResource("SHACL_rulesetLevel2.ttl");
		try {
			File ifc_file = new File(ifc_file_url.toURI());

			IFCtoLBDConverter c1nb = new IFCtoLBDConverter("https://dot.dc.rwth-aachen.de/IFCtoLBDset#", false, Integer.valueOf(1));
			Model m1nb = c1nb.convert(ifc_file.getAbsolutePath());
			m1nb.write(System.out, "TTL");
			Graph graph_m1nb = m1nb.getGraph();

			File rule1_file = new File(rule_file_url.toURI());
			Graph shapesGraph = RDFDataMgr.loadGraph(rule1_file.getAbsolutePath());
			Shapes shapes = Shapes.parse(shapesGraph);

			ValidationReport report = ShaclValidator.get().validate(shapes, graph_m1nb);
			if (!report.conforms()) {
				System.out.println("false");

				ShLib.printReport(report);
				RDFDataMgr.write(System.out, report.getModel(), Lang.TTL);
				fail("Conversion output does not conform SHACL_rulesetLevel3");
			} else
				System.out.println("Actually ok");

		} catch (Exception e) {
			System.out.println("ERROR");
			e.printStackTrace();
			fail("Conversion using set SHACL_rulesetLevel4 had an error: " + e.getMessage());
		}
	}

	@DisplayName("Test RTree")
	@Test
	public void testRTree() {
		try {
			RTree<Resource, Geometry> rtree = RTree.dimensions(3).create();

			Rectangle rectangle = Rectangle.create(-0.5, -0.5, -0.5, 0.1, 0.1, 0.1);
			Resource r1 = ResourceFactory.createResource("http://example.de/1");
			rtree = rtree.add(r1, rectangle); // rtree is immutable

			Rectangle rectangle2 = Rectangle.create(-0.1, -0.1, -0.1, 1, 1, 1);

			Iterable<Entry<Resource, Geometry>> results = rtree.search(rectangle2);
			boolean correct = false;
			for (@SuppressWarnings("unused")
			Entry<Resource, Geometry> e : results) {
				correct = true;
			}
			if (!correct)
				fail("RTree test failed");

		} catch (Exception e) {
			fail("RTree fails: " + e.getMessage());
		}
	}

	@DisplayName("ifcOWL test 1")
	@Test
	public void test_ifcOWL1() {
		System.out.println("Start");
		URL ifc_file_url = ClassLoader.getSystemResource("Duplex.ifc");
		try {
			File ifc_file = new File(ifc_file_url.toURI());
			File tmp_output = File.createTempFile("ifc", ".ttl");
			IFCtoLBDConverter c1nb = new IFCtoLBDConverter("https://test.de/", false, Integer.valueOf(1));
			c1nb.convert(ifc_file.getAbsolutePath(), tmp_output.getAbsolutePath(), true, false, true, false, true, false,
					true, false);
			File ifcOwlFile = new File(tmp_output.getAbsolutePath().split("\\.ttl")[0] + "_ifcOWL.ttl");
			if (!ifcOwlFile.exists()) {
				System.out.println("No ifcOWL File created");
				System.out.println("Filename was: " + ifcOwlFile.getAbsolutePath());
				fail("No ifcOWL File created");
			}
           
			long bytes = ifcOwlFile.length();
			if (bytes!=20289155) {
				System.out.println("Wrong file size for ifcOWL result. (can be Jena version dependent) size was: "+bytes);
				System.out.println("Filename was: " + ifcOwlFile.getAbsolutePath());
				fail("Wrong file size for ifcOWL result. (can be Jena version dependent)");
			}
		} catch (Exception e) {
			System.err.println("ERROR");
			e.printStackTrace();
			fail("ifcOWL test 1 had an error: " + e.getMessage());
		}
	}

	}
