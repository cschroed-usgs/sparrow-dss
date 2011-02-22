package gov.usgswim.sparrow.domain;

import static org.junit.Assert.*;
import gov.usgswim.sparrow.service.ServiceResponseWrapper;
import gov.usgswim.sparrow.service.ServletResponseParser;

import org.junit.Test;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

public class GeometryTest {

	@Test
	public void marshalUnmarshalAGeometry() {
		float[] ords = new float[] {0f, -1f, 1f, 3f};
		Geometry geom = new Geometry(ords, true);
		
		assertEquals(0f, geom.getMinLong(), .0001f);
		assertEquals(1f, geom.getMaxLong(), .0001f);
		
		assertEquals(-1f, geom.getMinLat(), .0001f);
		assertEquals(3f, geom.getMaxLat(), .0001f);
		
		assertEquals(.5f, geom.getCenterLong(), .0001f);
		assertEquals(1f, geom.getCenterLat(), .0001f);
	}
	

}
