package configs.cloud.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import configs.cloud.client.entity.Config;
import configs.cloud.client.entity.Dataset;
import configs.cloud.client.entity.Env;

public class CloudConfigClientTest {
	
	CloudConfigClient tester = new CloudConfigClient("dG04MHBwUEJsMG81NU1GUktYZmEy","https://app.configs.cloud/"); // MyClass is tested
	
	Config c = new Config();
	
	// *** Modify *** 
	// appropriately before testing.
	String testkey = "ldap.hostname";
	String testValue = "dev.ldap.example.com"; 
	String envSname = "Dev"; 
	String searchKey = "ldap.*";
	
	public CloudConfigClientTest(){
		tester.setClientDefaults(2, "Dev");
		
		// *** Modify *** 
		// this object as per database entry before running test.
		// create a test Config obj. 
		
		c.setConfigid(174L);
		c.setKey(testkey);
		c.setValue(testValue);
		
		c.setIspassword("N");
		c.setIsenabled("Y");
		c.setVersion(1);
				
		Dataset d = new Dataset();
		d.setDatasetid(2L);
		d.setDatasetname("ds_JoeBloggs");
		c.setDataset(d);
		
		Env e = new Env();
		e.setEnvid(2L);
		e.setSname("Dev");
		e.setName("DEV");
		c.setEnv(e);

	}
	
	@Test
	public void testClientDefaultSettings() {
		tester.setClientDefaults(2, "Dev");
		
        // assert statements
        assertEquals("Testing Current Environment", "Dev", tester.getCurrentEnvironment());
        assertEquals("Testing Current Dataset",Integer.valueOf("2"), tester.getCurrentDataset());

    }
	
	@Test
	public void testGetConfigs() throws Exception {	
		assertTrue(tester.getConfigs().size() >0);
    }

	@Test
	public void testGetConfigsWithDatasetIdSuccess() throws Exception {	
		// correct datasetid
		assertTrue(tester.getConfigs(2).size() >0);
    }
	
	@Test (expected = configs.cloud.client.exceptions.ForbiddenException.class)
	public void testGetConfigsWithDatasetIdExceptionForbidden() throws Exception {	
		//wrong datasetid
		assertTrue(tester.getConfigs(3).size() >0);
    }
	
	@Test
	public void testGetConfigsWithKeynameSucess() throws Exception {	
		//right keyname
		assertEquals("Get config with right key",testValue,tester.getConfigValue(testkey));
    }
	
	@Test
	public void testGetConfigsWithKeynameWrong() throws Exception {	
		//right keyname
		assertEquals("Get config with invalid key","",tester.getConfigValue(testkey + ".notexist"));
    }
	
	@Test
	public void testGetConfigObject() throws Exception {	
		//right keyname
		assertTrue(tester.getConfig(testkey).equals(c));
    }
	@Test
	public void testGetConfigObjectWrongKey() throws Exception {	
		//right keyname
		assertTrue(tester.getConfig(testkey+".notexist")== null);
    }
	
	@Test
	public void testGetConfigsWithEnvAndKey() throws Exception {	
		//right keyname, right env
		assertTrue(tester.getConfig(envSname,testkey).equals(c));
    }
	
	@Test
	public void testGetConfigsWithEnvAndWrongKey() throws Exception {	
		//wrong keyname, right env
		assertTrue(tester.getConfig(envSname,testkey + ".notexist") == null);
    }
	
	@Test
	public void testGetConfigsWithWrongEnvAndKey() throws Exception {	
		//wrong keyname, right env
		assertTrue(tester.getConfig(envSname+ ".notexist",testkey ) == null);
    }
	

	@Test
	public void testGetConfigsForEnv() throws Exception {	
		//right env name
		assertTrue(tester.getConfigs(envSname).size() >0);
    }
	
	// There is a issue here ****  needs attention *** 
	@Test
	public void testGetConfigsForWrongEnv() throws Exception {	
		//wrong env name
		assertTrue(tester.getConfigs(envSname+ ".notexist") == null);
    }
	
	// There is a issue here ****  needs attention *** 
	@Test
	public void testSearchConfigs() throws Exception {	
		//search for a key list
		System.out.println(tester.searchConfigs(searchKey));
		assertTrue(tester.searchConfigs(searchKey) == null);
    }
	
	// There is a issue here ****  needs attention *** 
	@Test
	public void testSearchConfigsWithIqkSuccess() throws Exception {	
		//search for a key list
		//System.out.println(tester.searchConfigs(searchKey,true));
		assertTrue(tester.searchConfigs(searchKey) == null);
    }
	
	// There is a issue here ****  needs attention *** 
	@Test
	public void testSearchConfigsWithIqkFailure() throws Exception {	
		//search for a key list
		//System.out.println(tester.searchConfigs(searchKey,false));
		assertTrue(tester.searchConfigs(searchKey) == null);
    }
	
	
}
