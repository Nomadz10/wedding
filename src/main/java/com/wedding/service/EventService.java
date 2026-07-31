package com.wedding.service;

import com.wedding.model.Event;
import com.wedding.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class EventService {

    private final EventRepository events;

    public EventService(EventRepository events) {
        this.events = events;
    }

    public List<Event> timeline() {
        return events.findAllByOrderByDisplayOrderAscStartTimeAsc();
    }

    /** One entry per calendar day, each holding that day's events in order. */
    public record DaySchedule(LocalDate date, List<Event> events) {}

    /**
     * Groups the timeline by day so the UI can lay days out horizontally with
     * each day's events stacked vertically. Days are ordered chronologically
     * (undated events, if any, come last).
     */
    public List<DaySchedule> days() {
        Map<LocalDate, List<Event>> byDay = new LinkedHashMap<>();
        for (Event e : timeline()) {
            LocalDate d = e.getStartTime() == null ? null : e.getStartTime().toLocalDate();
            byDay.computeIfAbsent(d, k -> new ArrayList<>()).add(e);
        }
        return byDay.entrySet().stream()
                .map(en -> new DaySchedule(en.getKey(), en.getValue()))
                .sorted(Comparator.comparing(DaySchedule::date,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    public Optional<Event> findById(Long id) {
        return events.findById(id);
    }

    public Event save(Event e) {
        return events.save(e);
    }

    public void delete(Long id) {
        events.deleteById(id);
    }
}
