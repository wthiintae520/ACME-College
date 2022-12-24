/***************************************************************************
 * File:  ClubMembership.java Course materials (22F) CST 8277
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

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.*;

@SuppressWarnings("unused")

/**
 * The persistent class for the club_membership database table.
 */
@Entity
@Table(name = "club_membership")
@NamedQuery(name = ClubMembership.FIND_ALL, query = "SELECT cm FROM ClubMembership cm left join fetch cm.club left join fetch cm.card")
@NamedQuery( name = ClubMembership.FIND_BY_ID, query = "SELECT cm FROM ClubMembership cm left join fetch cm.club left join fetch cm.card where cm.id = :param1")
@AttributeOverride(name = "id", column = @Column(name = "membership_id"))
public class ClubMembership extends PojoBase implements Serializable {
	private static final long serialVersionUID = 1L;

    public static final String FIND_ALL = "ClubMembership.findAll";
    public static final String FIND_BY_ID = "ClubMembership.findbyId";    

//    @Basic(optional = false)
	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "club_id", referencedColumnName = "club_id")
	private StudentClub club;

//    @Basic(optional = false)
	@OneToOne(mappedBy = "membership")
	@JsonIgnore
	private MembershipCard card;

	@Embedded
	private DurationAndStatus durationAndStatus;

	public ClubMembership() {
		durationAndStatus = new DurationAndStatus();
	}

	public StudentClub getStudentClub() {
		return club;
	}

	public void setStudentClub(StudentClub club) {
		this.club = club;
	}

	public MembershipCard getCard() {
		return card;
	}

	public void setCard(MembershipCard card) {
		this.card = card;
	}

	public DurationAndStatus getDurationAndStatus() {
		return durationAndStatus;
	}

	public void setDurationAndStatus(DurationAndStatus durationAndStatus) {
		this.durationAndStatus = durationAndStatus;
	}
	
	//Inherited hashCode/equals NOT sufficient for this Entity class
	/**
	 * Very important:  Use getter's for member variables because JPA sometimes needs to intercept those calls<br/>
	 * and go to the database to retrieve the value
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		// Only include member variables that really contribute to an object's identity
		// i.e. if variables like version/updated/name/etc. change throughout an object's lifecycle,
		// they shouldn't be part of the hashCode calculation
		
		// include DurationAndStatus in identity
		return prime * result + Objects.hash(getId(), getDurationAndStatus());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (obj instanceof ClubMembership otherClubMembership) {
			// See comment (above) in hashCode():  Compare using only member variables that are
			// truly part of an object's identity
			return Objects.equals(this.getId(), otherClubMembership.getId()) &&
				Objects.equals(this.getDurationAndStatus(), otherClubMembership.getDurationAndStatus());
		}
		return false;
	}
}
