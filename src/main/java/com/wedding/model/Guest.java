package com.wedding.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * An invited guest. Guests are pre-loaded (the invite list). "Login" is a
 * case-insensitive match on first + last name — no password, by design.
 */
@Entity
@Table(name = "guests",
        uniqueConstraints = @UniqueConstraint(columnNames = {"firstNameLower", "lastNameLower"}))
public class Guest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    /** Lower-cased copies used for the unique constraint and lookups. */
    @Column(nullable = false)
    private String firstNameLower;

    @Column(nullable = false)
    private String lastNameLower;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RsvpStatus rsvpStatus = RsvpStatus.PENDING;

    private LocalDateTime respondedAt;

    /** Whether this guest is bringing a plus-one. */
    private boolean bringingPlusOne = false;

    /** The plus-one's name (only meaningful when bringingPlusOne is true). */
    private String plusOneName;

    /** Optional dietary / accessibility notes from the guest. */
    @Column(length = 1000)
    private String notes;

    // ---- Filled in by the admin AFTER the hotel is booked ----

    /** e.g. "Room 214" — null until assigned. Presence means "hotel booked". */
    private String roomAssignment;

    /** e.g. "Grand Hotel, check-in Fri 3pm" — free text shown to the guest. */
    @Column(length = 1000)
    private String roomDetails;

    /**
     * The subset of events this guest is personally invited to. EAGER because
     * templates render it outside a transaction (open-in-view is off) and the
     * per-guest event count is tiny.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "guest_events",
            joinColumns = @JoinColumn(name = "guest_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id"))
    private Set<Event> invitedEvents = new HashSet<>();

    public boolean hasResponded() {
        return rsvpStatus != RsvpStatus.PENDING;
    }

    public boolean isRoomBooked() {
        return roomAssignment != null && !roomAssignment.isBlank();
    }

    /** Headcount this guest represents for hotel/catering: self + plus-one. */
    public int headcount() {
        if (rsvpStatus != RsvpStatus.ATTENDING) return 0;
        return 1 + (bringingPlusOne ? 1 : 0);
    }

    // ---- getters / setters ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) {
        this.firstName = firstName == null ? null : firstName.trim();
        this.firstNameLower = this.firstName == null ? null : this.firstName.toLowerCase();
    }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) {
        this.lastName = lastName == null ? null : lastName.trim();
        this.lastNameLower = this.lastName == null ? null : this.lastName.toLowerCase();
    }

    public String getFirstNameLower() { return firstNameLower; }
    public String getLastNameLower() { return lastNameLower; }

    public RsvpStatus getRsvpStatus() { return rsvpStatus; }
    public void setRsvpStatus(RsvpStatus rsvpStatus) { this.rsvpStatus = rsvpStatus; }

    public LocalDateTime getRespondedAt() { return respondedAt; }
    public void setRespondedAt(LocalDateTime respondedAt) { this.respondedAt = respondedAt; }

    public boolean isBringingPlusOne() { return bringingPlusOne; }
    public void setBringingPlusOne(boolean bringingPlusOne) { this.bringingPlusOne = bringingPlusOne; }

    public String getPlusOneName() { return plusOneName; }
    public void setPlusOneName(String plusOneName) { this.plusOneName = plusOneName; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getRoomAssignment() { return roomAssignment; }
    public void setRoomAssignment(String roomAssignment) { this.roomAssignment = roomAssignment; }

    public String getRoomDetails() { return roomDetails; }
    public void setRoomDetails(String roomDetails) { this.roomDetails = roomDetails; }

    public Set<Event> getInvitedEvents() { return invitedEvents; }
    public void setInvitedEvents(Set<Event> invitedEvents) { this.invitedEvents = invitedEvents; }
}
