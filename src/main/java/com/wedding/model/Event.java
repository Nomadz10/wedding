package com.wedding.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * A wedding event (ceremony, reception, mehndi, rehearsal dinner, etc.)
 * shown on the public timeline.
 */
@Entity
@Table(name = "events")
public class Event implements Comparable<Event> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 4000)
    private String description;

    private String location;

    private String address;

    private LocalDateTime startTime;

    /** Filename of an uploaded photo (served from /uploads/**), or null. */
    private String imageFilename;

    /** Controls ordering on the timeline (lower = earlier). */
    private int displayOrder = 0;

    /** Optional general dress code (used as a fallback if the split ones are empty). */
    private String dressCode;

    /** Dress code for women (e.g. "Lehengas, sarees"). */
    private String dressCodeWomen;

    /** Dress code for men (e.g. "Sherwanis, kurtas"). */
    private String dressCodeMen;

    /** Optional clickable link for the venue (e.g. a Google Maps URL). */
    @Column(length = 1000)
    private String mapUrl;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public String getImageFilename() { return imageFilename; }
    public void setImageFilename(String imageFilename) { this.imageFilename = imageFilename; }

    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }

    public String getDressCode() { return dressCode; }
    public void setDressCode(String dressCode) { this.dressCode = dressCode; }

    public String getDressCodeWomen() { return dressCodeWomen; }
    public void setDressCodeWomen(String dressCodeWomen) { this.dressCodeWomen = dressCodeWomen; }

    public String getDressCodeMen() { return dressCodeMen; }
    public void setDressCodeMen(String dressCodeMen) { this.dressCodeMen = dressCodeMen; }

    public String getMapUrl() { return mapUrl; }
    public void setMapUrl(String mapUrl) { this.mapUrl = mapUrl; }

    /** Orders by displayOrder, then start time (nulls last), for the personalised schedule. */
    @Override
    public int compareTo(Event other) {
        int byOrder = Integer.compare(this.displayOrder, other.displayOrder);
        if (byOrder != 0) return byOrder;
        if (this.startTime == null && other.startTime == null) return 0;
        if (this.startTime == null) return 1;
        if (other.startTime == null) return -1;
        return this.startTime.compareTo(other.startTime);
    }
}
