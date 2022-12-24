/***************************************************************************
 * File:  MembershipCard.java Course materials (22F) CST 8277
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

import javax.persistence.AttributeOverride;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@SuppressWarnings("unused")

/**
 * The persistent class for the membership_card database table.
 */
@Entity
@Table(name = "membership_card")
@NamedQuery(name = MembershipCard.ALL_CARDS_QUERY_NAME, query = "SELECT mc FROM MembershipCard mc left join fetch mc.membership")
//@NamedQuery(name = MembershipCard.ALL_CARDS_QUERY_NAME, query = "SELECT mc FROM MembershipCard mc")
@NamedQuery(name = MembershipCard.ID_CARD_QUERY_NAME, query = "SELECT mc FROM MembershipCard mc left join fetch mc.membership where mc.id = :param1")
@AttributeOverride(name = "id", column = @Column(name = "card_id"))
public class MembershipCard extends PojoBase implements Serializable {
	private static final long serialVersionUID = 1L;

    public static final String ALL_CARDS_QUERY_NAME = "MembershipCard.findAll";
    public static final String ID_CARD_QUERY_NAME = "MembershipCard.findById";

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name="membership_id", referencedColumnName = "membership_id")
	@JsonIgnore
	private ClubMembership membership;

	@ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
	@JoinColumn(name = "student_id", referencedColumnName = "id", nullable = false)
	private Student owner;

	@Basic(optional = false)
	@Column(name="signed", columnDefinition = "BIT(1)", nullable = false)
	private byte signed;

	public MembershipCard() {
		super();
	}
	
	public MembershipCard(ClubMembership membership, Student owner, byte signed) {
		this();
		this.membership = membership;
		this.owner = owner;
		this.signed = signed;
	}

	public ClubMembership getMembership() {
		return membership;
	}

	public void setClubMembership(ClubMembership membership) {
		this.membership = membership;
	}

	public Student getOwner() {
		return owner;
	}

	public void setOwner(Student owner) {
		this.owner = owner;
	}

	public byte getSigned() {
		return signed;
	}

	public void setSigned(boolean signed) {
		this.signed = (byte) (signed ? 0b0001 : 0b0000);
	}
	
	//Inherited hashCode/equals is sufficient for this entity class

}
