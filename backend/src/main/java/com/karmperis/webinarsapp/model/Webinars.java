package com.karmperis.webinarsapp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "webinars")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Webinars extends AbstractUuidEntity {
    private String title;
    private String description;
    private LocalDateTime scheduled_at;
    private Integer duration;
}