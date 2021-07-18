package com.cm.pdfextractor.pdfextractor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "pdf")
public class Pdf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pdf_id")
    private Long pdf_id;

    private String fileName;

    @Column(name = "url", nullable = false)
    private String url;

    @ManyToOne
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "pdf", cascade = CascadeType.ALL)
    private List<Pages> pages;

    @OneToMany(mappedBy = "pdf", cascade = CascadeType.ALL)
    private List<Category> categories;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
