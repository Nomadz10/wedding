package com.wedding.controller;

import com.wedding.model.Guest;
import com.wedding.model.RsvpStatus;
import com.wedding.service.GuestService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Guest-facing RSVP flow. "Login" is a name match against the invite list;
 * the matched guest id is stored in the session.
 */
@Controller
@RequestMapping("/rsvp")
public class RsvpController {

    static final String GUEST_SESSION_KEY = "GUEST_ID";

    private final GuestService guestService;
    private final LocalDate rsvpDeadline;

    private final boolean rsvpEnabled;

    public RsvpController(GuestService guestService,
                          @Value("${wedding.rsvp.deadline}") String deadline,
                          @Value("${wedding.rsvp.enabled:true}") boolean rsvpEnabled) {
        this.guestService = guestService;
        this.rsvpDeadline = LocalDate.parse(deadline);
        this.rsvpEnabled = rsvpEnabled;
    }

    private boolean rsvpClosed() {
        return LocalDate.now().isAfter(rsvpDeadline);
    }

    /** Step 1: the login form. */
    @GetMapping("/login")
    public String loginForm(HttpSession session) {
        if (!rsvpEnabled) return "redirect:/";
        if (session.getAttribute(GUEST_SESSION_KEY) != null) {
            return "redirect:/rsvp";
        }
        return "rsvp-login";
    }

    /** Step 1 (submit): match name to invite list. */
    @PostMapping("/login")
    public String login(@RequestParam String firstName,
                        @RequestParam String lastName,
                        HttpSession session,
                        Model model) {
        if (!rsvpEnabled) return "redirect:/";
        Optional<Guest> match = guestService.findByName(firstName, lastName);
        if (match.isEmpty()) {
            model.addAttribute("firstName", firstName);
            model.addAttribute("lastName", lastName);
            model.addAttribute("error",
                    "We couldn't find that name on our guest list. Please check the spelling, "
                    + "or reach out to us if you think this is a mistake.");
            return "rsvp-login";
        }
        session.setAttribute(GUEST_SESSION_KEY, match.get().getId());
        return "redirect:/rsvp";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute(GUEST_SESSION_KEY);
        if (!rsvpEnabled) return "redirect:/";
        return "redirect:/rsvp/login";
    }

    /** Step 2: the guest's home — RSVP form, or their booked info if the room is set. */
    @GetMapping
    public String dashboard(HttpSession session, Model model) {
        if (!rsvpEnabled) return "redirect:/";
        Guest guest = currentGuest(session);
        if (guest == null) return "redirect:/rsvp/login";
        model.addAttribute("guest", guest);
        return "rsvp-form";
    }

    /** Step 2 (submit): save the RSVP. */
    @PostMapping
    public String submit(HttpSession session,
                         @RequestParam String attending,
                         @RequestParam(required = false, defaultValue = "false") boolean bringingPlusOne,
                         @RequestParam(required = false) String plusOneName,
                         @RequestParam(required = false) String notes,
                         Model model) {
        if (!rsvpEnabled) return "redirect:/";
        Guest guest = currentGuest(session);
        if (guest == null) return "redirect:/rsvp/login";

        // Reject changes after the deadline (belt-and-suspenders — the form is also disabled).
        if (rsvpClosed()) {
            model.addAttribute("guest", guest);
            model.addAttribute("error", "The RSVP deadline has passed. Please contact us directly to make a change.");
            return "rsvp-form";
        }

        RsvpStatus status = "yes".equalsIgnoreCase(attending) ? RsvpStatus.ATTENDING : RsvpStatus.DECLINED;

        // If they're bringing a plus-one they must name them.
        if (status == RsvpStatus.ATTENDING && bringingPlusOne
                && (plusOneName == null || plusOneName.trim().isEmpty())) {
            model.addAttribute("guest", guest);
            model.addAttribute("error", "Please enter your plus-one's name, or uncheck the box.");
            return "rsvp-form";
        }

        guestService.saveRsvp(guest.getId(), status, bringingPlusOne, plusOneName, notes);
        return "redirect:/rsvp?saved";
    }

    /** Step 3: the guest's booked details (room + personalised schedule). */
    @GetMapping("/my-info")
    public String myInfo(HttpSession session, Model model) {
        if (!rsvpEnabled) return "redirect:/";
        Guest guest = currentGuest(session);
        if (guest == null) return "redirect:/rsvp/login";
        model.addAttribute("guest", guest);
        return "my-info";
    }

    private Guest currentGuest(HttpSession session) {
        Object id = session.getAttribute(GUEST_SESSION_KEY);
        if (id == null) return null;
        return guestService.findById((Long) id).orElse(null);
    }
}
