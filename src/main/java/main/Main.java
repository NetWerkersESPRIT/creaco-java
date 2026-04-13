package main;

import java.time.LocalDateTime;

import entities.CourseCategory;
import services.CourseCategoryService;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        CourseCategoryService service = new CourseCategoryService();

        try {

            // CREATE

            CourseCategory category = new CourseCategory();
            category.setNom("Programming"); // Try duplicate to test check
            category.setDescription("All programming courses");
            category.setDateDeCreation(Date.valueOf(LocalDate.now()));
            category.setDateDeModification(Date.valueOf(LocalDate.now()));
            category.setSlug("programming");
            category.setDeletedAt(null);

            // Check before inserting
            if (service.existsByNom(category.getNom())) {
                System.out.println("Category with this name already exists!");
            } else {
                service.ajouter(category);
                System.out.println("Category added successfully.");
            }

            // READ ALL

            System.out.println("\n All Categories");
            List<CourseCategory> categories = service.afficher();

            for (CourseCategory c : categories) {
                System.out.println(c);
            }

            // FIND BY ID (example id = 1)



            System.out.println("\n--- Find Category By ID (1) ---");
            CourseCategory found = service.trouverParId(1);

            if (found != null) {
                System.out.println(found);

                // =========================
                // UPDATE
                // =========================
                found.setNom("Updated Programming " + LocalDateTime.now());
                found.setDescription("Updated description");
                found.setDateDeModification(Date.valueOf(LocalDate.now()));

                service.modifier(found.getId(), found);
                System.out.println("Category updated successfully.");

            } else {
                System.out.println("Category not found");
            }


            // DELETE (optional)
            // =========================
            // service.supprimer(1);

            System.out.println("\n=== Test Finished Successfully ===");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}