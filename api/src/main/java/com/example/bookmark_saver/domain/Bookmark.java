package com.example.bookmark_saver.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.annotation.JsonManagedReference;

/**
 * Entity representing a saved bookmark with embedded metadata and related tags.
 * Mapped to the {@code bookmarks} table.
 */
@Entity
@Table(name = "bookmarks")
public class Bookmark {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String url;
    
    private String notes;

    private Boolean favorite = false;

    @JsonManagedReference
    @ManyToMany
    @JoinTable(
        name = "bookmark_lists",
        joinColumns = @JoinColumn(name = "bookmark_id"),
        inverseJoinColumns = @JoinColumn(name = "list_id")
    )
    private Set<BookmarkList> lists = new HashSet<>();

    @JsonManagedReference
    @ManyToMany
    @JoinTable(
        name = "bookmark_tags",
        joinColumns = @JoinColumn(name = "bookmark_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Metadata metadata;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MetadataStatus metadataStatus = MetadataStatus.PENDING;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    public Bookmark() {}

    public Long getId() {
        return this.id;
    }

    public String getUrl() {
        return this.url;
    }

    public String getNotes() {
        return this.notes;
    }

    public Boolean isFavorite() {
        return this.favorite;
    }

    public Set<BookmarkList> getLists() {
        return this.lists;
    }

    public Set<Tag> getTags() {
        return this.tags;
    }

    public Metadata getMetadata() {
        return this.metadata;
    }

    public MetadataStatus getMetadataStatus() {
        return this.metadataStatus;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public Instant getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setFavorite(Boolean favorite) {
        this.favorite = Boolean.TRUE.equals(favorite);
    }

    public void setLists(Set<BookmarkList> lists) {
        this.lists = lists;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public void setMetadataStatus(MetadataStatus status) {
        this.metadataStatus = status;
    }
}