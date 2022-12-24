/**
 * File:  TestStudentClub.java
 * @author Yunting Yin
 * @date December 8, 2022 created
 * @date December 9, 2022 modified
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import acmecollege.entity.ClubMembership;
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

import acmecollege.entity.StudentClub;
import acmecollege.entity.StudentClub_;
import acmecollege.entity.AcademicStudentClub;
import acmecollege.entity.AcademicStudentClub_;

@SuppressWarnings("unused")

@TestMethodOrder(MethodOrderer.MethodName.class)
public class TestStudentClubAndClubMembership {
	
	private static final Class<?> _thisClaz = MethodHandles.lookup().lookupClass();
    private static final Logger logger = LogManager.getLogger(_thisClaz);
    static final String HTTP_SCHEMA = "http";
    static final String HOST = "localhost";
    static final int PORT = 8080;
	
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
    }

    protected WebTarget webTarget;
    @BeforeEach
    public void setUp() {
        Client client = ClientBuilder.newClient(
            new ClientConfig().register(MyObjectMapperProvider.class).register(new LoggingFeature()));
        webTarget = client.target(uri);
    }

	@Test
	@Order(4)
	public void test01_all_student_clubs_with_adminrole() throws JsonMappingException, JsonProcessingException {
		Response response = webTarget
            .register(adminAuth)
            .path(STUDENT_CLUB_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
    }

	@Test
	@Order(5)
	public void test02_all_student_clubs_with_userrole() throws JsonMappingException, JsonProcessingException {
		Response response = webTarget
            .register(userAuth)
            .path(STUDENT_CLUB_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
    }
	
	@Test
	@Order(6)
	public void test03_add_student_club_with_userrole() throws JsonMappingException, JsonProcessingException {
		AcademicStudentClub academicStudentClub = new AcademicStudentClub();
		Response response = webTarget
            .register(userAuth)
            .path(STUDENT_CLUB_RESOURCE_NAME)
            .request()
            .post(Entity.json(academicStudentClub)); 
        assertThat(response.getStatus(), is(403));
    }
	
	@Test
	@Order(7)
	public void test04_delete_student_club_by_id_with_userrole() throws JsonMappingException, JsonProcessingException {
		Response response = webTarget
            .register(userAuth)
            .path(STUDENT_CLUB_RESOURCE_NAME + "/" + 2)
            .request()
            .delete(); 
        assertThat(response.getStatus(), is(403));
    }
	
	@Test
	@Order(8)
	public void test05_all_club_memberships_with_adminrole() throws JsonMappingException, JsonProcessingException {
		Response response = webTarget
            .register(adminAuth)
            .path(CLUB_MEMBERSHIP_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        List<ClubMembership> clubmemberships = response.readEntity(new GenericType<List<ClubMembership>>(){});
        assertThat(clubmemberships, is(not(empty())));
    }
	
	@Test
	@Order(9)
	public void test06_all_club_memberships_with_userrole() throws JsonMappingException, JsonProcessingException {
		Response response = webTarget
            .register(userAuth)
            .path(CLUB_MEMBERSHIP_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        List<ClubMembership> clubmemberships = response.readEntity(new GenericType<List<ClubMembership>>(){});
        assertThat(clubmemberships, is(not(empty())));
    }
	
	@Test
	@Order(10)
	public void test07_add_club_membership_with_userrole() throws JsonMappingException, JsonProcessingException {
		ClubMembership clubMembership = new ClubMembership();
		Response response = webTarget
            .register(userAuth)
            .path(CLUB_MEMBERSHIP_RESOURCE_NAME)
            .request()
            .post(Entity.json(clubMembership)); 
        assertThat(response.getStatus(), is(403));
    }
	
	@Test
	@Order(11)
	public void test08_delete_club_membership_by_id_with_userrole() throws JsonMappingException, JsonProcessingException {
		Response response = webTarget
            .register(userAuth)
            .path(CLUB_MEMBERSHIP_RESOURCE_NAME + "/" + 2)
            .request()
            .delete(); 
        assertThat(response.getStatus(), is(403));
    }
}
