package com.example.UrlShortner.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners({AuditingEntityListener.class})
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class BaseAuditableEntity implements Serializable {

    private static final long serialVersionUID = 2841486706711459928L;

    @CreatedDate
    @Column(
            updatable = false,
            name = "created_on",
            columnDefinition = "TIMESTAMP"
    )
    protected LocalDateTime createdOn;

    @LastModifiedDate
    @Column(
            name = "modified_on",
            columnDefinition = "TIMESTAMP"
    )
    protected LocalDateTime modifiedOn;

    @CreatedBy
    @Column(
            name = "created_by",
            updatable = false
    )
    protected String createdBy;

    @LastModifiedBy
    @Column(
            name = "modified_by"
    )
    protected String modifiedBy;
}