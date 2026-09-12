package com.wedding.config;

import com.wedding.model.ContentPage;
import com.wedding.service.ContentPageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDate;
import java.util.List;

/**
 * Makes the RSVP deadline (and whether RSVP is still open) available to every
 * Thymeleaf template without each controller having to add it.
 */
@ControllerAdvice
public class GlobalModelAdvice {

    private final LocalDate rsvpDeadline;
    private final boolean rsvpEnabled;
    private final ContentPageService contentPages;

    public GlobalModelAdvice(@Value("${wedding.rsvp.deadline}") String deadline,
                             @Value("${wedding.rsvp.enabled:true}") boolean rsvpEnabled,
                             ContentPageService contentPages) {
        this.rsvpDeadline = LocalDate.parse(deadline); // ISO yyyy-MM-dd
        this.rsvpEnabled = rsvpEnabled;
        this.contentPages = contentPages;
    }

    /** Published info pages, for the top navigation. */
    @ModelAttribute("navPages")
    public List<ContentPage> navPages() {
        return contentPages.published();
    }

    @ModelAttribute("rsvpDeadline")
    public LocalDate rsvpDeadline() {
        return rsvpDeadline;
    }

    /** Whether the RSVP feature is shown to guests at all. */
    @ModelAttribute("rsvpEnabled")
    public boolean rsvpEnabled() {
        return rsvpEnabled;
    }

    /** RSVP is open up to and including the deadline date. */
    @ModelAttribute("rsvpOpen")
    public boolean rsvpOpen() {
        return !LocalDate.now().isAfter(rsvpDeadline);
    }
}
