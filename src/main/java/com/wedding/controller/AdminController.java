package com.wedding.controller;

import com.wedding.config.AdminAuthInterceptor;
import com.wedding.model.Event;
import com.wedding.model.Guest;
import com.wedding.model.Party;
import com.wedding.repository.PartyRepository;
import com.wedding.service.ContentPageService;
import com.wedding.service.EventService;
import com.wedding.service.FileStorageService;
import com.wedding.service.GalleryService;
import com.wedding.service.GuestService;
import com.wedding.repository.GuestRepository;
import com.wedding.repository.EventRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final GuestService guestService;
    private final EventService eventService;
    private final FileStorageService fileStorage;
    private final GalleryService galleryService;
    private final ContentPageService contentPageService;
    private final GuestRepository guestRepository;
    private final EventRepository eventRepository;
    private final PartyRepository partyRepository;
    private final String adminPassword;

    public AdminController(GuestService guestService, EventService eventService,
                           FileStorageService fileStorage, GalleryService galleryService,
                           ContentPageService contentPageService,
                           GuestRepository guestRepository, EventRepository eventRepository,
                           PartyRepository partyRepository,
                           @Value("${wedding.admin.password}") String adminPassword) {
        this.guestService = guestService;
        this.eventService = eventService;
        this.fileStorage = fileStorage;
        this.galleryService = galleryService;
        this.contentPageService = contentPageService;
        this.guestRepository = guestRepository;
        this.eventRepository = eventRepository;
        this.partyRepository = partyRepository;
        this.adminPassword = adminPassword;
    }

    // ---------- Auth ----------

    @GetMapping("/login")
    public String loginForm() {
        return "admin/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String password, HttpSession session, Model model) {
        if (adminPassword.equals(password)) {
            session.setAttribute(AdminAuthInterceptor.ADMIN_SESSION_KEY, true);
            return "redirect:/admin";
        }
        model.addAttribute("error", "Incorrect password.");
        return "admin/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute(AdminAuthInterceptor.ADMIN_SESSION_KEY);
        return "redirect:/admin/login";
    }

    // ---------- Dashboard ----------

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("stats", guestService.stats());
        return "admin/dashboard";
    }

    // ---------- Events ----------

    @GetMapping("/events")
    public String events(Model model) {
        model.addAttribute("events", eventService.timeline());
        return "admin/events";
    }

    @GetMapping("/events/new")
    public String newEvent(Model model) {
        model.addAttribute("event", new Event());
        model.addAttribute("mode", "new");
        return "admin/event-form";
    }

    @GetMapping("/events/{id}/edit")
    public String editEvent(@PathVariable Long id, Model model) {
        Event e = eventService.findById(id).orElseThrow();
        model.addAttribute("event", e);
        model.addAttribute("mode", "edit");
        return "admin/event-form";
    }

    @PostMapping("/events")
    public String saveEvent(@RequestParam(required = false) Long id,
                            @RequestParam String title,
                            @RequestParam(required = false) String description,
                            @RequestParam(required = false) String location,
                            @RequestParam(required = false) String address,
                            @RequestParam(required = false) String dressCode,
                            @RequestParam(required = false) String dressCodeWestern,
                            @RequestParam(required = false) String mapUrl,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
                            @RequestParam(required = false, defaultValue = "0") int displayOrder,
                            @RequestParam(required = false) MultipartFile photo,
                            RedirectAttributes ra) {
        Event e = (id != null) ? eventService.findById(id).orElse(new Event()) : new Event();
        e.setTitle(title);
        e.setDescription(description);
        e.setLocation(location);
        e.setAddress(address);
        e.setDressCode(blankToNull(dressCode));
        e.setDressCodeWestern(blankToNull(dressCodeWestern));
        e.setMapUrl(blankToNull(mapUrl));
        e.setStartTime(startTime);
        e.setDisplayOrder(displayOrder);
        String stored = fileStorage.storeImageAsJpeg(photo);
        if (stored != null) {
            e.setImageFilename(stored);
        }
        eventService.save(e);
        ra.addFlashAttribute("message", "Event saved.");
        return "redirect:/admin/events";
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    @PostMapping("/events/{id}/delete")
    public String deleteEvent(@PathVariable Long id, RedirectAttributes ra) {
        eventService.delete(id);
        ra.addFlashAttribute("message", "Event deleted.");
        return "redirect:/admin/events";
    }

    // ---------- Guests / invite list ----------

    @GetMapping("/guests")
    public String guests(Model model) {
        model.addAttribute("guests", guestService.all());
        model.addAttribute("stats", guestService.stats());
        return "admin/guests";
    }

    @PostMapping("/guests")
    public String addGuest(@RequestParam String firstName,
                           @RequestParam String lastName,
                           RedirectAttributes ra) {
        if (firstName.isBlank() || lastName.isBlank()) {
            ra.addFlashAttribute("error", "First and last name are required.");
            return "redirect:/admin/guests";
        }
        if (guestService.findByName(firstName, lastName).isPresent()) {
            ra.addFlashAttribute("error", firstName + " " + lastName + " is already on the list.");
            return "redirect:/admin/guests";
        }
        Guest g = new Guest();
        g.setFirstName(firstName);
        g.setLastName(lastName);
        guestRepository.save(g);
        ra.addFlashAttribute("message", "Added " + g.getFirstName() + " " + g.getLastName() + ".");
        return "redirect:/admin/guests";
    }

    /** Bulk import: one guest per line, "First Last" (or "First,Last"). */
    @PostMapping("/guests/import")
    public String importGuests(@RequestParam String bulk, RedirectAttributes ra) {
        int added = 0, skipped = 0;
        for (String raw : bulk.split("\\r?\\n")) {
            String line = raw.trim();
            if (line.isEmpty()) continue;
            String first, last;
            if (line.contains(",")) {
                String[] parts = line.split(",", 2);
                first = parts[0].trim();
                last = parts[1].trim();
            } else {
                int sp = line.lastIndexOf(' ');
                if (sp <= 0) { skipped++; continue; }
                first = line.substring(0, sp).trim();
                last = line.substring(sp + 1).trim();
            }
            if (first.isEmpty() || last.isEmpty() || guestService.findByName(first, last).isPresent()) {
                skipped++;
                continue;
            }
            Guest g = new Guest();
            g.setFirstName(first);
            g.setLastName(last);
            guestRepository.save(g);
            added++;
        }
        ra.addFlashAttribute("message", "Imported " + added + " guest(s), skipped " + skipped + ".");
        return "redirect:/admin/guests";
    }

    @GetMapping("/guests/{id}")
    public String editGuest(@PathVariable Long id, Model model) {
        Guest g = guestRepository.findById(id).orElseThrow();
        model.addAttribute("guest", g);
        model.addAttribute("allEvents", eventService.timeline());
        return "admin/guest-detail";
    }

    /** Assign room + personalised event schedule (used after the hotel is booked). */
    @PostMapping("/guests/{id}")
    public String updateGuest(@PathVariable Long id,
                              @RequestParam(required = false) String roomAssignment,
                              @RequestParam(required = false) String roomDetails,
                              @RequestParam(required = false) List<Long> eventIds,
                              RedirectAttributes ra) {
        Guest g = guestRepository.findById(id).orElseThrow();
        g.setRoomAssignment(roomAssignment == null || roomAssignment.isBlank() ? null : roomAssignment.trim());
        g.setRoomDetails(roomDetails == null || roomDetails.isBlank() ? null : roomDetails.trim());

        Set<Event> selected = new HashSet<>();
        if (eventIds != null) {
            List<Event> found = eventRepository.findAllById(eventIds);
            selected.addAll(found);
        }
        g.setInvitedEvents(selected);
        guestRepository.save(g);
        ra.addFlashAttribute("message", "Saved details for " + g.getFirstName() + " " + g.getLastName() + ".");
        return "redirect:/admin/guests/" + id;
    }

    @PostMapping("/guests/{id}/delete")
    public String deleteGuest(@PathVariable Long id, RedirectAttributes ra) {
        guestRepository.deleteById(id);
        ra.addFlashAttribute("message", "Guest removed.");
        return "redirect:/admin/guests";
    }

    // ---------- Parties (room-sharing groups) ----------

    @GetMapping("/parties")
    public String parties(Model model) {
        List<Guest> all = guestService.all();
        model.addAttribute("parties", partyRepository.findAllByOrderByNameAsc());
        model.addAttribute("ungrouped", all.stream().filter(g -> g.getParty() == null).toList());
        model.addAttribute("roomShareNotes",
                all.stream().filter(g -> g.getComingWith() != null && !g.getComingWith().isBlank()).toList());
        model.addAttribute("stats", guestService.stats());
        return "admin/parties";
    }

    @PostMapping("/parties")
    public String createParty(@RequestParam String name, RedirectAttributes ra) {
        if (name != null && !name.isBlank()) {
            Party p = new Party();
            p.setName(name.trim());
            partyRepository.save(p);
            ra.addFlashAttribute("message", "Party \"" + p.getName() + "\" created.");
        }
        return "redirect:/admin/parties";
    }

    @PostMapping("/parties/{id}")
    public String updateParty(@PathVariable Long id,
                              @RequestParam(required = false) String name,
                              @RequestParam(required = false) String roomAssignment,
                              @RequestParam(required = false) String roomDetails,
                              RedirectAttributes ra) {
        Party p = partyRepository.findById(id).orElseThrow();
        if (name != null && !name.isBlank()) p.setName(name.trim());
        p.setRoomAssignment(blankToNull(roomAssignment));
        p.setRoomDetails(blankToNull(roomDetails));
        partyRepository.save(p);
        ra.addFlashAttribute("message", "Party updated.");
        return "redirect:/admin/parties";
    }

    @PostMapping("/parties/{id}/add")
    public String addPartyMember(@PathVariable Long id, @RequestParam Long guestId, RedirectAttributes ra) {
        Party p = partyRepository.findById(id).orElseThrow();
        Guest g = guestRepository.findById(guestId).orElseThrow();
        g.setParty(p);
        guestRepository.save(g);
        return "redirect:/admin/parties";
    }

    @PostMapping("/parties/{id}/remove")
    public String removePartyMember(@PathVariable Long id, @RequestParam Long guestId, RedirectAttributes ra) {
        Guest g = guestRepository.findById(guestId).orElseThrow();
        g.setParty(null);
        guestRepository.save(g);
        return "redirect:/admin/parties";
    }

    @PostMapping("/parties/{id}/delete")
    public String deleteParty(@PathVariable Long id, RedirectAttributes ra) {
        Party p = partyRepository.findById(id).orElseThrow();
        for (Guest g : new ArrayList<>(p.getMembers())) {
            g.setParty(null);
            guestRepository.save(g);
        }
        partyRepository.delete(p);
        ra.addFlashAttribute("message", "Party deleted.");
        return "redirect:/admin/parties";
    }

    // ---------- Info pages (visa, travel, things to do, …) ----------

    @GetMapping("/pages")
    public String pages(Model model) {
        model.addAttribute("pages", contentPageService.all());
        return "admin/pages";
    }

    @GetMapping("/pages/new")
    public String newPage(Model model) {
        model.addAttribute("page", new com.wedding.model.ContentPage());
        model.addAttribute("mode", "new");
        return "admin/page-form";
    }

    @GetMapping("/pages/{id}/edit")
    public String editPage(@PathVariable Long id, Model model) {
        model.addAttribute("page", contentPageService.byId(id).orElseThrow());
        model.addAttribute("mode", "edit");
        return "admin/page-form";
    }

    @PostMapping("/pages")
    public String savePage(@RequestParam(required = false) Long id,
                           @RequestParam String navLabel,
                           @RequestParam String title,
                           @RequestParam(required = false) String slug,
                           @RequestParam(required = false) String subtitle,
                           @RequestParam(required = false) String body,
                           @RequestParam(required = false, defaultValue = "0") int displayOrder,
                           @RequestParam(required = false, defaultValue = "false") boolean published,
                           RedirectAttributes ra) {
        com.wedding.model.ContentPage p = (id != null)
                ? contentPageService.byId(id).orElseGet(com.wedding.model.ContentPage::new)
                : new com.wedding.model.ContentPage();
        p.setNavLabel(navLabel.trim());
        p.setTitle(title.trim());
        // Derive slug from nav label if none given.
        p.setSlug((slug == null || slug.isBlank()) ? navLabel : slug);
        p.setSubtitle(blankToNull(subtitle));
        p.setBody(body);
        p.setDisplayOrder(displayOrder);
        p.setPublished(published);
        contentPageService.save(p);
        ra.addFlashAttribute("message", "Page saved.");
        return "redirect:/admin/pages";
    }

    @PostMapping("/pages/{id}/delete")
    public String deletePage(@PathVariable Long id, RedirectAttributes ra) {
        contentPageService.delete(id);
        ra.addFlashAttribute("message", "Page deleted.");
        return "redirect:/admin/pages";
    }

    // ---------- Mailing addresses (for sending invitations) ----------

    @GetMapping("/addresses")
    public String addresses(Model model) {
        model.addAttribute("guests", guestService.withAddresses());
        return "admin/addresses";
    }

    @GetMapping(value = "/addresses.csv", produces = "text/csv")
    @org.springframework.web.bind.annotation.ResponseBody
    public org.springframework.http.ResponseEntity<String> addressesCsv() {
        StringBuilder sb = new StringBuilder("First Name,Last Name,Plus One,Mailing Address\n");
        for (Guest g : guestService.withAddresses()) {
            sb.append(csv(g.getFirstName())).append(',')
              .append(csv(g.getLastName())).append(',')
              .append(csv(g.isBringingPlusOne() ? g.getPlusOneName() : "")).append(',')
              .append(csv(g.getMailingAddress())).append('\n');
        }
        return org.springframework.http.ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"wedding-addresses.csv\"")
                .body(sb.toString());
    }

    /** Minimal CSV field escaping. */
    private static String csv(String s) {
        if (s == null) return "";
        String v = s.replace("\r", " ").replace("\n", " ").trim();
        if (v.contains(",") || v.contains("\"")) {
            v = "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }

    // ---------- Guest photo wall ----------

    @GetMapping("/gallery")
    public String gallery(Model model) {
        model.addAttribute("photos", galleryService.all());
        return "admin/gallery";
    }

    @PostMapping("/gallery/{id}/delete")
    public String deletePhoto(@PathVariable Long id, RedirectAttributes ra) {
        galleryService.delete(id);
        ra.addFlashAttribute("message", "Photo removed.");
        return "redirect:/admin/gallery";
    }

    /** Upload photos that go straight to the home-page slideshow (featured). */
    @PostMapping("/gallery/upload")
    public String uploadFeatured(@RequestParam(value = "photos", required = false) MultipartFile[] photos,
                                 RedirectAttributes ra) {
        int saved = galleryService.saveAll(photos, "Couple", true);
        ra.addFlashAttribute("message", "Added " + saved + " photo(s) to the home slideshow.");
        return "redirect:/admin/gallery";
    }

    /** Toggle whether a photo appears in the home slideshow. */
    @PostMapping("/gallery/{id}/feature")
    public String toggleFeatured(@PathVariable Long id, RedirectAttributes ra) {
        galleryService.toggleFeatured(id);
        return "redirect:/admin/gallery";
    }
}
