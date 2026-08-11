package ng.edu.futa.uclear.repository;

import ng.edu.futa.uclear.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, String> {
    Optional<Profile> findByMatricNo(String matricNo);
    Optional<Profile> findByStaffId(String staffId);
    Optional<Profile> findByEmail(String email);
    boolean existsByMatricNo(String matricNo);
    boolean existsByStaffId(String staffId);
    boolean existsByEmail(String email);
}
