package com.wedding.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDate;

/**
 * Makes the RSVP deadline (and whether RSVP is still open) available to every
 * Thymeleaf template without each controller having to add it.
 */
@ControllerAdvice
public class GlobalModelAdvice {

    private final LocalDate rsvpDeadline;

    public GlobalModelAdvice(@Value("${wedding.rsvp.deadline}") String deadline) {
        this.rsvpDeadline = LocalDate.parse(deadline); // ISO yyyy-MM-dd
    }

    @ModelAttribute("rsvpDeadline")
    public LocalDate rsvpDeadline() {
        return rsvpDeadline;
    }

    /** RSVP is open up to and including the deadline date. */
    @ModelAttribute("rsvpOpen")
    public boolean rsvpOpen() {
        return !LocalDate.now().isAfter(rsvpDeadline);
    }
}
