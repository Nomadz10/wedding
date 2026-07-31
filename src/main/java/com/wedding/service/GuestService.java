package com.wedding.service;

import com.wedding.model.Guest;
import com.wedding.model.RsvpStatus;
import com.wedding.repository.GuestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class GuestService {

    private final GuestRepository guests;

    public GuestService(GuestRepository guests) {
        this.guests = guests;
    }

    /** Case-insensitive, whitespace-tolerant login against the invite list. */
    public Optional<Guest> findByName(String firstName, String lastName) {
        if (firstName == null || lastName == null) return Optional.empty();
        String f = firstName.trim().toLowerCase();
        String l = lastName.trim().toLowerCase();
        if (f.isEmpty() || l.isEmpty()) return Optional.empty();
        return guests.findByFirstNameLowerAndLastNameLower(f, l);
    }

    public Optional<Guest> findById(Long id) {
        return guests.findById(id);
    }

    public List<Guest> all() {
        return guests.findAllByOrderByLastNameAscFirstNameAsc();
    }

    @Transactional
    public Guest saveRsvp(Long guestId, RsvpStatus status, boolean bringingPlusOne,
                          String plusOneName, String notes) {
        Guest g = guests.findById(guestId).orElseThrow();
        g.setRsvpStatus(status);
        boolean plusOne = status == RsvpStatus.ATTENDING && bringingPlusOne;
        g.setBringingPlusOne(plusOne);
        g.setPlusOneName(plusOne ? (plusOneName == null ? null : plusOneName.trim()) : null);
        g.setNotes(notes == null ? null : notes.trim());
        g.setRespondedAt(LocalDateTime.now());
        return guests.save(g);
    }

    // ---- Admin stats used for hotel/catering planning ----

    public Stats stats() {
        List<Guest> all = guests.findAll();
        long attending = 0, declined = 0, pending = 0, plusOnes = 0, roomsAssigned = 0;
        for (Guest g : all) {
            switch (g.getRsvpStatus()) {
                case ATTENDING -> attending++;
                case DECLINED -> declined++;
                case PENDING -> pending++;
            }
            if (g.getRsvpStatus() == RsvpStatus.ATTENDING && g.isBringingPlusOne()) plusOnes++;
            if (g.isRoomBooked()) roomsAssigned++;
        }
        long headcount = attending + plusOnes; // total bodies to house/feed
        return new Stats(all.size(), attending, declined, pending, plusOnes, headcount, roomsAssigned);
    }

    public record Stats(long invited, long attending, long declined, long pending,
                        long plusOnes, long totalHeadcount, long roomsAssigned) {}
}
