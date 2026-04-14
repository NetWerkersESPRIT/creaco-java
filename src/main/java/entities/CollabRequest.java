package entities;
import java.util.Date;
import java.math.BigDecimal;

public class CollabRequest {

        private int id;
        private String title;
        private String description;
        private BigDecimal budget;
        private Date startDate;
        private Date endDate;
        private String status;
        private String rejectionReason;
        private String deliverables;
        private String paymentTerms;
        private Date createdAt;
        private Date updatedAt;
        private Date respondedAt;
        private int creatorId;
        private int revisorId;
        private int collaboratorId;

        public CollabRequest(int id, String title, String description, BigDecimal budget,
                             Date startDate, Date endDate, String status,
                             String rejectionReason, String deliverables, String paymentTerms,
                             Date createdAt, Date updatedAt, Date respondedAt,
                             int creatorId, int revisorId, int collaboratorId) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.budget = budget;
            this.startDate = startDate;
            this.endDate = endDate;
            this.status = status;
            this.rejectionReason = rejectionReason;
            this.deliverables = deliverables;
            this.paymentTerms = paymentTerms;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
            this.respondedAt = respondedAt;
            this.creatorId = creatorId;
            this.revisorId = revisorId;
            this.collaboratorId = collaboratorId;
        }


        public CollabRequest(String title, String description, BigDecimal budget,
                             Date startDate, Date endDate, String status,
                             String deliverables, String paymentTerms,
                             int collaboratorId) {
            this.title = title;
            this.description = description;
            this.budget = budget;
            this.startDate = startDate;
            this.endDate = endDate;
            this.status = status;
            this.deliverables = deliverables;
            this.paymentTerms = paymentTerms;
            this.collaboratorId = collaboratorId;
        }


        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public BigDecimal getBudget() { return budget; }
        public void setBudget(BigDecimal budget) { this.budget = budget; }

        public Date getStartDate() { return startDate; }
        public void setStartDate(Date startDate) { this.startDate = startDate; }

        public Date getEndDate() { return endDate; }
        public void setEndDate(Date endDate) { this.endDate = endDate; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getRejectionReason() { return rejectionReason; }
        public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

        public String getDeliverables() { return deliverables; }
        public void setDeliverables(String deliverables) { this.deliverables = deliverables; }

        public String getPaymentTerms() { return paymentTerms; }
        public void setPaymentTerms(String paymentTerms) { this.paymentTerms = paymentTerms; }

        public Date getCreatedAt() { return createdAt; }
        public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

        public Date getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

        public Date getRespondedAt() { return respondedAt; }
        public void setRespondedAt(Date respondedAt) { this.respondedAt = respondedAt; }

        public int getCreatorId() { return creatorId; }
        public void setCreatorId(int creatorId) { this.creatorId = creatorId; }

        public int getRevisorId() { return revisorId; }
        public void setRevisorId(int revisorId) { this.revisorId = revisorId; }

        public int getCollaboratorId() { return collaboratorId; }
        public void setCollaboratorId(int collaboratorId) { this.collaboratorId = collaboratorId; }

        @Override
        public String toString() {
            return "CollabRequest{" +
                    "id=" + id +
                    ", title='" + title + '\'' +
                    ", status='" + status + '\'' +
                    ", budget=" + budget +
                    ", startDate=" + startDate +
                    ", endDate=" + endDate +
                    ", collaboratorId=" + collaboratorId +
                    '}';
        }
}
