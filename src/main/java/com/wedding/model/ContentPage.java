package com.wedding.model;

import jakarta.persistence.*;

/**
 * An admin-editable information page (e.g. visa guidance, travel tips, things to do).
 * Body holds rich HTML written in the admin editor.
 */
@Entity
@Table(name = "content_pages")
public class ContentPage implements Comparable<ContentPage> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** URL slug, e.g. "visa" -> /info/visa. Unique. */
    @Column(nullable = false, unique = true)
    private String slug;

    /** Short label shown in the top navigation. */
    @Column(nullable = false)
    private String navLabel;

    /** Page heading. */
    @Column(nullable = false)
    private String title;

    /** Optional one-line intro under the heading. */
    private String subtitle;

    /** Rich HTML body from the admin editor. */
    @Column(columnDefinition = "TEXT")
    private String body;

    private int displayOrder = 0;

    /** Only published pages appear in the nav / are viewable. */
    @Column(nullable = false)
    private boolean published = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug == null ? null : slug.trim().toLowerCase().replaceAll("[^a-z0-9-]+", "-").replaceAll("(^-+|-+$)", ""); }

    public String getNavLabel() { return navLabel; }
    public void setNavLabel(String navLabel) { this.navLabel = navLabel; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }

    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }

    @Override
    public int compareTo(ContentPage o) {
        int byOrder = Integer.compare(this.displayOrder, o.displayOrder);
        return byOrder != 0 ? byOrder : String.valueOf(navLabel).compareToIgnoreCase(String.valueOf(o.navLabel));
    }
}
