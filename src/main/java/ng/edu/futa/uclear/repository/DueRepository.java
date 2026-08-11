package ng.edu.futa.uclear.repository;

import ng.edu.futa.uclear.model.Due;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DueRepository extends JpaRepository<Due, String> {
    List<Due> findByRoleTargetInAndIsActiveTrue(List<Due.RoleTarget> roleTargets);
    List<Due> findByIsActiveTrue();
}
