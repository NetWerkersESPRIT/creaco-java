package entities;
import java.util.Date;

public class Collaborator {

    private int id;
    private String name;
    private String companyName;
    private String email;
    private String phone;
    private String address;
    private String website;
    private String domain;
    private String description;
    private String logo;
    private boolean isPublic;
    private String status;
    private Date createdAt;
    private int addedByUserId;



    public Collaborator(int id, String name, String companyName, String email,
                   String phone, String address, String website, String domain,
                   String description, String logo, boolean isPublic,
                   String status, Date createdAt, int addedByUserId) {
        this.id = id;
        this.name = name;
        this.companyName = companyName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.website = website;
        this.domain = domain;
        this.description = description;
        this.logo = logo;
        this.isPublic = isPublic;
        this.status = status;
        this.createdAt = createdAt;
        this.addedByUserId = addedByUserId;
    }

    public Collaborator(String name, String companyName, String email,
                   String phone, String address, String website, String domain,
                   String description, String logo, boolean isPublic,
                   String status, int addedByUserId) {
        this.name = name;
        this.companyName = companyName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.website = website;
        this.domain = domain;
        this.description = description;
        this.logo = logo;
        this.isPublic = isPublic;
        this.status = status;
        this.addedByUserId = addedByUserId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }

    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public int getAddedByUserId() { return addedByUserId; }
    public void setAddedByUserId(int addedByUserId) { this.addedByUserId = addedByUserId; }


    @Override
    public String toString() {
        return "Company{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", companyName='" + companyName + '\'' +
                ", email='" + email + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}

