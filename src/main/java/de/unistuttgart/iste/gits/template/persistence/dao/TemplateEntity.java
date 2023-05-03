package de.unistuttgart.iste.gits.template.persistence.dao;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.time.Instant;

@Entity(name = "Template")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false, length = 255)
    @NotNull(message = "Name must not be null")
    @Length(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    private String name;

    @Temporal(TemporalType.TIMESTAMP)
    @Past(message = "createdAt must be in the past")
    private Instant createdAt;

    @Email(message = "Email must be valid")
    private String email;
}
