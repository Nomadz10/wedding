package com.wedding.config;

import com.wedding.model.Event;
import com.wedding.model.Guest;
import com.wedding.repository.EventRepository;
import com.wedding.repository.GuestRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Seeds a few sample events and guests on first run so the site isn't empty.
 * Once you add your real data via the admin panel this does nothing (it only
 * seeds when the tables are empty). Delete the ./data folder to reset.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final EventRepository events;
    private final GuestRepository guests;

    public DataSeeder(EventRepository events, GuestRepository guests) {
        this.events = events;
        this.guests = guests;
    }

    @Override
    public void run(String... args) {
        if (events.count() == 0) {
            seedEvents();
        }
        if (guests.count() == 0) {
            seedGuests();
        }
    }

    private void seedEvents() {
        events.saveAll(List.of(
                event("Sangeet", 1,
                        LocalDateTime.of(2026, 11, 23, 19, 0),
                        "The Grand Ballroom", "Venue address here",
                        "An evening of music, dance and celebration as both families come together. "
                        + "Expect performances, dhol, and plenty of dancing — come ready to hit the floor!",
                        "Indian festive — lehengas, sarees, sherwanis & kurtas"),
                event("Reception", 2,
                        LocalDateTime.of(2026, 11, 24, 19, 0),
                        "The Grand Ballroom", "Venue address here",
                        "A glamorous evening to toast the couple, with dinner, drinks and dancing into the night.",
                        "Cocktail / Indo-western formal"),
                event("Wedding", 3,
                        LocalDateTime.of(2026, 11, 25, 21, 30),
                        "The Grand Ballroom", "Venue address here",
                        "The main ceremony — join us under the mandap as we say our vows. "
                        + "The muhurat begins at 9:30pm; please be seated beforehand.",
                        "Traditional Indian formal")
        ));
    }

    private void seedGuests() {
        // Sample invite list — replace these with your real guests in the admin panel.
        guests.saveAll(List.of(
                guest("Nick", "Malone"),
                guest("Nick", "Behrens"),
                guest("Jeremy", "Stapleton"),
                guest("Chris", "Dang"),
                guest("Christine", "Dang")
        ));
    }

    private Event event(String title, int order, LocalDateTime when, String location,
                        String address, String description, String dressCode) {
        Event e = new Event();
        e.setTitle(title);
        e.setDisplayOrder(order);
        e.setStartTime(when);
        e.setLocation(location);
        e.setAddress(address);
        e.setDescription(description);
        e.setDressCode(dressCode);
        return e;
    }

    private Guest guest(String first, String last) {
        Guest g = new Guest();
        g.setFirstName(first);
        g.setLastName(last);
        return g;
    }
}
