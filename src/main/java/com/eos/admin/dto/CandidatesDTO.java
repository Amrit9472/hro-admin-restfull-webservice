package com.eos.admin.dto;

import java.time.LocalDateTime;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CandidatesDTO {

	private String candiName;
	private String candiMobile;
	private String candiEmail;

	private LocalDateTime submittedDate;
	private Date interviewAttendingDate;
	private String vendorEmail;
//	private String vendorId;

	private String venId;
	private String scheme;

	private String hrStatus;
	private Long appearedForInterview;
	private boolean loiIssued;

}
