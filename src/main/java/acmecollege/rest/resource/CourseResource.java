package acmecollege.rest.resource;

import static acmecollege.utility.MyConstants.*;

import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.security.enterprise.SecurityContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import acmecollege.ejb.ACMECollegeService;
import acmecollege.entity.Course;
import acmecollege.entity.CourseRegistration;
import acmecollege.entity.SecurityUser;
//import acmecollege.entity.Student;
//import acmecollege.entity.StudentClub;
import acmecollege.entity.Student;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.soteria.WrappingCallerPrincipal;

/**
 * File:  PersonResource.java Course materials (22F) CST 8277
 *
 * @author Teddy Yap
 * @author Shariar (Shawn) Emami
 * @author (original) Mike Norman
 * 
 * Updated by:  Group 7
 *   040778696, Meina, He (as from ACSIS)
 * 
 */

@Path(COURSE_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CourseResource {
	private static final Logger LOG = LogManager.getLogger();

    @EJB
    protected ACMECollegeService service;

    @Inject
    protected SecurityContext sc;
    
    @GET
    public Response getCourses() {
        LOG.debug("retrieving all courses ...");
        List<Course> courses = service.getAllCourses();
        Response response = Response.ok(courses).build();
        return response;
    }
    //meina
    @GET
    //@RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response getCourseById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
    	LOG.debug("try to retrieve specific course " + id);
        Course course = service.getCourseById(id);
        Response response = Response.ok(course).build();
        return response;
    }
    //meina
    @POST
    @RolesAllowed({ADMIN_ROLE})
    public Response addCourse(Course newCourse) {
        Response response = null;
        Course newCourseWithIdTimestamps = service.persistCourse(newCourse);
        // Build a SecurityUser linked to the new student
       // service.buildUserForNewCourse(newCourseWithIdTimestamps);
        response = Response.ok(newCourseWithIdTimestamps).build();
        return response;
    }
    //meina
    @PUT
    @RolesAllowed({ADMIN_ROLE})
    @Path("/{courseId}")
    public Response updateCourse(@PathParam("courseId") int cId, Course updatingCourse){
    	LOG.debug("Updating a specific course with id = {}", cId);
    	Response response = null;
        Course updatedCourse = service.updateCourseById(cId, updatingCourse);
        response = Response.ok(updatedCourse).build();
        return response;
    }
    
    @DELETE
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response deleteCourse(@PathParam(RESOURCE_PATH_ID_ELEMENT) int courseId) {
        LOG.debug("Deleting course with id = {}", courseId);
        Course course = service.deleteCourseById(courseId);
        Response response = Response.ok(course).build();
        return response;
    }
    
    
}
