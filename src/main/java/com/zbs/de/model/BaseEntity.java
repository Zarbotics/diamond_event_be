package com.zbs.de.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import com.zbs.de.util.UtilDateAndTime;

@MappedSuperclass
public class BaseEntity {

    @CreatedDate
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date", updatable = false)
    protected Date createdDate;

    @LastModifiedDate
    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_date")
    protected Date updatedDate;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    protected Integer createdBy = 0;

    @LastModifiedBy
    @Column(name = "updated_by")
    protected Integer updatedBy = 0;

    @Column(name = "bln_is_deleted")
    protected Boolean blnIsDeleted;

    @Column(name = "bln_is_active")
    protected Boolean blnIsActive;

    @Column(name = "bln_is_approved")
    protected Boolean blnIsApproved;

    @PrePersist
    protected void prePersist() {
        if (createdBy == null) {
            createdBy = 0;
        }
        if (updatedBy == null || updatedBy == 0) {
            updatedBy = createdBy;
        }
    }

    public BaseEntity() {
        Date now = UtilDateAndTime.getCurrentDate();
        this.createdDate = now;
        this.updatedDate = now;
        this.blnIsDeleted = false;
        this.blnIsActive = true;
        this.blnIsApproved = false;
    }

    public BaseEntity(Date createdDate, Date updatedDate) {
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
        this.blnIsDeleted = false;
        this.blnIsActive = true;
        this.blnIsApproved = false;
    }

    // Getters and Setters

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Integer getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Integer updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public Boolean getBlnIsDeleted() {
        return blnIsDeleted;
    }

    public void setBlnIsDeleted(Boolean blnIsDeleted) {
        this.blnIsDeleted = blnIsDeleted;
    }

    public Boolean getBlnIsActive() {
        return blnIsActive;
    }

    public void setBlnIsActive(Boolean blnIsActive) {
        this.blnIsActive = blnIsActive;
    }

    public Boolean getBlnIsApproved() {
        return blnIsApproved;
    }

    public void setBlnIsApproved(Boolean blnIsApproved) {
        this.blnIsApproved = blnIsApproved;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((createdDate == null) ? 0 : createdDate.hashCode());
        result = prime * result + ((blnIsDeleted == null) ? 0 : blnIsDeleted.hashCode());
        result = prime * result + ((updatedDate == null) ? 0 : updatedDate.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BaseEntity other = (BaseEntity) obj;
        return (createdDate != null && createdDate.equals(other.createdDate)) &&
               (blnIsDeleted != null && blnIsDeleted.equals(other.blnIsDeleted)) &&
               (updatedDate != null && updatedDate.equals(other.updatedDate));
    }

    @Override
    public String toString() {
        return "BaseEntity [createdDate=" + createdDate + ", updatedDate=" + updatedDate +
               ", createdBy=" + createdBy + ", updatedBy=" + updatedBy +
               ", blnIsDeleted=" + blnIsDeleted + ", blnIsActive=" + blnIsActive +
               ", blnIsApproved=" + blnIsApproved + "]";
    }
}
