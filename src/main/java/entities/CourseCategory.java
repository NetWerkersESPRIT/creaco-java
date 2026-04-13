package entities;

import java.sql.Date;

public class CourseCategory {
    private int id;
    private String nom;
    private String description;
    private Date date_de_creation;
    private Date date_de_modification;
    private String slug;
    private Date deleted_at;

    public CourseCategory() {}

    public CourseCategory(int id, String nom, String description, Date date_de_creation, Date date_de_modification, String slug, Date deleted_at) {
        this.id=id;
        this.nom = nom;
        this.description = description;
        this.date_de_creation = date_de_creation;
        this.date_de_modification = date_de_modification;
        this.slug = slug;
        this.deleted_at = deleted_at;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDateDeCreation() {
        return date_de_creation;
    }

    public void setDateDeCreation(Date date_de_creation) {
        this.date_de_creation = date_de_creation;
    }

    public Date getDateDeModification() {
        return date_de_modification;
    }

    public void setDateDeModification(Date date_de_modification) {
        this.date_de_modification = date_de_modification;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Date getDeletedAt() {
        return deleted_at;
    }

    public void setDeletedAt(Date deleted_at) {
        this.deleted_at = deleted_at;
    }

    @Override
    public String toString() {
        return "CategorieCours{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", description='" + description + '\'' +
                ", dateDeCreation='" + date_de_creation + '\'' +
                ", dateDeModification='" + date_de_modification + '\'' +
                ", slug='" + slug + '\'' +
                ", deletedAt='" + deleted_at + '\'' +
                '}';
    }
}
