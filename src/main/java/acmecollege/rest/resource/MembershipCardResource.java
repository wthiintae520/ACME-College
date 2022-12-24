package acmecollege.rest.resource;

import acmecollege.ejb.ACMECollegeService;
import acmecollege.entity.MembershipCard;
import acmecollege.entity.SecurityUser;
import acmecollege.entity.Student;
import acmecollege.entity.StudentClub;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.soteria.WrappingCallerPrincipal;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.security.enterprise.SecurityContext;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.List;

import static acmecollege.utility.MyConstants.*;

/**
 * File:  PersonResource.java Course materials (22F) CST 8277
 *
 * @author Teddy Yap
 * @author Shariar (Shawn) Emami
 * @author (original) Mike Norman
 * 
 * Updated by:  Group 7
 * 041025684,Kai Zhao 
 * 
 */

@Path(MEMBERSHIP_CARD_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MembershipCardResource {

    private static final Logger LOG = LogManager.getLogger();

    @EJB
    protected ACMECollegeService service;

    @Inject
    protected SecurityContext sc;

    @GET
    @RolesAllowed({ADMIN_ROLE})
    public Response getMembershipCards() {
        LOG.debug("Retrieving all membership cards...");
        List<MembershipCard> membershipCards = service.getAllMembershipCards();
        LOG.debug("Membership cards found = {}", membershipCards);
        Response response = Response.ok(membershipCards).build();
        return response;
    }

    //Only a ‘USER_ROLE’ user can read their ownMembershipCard.
    @GET
//    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response getMembershipCardById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int cardId) {
        LOG.debug("Retrieving membership card with id = {}", cardId);
        Response response = null;
        MembershipCard membershipCard = null;

        if(sc.isCallerInRole(ADMIN_ROLE)){
            membershipCard = service.getMembershipCardById(cardId);
            response = Response.status(membershipCard == null ? javax.ws.rs.core.Response.Status.NOT_FOUND : javax.ws.rs.core.Response.Status.OK).entity(membershipCard).build();
        } else if (sc.isCallerInRole(USER_ROLE)) {
            WrappingCallerPrincipal wCallerPrincipal = (WrappingCallerPrincipal) sc.getCallerPrincipal();
            SecurityUser sUser = (SecurityUser) wCallerPrincipal.getWrapped();
            Student student = sUser.getStudent();
            List<MembershipCard> membershipCards = service.getAllMembershipCards();
            Student studentSearched = null;
            for(MembershipCard card : membershipCards){
                if (cardId == card.getId()){
                    studentSearched = card.getOwner();
                }}
            if (student != null && student.getId() == studentSearched.getId()) {
                response = Response.status(Response.Status.OK).entity(student).build();
            } else {
                throw new ForbiddenException("User trying to access resource it does not own (wrong userid)");
            }
        } else {
            response = Response.status(Response.Status.BAD_REQUEST).build();
        }
        return response;
    }

    @RolesAllowed({ADMIN_ROLE})
    @POST
    public Response addMembershipCard(MembershipCard newMembershipCard) {
        LOG.debug("Adding a membership card = {}", newMembershipCard);
            MembershipCard tempMembershipCard = service.persistMembershipCard(newMembershipCard);
            return Response.ok(tempMembershipCard).build();
    }

    @DELETE
    // TODO SCR02 - Specify the roles allowed for this method
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response deleteMembershipCard(@PathParam(RESOURCE_PATH_ID_ELEMENT) int cardId) {
        LOG.debug("Deleting membership card with id = {}", cardId);
        MembershipCard card = service.deleteMembershipCardById(cardId);
        Response response = Response.ok(sc).build();
        return response;
    }

    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    @PUT
    @Path(RESOURCE_PATH_ID_PATH)
    public Response updateMembershipCard(@PathParam(RESOURCE_PATH_ID_ELEMENT) int cardId, MembershipCard updatingMembershipCard) {
        LOG.debug("Updating a specific membership card with id = {}", cardId);
        Response response = null;
        MembershipCard updatedMembershipCard = service.updateMembershipCardById(cardId, updatingMembershipCard);
        response = Response.ok(updatedMembershipCard).build();
        return response;
    }

}
