package entities;

public class Ressource {
    private int id;
    private String nom;
    private String url;
    private String type;
    private String contenu;
    private String dateDeCreation;
    private String dateDeModification;
    private int coursId;

    public Ressource() {}

    public Ressource(String nom, String url, String type, String contenu, String dateDeCreation, int coursId) {
        this.nom = nom;
        this.url = url;
        this.type = type;
        this.contenu = contenu;
        this.dateDeCreation = dateDeCreation;
        this.coursId = coursId;
    }

    public Ressource(int id, String nom, String url, String type, String contenu, String dateDeCreation, String dateDeModification, int coursId) {
        this.id = id;
        this.nom = nom;
        this.url = url;
        this.type = type;
        this.contenu = contenu;
        this.dateDeCreation = dateDeCreation;
        this.dateDeModification = dateDeModification;
        this.coursId = coursId;
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

    public String getDateDeModification() {
        return dateDeModification;
    }

    public void setDateDeModification(String dateDeModification) {
        this.dateDeModification = dateDeModification;
    }

    public int getCoursId() {
        return coursId;
    }

    public void setCoursId(int coursId) {
        this.coursId = coursId;
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
                ", dateDeModification='" + dateDeModification + '\'' +
                ", coursId=" + coursId +
                '}';
    }
}
