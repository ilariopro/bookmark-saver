package com.example.bookmark_saver.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonBackReference;

/**
 * Entity representing a tag that can be associated with multiple bookmarks.
 * Mapped to the {@code tags} table.
 */
@Entity
@Table(name = "tags")
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @JsonBackReference
    @ManyToMany(mappedBy = "tags")
    private Set<Bookmark> bookmarks = new HashSet<>();

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    public Tag() {}

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Set<Bookmark> getBookmarks() {
        return this.bookmarks;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public Instant getUpdatedAt() {
        return this.updatedAt;
    }

    public void setName(String name) {
        this.name = name;
    }
}