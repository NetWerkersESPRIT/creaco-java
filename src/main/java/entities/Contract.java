package entities;

import java.util.Date;
import java.math.BigDecimal;

public class Contract {

    private int id;
    private String contractNumber;
    private String title;
    private Date startDate;
    private Date endDate;
    private BigDecimal amount;
    private String pdfPath;
    private String status;
    private boolean signedByCreator;
    private boolean signedByCollaborator;
    private Date creatorSignatureDate;
    private Date collaboratorSignatureDate;
    private String terms;
    private String paymentSchedule;
    private String confidentialityClause;
    private String cancellationTerms;
    private String signatureToken;
    private Date createdAt;
    private Date sentAt;
    private int collabRequestId;
    private Integer creatorId;       // nullable → Integer
    private int collaboratorId;

    public Contract(int id, String contractNumber, String title,
                    Date startDate, Date endDate, BigDecimal amount,
                    String pdfPath, String status,
                    boolean signedByCreator, boolean signedByCollaborator,
                    Date creatorSignatureDate, Date collaboratorSignatureDate,
                    String terms, String paymentSchedule,
                    String confidentialityClause, String cancellationTerms,
                    String signatureToken, Date createdAt, Date sentAt,
                    int collabRequestId, Integer creatorId, int collaboratorId) {
        this.id = id;
        this.contractNumber = contractNumber;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.amount = amount;
        this.pdfPath = pdfPath;
        this.status = status;
        this.signedByCreator = signedByCreator;
        this.signedByCollaborator = signedByCollaborator;
        this.creatorSignatureDate = creatorSignatureDate;
        this.collaboratorSignatureDate = collaboratorSignatureDate;
        this.terms = terms;
        this.paymentSchedule = paymentSchedule;
        this.confidentialityClause = confidentialityClause;
        this.cancellationTerms = cancellationTerms;
        this.signatureToken = signatureToken;
        this.createdAt = createdAt;
        this.sentAt = sentAt;
        this.collabRequestId = collabRequestId;
        this.creatorId = creatorId;
        this.collaboratorId = collaboratorId;
    }

    public Contract(String contractNumber, String title,
                    Date startDate, Date endDate, BigDecimal amount,
                    String status, String terms, String paymentSchedule,
                    String confidentialityClause, String cancellationTerms,
                    String signatureToken, int collabRequestId,
                    Integer creatorId, int collaboratorId) {
        this.contractNumber = contractNumber;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.amount = amount;
        this.status = status;
        this.signedByCreator = false;
        this.signedByCollaborator = false;
        this.terms = terms;
        this.paymentSchedule = paymentSchedule;
        this.confidentialityClause = confidentialityClause;
        this.cancellationTerms = cancellationTerms;
        this.signatureToken = signatureToken;
        this.collabRequestId = collabRequestId;
        this.creatorId = creatorId;
        this.collaboratorId = collaboratorId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getContractNumber() { return contractNumber; }
    public void setContractNumber(String contractNumber) { this.contractNumber = contractNumber; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getPdfPath() { return pdfPath; }
    public void setPdfPath(String pdfPath) { this.pdfPath = pdfPath; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isSignedByCreator() { return signedByCreator; }
    public void setSignedByCreator(boolean signedByCreator) { this.signedByCreator = signedByCreator; }

    public boolean isSignedByCollaborator() { return signedByCollaborator; }
    public void setSignedByCollaborator(boolean signedByCollaborator) { this.signedByCollaborator = signedByCollaborator; }

    public Date getCreatorSignatureDate() { return creatorSignatureDate; }
    public void setCreatorSignatureDate(Date creatorSignatureDate) { this.creatorSignatureDate = creatorSignatureDate; }

    public Date getCollaboratorSignatureDate() { return collaboratorSignatureDate; }
    public void setCollaboratorSignatureDate(Date collaboratorSignatureDate) { this.collaboratorSignatureDate = collaboratorSignatureDate; }

    public String getTerms() { return terms; }
    public void setTerms(String terms) { this.terms = terms; }

    public String getPaymentSchedule() { return paymentSchedule; }
    public void setPaymentSchedule(String paymentSchedule) { this.paymentSchedule = paymentSchedule; }

    public String getConfidentialityClause() { return confidentialityClause; }
    public void setConfidentialityClause(String confidentialityClause) { this.confidentialityClause = confidentialityClause; }

    public String getCancellationTerms() { return cancellationTerms; }
    public void setCancellationTerms(String cancellationTerms) { this.cancellationTerms = cancellationTerms; }

    public String getSignatureToken() { return signatureToken; }
    public void setSignatureToken(String signatureToken) { this.signatureToken = signatureToken; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getSentAt() { return sentAt; }
    public void setSentAt(Date sentAt) { this.sentAt = sentAt; }

    public int getCollabRequestId() { return collabRequestId; }
    public void setCollabRequestId(int collabRequestId) { this.collabRequestId = collabRequestId; }

    public Integer getCreatorId() { return creatorId; }
    public void setCreatorId(Integer creatorId) { this.creatorId = creatorId; }

    public int getCollaboratorId() { return collaboratorId; }
    public void setCollaboratorId(int collaboratorId) { this.collaboratorId = collaboratorId; }

    @Override
    public String toString() {
        return "Contract{" +
                "id=" + id +
                ", contractNumber='" + contractNumber + '\'' +
                ", title='" + title + '\'' +
                ", status='" + status + '\'' +
                ", amount=" + amount +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", signedByCreator=" + signedByCreator +
                ", signedByCollaborator=" + signedByCollaborator +
                ", collabRequestId=" + collabRequestId +
                '}';
    }
}
