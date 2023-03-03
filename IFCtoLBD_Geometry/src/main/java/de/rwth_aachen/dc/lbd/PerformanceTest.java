package de.rwth_aachen.dc.lbd;

import java.io.File;
import java.io.IOException;

import org.bimserver.plugins.deserializers.DeserializeException;
import org.bimserver.plugins.renderengine.RenderEngineException;

public class PerformanceTest {

	public static void main(String[] args) {
		 try {
			new IFCGeometry(new File("c:\\jo\\Duplex_A_20110907.ifc"));
		} catch (RenderEngineException | DeserializeException | IOException e) {
			e.printStackTrace();
		}
		System.out.println("done");
	}

}