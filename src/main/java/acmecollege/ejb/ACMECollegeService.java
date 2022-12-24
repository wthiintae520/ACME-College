/**
 * File:  ACMEColegeService.java
 * Course materials (22F) CST 8277
 *
 * @author Teddy Yap
 * @author Shariar (Shawn) Emami
 * @author (original) Mike Norman
 *
 * Updated by:  Group 7
*  040623714,Hongyu Wang 
*  040778696,Meina He 
*  041025684,Kai Zhao 
*  040994443,Yunting Yin 
 *
 */
package acmecollege.ejb;

import static acmecollege.entity.MembershipCard.ID_CARD_QUERY_NAME;
import static acmecollege.entity.Professor.ID_PROFESSOR_QUERY_NAME;
import static acmecollege.entity.SecurityUser.USER_BY_NAME_QUERY;
import static acmecollege.entity.StudentClub.ALL_STUDENT_CLUBS_QUERY_NAME;
import static acmecollege.entity.StudentClub.SPECIFIC_STUDENT_CLUB_QUERY_NAME;
import static acmecollege.entity.StudentClub.IS_DUPLICATE_QUERY_NAME;
import static acmecollege.entity.Student.ALL_STUDENTS_QUERY_NAME;
import static acmecollege.utility.MyConstants.DEFAULT_KEY_SIZE;
import static acmecollege.utility.MyConstants.DEFAULT_PROPERTY_ALGORITHM;
import static acmecollege.utility.MyConstants.DEFAULT_PROPERTY_ITERATIONS;
import static acmecollege.utility.MyConstants.DEFAULT_SALT_SIZE;
import static acmecollege.utility.MyConstants.DEFAULT_USER_PASSWORD;
import static acmecollege.utility.MyConstants.DEFAULT_USER_PREFIX;
import static acmecollege.utility.MyConstants.PARAM1;
import static acmecollege.utility.MyConstants.PROPERTY_ALGORITHM;
import static acmecollege.utility.MyConstants.PROPERTY_ITERATIONS;
import static acmecollege.utility.MyConstants.PROPERTY_KEY_SIZE;
import static acmecollege.utility.MyConstants.PROPERTY_SALT_SIZE;
import static acmecollege.utility.MyConstants.PU_NAME;
import static acmecollege.utility.MyConstants.USER_ROLE;
import static acmecollege.entity.SecurityRole.ROLE_BY_NAME_QUERY;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.security.enterprise.identitystore.Pbkdf2PasswordHash;
import javax.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import acmecollege.entity.ClubMembership;
import acmecollege.entity.Course;
import acmecollege.entity.CourseRegistration;
import acmecollege.entity.MembershipCard;
import acmecollege.entity.Professor;
import acmecollege.entity.SecurityRole;
import acmecollege.entity.SecurityUser;
import acmecollege.entity.Student;
import acmecollege.entity.StudentClub;

@SuppressWarnings("unused")

/**
 * Stateless Singleton EJB Bean - ACMECollegeService
 */
@Singleton
public class ACMECollegeService implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private static final Logger LOG = LogManager.getLogger();
    
    @PersistenceContext(name = PU_NAME)
    protected EntityManager em;
    
    @Inject
    protected Pbkdf2PasswordHash pbAndjPasswordHash;

    public List<Student> getAllStudents() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Student> cq = cb.createQuery(Student.class);
        cq.select(cq.from(Student.class));
        return em.createQuery(cq).getResultList();
    }

    public Student getStudentById(int id) {
        return em.find(Student.class, id);
    }

    @Transactional
    public Student persistStudent(Student newStudent) {
        em.persist(newStudent);
        return newStudent;
    }

    @Transactional
    public void buildUserForNewStudent(Student newStudent) {
        SecurityUser userForNewStudent = new SecurityUser();
        userForNewStudent.setUsername(
            DEFAULT_USER_PREFIX + "_" + newStudent.getFirstName() + "." + newStudent.getLastName());
        Map<String, String> pbAndjProperties = new HashMap<>();
        pbAndjProperties.put(PROPERTY_ALGORITHM, DEFAULT_PROPERTY_ALGORITHM);
        pbAndjProperties.put(PROPERTY_ITERATIONS, DEFAULT_PROPERTY_ITERATIONS);
        pbAndjProperties.put(PROPERTY_SALT_SIZE, DEFAULT_SALT_SIZE);
        pbAndjProperties.put(PROPERTY_KEY_SIZE, DEFAULT_KEY_SIZE);
        pbAndjPasswordHash.initialize(pbAndjProperties);
        String pwHash = pbAndjPasswordHash.generate(DEFAULT_USER_PASSWORD.toCharArray());
        userForNewStudent.setPwHash(pwHash);
        userForNewStudent.setStudent(newStudent);
        SecurityRole userRole = /* TODO ACMECS01 - Use NamedQuery on SecurityRole to find USER_ROLE */
        		em.<SecurityRole>createNamedQuery("SecurityRole.findRoleByName", SecurityRole.class)
        		.setParameter(PARAM1, "USER_ROLE").getSingleResult();
        userForNewStudent.getRoles().add(userRole);
        userRole.getUsers().add(userForNewStudent);
        em.persist(userForNewStudent);
    }

    @Transactional
    public Professor setProfessorForStudentCourse(int studentId, int courseId, Professor newProfessor) {
        Student studentToBeUpdated = em.find(Student.class, studentId);
        if (studentToBeUpdated != null) { // Student exists
            Set<CourseRegistration> courseRegistrations = studentToBeUpdated.getCourseRegistrations();
            courseRegistrations.forEach(c -> {
                if (c.getCourse().getId() == courseId) {
                    if (c.getProfessor() != null) { // Professor exists
                        Professor prof = em.find(Professor.class, c.getProfessor().getId());
                        prof.setProfessor(newProfessor.getFirstName(),
                        				  newProfessor.getLastName(),
                        				  newProfessor.getDepartment());
                        em.merge(prof);
                    }
                    else { // Professor does not exist
                        c.setProfessor(newProfessor);
                        em.merge(studentToBeUpdated);
                    }
                }
            });
            return newProfessor;
        }
        else return null;  // Student doesn't exists
    }

    /**
     * To update a student
     * 
     * @param id - id of entity to update
     * @param studentWithUpdates - entity with updated information
     * @return Entity with updated information
     */
    @Transactional
    public Student updateStudentById(int id, Student studentWithUpdates) {
        Student studentToBeUpdated = getStudentById(id);
        if (studentToBeUpdated != null) {
            em.refresh(studentToBeUpdated);
            em.merge(studentWithUpdates);
            em.flush();
        }
        return studentToBeUpdated;
    }

    /**
     * To delete a student by id
     * 
     * @param id - student id to delete
     */
    @Transactional
    public void deleteStudentById(int id) {
        Student student = getStudentById(id);
        if (student != null) {
            em.refresh(student);
            TypedQuery<SecurityUser> findUser = 
                /* TODO ACMECS02 - Use NamedQuery on SecurityRole to find this related Student
                   so that when we remove it, the relationship from SECURITY_USER table
                   is not dangling
                */
            		em.<SecurityUser>createNamedQuery("SecurityUser.userByStudentId", SecurityUser.class)
            		.setParameter(PARAM1, id);
            SecurityUser sUser = findUser.getSingleResult();
            em.remove(sUser);
            em.remove(student);
        }
    }
    
    public List<StudentClub> getAllStudentClubs() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<StudentClub> cq = cb.createQuery(StudentClub.class);
        cq.select(cq.from(StudentClub.class));
        return em.createQuery(cq).getResultList();
    }

    // Why not use the build-in em.find?  The named query SPECIFIC_STUDENT_CLUB_QUERY_NAME
    // includes JOIN FETCH that we cannot add to the above API
    public StudentClub getStudentClubById(int id) {
        TypedQuery<StudentClub> specificStudentClubQuery = em.createNamedQuery(SPECIFIC_STUDENT_CLUB_QUERY_NAME, StudentClub.class);
        specificStudentClubQuery.setParameter(PARAM1, id);
        return specificStudentClubQuery.getSingleResult();
    }

    //kai
    public List<MembershipCard> getAllMembershipCards() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<MembershipCard> cq = cb.createQuery(MembershipCard.class);
        cq.select(cq.from(MembershipCard.class));
        return em.createQuery(cq).getResultList();
    }

    //kai
    @Transactional
    public MembershipCard deleteMembershipCardById(int id) {
        MembershipCard membershipCard = getMembershipCardById(id);
        if (membershipCard != null) {
//            em.refresh(membershipCard);
//            TypedQuery<MembershipCard> findCard =
//                /* TODO ACMECS02 - Use NamedQuery on SecurityRole to find this related Student
//                   so that when we remove it, the relationship from SECURITY_USER table
//                   is not dangling
//                */
//                    em.createNamedQuery(ID_CARD_QUERY_NAME, MembershipCard.class);
//            MembershipCard cardFound = findCard.getSingleResult();
//            em.remove(cardFound);
            em.remove(membershipCard);
            return membershipCard;
        }
        return null;
    }

    //kai
    @Transactional
    public MembershipCard updateMembershipCardById(int cardId, MembershipCard updatingMembershipCard) {
        MembershipCard membershipCardToBeUpdated = getMembershipCardById(cardId);
        if (membershipCardToBeUpdated != null) {
            em.refresh(membershipCardToBeUpdated);
            membershipCardToBeUpdated.setClubMembership(updatingMembershipCard.getMembership());
            em.merge(membershipCardToBeUpdated);
            em.flush();
        }
        return membershipCardToBeUpdated;
    }

    //kai
    public MembershipCard getMembershipCardById(int id) {
        TypedQuery<MembershipCard> specificMembershipCardQuery = em.createNamedQuery(ID_CARD_QUERY_NAME, MembershipCard.class);
        specificMembershipCardQuery.setParameter(PARAM1, id);
        return specificMembershipCardQuery.getSingleResult();
    }

    @Transactional
    public MembershipCard persistMembershipCard(MembershipCard newMembershipCard) {
        em.persist(newMembershipCard);
        return newMembershipCard;
    }

    //hy
    public Professor getProfessorById(int id) {
        TypedQuery<Professor> specificProfessorQuery = em.createNamedQuery(ID_PROFESSOR_QUERY_NAME, Professor.class);
        specificProfessorQuery.setParameter(PARAM1, id);
        return specificProfessorQuery.getSingleResult();
    }

    @Transactional
    public Professor persistProfessor(Professor newProfessor) {
        em.persist(newProfessor);
        return newProfessor;
    }

    @Transactional
    public Professor deleteProfessorById(int id) {
        Professor professor = getProfessorById(id);
        if (professor != null) {
            em.remove(professor);
            return professor;
        }
        return null;
    }

    // These methods are more generic

    public <T> List<T> getAll(Class<T> entity, String namedQuery) {
        TypedQuery<T> allQuery = em.createNamedQuery(namedQuery, entity);
        return allQuery.getResultList();
    }
    
    public <T> T getById(Class<T> entity, String namedQuery, int id) {
        TypedQuery<T> allQuery = em.createNamedQuery(namedQuery, entity);
        allQuery.setParameter(PARAM1, id);
        return allQuery.getSingleResult();
    }

    @Transactional
    public StudentClub deleteStudentClub(int id) {
        //StudentClub sc = getStudentClubById(id);
    	StudentClub sc = getById(StudentClub.class, StudentClub.SPECIFIC_STUDENT_CLUB_QUERY_NAME, id);
        if (sc != null) {
            Set<ClubMembership> memberships = sc.getClubMemberships();
            List<ClubMembership> list = new LinkedList<>();
            memberships.forEach(list::add);
            list.forEach(m -> {
                if (m.getCard() != null) {
                    MembershipCard mc = getById(MembershipCard.class, ID_CARD_QUERY_NAME, m.getCard().getId());
                    mc.setClubMembership(null);
                }
                m.setCard(null);
                em.merge(m);
            });
            em.remove(sc);
            return sc;
        }
        return null;
    }
    
    // Please study & use the methods below in your test suites
    
    public boolean isDuplicated(StudentClub newStudentClub) {
        TypedQuery<Long> allStudentClubsQuery = em.createNamedQuery(IS_DUPLICATE_QUERY_NAME, Long.class);
        allStudentClubsQuery.setParameter(PARAM1, newStudentClub.getName());
        return (allStudentClubsQuery.getSingleResult() >= 1);
    }

    @Transactional
    public StudentClub persistStudentClub(StudentClub newStudentClub) {
        em.persist(newStudentClub);
        return newStudentClub;
    }

    @Transactional
    public StudentClub updateStudentClub(int id, StudentClub updatingStudentClub) {
    	StudentClub studentClubToBeUpdated = getStudentClubById(id);
        if (studentClubToBeUpdated != null) {
            em.refresh(studentClubToBeUpdated);
            studentClubToBeUpdated.setName(updatingStudentClub.getName());
            em.merge(studentClubToBeUpdated);
            em.flush();
        }
        return studentClubToBeUpdated;
    }
    
    @Transactional
    public ClubMembership persistClubMembership(ClubMembership newClubMembership) {
        em.persist(newClubMembership);
        return newClubMembership;
    }

    public List<ClubMembership> getAllClubMemberships() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ClubMembership> cq = cb.createQuery(ClubMembership.class);
        cq.select(cq.from(ClubMembership.class));
        return em.createQuery(cq).getResultList();
    }

    public ClubMembership getClubMembershipById(int cmId) {
        TypedQuery<ClubMembership> allClubMembershipQuery = em.createNamedQuery(ClubMembership.FIND_BY_ID, ClubMembership.class);
        allClubMembershipQuery.setParameter(PARAM1, cmId);
        return allClubMembershipQuery.getSingleResult();
    }

    @Transactional
    public ClubMembership updateClubMembership(int id, ClubMembership clubMembershipWithUpdates) {
    	ClubMembership clubMembershipToBeUpdated = getClubMembershipById(id);
        if (clubMembershipToBeUpdated != null) {
            em.refresh(clubMembershipToBeUpdated);
            em.merge(clubMembershipWithUpdates);
            em.flush();
        }
        return clubMembershipToBeUpdated;
    }

 // Meina
 	public List<Course> getAllCourses() {
 		 CriteriaBuilder cb = em.getCriteriaBuilder();
 	     CriteriaQuery<Course> cq = cb.createQuery(Course.class);
 	     cq.select(cq.from(Course.class));
 	     return em.createQuery(cq).getResultList();
 	}

 	// Meina
 	public Course getCourseById(int courseId) {
        TypedQuery<Course> specificCourseQuery = em.createNamedQuery(Course.FIND_BY_ID, Course.class);
        specificCourseQuery.setParameter(PARAM1, courseId);
        return specificCourseQuery.getSingleResult();
    }

 	//Meina
 	@Transactional
    public Course deleteCourseById(int id) {
        Course course = getCourseById(id);
        if (course != null) {
//            em.refresh(membershipCard);
//            TypedQuery<MembershipCard> findCard =
//                /* TODO ACMECS02 - Use NamedQuery on SecurityRole to find this related Student
//                   so that when we remove it, the relationship from SECURITY_USER table
//                   is not dangling
//                */
//                    em.createNamedQuery(ID_CARD_QUERY_NAME, MembershipCard.class);
//            MembershipCard cardFound = findCard.getSingleResult();
//            em.remove(cardFound);

        	em.remove(course);
            return course;
        }
        return null;
    }
 	//Meina
 	@Transactional
    public Course updateCourseById(int courseId, Course updatingCourse) {
        Course courseToBeUpdated = getCourseById(courseId);
        if (courseToBeUpdated != null) {
            em.refresh(courseToBeUpdated);
          //  courseToBeUpdated.setClubMembership(updatingCourse.getCourse());
            em.merge(courseToBeUpdated);
            em.flush();
        }
        return courseToBeUpdated;
    }
 	//meina
 	 @Transactional
     public Course persistCourse(Course newCourse) {
         em.persist(newCourse);
         return newCourse;
     }



    @Transactional
    public ClubMembership deleteClubMembershipById(int clubMembershipId) {
        ClubMembership clubMembership = getClubMembershipById(clubMembershipId);
        if (clubMembership != null) {
            MembershipCard membershipCard = clubMembership.getCard();
            if (membershipCard != null) {
                membershipCard.setClubMembership(null);
            }
            clubMembership.setCard(null);
            em.merge(clubMembership);
            em.remove(clubMembership);
            return clubMembership;
        }
        return null;
    }

}
