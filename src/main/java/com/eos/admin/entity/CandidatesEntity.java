package com.eos.admin.entity;

import java.time.LocalDateTime;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Data
public class CandidatesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String candiName;
    private String candiMobile;
    
    @Column(name = "candi_email")
    private String candiEmail;
    
    private LocalDateTime submittedDate;
    
    @Column(name = "interviewAttendingDate")
    private Date interviewAttendingDate;
    
    @PrePersist
    protected void onCreate() {
        submittedDate = LocalDateTime.now();
    }
    
    @Column(name = "vendor_email")
    private String vendorEmail;
    
    @Column(name = "scheme")
    private String scheme;
    
    @Column(name = "venId")
    private String venId;
    
//    @Column(name = "vendorId")
//    private String vendorId;
    
}
