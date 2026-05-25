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
@Table(
    name = "tags",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_tags_parent_name",
            columnNames = {"parent_id", "name"}
        )
    }
)
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Tag parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @OrderBy("name ASC")
    private Set<Tag> children = new HashSet<>();

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

    public String getColor() {
        return this.color;
    }

    public Tag getParent() {
        return this.parent;
    }

    public Set<Tag> getChildren() {
        return this.children;
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

    public void setColor(String color) {
        this.color = color;
    }

    public void setParent(Tag parent) {
        this.parent = parent;
    }
}