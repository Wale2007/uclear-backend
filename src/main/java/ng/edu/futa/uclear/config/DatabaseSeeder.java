package ng.edu.futa.uclear.config;

import lombok.RequiredArgsConstructor;
import ng.edu.futa.uclear.model.Due;
import ng.edu.futa.uclear.model.Profile;
import ng.edu.futa.uclear.repository.DueRepository;
import ng.edu.futa.uclear.repository.ProfileRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DatabaseSeeder — runs once on startup if the database is empty.
 * Seeds all 20 students, 20 staff, and 10 dues into MySQL.
 * This replaces the separate seedFirestore.js Node script.
 */
@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final ProfileRepository profileRepository;
    private final DueRepository dueRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedAllIfMissing();
    }

    public void seedAllIfMissing() {
        String hashedPassword = passwordEncoder.encode("password123");

        long studentCount = profileRepository.findAll().stream().filter(p -> p.getRole() == Profile.Role.student).count();
        long staffCount   = profileRepository.findAll().stream().filter(p -> p.getRole() == Profile.Role.staff).count();
        long duesCount    = dueRepository.count();

        if (studentCount == 0) {
            System.out.println("[Uclear] Seeding student accounts into MySQL...");
            seedStudents(hashedPassword);
        }

        if (staffCount == 0) {
            System.out.println("[Uclear] Seeding staff accounts into MySQL...");
            seedStaff(hashedPassword);
        }

        if (!profileRepository.existsByEmail("sug.admin@futa.edu.ng")) {
            System.out.println("[Uclear] Seeding admin accounts into MySQL...");
            seedAdmins(hashedPassword);
        }

        if (duesCount == 0) {
            System.out.println("[Uclear] Seeding dues catalog into MySQL...");
            seedDues();
        }

        System.out.println("[Uclear] MySQL database seeding check complete!");
    }

    private void seedStudents(String hashedPassword) {
        List<Profile> students = List.of(
            student("OLA-SALAWU OLAWALE OLUWASEGUN", "wola77923@gmail.com", "08034567890", "SEN/22/9292", "Software Engineering", "Computing", "300 Level"),
            student("Chukwuemeka Nwosu", "c.nwosu@futa.edu.ng", "07065432109", "EEE/21/3321", "Electrical Engineering", "Engineering", "400 Level"),
            student("Fatima Al-Hassan", "f.alhassan@futa.edu.ng", "08112345678", "MEE/23/5012", "Mechanical Engineering", "Engineering", "200 Level"),
            student("Blessing Okafor", "b.okafor@futa.edu.ng", "09023456789", "CHE/20/2209", "Chemical Engineering", "Engineering", "500 Level"),
            student("Ibrahim Musa", "i.musa@futa.edu.ng", "08098765432", "PHY/22/4120", "Physics", "Science", "300 Level"),
            student("Ngozi Adeleke", "n.adeleke@futa.edu.ng", "07011223344", "MTH/21/3890", "Mathematics", "Science", "400 Level"),
            student("Afolabi Taiwo Adeyemi", "a.adeyemi@futa.edu.ng", "08055678901", "CSC/23/6001", "Computer Science", "Computing", "200 Level"),
            student("Mercy Chidinma Eze", "m.eze@futa.edu.ng", "09011234567", "BCH/22/4455", "Biochemistry", "Science", "300 Level"),
            student("Suleiman Yakubu Danjuma", "s.danjuma@futa.edu.ng", "07022334455", "CVE/21/3100", "Civil Engineering", "Engineering", "400 Level"),
            student("Adaeze Nkechi Onyeka", "a.onyeka@futa.edu.ng", "08077889900", "ARC/23/5500", "Architecture", "Environmental Technology", "200 Level"),
            student("Tunde Babatunde Akinola", "t.akinola@futa.edu.ng", "08033445566", "IEE/20/2001", "Industrial Engineering", "Engineering", "500 Level"),
            student("Hauwa Umar Bello", "h.bello@futa.edu.ng", "07099887766", "CHM/22/4300", "Chemistry", "Science", "300 Level"),
            student("Emeka Chijioke Obi", "e.obi@futa.edu.ng", "09066778899", "GEO/21/3600", "Geology", "Science", "400 Level"),
            student("Yetunde Funmilayo Bakare", "y.bakare@futa.edu.ng", "08044556677", "APG/23/5700", "Applied Geophysics", "Science", "200 Level"),
            student("Chisom Adaora Igwe", "c.igwe@futa.edu.ng", "08155566677", "TPS/22/4800", "Transport Planning", "Environmental Technology", "300 Level"),
            student("Abdulrahman Salisu Idris", "a.idris@futa.edu.ng", "07033221100", "MIN/20/1900", "Mining Engineering", "Engineering", "500 Level"),
            student("Oluwakemi Adesanya", "o.adesanya@futa.edu.ng", "09077665544", "MAS/23/5900", "Marine Science & Technology", "Science", "200 Level"),
            student("Victor Onyekachi Nzegwu", "v.nzegwu@futa.edu.ng", "08088997766", "MTE/21/3200", "Metallurgical Engineering", "Engineering", "400 Level"),
            student("Amina Sani Garba", "a.garba@futa.edu.ng", "07044332211", "BLD/22/4100", "Building Technology", "Environmental Technology", "300 Level"),
            student("Olumide Adebayo Ogunleye", "o.ogunleye@futa.edu.ng", "09055443322", "PET/20/1800", "Petroleum Engineering", "Engineering", "500 Level")
        );

        students.forEach(s -> { s.setPassword(hashedPassword); profileRepository.save(s); });
        System.out.println("  ✔ 20 students seeded");
    }

    private void seedStaff(String hashedPassword) {
        List<Profile> staff = List.of(
            staff("PROF S.O SALAWU", "Professor", "sosalawu@futa.edu.ng", "08129038475", "FUTA/STF/CS/1092", "Computer Science", "Computing"),
            staff("Dr. Aminu Garba", "Senior Lecturer", "a.garba2@futa.edu.ng", "07032198765", "FUTA/STF/EE/0881", "Electrical Engineering", "Engineering"),
            staff("Mrs. Rachael Idowu", "Lecturer I", "r.idowu@futa.edu.ng", "09087654321", "FUTA/STF/MT/0443", "Mathematics", "Science"),
            staff("Dr. Benjamin Okafor", "Associate Prof", "b.okafor2@futa.edu.ng", "08011223344", "FUTA/STF/CE/0901", "Chemical Engineering", "Engineering"),
            staff("Prof. Helen Adeyemi", "Professor", "h.adeyemi@futa.edu.ng", "08022334455", "FUTA/STF/BI/0772", "Biochemistry", "Science"),
            staff("Dr. Tunde Bakare", "Senior Lecturer", "t.bakare@futa.edu.ng", "07033445566", "FUTA/STF/CV/0663", "Civil Engineering", "Engineering"),
            staff("Dr. Sarah Ibrahim", "Lecturer I", "s.ibrahim@futa.edu.ng", "09044556677", "FUTA/STF/AR/0554", "Architecture", "Environmental Technology"),
            staff("Engr. Michael Obi", "Lecturer II", "m.obi@futa.edu.ng", "08155667788", "FUTA/STF/IE/0445", "Industrial Engineering", "Engineering"),
            staff("Dr. Fatima Umar", "Senior Lecturer", "f.umar@futa.edu.ng", "07066778899", "FUTA/STF/CH/0336", "Chemistry", "Science"),
            staff("Prof. Adebayo Ogunleye", "Professor", "a.ogunleye@futa.edu.ng", "08077889900", "FUTA/STF/GL/0227", "Geology", "Science"),
            staff("Dr. Ngozi Igwe", "Lecturer I", "n.igwe@futa.edu.ng", "09088990011", "FUTA/STF/AP/0118", "Applied Geophysics", "Science"),
            staff("Dr. Abdul Idris", "Senior Lecturer", "a.idris2@futa.edu.ng", "08199001122", "FUTA/STF/TP/0999", "Transport Planning", "Environmental Technology"),
            staff("Engr. Kemi Adesanya", "Lecturer II", "k.adesanya@futa.edu.ng", "07011002233", "FUTA/STF/MI/0888", "Mining Engineering", "Engineering"),
            staff("Dr. Victor Nzegwu", "Senior Lecturer", "v.nzegwu2@futa.edu.ng", "09022113344", "FUTA/STF/MS/0777", "Marine Science & Technology", "Science"),
            staff("Dr. Amina Sani", "Lecturer I", "a.sani@futa.edu.ng", "08133224455", "FUTA/STF/BU/0666", "Building Technology", "Environmental Technology"),
            staff("Engr. Olumide Tobi", "Lecturer II", "o.tobi@futa.edu.ng", "07044335566", "FUTA/STF/PG/0555", "Petroleum Engineering", "Engineering"),
            staff("Dr. Chidinma Eze", "Senior Lecturer", "c.eze@futa.edu.ng", "08055446677", "FUTA/STF/ME/0444", "Mechanical Engineering", "Engineering"),
            staff("Prof. Yakubu Danjuma", "Professor", "y.danjuma@futa.edu.ng", "09066557788", "FUTA/STF/EE/0333", "Electrical Engineering", "Engineering"),
            staff("Dr. Nkechi Onyeka", "Lecturer I", "n.onyeka@futa.edu.ng", "08177668899", "FUTA/STF/MT/0222", "Mathematics", "Science"),
            staff("Engr. Babatunde Akinola", "Lecturer II", "b.akinola@futa.edu.ng", "07088779900", "FUTA/STF/CS/0111", "Computer Science", "Computing")
        );

        staff.forEach(s -> { s.setPassword(hashedPassword); profileRepository.save(s); });
        System.out.println("  ✔ 20 staff seeded");
    }

    private void seedAdmins(String hashedPassword) {
        List<Profile> admins = List.of(
            admin("SUG Executive Admin", "Association Admin", "sug.admin@futa.edu.ng", "08011112222", "FUTA/ADM/SUG/01", "Student Union", "Computing"),
            admin("Faculty of Computing Admin", "Faculty Admin", "computing.admin@futa.edu.ng", "08022223333", "FUTA/ADM/FAC/01", "Deanery", "Computing"),
            admin("Software Eng. Dept Admin", "Department Admin", "sen.admin@futa.edu.ng", "08033334444", "FUTA/ADM/DEP/01", "Software Engineering", "Computing"),
            admin("University Bursar Admin", "Bursary Officer", "bursar.admin@futa.edu.ng", "08044445555", "FUTA/ADM/BUR/01", "Bursary", "Administration")
        );

        admins.forEach(a -> { a.setPassword(hashedPassword); profileRepository.save(a); });
        System.out.println("  ✔ 4 admin accounts seeded");
    }

    private void seedDues() {
        List<Due> dues = List.of(
            due("dues-std-001", "Student Union Government (SUG) Dues", 2000, "Student Union", "Annual SUG developmental levy covering all student activities.", LocalDate.of(2026, 8, 31), Due.RoleTarget.student),
            due("dues-std-002", "Faculty Developmental Levy", 3500, "Faculty", "Faculty-level operational fees and seminar series.", LocalDate.of(2026, 9, 15), Due.RoleTarget.student),
            due("dues-std-003", "Departmental Dues", 5000, "Departmental", "Laboratory maintenance and final year project seed fund.", LocalDate.of(2026, 6, 25), Due.RoleTarget.student),
            due("dues-std-004", "Library Clearance & E-Resource Fee", 1500, "Other", "Library clearance and digital resources access fee.", LocalDate.of(2026, 9, 30), Due.RoleTarget.student),
            due("dues-std-005", "Sports & Recreation Levy", 1000, "Other", "Annual sports complex access and inter-faculty games levy.", LocalDate.of(2026, 10, 15), Due.RoleTarget.student),
            due("dues-std-006", "Medical / Health Insurance Levy", 2500, "Health", "Student Health Centre operational levy and insurance cover.", LocalDate.of(2026, 7, 1), Due.RoleTarget.student),
            due("dues-stf-001", "ASUU Union Monthly Dues", 2000, "Union", "Monthly Academic Staff Union of Universities (ASUU) dues.", LocalDate.of(2026, 8, 31), Due.RoleTarget.staff),
            due("dues-stf-002", "Staff Welfare & Cooperative Fund", 5000, "Welfare", "Annual staff cooperative fund and welfare contributions.", LocalDate.of(2026, 9, 15), Due.RoleTarget.staff),
            due("dues-stf-003", "Staff Club Annual Membership", 10000, "Club", "Annual membership fee for the FUTA staff club.", LocalDate.of(2026, 6, 30), Due.RoleTarget.staff),
            due("dues-stf-004", "NASU Non-Academic Staff Levy", 1500, "Union", "Non-Academic Staff Union monthly levy.", LocalDate.of(2026, 10, 1), Due.RoleTarget.staff)
        );

        dues.forEach(dueRepository::save);
        System.out.println("  ✔ 10 dues seeded");
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Profile student(String name, String email, String phone, String matricNo,
                            String dept, String faculty, String level) {
        Profile p = new Profile();
        p.setId(UUID.randomUUID().toString());
        p.setRole(Profile.Role.student);
        p.setName(name);
        p.setEmail(email);
        p.setPhone(phone);
        p.setMatricNo(matricNo);
        p.setDepartment(dept);
        p.setFaculty(faculty);
        p.setLevel(level);
        return p;
    }

    private Profile staff(String name, String title, String email, String phone,
                          String staffId, String dept, String faculty) {
        Profile p = new Profile();
        p.setId(UUID.randomUUID().toString());
        p.setRole(Profile.Role.staff);
        p.setName(name);
        p.setTitle(title);
        p.setEmail(email);
        p.setPhone(phone);
        p.setStaffId(staffId);
        p.setDepartment(dept);
        p.setFaculty(faculty);
        return p;
    }

    private Profile admin(String name, String title, String email, String phone,
                          String staffId, String dept, String faculty) {
        Profile p = new Profile();
        p.setId(UUID.randomUUID().toString());
        p.setRole(Profile.Role.admin);
        p.setName(name);
        p.setTitle(title);
        p.setEmail(email);
        p.setPhone(phone);
        p.setStaffId(staffId);
        p.setDepartment(dept);
        p.setFaculty(faculty);
        return p;
    }

    private Due due(String id, String name, double amount, String category,
                    String description, LocalDate deadline, Due.RoleTarget target) {
        Due d = new Due();
        d.setId(id);
        d.setName(name);
        d.setAmount(BigDecimal.valueOf(amount));
        d.setCategory(category);
        d.setDescription(description);
        d.setDeadline(deadline);
        d.setRoleTarget(target);
        d.setIsActive(true);
        return d;
    }
}
