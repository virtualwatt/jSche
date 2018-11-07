package vvat.jsche.core;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import junit.framework.TestCase;
import vvat.jsche.core.scheduleconfig.JScheConfig;
import vvat.jsche.core.scheduleconfig.JScheConfigs;

/**
 * Unit test for simple App.
 */
public class JScheTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
//    public JScheTest( String testName )
//    {
//        super( testName );
//    }

    /**
     * @return the suite of tests being tested
     */
//    public static Test suite()
//    {
//        return new TestSuite( JScheTest.class );
//    }

    /**
     * @throws JAXBException 
     */
    public void testMarshaller() throws JAXBException
    {
		JAXBContext jc = JAXBContext.newInstance(JScheConfigs.class, TestEventClassTest.class);
		Marshaller marshaller = jc.createMarshaller();
		JScheConfigs jscs = new JScheConfigs();
		JScheConfig jsc = new JScheConfig();
		List<JScheConfig> jscl = new ArrayList<JScheConfig>();
		jscl.add(jsc);
		jscs.setjScheConfig(jscl);
		jsc.setTimeZone("EET");
		List<String> time = new ArrayList<String>();
		time.add("11:12");
		jsc.setTime(time);
		TestEventClassTest testEventClass = new TestEventClassTest();
		jsc.setEvent(testEventClass);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		marshaller.marshal(jscs, baos);
		assertTrue(baos.toString().endsWith(
				"<jScheConfigs><jScheConfig><timeZone>EET</timeZone><time>11:12</time><testEventClassTest/></jScheConfig></jScheConfigs>"));
    }
    
    public void testUnmarshaller() throws JAXBException
    {
		JAXBContext jc = JAXBContext.newInstance(JScheConfigs.class, TestEventClassTest.class);
		Unmarshaller unmarshaller = jc.createUnmarshaller();
		JScheConfigs jscs = (JScheConfigs)unmarshaller.unmarshal(JScheTest.class.getClassLoader().getResourceAsStream("test.jSche.test.xml"));
		List<JScheConfig> getjScheConfig = jscs.getjScheConfig();
		JScheConfig jScheConfig = getjScheConfig.get(0);
		List<String> time = jScheConfig.getTime();
		assertEquals(time.get(0), "20:13");
		assertEquals(time.get(1), "20:14");
		assertEquals(jScheConfig.getTimeZone(), "EET");
		assertEquals(jScheConfig.getEvent().getClass(), TestEventClassTest.class);
    }
    
    public void testSettings()
    {
    	// TODO testSettings
    }
}
