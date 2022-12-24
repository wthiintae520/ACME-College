/**
 * File:  TestACMECollegeSystem.java
 * Course materials (22F) CST 8277
 * Teddy Yap
 * (Original Author) Mike Norman
 *
 * @date 2022 12
 *
 * (Modified) @author Student Name
 *  040778696,Meina He 
 *  041025684,Kai Zhao 
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

import javax.json.JsonObject;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import acmecollege.entity.Student;

@SuppressWarnings("unused")

@TestMethodOrder(MethodOrderer.MethodName.class)
public class TestACMECollegeSystem {
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
    public void test01_all_students_with_adminrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            //.register(userAuth)
            .register(adminAuth)
            .path(STUDENT_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        List<Student> students = response.readEntity(new GenericType<List<Student>>(){});
        assertThat(students, is(not(empty())));
        assertThat(students, hasSize(1));
    }

    /**
     * Test get all membership cards with admin privilege
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    @Test
    public void test01_all_membership_cards_with_adminrole() throws JsonMappingException, JsonProcessingException{
            Response response = webTarget
                    //.register(userAuth)
                    .register(adminAuth)
                    .path(MEMBERSHIP_CARD_RESOURCE_NAME)
                    .request()
                    .get();
            assertThat(response.getStatus(), is(200));
            List<MembershipCard> cards = response.readEntity(new GenericType<List<MembershipCard>>(){});
            assertThat(cards, is(not(empty())));
    }

    /**
     * Test get all membership cards with user privilege, which will get HTTP 403 Forbidden response,
     * insufficient rights to a resource
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    @Test
    public void test06_all_membership_cards_with_userrole() throws JsonMappingException, JsonProcessingException{
        Response response = webTarget
                .register(userAuth)
//                .register(adminAuth)
                .path(MEMBERSHIP_CARD_RESOURCE_NAME)
                .request()
                .get();
        assertThat(response.getStatus(), is(403)); //insufficient rights to a resource
    }

    @Test
    public void test02_membership_card_by_id_with_adminrole() throws JsonMappingException, JsonProcessingException{
        Response response = webTarget
                //.register(userAuth)
                .register(adminAuth)
                .path(MEMBERSHIP_CARD_RESOURCE_NAME + "/1")//add by kai
                .request()
                .get();
        assertThat(response.getStatus(), is(200));
        MembershipCard card = response.readEntity(new GenericType<>() {
        });
        assertThat(card.getId(), is(1));
    }

    @Test
    public void test03_add_membership_card() throws JsonMappingException, JsonProcessingException{
        Student owner = new Student();
        owner.setId(1);

        ClubMembership membership = new ClubMembership();
        membership.setId(1);
        MembershipCard card = new MembershipCard();
        card.setClubMembership(membership);
        card.setOwner(owner);
        card.setSigned(true);
//        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
//        String json = ow.writeValueAsString(card);
//        System.out.println(json);
        Response response = webTarget
                //.register(userAuth)
                .register(adminAuth)
                .path(MEMBERSHIP_CARD_RESOURCE_NAME)//
                .request()
                .post(Entity.entity(card, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus(), is(200));
        assertThat(card.getOwner().getId(), is(1));
    }

    /**
     * Test to add a membership cards with user privilege, which will get HTTP 403 Forbidden response,
     * insufficient rights to a resource
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    @Test
    public void test08_add_membership_card_with_userrole() throws JsonMappingException, JsonProcessingException{
        Student owner = new Student();
        owner.setId(1);

        ClubMembership membership = new ClubMembership();
        membership.setId(1);
        MembershipCard card = new MembershipCard();
        card.setClubMembership(membership);
        card.setOwner(owner);
        card.setSigned(true);
        Response response = webTarget
                .register(userAuth)
//                .register(adminAuth)
                .path(MEMBERSHIP_CARD_RESOURCE_NAME)//
                .request()
                .post(Entity.entity(card, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus(), is(403)); //insufficient rights to a resource
    }


    /**
     * Membership Card 9 does not belong to user cst8277, expect get 403 no privilege
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    @Test
    public void test07_membership_card_by_id_with_userrole() throws JsonMappingException, JsonProcessingException{
        Response response = webTarget
                .register(userAuth)
//                .register(adminAuth)
                .path(MEMBERSHIP_CARD_RESOURCE_NAME + "/9")//add by kai
                .request()
                .get();
        assertThat(response.getStatus(), is(403));
//        MembershipCard card = response.readEntity(new GenericType<>() {
//        });
//        assertThat(card.getId(), is(1));
    }

    /**
     * User cannot delete a membership card
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    @Test
    public void test04_delete_membership_card_by_id_with_userrole() throws JsonMappingException, JsonProcessingException{
        Response response1 = webTarget
                //.register(userAuth)
                .register(adminAuth)
                .path(MEMBERSHIP_CARD_RESOURCE_NAME)
                .request()
                .get();
        List<MembershipCard> cards = response1.readEntity(new GenericType<List<MembershipCard>>(){});
        String id = Integer.toString(cards.get(cards.size() -1).getId());
        
    	Response response2 = webTarget
                .register(userAuth)
//                .register(adminAuth)
                .path(MEMBERSHIP_CARD_RESOURCE_NAME + "/" + id)
                .request()
                .delete();
        assertThat(response2.getStatus(), is(400));
    }

    @Test
    public void test05_update_membership_card_by_id_with_adminrole() throws JsonMappingException, JsonProcessingException{
        Response response1 = webTarget
                //.register(userAuth)
                .register(adminAuth)
                .path(MEMBERSHIP_CARD_RESOURCE_NAME)
                .request()
                .get();
        List<MembershipCard> cards = response1.readEntity(new GenericType<List<MembershipCard>>(){});
        Student updatingStudent = new Student();
        updatingStudent.setLastName("Swift");
        updatingStudent.setFirstName("Taylor");
        MembershipCard card = cards.get(cards.size() - 1);
        card.setOwner(updatingStudent);
//        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
//        String json = ow.writeValueAsString(card);
//        System.out.println(json);

        Response response2 = webTarget
                //.register(userAuth)
                .register(adminAuth)
                .path(MEMBERSHIP_CARD_RESOURCE_NAME + "/1")
                .request()
                .put(Entity.entity(card, MediaType.APPLICATION_JSON));
        assertThat(response2.getStatus(), is(200));
        assertThat(card.getOwner().getFirstName(), is("Taylor"));
    }

}
