package com.wedding.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A group of guests who book one hotel room together (a couple, family, or
 * friends sharing). The room is assigned at the party level.
 */
@Entity
@Table(name = "parties")
public class Party {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Display name, e.g. "Chris & Christine Dang" or "The Malones". */
    @Column(nullable = false)
    private String name;

    /** e.g. "Room 214" — null until assigned. */
    private String roomAssignment;

    @Column(length = 1000)
    private String roomDetails;

    @OneToMany(mappedBy = "party", fetch = FetchType.EAGER)
    private List<Guest> members = new ArrayList<>();

    public boolean isRoomBooked() {
        return roomAssignment != null && !roomAssignment.isBlank();
    }

    /** A party needs a room if at least one member is attending. */
    public boolean hasAttendingMember() {
        return members.stream().anyMatch(g -> g.getRsvpStatus() == RsvpStatus.ATTENDING);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRoomAssignment() { return roomAssignment; }
    public void setRoomAssignment(String roomAssignment) { this.roomAssignment = roomAssignment; }

    public String getRoomDetails() { return roomDetails; }
    public void setRoomDetails(String roomDetails) { this.roomDetails = roomDetails; }

    public List<Guest> getMembers() { return members; }
    public void setMembers(List<Guest> members) { this.members = members; }
}
