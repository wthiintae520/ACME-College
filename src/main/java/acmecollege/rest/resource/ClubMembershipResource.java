/**
 * File:  ClubMembershipResource.java
 *
 * @author Yunting Yin 040994443
 */
package acmecollege.rest.resource;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.security.enterprise.SecurityContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import static acmecollege.utility.MyConstants.ADMIN_ROLE;
import static acmecollege.utility.MyConstants.USER_ROLE;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import acmecollege.ejb.ACMECollegeService;
import acmecollege.entity.ClubMembership;
import acmecollege.entity.MembershipCard;

@Path("clubmembership")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ClubMembershipResource {
    
    private static final Logger LOG = LogManager.getLogger();

    @EJB
    protected ACMECollegeService service;

    @Inject
    protected SecurityContext sc;
    
    @GET
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    public Response getClubMemberships() {
        LOG.debug("Retrieving all club memberships...");
        List<ClubMembership> clubMemberships = service.getAllClubMemberships();
        LOG.debug("Membership cards found = {}", clubMemberships);
        Response response = Response.ok(clubMemberships).build();
        return response;
    }
    
    @GET
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    @Path("/{clubMembershipId}")
    public Response getClubMembershipById(@PathParam("clubMembershipId") int clubMembershipId) {
    	LOG.debug("Retrieving club membership with id = {}", clubMembershipId);
    	ClubMembership clubMembership = service.getClubMembershipById(clubMembershipId);
        Response response = Response.ok(clubMembership).build();
        return response;
    }

    @DELETE
    @RolesAllowed({ADMIN_ROLE})
    @Path("/{clubMembershipId}")
    public Response deleteClubMembership(@PathParam("clubMembershipId") int clubMembershipId) {
    	LOG.debug("Deleting club membership with id = {}", clubMembershipId);
        Response response = null;
        ClubMembership clubMembership = service.deleteClubMembershipById(clubMembershipId);
        response = Response.ok(clubMembership).build();
        return response;
    }

    @POST
    @RolesAllowed({ADMIN_ROLE})
    public Response addClubMembership(ClubMembership newClubMembership) {
    	LOG.debug("Adding a new club membership = {}", newClubMembership);
    	Response response = null;
    	ClubMembership clubMembership = service.persistClubMembership(newClubMembership);
    	response = Response.ok(clubMembership).build();
    	return response;
    }
    
    @POST
    @RolesAllowed({ADMIN_ROLE})
    @Path("/{clubMembershipId}/membershipcard")
    public Response addMembershipCardToClubMembership(@PathParam("clubMembershipId") int clubMembershipId, MembershipCard newMembershipCard) {
        LOG.debug( "Adding a new membership card to club membership with id = {}", clubMembershipId);
        ClubMembership clubMembership = service.getClubMembershipById(clubMembershipId);
        newMembershipCard.setClubMembership(clubMembership);
        clubMembership.setCard(newMembershipCard);
        service.updateClubMembership(clubMembershipId, clubMembership);
        return Response.ok(clubMembership).build();
    }
    
    @PUT
    @RolesAllowed({ADMIN_ROLE})
    @Path("/{clubMembershipId}")
    public Response updateClubMembership(@PathParam("clubMembershipId")int clubMembershipId, ClubMembership updatedClubMembership) {
    	LOG.debug("Updating a specific club membership with id = {}", clubMembershipId);
    	Response response = null;
    	ClubMembership clubMembership = service.updateClubMembership(clubMembershipId, updatedClubMembership);
    	response = Response.ok(clubMembership).build();
    	return response;
    }
}
