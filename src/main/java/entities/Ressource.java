package entities;

public class Ressource {
    private int id;
    private String nom;
    private String url;
    private String type;
    private String contenu;
    private String date_de_creation;
    private String date_de_modification;
    private int cours_id;
    public Ressource() {}

    public Ressource(String nom, String url, String type, String contenu, String date_de_creation, int cours_id) {
        this.nom = nom;
        this.url = url;
        this.type = type;
        this.contenu = contenu;
        this.date_de_creation = date_de_creation;
        this.cours_id = cours_id;
    }

    public Ressource(int id, String nom, String url, String type, String contenu, String dateDeCreation, String dateDeModification, int coursId) {
        this.id = id;
        this.nom = nom;
        this.url = url;
        this.type = type;
        this.contenu = contenu;
        this.date_de_creation = date_de_creation;
        this.date_de_modification = Date_de_modification;
        this.cours_id = cours_id;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public String getDateDeCreation() {
        return dateDeCreation;
    }

    public void setDateDeCreation(String dateDeCreation) {
        this.dateDeCreation = dateDeCreation;
    }

    public String getDate_de_modification() {
        return date_de_modification;
    }

    public void setDate_de_modification(String date_de_modification) {
        this.date_de_modification = date_de_modification;
    }

    public int getCourse_id() {
        return course_id;
    }

    public void setCourse_id(int course_id) {
        this.course_id = course_id;
    }

    @Override
    public String toString() {
        return "Ressource{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", url='" + url + '\'' +
                ", type='" + type + '\'' +
                ", contenu='" + contenu + '\'' +
                ", dateDeCreation='" + dateDeCreation + '\'' +
                ", dateDeModification='" + date_de_modification + '\'' +
                ", coursId=" + course_id +
                '}';
    }
}
