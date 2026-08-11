package ng.edu.futa.uclear.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Profile {

    @Id
    @Column(length = 36)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    private Role role;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, unique = true, length = 200)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false, length = 255)
    private String password; // BCrypt hashed

    @Column(name = "matric_no", unique = true, length = 50)
    private String matricNo;

    @Column(name = "staff_id", unique = true, length = 50)
    private String staffId;

    @Column(length = 100)
    private String department;

    @Column(length = 100)
    private String faculty;

    @Column(length = 50)
    private String level; // Students only

    @Column(length = 50)
    private String title; // Staff only

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public enum Role {
        student, staff, admin
    }
}
