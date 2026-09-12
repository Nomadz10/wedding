package com.wedding.config;

import com.wedding.model.ContentPage;
import com.wedding.model.Event;
import com.wedding.model.Guest;
import com.wedding.repository.ContentPageRepository;
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
    private final ContentPageRepository pages;

    public DataSeeder(EventRepository events, GuestRepository guests, ContentPageRepository pages) {
        this.events = events;
        this.guests = guests;
        this.pages = pages;
    }

    @Override
    public void run(String... args) {
        if (events.count() == 0) {
            seedEvents();
        }
        if (guests.count() == 0) {
            seedGuests();
        }
        if (pages.count() == 0) {
            seedPages();
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

    private void seedPages() {
        pages.saveAll(List.of(
                page("visa", "Visa", 1, "Indian Visa for U.S. Citizens",
                        "A starting point — always confirm details on the official government portal.",
                        "<p>Most U.S. citizens travel to India on an <strong>e-Visa (Tourist)</strong>, which you apply for online before you fly. "
                        + "Rules and fees change, so please <strong>verify everything on the official Government of India portal</strong>: "
                        + "<a href=\"https://indianvisaonline.gov.in/evisa/\" target=\"_blank\" rel=\"noopener\">indianvisaonline.gov.in/evisa</a>.</p>"
                        + "<h3>General steps</h3>"
                        + "<ol>"
                        + "<li>Make sure your <strong>passport is valid for at least 6 months</strong> beyond your arrival date and has two blank pages.</li>"
                        + "<li>Apply on the official e-Visa portal. You'll need a passport scan and a recent passport-style photo.</li>"
                        + "<li>Choose the tourist e-Visa option that matches your trip length (e.g. 30-day, 1-year, or 5-year).</li>"
                        + "<li>Pay the fee and submit. Approval typically arrives by email — apply <strong>at least a few weeks ahead</strong>.</li>"
                        + "<li>Print the approved e-Visa (ETA) and carry it with you to show on arrival.</li>"
                        + "</ol>"
                        + "<p><em>Tip:</em> use only the official <code>.gov.in</code> site — many look-alike sites charge extra. "
                        + "If you hold a passport from another country or have a special situation, check the requirements that apply to you.</p>"
                        + "<p>Questions? Reach out to us and we'll help however we can.</p>"),
                page("travel", "Travel", 2, "Getting Here & Flights",
                        "When to book and how to plan your trip to Hyderabad.",
                        "<h3>When to book</h3>"
                        + "<p>For the best fares to India, aim to book roughly <strong>2–4 months in advance</strong>. "
                        + "Prices tend to climb in the last few weeks, and November is a popular travel season.</p>"
                        + "<h3>Which airport</h3>"
                        + "<p>Fly into <strong>Rajiv Gandhi International Airport (HYD)</strong> in Hyderabad. "
                        + "Common one-stop routes from the U.S. connect through hubs such as the Gulf (Doha, Dubai, Abu Dhabi) or Europe.</p>"
                        + "<h3>Tips</h3>"
                        + "<ul>"
                        + "<li>Give yourself a <strong>day or two to adjust</strong> to the time difference before the events.</li>"
                        + "<li>Consider arriving a couple of days early to settle in and explore.</li>"
                        + "<li>Set fare alerts and compare nearby departure dates — mid-week flights are often cheaper.</li>"
                        + "<li>Once the hotel block is set, we'll share those details here and on your RSVP page.</li>"
                        + "</ul>"
                        + "<p>Let us know your rough travel plans and we'll help coordinate.</p>"),
                page("hyderabad", "Hyderabad", 3, "Things to Do in Hyderabad",
                        "A few of our favourite places if you have time to explore.",
                        "<h3>Sights &amp; history</h3>"
                        + "<ul>"
                        + "<li><strong>Charminar</strong> — the iconic 16th-century monument and the bustling bazaars around it.</li>"
                        + "<li><strong>Golconda Fort</strong> — a hilltop fort with great views; try the evening sound-and-light show.</li>"
                        + "<li><strong>Qutb Shahi Tombs</strong> — beautiful domed tombs near Golconda.</li>"
                        + "<li><strong>Chowmahalla Palace</strong> — the former palace of the Nizams.</li>"
                        + "<li><strong>Salar Jung Museum</strong> — one of India's largest art and antiquity collections.</li>"
                        + "</ul>"
                        + "<h3>Relax &amp; enjoy</h3>"
                        + "<ul>"
                        + "<li><strong>Hussain Sagar Lake</strong> — a boat ride to the Buddha statue in the middle of the lake.</li>"
                        + "<li><strong>Ramoji Film City</strong> — a huge film studio and theme park (plan a full day).</li>"
                        + "<li><strong>Shopping</strong> — pearls, bangles and textiles around Laad Bazaar.</li>"
                        + "</ul>"
                        + "<h3>Eat</h3>"
                        + "<p>Don't miss <strong>Hyderabadi biryani</strong>, along with haleem, Irani chai and Osmania biscuits at a classic Irani cafe.</p>"
                        + "<p>Ask us for recommendations — we're happy to point you to our favourites!</p>")
        ));
    }

    private ContentPage page(String slug, String navLabel, int order, String title, String subtitle, String body) {
        ContentPage p = new ContentPage();
        p.setSlug(slug);
        p.setNavLabel(navLabel);
        p.setDisplayOrder(order);
        p.setTitle(title);
        p.setSubtitle(subtitle);
        p.setBody(body);
        p.setPublished(true);
        return p;
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
