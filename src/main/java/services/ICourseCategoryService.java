package services;

import entities.CourseCategory;
import java.util.List;

public interface ICourseCategoryService {
    boolean createCourseCategory(CourseCategory category);
    CourseCategory getCourseCategoryById(int id);
    List<CourseCategory> getAllCourseCategories();
    List<CourseCategory> searchCourseCategoriesByName(String nom);
    boolean updateCourseCategory(CourseCategory category);
    boolean deleteCourseCategory(int id);
    boolean softDeleteCourseCategory(int id, String deletedAt);
}
