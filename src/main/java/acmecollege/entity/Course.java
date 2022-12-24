/***************************************************************************
 * File:  Course.java Course materials (22F) CST 8277
 * 
 * @author Teddy Yap
 * @author Shariar (Shawn) Emami
 * @date August 28, 2022
 * 
 * Updated by:  Group 7
*  040623714,Hongyu Wang 
*  040778696,Meina He 
*  041025684,Kai Zhao 
*  040994443,Yunting Yin 
 * 
 */
package acmecollege.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

@SuppressWarnings("unused")

/**
 * The persistent class for the course database table.
 */
@Entity
@Table(name = "course")
//@NamedQuery(name = Course.ALL_COURSES_QUERY, query = "SELECT c FROM Course c")
@NamedQuery(name = Course.ALL_COURSES_QUERY, query = "SELECT c FROM Course c left join fetch c.courseRegistrations")
//@NamedQuery(name = Course.ALL_COURSES_QUERY, query = "SELECT c FROM Course c")
@NamedQuery(name = Course.FIND_BY_ID, query = "SELECT c FROM Course c left join fetch c.courseRegistrations where c.id = :param1")

@AttributeOverride(name = "id", column = @Column(name = "course_id"))
public class Course extends PojoBase implements Serializable {
	private static final long serialVersionUID = 1L;

    public static final String ALL_COURSES_QUERY = "Course.findAll";
    public static final String FIND_BY_ID = "Course.courseById";

	@Basic(optional = false)
	@Column(name = "course_code", nullable = false, length = 7)
	private String courseCode;

	@Basic(optional = false)
    @Column(name = "course_title", nullable = false, length = 100)
	private String courseTitle;
	@JsonIgnore
	@Basic(optional = false)
	@Column(name = "year",  nullable = false)
	private int year;
	
	@Basic(optional = false)
	@Column(name = "semester",  nullable = false, length = 6)
	private String semester;
    
	@JsonIgnore
	@Basic(optional = false)
	@Column(name = "credit_units",  nullable = false)
	private int creditUnits;
    
	@JsonIgnore
	@Basic(optional = false)
	@Column(name = "online",  nullable = false)
	private byte online;

	@OneToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY, mappedBy = "course")
	private Set<CourseRegistration> courseRegistrations = new HashSet<>();

	public Course() {
		super();
	}

	public Course(String courseCode, String courseTitle, int year, String semester, int creditUnits, byte online) {
		this();
		this.courseCode = courseCode;
		this.courseTitle = courseTitle;
		this.year = year;
		this.semester = semester;
		this.creditUnits = creditUnits;
		this.online = online;
	}

	public Course setCourse(String courseCode, String courseTitle, int year, String semester, int creditUnits, byte online) {
		setCourseCode(courseCode);
		setCourseTitle(courseTitle);
		setYear(year);
		setSemester(semester);
		setCreditUnits(creditUnits);
		setOnline(online);
		return this;
	}

	public String getCourseCode() {
		return courseCode;
	}

	public void setCourseCode(String courseCode) {
		this.courseCode = courseCode;
	}

	public String getCourseTitle() {
		return courseTitle;
	}

	public void setCourseTitle(String courseTitle) {
		this.courseTitle = courseTitle;
	}
	@JsonIgnore
	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public String getSemester() {
		return semester;
	}

	public void setSemester(String semester) {
		this.semester = semester;
	}
	@JsonIgnore
	public int getCreditUnits() {
		return creditUnits;
	}

	public void setCreditUnits(int creditUnits) {
		this.creditUnits = creditUnits;
	}
	@JsonIgnore
	public byte getOnline() {
		return online;
	}

	public void setOnline(byte online) {
		this.online = online;
	}
	@JsonIgnore
	public Set<CourseRegistration> getCourseRegistrations() {
		return courseRegistrations;
	}

	public void setCourseRegistrations(Set<CourseRegistration> courseRegistrations) {
		this.courseRegistrations = courseRegistrations;
	}

	//Inherited hashCode/equals is sufficient for this Entity class

}
