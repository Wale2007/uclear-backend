package ng.edu.futa.uclear.controller;

import lombok.RequiredArgsConstructor;
import ng.edu.futa.uclear.model.Due;
import ng.edu.futa.uclear.repository.DueRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dues")
@RequiredArgsConstructor
public class DuesController {

    private final DueRepository dueRepository;

    /**
     * GET /api/dues?role=student
     * Returns the dues catalog for a given role (student or staff).
     * Public endpoint — no JWT required.
     */
    @GetMapping
    public ResponseEntity<List<Due>> getDues(@RequestParam(defaultValue = "student") String role) {
        if (role.equalsIgnoreCase("all") || role.equalsIgnoreCase("admin")) {
            return ResponseEntity.ok(dueRepository.findAll());
        }

        Due.RoleTarget targetRole;
        try {
            targetRole = Due.RoleTarget.valueOf(role.toLowerCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        List<Due> dues = dueRepository.findByRoleTargetInAndIsActiveTrue(
                List.of(targetRole, Due.RoleTarget.all)
        );

        return ResponseEntity.ok(dues);
    }

    /**
     * GET /api/dues/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Due> getDueById(@PathVariable String id) {
        return dueRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/dues  — Admin only (handled by SecurityConfig)
     */
    @PostMapping
    public ResponseEntity<Due> createDue(@RequestBody Due due) {
        if (due.getId() == null || due.getId().isBlank()) {
            due.setId(java.util.UUID.randomUUID().toString());
        }
        return ResponseEntity.ok(dueRepository.save(due));
    }

    /**
     * PUT /api/dues/{id}  — Admin only
     */
    @PutMapping("/{id}")
    public ResponseEntity<Due> updateDue(@PathVariable String id, @RequestBody Due updated) {
        return dueRepository.findById(id).map(due -> {
            due.setName(updated.getName());
            due.setAmount(updated.getAmount());
            due.setCategory(updated.getCategory());
            due.setDescription(updated.getDescription());
            due.setDeadline(updated.getDeadline());
            if (updated.getRoleTarget() != null) due.setRoleTarget(updated.getRoleTarget());
            if (updated.getIsActive() != null) due.setIsActive(updated.getIsActive());
            return ResponseEntity.ok(dueRepository.save(due));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/dues/{id}  — Admin only
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDue(@PathVariable String id) {
        if (!dueRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        dueRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * PATCH /api/dues/{id}/toggle-active  — Admin only
     * Activates or deactivates a due (soft delete / hide from catalog)
     */
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<Due> toggleActive(@PathVariable String id) {
        return dueRepository.findById(id).map(due -> {
            due.setIsActive(due.getIsActive() == null || !due.getIsActive());
            return ResponseEntity.ok(dueRepository.save(due));
        }).orElse(ResponseEntity.notFound().build());
    }
}
