package com.eos.admin.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import com.eos.admin.dto.EmployeeDetailsOnManagerPageDTO;
import com.eos.admin.dto.EmployeeDto;
import com.eos.admin.dto.EmployeeExcelReportDto;
import com.eos.admin.dto.EmployeeExcelReportInSequenceDto;
import com.eos.admin.dto.EmployeeInformationDTO;
import com.eos.admin.dto.EmployeeStatusHistroyDTO;
import com.eos.admin.dto.ManagerPageResponseDTO;
import com.eos.admin.dto.ProfileScreanRejectedDTO;
import com.eos.admin.dto.ProfileScreaningResponseDto;
import com.eos.admin.dto.RejectPageEmployeeDTO;
import com.eos.admin.dto.ScheduleInterviewPageRequestDTO;
import com.eos.admin.dto.SelectedEmployeeDTO;
import com.eos.admin.dto.StatusRequestDTO;
import com.eos.admin.enums.RemarksType;

import jakarta.servlet.http.HttpServletResponse;

public interface EmployeeService {

	EmployeeDto createEmployee(EmployeeDto employeeDto, List<MultipartFile> images, String path) throws IOException;

	boolean checkDuplicateEmail(String email);

	boolean checkDuplicateAdhaarNo(String aadharNumber);

	List<ProfileScreaningResponseDto> getListOfEmployeesOnProfileScreanig(String location ,String branch);

	void updateRemarks(Long employeeId, StatusRequestDTO statusRequestDTO, RemarksType remarksType);

	List<EmployeeStatusHistroyDTO> getListOfStatusHistoryRecords(Long employeeId);

	List<ScheduleInterviewPageRequestDTO> getListOfEmployeesOnScheduleInterviewPage(String location ,String branch);

	List<RejectPageEmployeeDTO> getListOfEmployeeRejectedByManager(String location);

	void assignInterviewProcessFromRejectPage(Long employeeId, StatusRequestDTO statusRequestDTO);

	List<SelectedEmployeeDTO> getAllSelectedInterviewList(String location , String branch);

	List<ProfileScreanRejectedDTO> getListOfProfileScreaningRejected(String location ,String branch);

	List<EmployeeInformationDTO> getEmployeeInformation();

	List<EmployeeExcelReportDto> getEmployeesDumpReportData(LocalDate startDate, LocalDate endDate);

	ByteArrayOutputStream exportToRawExcel(List<EmployeeExcelReportDto> response) throws IOException;

	List<Object[]> getEmployeesDumpReportData(Date startDate, Date endDate);

	void exportToExcel(List<EmployeeExcelReportInSequenceDto> data, HttpServletResponse response) throws IOException;

	List<ManagerPageResponseDTO> getScheduleInterviewManagerPage(String uniqueCodeProcess);


	List<EmployeeDetailsOnManagerPageDTO> getAllResponseValueOnProcessType(String role, String location ,String branch);

	EmployeeDto submitResponseForReScreeningProfile(Long employeeId, StatusRequestDTO statusRequestDTO);
	
	String getVendorNameByEmployeeEmail(String empEmail);
	
	Long hasScreeningRemarks(String email);
}
