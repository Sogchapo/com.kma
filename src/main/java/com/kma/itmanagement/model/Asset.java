package com.kma.itmanagement.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "assets")
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String assetTag;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String status;

    private String location;
    private LocalDate purchaseDate;

    // Constructors
    public Asset() {}

    public Asset(String assetTag, String name, String type, String status, String location, LocalDate purchaseDate) {
        this.assetTag = assetTag;
        this.name = name;
        this.type = type;
        this.status = status;
        this.location = location;
        this.purchaseDate = purchaseDate;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAssetTag() { return assetTag; }
    public void setAssetTag(String assetTag) { this.assetTag = assetTag; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
}