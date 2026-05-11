package org.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.backend.enums.Role;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a user in the system.
 * Each user can have a role.
 * Contains personal details and authentication information.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "gender", nullable = false)
    private String gender;

    @Column(name = "age", nullable = false)
    private Integer age;

    @Column(name = "experience", nullable = true)
    private Integer experience;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "mobile", nullable = false, unique = true)
    private String mobile;

    @Column(name = "email")
    private String email;

    @Column(name = "pass_phrase", nullable = true)
    private String password;

    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "last_modified_date", nullable = false)
    private LocalDateTime lastModified;


    /**
     * Custom setter: If email is empty string "", convert to null
     * Lombok will NOT override this method.
     */
    public void setEmail(String email) {
        if (email != null && email.trim().isEmpty()) {
            this.email = null;
        } else {
            this.email = email;
        }
    }

    /**
     * Checks if the user's profile is complete by verifying that all required fields are non-null.
     *
     * @return true if the profile is complete, false otherwise
     */
    public boolean isProfileComplete() {
        return firstName != null &&
                lastName != null &&
                gender != null &&
                age != null &&
                email != null;
    }
}
