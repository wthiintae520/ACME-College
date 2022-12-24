/**
 * File:  TestACMECollegeSystem.java
 * Course materials (22F) CST 8277
 * Teddy Yap
 * (Original Author) Mike Norman
 *
 * @date 2022 Dec. 07th.
 *
 * (Modified) @author 040623714,Hongyu Wang 
 */
package acmecollege;

import static acmecollege.utility.MyConstants.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import acmecollege.entity.MembershipCard;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import acmecollege.entity.Student;
import acmecollege.entity.Professor;

@SuppressWarnings("unused")

@TestMethodOrder(MethodOrderer.MethodName.class)
public class TestProfessor {
    private static final Class<?> _thisClaz = MethodHandles.lookup().lookupClass();
    private static final Logger logger = LogManager.getLogger(_thisClaz);

    static final String HTTP_SCHEMA = "http";
    static final String HOST = "localhost";
    static final int PORT = 8080;
    static Professor professor;

    // Test fixture(s)
    static URI uri;
    static HttpAuthenticationFeature adminAuth;
    static HttpAuthenticationFeature userAuth;

    @BeforeAll
    public static void oneTimeSetUp() throws Exception {
        logger.debug("oneTimeSetUp");
        uri = UriBuilder
            .fromUri(APPLICATION_CONTEXT_ROOT + APPLICATION_API_VERSION)
            .scheme(HTTP_SCHEMA)
            .host(HOST)
            .port(PORT)
            .build();
        adminAuth = HttpAuthenticationFeature.basic(DEFAULT_ADMIN_USER, DEFAULT_ADMIN_USER_PASSWORD);
        userAuth = HttpAuthenticationFeature.basic(DEFAULT_USER, DEFAULT_USER_PASSWORD);
    
        professor = new Professor();
        professor.setProfessor("Teddy", "Yap", "Java");
    }

    protected WebTarget webTarget;
    @BeforeEach
    public void setUp() {
        Client client = ClientBuilder.newClient(
            new ClientConfig().register(MyObjectMapperProvider.class).register(new LoggingFeature()));
        webTarget = client.target(uri);
    }

    @Order(1)
    @Test
    public void test01_get_all_professor_with_adminrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            //.register(userAuth)
            .register(adminAuth)
            .path(PROFESSOR_SUBRESOURCE_NAME)
            .request()
            .get();
        List<Professor> professors = response.readEntity(new GenericType<List<Professor>>(){});
        assertThat(response.getStatus(), is(200));
        assertThat(professors, is(not(empty())));
    }

	@Order(2)
    @Test
    public void test02_get_all_professor_with_userrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(userAuth)
            //.register(adminAuth)
            .path(PROFESSOR_SUBRESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(403));
	}
	
	 @Order(3)
	 @Test
	 public void test03_get_professor_by_id_with_adminrole() throws JsonMappingException, JsonProcessingException {
		 Response response = webTarget
			//.register(userAuth)
			.register(adminAuth)
			.path(PROFESSOR_SUBRESOURCE_NAME+"/1")
			.request()
			.get();
		 assertThat(response.getStatus(), is(200));
	     Professor professor = response.readEntity(new GenericType<>() {	    	 
	     });
	     assertThat(professor.getId(), is(1));
	    }
	 
	 @Order(4)
	 @Test
	 public void test04_get_professor_by_id_with_userrole() throws JsonMappingException, JsonProcessingException {
		 Response response = webTarget
			.register(userAuth)
			//.register(adminAuth)
			.path(PROFESSOR_SUBRESOURCE_NAME+"/1")
			.request()
			.get();
		 assertThat(response.getStatus(), is(403));
	 }
	 
	 @Order(5)
	 @Test
     public void test05_add_professor_with_adminrole() throws JsonMappingException, JsonProcessingException{
		 Professor professor = new Professor("X", "Man", "Movie", null);
		 Response response = webTarget
			//.register(userAuth)
            .register(adminAuth)
            .path(PROFESSOR_SUBRESOURCE_NAME)
            .request()
            .post(Entity.json(professor));
		 assertThat(response.getStatus(), is(200));
	 }
	 
	 @Order(6)
	 @Test
     public void test06_add_professor_with_userrole() throws JsonMappingException, JsonProcessingException{
		 Professor professor = new Professor("X", "Man", "Movie", null);
		 Response response = webTarget
			.register(userAuth)
            //.register(adminAuth)
            .path(PROFESSOR_SUBRESOURCE_NAME)
            .request()
            .post(Entity.json(professor));
		 assertThat(response.getStatus(), is(403));
    }
	
    @Order(7)
    @Test
    public void test07_delete_professor_by_id_with_adminrole() throws JsonMappingException, JsonProcessingException {
    	Response response = webTarget
	    	//.register(userAuth)        	
	        .register(adminAuth)
	        .path(PROFESSOR_SUBRESOURCE_NAME+"/"+ 1)
	        .request()
	        .delete();
        assertThat(response.getStatus(), is(200));
    }
    
    @Order(8)
    @Test
    public void test08_delete_professor_by_id_with_userrole() throws JsonMappingException, JsonProcessingException {
    	Response response = webTarget
	    	.register(userAuth)        	
	        //.register(adminAuth)
	        .path(PROFESSOR_SUBRESOURCE_NAME+"/"+professor.getId())
	        .request()
	        .delete();
        assertThat(response.getStatus(), is(403));
    }
}
