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

    public Ressource(int id, String nom, String url, String type, String contenu, String date_de_creation, String date_de_modification, int cours_id) {
        this.id = id;
        this.nom = nom;
        this.url = url;
        this.type = type;
        this.contenu = contenu;
        this.date_de_creation = date_de_creation;
        this.date_de_modification = date_de_modification;
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

    public int getCourseId() {
        return cours_id;
    }

    public void setCourseId(int cours_id) {
        this.cours_id = cours_id;
    }

    @Override
    public String toString() {
        return "Ressource{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", url='" + url + '\'' +
                ", type='" + type + '\'' +
                ", contenu='" + contenu + '\'' +
                ", dateDeCreation='" + date_de_creation + '\'' +
                ", dateDeModification='" + date_de_modification + '\'' +
                ", coursId=" + cours_id +
                '}';
    }
}