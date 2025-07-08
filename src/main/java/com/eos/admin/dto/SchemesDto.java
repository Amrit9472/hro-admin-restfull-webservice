package com.eos.admin.dto;

import lombok.Data;
import java.util.Date;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
public class SchemesDto {
	@NotBlank(message = "Type should not be null")
	private String type;

	@NotNull(message = "Start Date Can't be null")
	private Date startDate;

	private Date endDate;

	@NotNull(message = "Amount can't be null")
	@Positive(message = "Amount can't be negative")
	private Double amount;
//    private String vendor;

	private String scheme;

	@NotEmpty
	private List<@NotBlank String> process;

	@NotBlank(message = "Details Can't be null")
	private String details;

	@NotNull(message = "Pay out days can't be null ")
	@Positive
	private Integer payoutDays;

	@NotBlank(message = "Grade can't be null")
	private String grade;
	private Long targetCandidates;
	private Boolean isDefault;
	private List<String> vendorNames;
}
