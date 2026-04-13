package main.java.org.Per.Controllers.Entities;

public class Course {
    private int id;
    private String titre;
    private String description;
    private String image;
    private String date_de_creation;
    private String date_de_modification;
    private int categorie_id;
    private String slug;
    private Integer views;
    private String statut;
    private String niveau;
    private Integer duree_estimee;
    private String deleted_at;

    public Course() {}

    public Course(String titre, String description, String image, String date_de_creation, int categorie_id, String slug, String statut, String niveau) {
        this.titre = titre;
        this.description = description;
        this.image = image;
        this.date_de_creation = this.date_de_creation;
        this.categorie_id = categorie_id;
        this.slug = slug;
        this.statut = statut;
        this.niveau = niveau;
    }

    public Course(int id, String titre, String description, String image, String date_de_creation, String date_de_modification, int categorie_id, String slug, Integer views, String statut, String niveau, Integer dureeEstimee, String deletedAt) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.image = image;
        this.date_de_creation = date_de_creation;
        this.date_de_modification = date_de_modification;
        this.categorie_id = categorie_id;
        this.slug = slug;
        this.views = views;
        this.statut = statut;
        this.niveau = niveau;
        this.duree_estimee = duree_estimee;
        this.deleted_at = deleted_at;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDateDeCreation() {
        return date_de_creation;
    }

    public void setDateDeCreation(String date_de_creation) {
        this.date_de_creation = date_de_creation;
    }

    public String getDateDeModification() {
        return date_de_modification;
    }

    public void setDateDeModification(String date_de_modification) {
        this.date_de_modification = date_de_modification;
    }

    public int getCategorieId() {
        return categorie_id;
    }

    public void setCategorieId(int categorie_id) {
        this.categorie_id = categorie_id;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Integer getViews() {
        return views;
    }

    public void setViews(Integer views) {
        this.views = views;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getNiveau() {
        return niveau;
    }

    public void setNiveau(String niveau) {
        this.niveau = niveau;
    }

    public Integer getDureeEstimee() {
        return duree_estimee;
    }

    public void setDureeEstimee(Integer duree_estimee) {
        this.duree_estimee = duree_estimee;
    }

    public String getDeletedAt() {
        return deleted_at;
    }

    public void setDeletedAt(String deleted_at) {
        this.deleted_at = deleted_at;
    }

    @Override
    public String toString() {
        return "Course{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", image='" + image + '\'' +
                ", dateDeCreation='" + date_de_creation + '\'' +
                ", dateDeModification='" + date_de_modification + '\'' +
                ", categorieId=" + categorie_id +
                ", slug='" + slug + '\'' +
                ", views=" + views +
                ", statut='" + statut + '\'' +
                ", niveau='" + niveau + '\'' +
                ", dureeEstimee=" + duree_estimee +
                ", deletedAt='" + deleted_at + '\'' +
                '}';
    }
}
