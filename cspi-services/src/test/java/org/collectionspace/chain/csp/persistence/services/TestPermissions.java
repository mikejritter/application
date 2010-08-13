package org.collectionspace.chain.csp.persistence.services;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.collectionspace.bconfigutils.bootstrap.BootstrapConfigLoadFailedException;
import org.collectionspace.chain.csp.persistence.services.connection.ConnectionException;
import org.collectionspace.chain.csp.persistence.services.connection.RequestMethod;
import org.collectionspace.chain.csp.persistence.services.connection.ReturnedDocument;
import org.collectionspace.chain.csp.persistence.services.connection.ReturnedURL;
import org.collectionspace.csp.api.core.CSPDependencyException;
import org.collectionspace.csp.api.persistence.ExistException;
import org.collectionspace.csp.api.persistence.Storage;
import org.collectionspace.csp.api.persistence.UnderlyingStorageException;
import org.collectionspace.csp.api.persistence.UnimplementedException;
import org.dom4j.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestPermissions  extends ServicesBaseClass  {
	private static final Logger log=LoggerFactory.getLogger(TestPermissions.class);
	///cspace-services/authorization/permissions?res=acquisition&actGrp=CRUDL
	 
	
	@Before public void checkServicesRunning() throws BootstrapConfigLoadFailedException, ConnectionException {
		setup();
	}

	@Test public void testAssumptionMechanism() {
		log.debug("Services Running!");
	}

	//permissions
		//create user
		
		//list all permissions for a role
		//add permission to a role
		//change permission action in a role
	@Test public void testRoles() throws Exception{
		//create user
		String user = createUser("user1.json");
		assertFalse(user.equals(""));
		
		//list roles for user
		
		//list permissions for a role
		
	}
	@Test public void testPermissions() throws Exception{
		//create role
		JSONObject roledata = createRole("role.json");
		String role = roledata.getString("roleId");
		log.info(roledata.toString());
		assertFalse(roledata==null);
		
		//list permissions for role
		//cspace-services/authorization/roles/{csid}/permroles/xxx

		Storage ss;
		ss = makeServicesStorage(base+"/cspace-services/");
		JSONObject data = ss.getPathsJSON("roles/"+role+"/permrole",null);
		String[] roleperms=(String[]) data.get("listItems");
		log.info(data.toString());

		if(roleperms.length>0){
			log.info("has permissions already");
		}
		
		//add a permission
		//get acquisition crudl

		///cspace-services/authorization/permissions?res=acquisition&actGrp=CRUDL
		String resourceName = "acquisition";
		
		JSONObject permrestrictions = new JSONObject();
		permrestrictions.put("keywords", resourceName);
		permrestrictions.put("queryString", "CRUDL");
		permrestrictions.put("queryTerm", "actGrp");

		JSONObject pdata = ss.getPathsJSON("permission",permrestrictions);
		String[] perms=(String[]) pdata.get("listItems");
		String permID = "";
		if(perms.length>0){
			permID = perms[0];
		}
		else{
			//need to create this permission type
			fail("missing permission type Acquisition CRUDL " + permrestrictions.toString());			
		}
		
		JSONObject permdata = new JSONObject();
		permdata.put("permissionId", permID);
		permdata.put("resourceName", resourceName);
		JSONArray permarray = new JSONArray();
		permarray.put(permdata);
		
		JSONObject addperm = new JSONObject();
		addperm.put("permission", permarray);
		addperm.put("role", roledata);
		
		log.info(addperm.toString());
		//add permissions to role

		String path=ss.autocreateJSON("users",addperm);
		log.info(path);
		assertNotNull(path);
		
		//test permissions is in role
		
	}
	private JSONObject createRole(String jsonFile){

		Storage ss;
		try{
			//delete this role if exist
			JSONObject u1=getJSON(jsonFile);
			String roleName = u1.getString("roleName");
			JSONObject test = new JSONObject();
			test.put("keywords", roleName);
			ss = makeServicesStorage(base+"/cspace-services/");
			/* delete role if already exists */
			JSONObject data = ss.getPathsJSON("role/",test);
			String[] paths= (String[])data.get("listItems");
			log.info(data.toString());
			if(paths.length>0){
				ss.deleteJSON("role/"+paths[0]);
			}
			
			
			//create role
			String path=ss.autocreateJSON("role/",u1);
			assertNotNull(path);

			JSONObject u3=ss.retrieveJSON("role/"+path);
			assertNotNull(u3);
			//return role path

			JSONObject roledata = new JSONObject();
			roledata.put("roleName", roleName);
			roledata.put("roleId", path);
			return roledata;
			
		} catch (CSPDependencyException e) {
			fail("CSPDependencyException:"+e.getMessage());
		} catch (JSONException e) {
			fail("JSONException:"+e.getMessage());
		} catch (ExistException e) {
			fail("ExistException:"+e.getMessage());
		} catch (UnimplementedException e) {
			fail("UnimplementedException:"+e.getMessage());
		} catch (UnderlyingStorageException e) {
			fail("UnderlyingStorageException:"+e.getMessage());
		} catch (IOException e) {
			fail("IOException:"+e.getMessage());
		}
		return null;
	}
	private String createUser(String jsonFile){

		Storage ss;
		try {
			JSONObject u1=getJSON(jsonFile);
			String userId = u1.getString("userId");
			JSONObject test = new JSONObject();
			test.put("userId", userId);
			ss = makeServicesStorage(base+"/cspace-services/");
			/* delete user if already exists */
			JSONObject data = ss.getPathsJSON("users/",test);
			String[] paths= (String[])data.get("listItems");
			if(paths.length>0)
				ss.deleteJSON("users/"+paths[0]);
			
			/* create the user based on json */
			/* will give a hidden 500 error if userid is not unique (useful eh?) */
			String path=ss.autocreateJSON("users/",u1);
			assertNotNull(path);
			JSONObject u2=getJSON(jsonFile);
			ss.updateJSON("users/"+path,u2);
			JSONObject u3=ss.retrieveJSON("users/"+path);
			assertNotNull(u3);
			return path;
		} catch (CSPDependencyException e) {
			fail("CSPDependencyException:"+e.getMessage());
		} catch (JSONException e) {
			fail("JSONException:"+e.getMessage());
		} catch (ExistException e) {
			fail("ExistException:"+e.getMessage());
		} catch (UnimplementedException e) {
			fail("UnimplementedException:"+e.getMessage());
		} catch (UnderlyingStorageException e) {
			fail("UnderlyingStorageException:"+e.getMessage());
		} catch (IOException e) {
			fail("IOException:"+e.getMessage());
		}
		return "";
	}
	
//roles
	//list all roles for a user
	//add a role to a user
	//remove role from user
	
	
	
	
	

}
