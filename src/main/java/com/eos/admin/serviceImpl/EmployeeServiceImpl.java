package com.eos.admin.serviceImpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
import com.eos.admin.dto.StatusHistoryDTO;
import com.eos.admin.dto.StatusHistoryExcelDto;
import com.eos.admin.dto.StatusRequestDTO;
import com.eos.admin.entity.Employee;
import com.eos.admin.entity.EmployeeStatusDetails;
import com.eos.admin.entity.InterviewProcesses;
import com.eos.admin.entity.Language;
import com.eos.admin.entity.StatusHistory;
import com.eos.admin.enums.RemarksType;
import com.eos.admin.exception.DuplicateRecordException;
import com.eos.admin.exception.EntityNotFoundException;
import com.eos.admin.exception.ResourceNotFoundException;
import com.eos.admin.repository.EmployeeRepository;
import com.eos.admin.repository.EmployeeStatusDetailsRepository;
import com.eos.admin.repository.InterviewProcessRepository;
import com.eos.admin.repository.LanguagesRepository;
import com.eos.admin.repository.StatusHistoryRepository;
import com.eos.admin.service.EmailService;
import com.eos.admin.service.EmployeeService;
import com.eos.admin.service.FileService;
import com.eos.admin.service.StatusHistoryService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

	private EmployeeRepository employeeRepository;
	private FileService fileSercice;
	private StatusHistoryService statusHistoryService;
	private StatusHistoryRepository statusHistoryRepository;
	private NotificationServiceImple notificationServiceImple;
	private ModelMapper modelMapper;
	private EmployeeStatusDetailsRepository employeeStatusDetailsRepository;
	private InterviewProcessRepository interviewProcessRepository;
	private LanguagesServiceImpl languagesServiceImpl;
	private LanguagesRepository languagesRepository;
	private OurUserDetailsServiceImpl ourUserDetailsServiceImpl;
	private EmailService emailService;

	@Autowired
	public EmployeeServiceImpl(EmployeeRepository employeeRepository, FileService fileSercice,
			StatusHistoryService statusHistoryService, StatusHistoryRepository statusHistoryRepository,
			NotificationServiceImple notificationServiceImple, ModelMapper modelMapper,
			EmployeeStatusDetailsRepository employeeStatusDetailsRepository,
			InterviewProcessRepository interviewProcessRepository, LanguagesServiceImpl languagesServiceImpl,
			LanguagesRepository languagesRepository,OurUserDetailsServiceImpl ourUserDetailsServiceImpl,EmailService emailService) {
		super();
		this.employeeRepository = employeeRepository;
		this.fileSercice = fileSercice;
		this.statusHistoryService = statusHistoryService;
		this.statusHistoryRepository = statusHistoryRepository;
		this.notificationServiceImple = notificationServiceImple;
		this.modelMapper = modelMapper;
		this.employeeStatusDetailsRepository = employeeStatusDetailsRepository;
		this.interviewProcessRepository = interviewProcessRepository;
		this.languagesServiceImpl = languagesServiceImpl;
		this.languagesRepository = languagesRepository;
		this.ourUserDetailsServiceImpl = ourUserDetailsServiceImpl;
		this.emailService = emailService;
	}

	@Override
	@Transactional
	public EmployeeDto createEmployee(EmployeeDto employeeDto, List<MultipartFile> file, String path)
			throws IOException {
		// Check for duplicate email
		if (checkDuplicateEmail(employeeDto.getEmail())) {
			log.info("Starting employee creation process for: {}", employeeDto.getFullName());
			throw new DuplicateRecordException("Email Already Exists");
		}
		// Check for duplicate Aadhaar number
		if (checkDuplicateAdhaarNo(employeeDto.getAadhaarNumber())) {
			log.error("Duplicate Aadhaar number detected for: {}", employeeDto.getAadhaarNumber());
			throw new DuplicateRecordException("Aadhaar Number Exists ");
		}
		// Upload the file
		log.info("Uploading image for Aadhaar number: {}", employeeDto.getAadhaarNumber());

		List<String> fileName = fileSercice.uploadImage(path, file, employeeDto.getAadhaarNumber());
//		employeeDto.setAadharFilename(fileName);
		log.info("File uploaded successfully with name: {}", fileName);

		// Format and set the full name
		String formattedName = capitalizeStringAfterSpacing(employeeDto.getFullName());
		employeeDto.setFullName(formattedName);
		log.info("Formatted full name: {}", formattedName);

		// Map DTO to entity and save
		Employee employeeEntity = modelMapper.map(employeeDto, Employee.class);

		// **Crucial Step: Set the employee in each Language object**
		if (employeeEntity.getLanguage() != null) {
			for (Language language : employeeEntity.getLanguage()) {
				language.setEmployee(employeeEntity);
			}
		}

		Employee savedEmployeeEntity = employeeRepository.save(employeeEntity);
		log.info("Employee entity saved with ID: {}", savedEmployeeEntity.getId());

		// Create initial status and update status
		statusHistoryService.createInitialStatus(savedEmployeeEntity);
//		updateEmployeeStatus(savedEmployeeEntity);
		log.info("Initial status and employee status updated for employee ID: {}", savedEmployeeEntity.getId());

		// Create EmployeeStatusDetails if needed
		createEmployeeStatusDetails(savedEmployeeEntity);

		// Notify admin
		notificationServiceImple.notifyAdminNewEmployee(savedEmployeeEntity.getId(), savedEmployeeEntity.getFullName());
		log.info("Admin notified about new employee with ID: {}", savedEmployeeEntity.getId());

		// Return the saved employee as DTO
		EmployeeDto result = modelMapper.map(savedEmployeeEntity, EmployeeDto.class);
		log.info("Employee creation process completed successfully for: {}", result.getFullName());

		return result;
	}

	@Override
	public List<ProfileScreaningResponseDto> getListOfEmployeesOnProfileScreanig(String location , String branch) {
		log.info("Entering getListOfEmployeesOnProfileScreaning method.");
		List<ProfileScreaningResponseDto> employees = new ArrayList<>();
		try {
			List<Object[]> response = employeeRepository.getListOfEmployeeOnProfileScreening(location.trim(),branch.trim());
			if (response == null || response.isEmpty()) {
				log.warn("No employee data found for profile screening.");
				return employees;
			}
			log.info("Successfully fetched employee data from the repository.");
			for (Object[] result : response) {
				if (result == null || result.length != 9) {
					log.warn("Invalid data encountered: skipping incomplete record.");
					continue; // Skip incomplete records
				}

				try {
					ProfileScreaningResponseDto employee = new ProfileScreaningResponseDto();
					employee.setId((Long) result[0]);
					employee.setFullName((String) result[1]);
					employee.setEmail((String) result[2]);
					employee.setJobProfile((String) result[3]);
					employee.setMobileNo((Long) result[4]);
					employee.setPermanentAddress((String) result[5]);
					employee.setGender((String) result[6]);
					employee.setCreationDate((Date) result[7]);
					String languagesString = (String) result[8];
					if (languagesString != null && !languagesString.isEmpty()) {
						List<String> languages = Arrays.asList(languagesString.split(", "));
						employee.setLanguages(languages); // Set the languages to the employee DTO
					} else {
						employee.setLanguages(new ArrayList<>()); // No languages available
					}
					employees.add(employee);
				} catch (NullPointerException e) {
					log.error("Null value encountered while processing the employee data: " + e.getMessage());
					continue; // Skip this record and move to the next
				} catch (ClassCastException e) {
					log.error("Data type mismatch while processing employee record: " + e.getMessage());
					continue; // Skip this record and move to the next
				}
			}
			log.info("Successfully processed employee data.");

			// Sorting by creationDate in descending order
			return employees.stream().sorted((e1, e2) -> e2.getCreationDate().compareTo(e1.getCreationDate())) // Sort
																												// in
																												// reverse
																												// order
					.collect(Collectors.toList());
		} catch (Exception e) {
			log.error("An error occurred while fetching or processing the employee data: ", e);
			throw new RuntimeException("Error fetching employees for profile screening", e);
		}

	}

	@Override
	public void updateRemarks(Long employeeId, StatusRequestDTO statusRequestDTO, RemarksType remarksType) {

		try {
			log.info("Updating remarks for employee ID: {} with remarks type: {}", employeeId, remarksType);

			Employee searchEmployee = employeeRepository.findById(employeeId)
					.orElseThrow(() -> new ResourceNotFoundException("Employee is not Present in Records"));
			log.info("Employee found: {}", searchEmployee);

			EmployeeStatusDetails employeeStatusDetails = searchEmployee.getEmployeeStatusDetails();
			switch (remarksType) {
			case PROFILE: {
				// Check if EmployeeStatusDetails already exists
				log.info("Updating profile screen remarks for employee ID: {}", employeeId);

				if (employeeStatusDetails == null) {

					log.info("No existing EmployeeStatusDetails found for employee ID: {}, creating a new one.",
							employeeId);
					employeeStatusDetails = new EmployeeStatusDetails();
					employeeStatusDetails.setEmployee(searchEmployee); // Set the employee reference

				}

				// Update the profile screen remarks
				log.info("Setting profile screen remarks: {} and HR status: {} for employee ID: {}",
						statusRequestDTO.getRemarks(), statusRequestDTO.getNewStatus(), employeeId);

				employeeStatusDetails.setProfileScreenRemarks(statusRequestDTO.getRemarks());
				employeeStatusDetails.setHrStatus(statusRequestDTO.getNewStatus());

				// Save the updated or new EmployeeStatusDetails back to the repository
				employeeStatusDetailsRepository.save(employeeStatusDetails);
				log.info("EmployeeStatusDetails saved for employee ID: {}", employeeId);

				statusHistoryService.trackStatusChange(statusRequestDTO, employeeId);
				log.info("Status change tracked for employee ID: {}", employeeId);
				break; // Exit the switch case

			}
			case SCHEDULE: {
				if (employeeStatusDetails == null) {
					log.info("No existing EmployeeStatusDetails found for employee ID : {}", employeeId);
					employeeStatusDetails = new EmployeeStatusDetails();
					employeeStatusDetails.setEmployee(searchEmployee);
				}
				log.info("Setting Schedule Interview remarks : {} and  status: {} for employee ID: {} ",
						statusRequestDTO.getRemarks(), statusRequestDTO.getNewStatus(), employeeId);
				employeeStatusDetails.setRemarksByHr(statusRequestDTO.getRemarks());
				employeeStatusDetails.setProcessesStatus(statusRequestDTO.getProcessName());
				if(statusRequestDTO.getProcessName() != null && !statusRequestDTO.getProcessName().isEmpty()) {
				 List<String> emailList = ourUserDetailsServiceImpl.findEmailByProceeName(statusRequestDTO.getProcessName());
				 log.info("Email list to send interview schedule: {}", emailList);
               
//				 emailService.sendScheduleInterviewEmailToManager(emailList , employeeStatusDetails, statusRequestDTO);
				}
				
				employeeStatusDetails.setLastInterviewAssign(statusRequestDTO.getProcessName());
				employeeStatusDetails.setGrade(statusRequestDTO.getGrade());
				employeeStatusDetails.setCompanyType(statusRequestDTO.getCompanyType());
				employeeStatusDetails.setDepartment(statusRequestDTO.getDepartment());
//				employeeStatusDetails.set
				employeeStatusDetailsRepository.save(employeeStatusDetails);
				log.info("EmployeeStatusDetails saved successfully for employee ID: {}", employeeId);

				if (statusRequestDTO.getJobProfile() != null) {
					log.info("Updating job profile to '{}' for employee ID: {}", statusRequestDTO.getJobProfile(),
							employeeId);
					searchEmployee.setJobProfile(statusRequestDTO.getJobProfile());
					employeeRepository.save(searchEmployee);
					log.info("Employee job profile updated for employee ID: {}", employeeId);
				} else {
					log.warn("Job profile is null for employee ID: {}. Skipping job profile update.", employeeId);

				}
				InterviewProcesses interviewProcesses = new InterviewProcesses();
				interviewProcesses.setEmployee(searchEmployee);
				interviewProcessRepository.save(interviewProcesses);
				log.info("InterviewProcesses record created for employee ID: {}", employeeId);

				statusHistoryService.trackStatusChange(statusRequestDTO, employeeId);
				log.info("Status change tracked for employee ID: {}", employeeId);
				break;
			}
			case MANAGER: {
				log.info("Setting manager remarks: {} and manager status: {} for employee ID: {}",
						statusRequestDTO.getRemarks(), statusRequestDTO.getNewStatus(), employeeId);

				if (employeeStatusDetails == null) {
					log.info("No existing EmployeeStatusDetails found for employee ID : {}", employeeId);
					employeeStatusDetails = new EmployeeStatusDetails();
					employeeStatusDetails.setEmployee(searchEmployee);
				}

				employeeStatusDetails.setRemarksByManager(statusRequestDTO.getRemarks()); // Assuming you have this
																							// field
				employeeStatusDetails.setManagerStatus(statusRequestDTO.getNewStatus());
				employeeStatusDetails.setProcessesStatus(statusRequestDTO.getNewStatus());// Assuming you have this
																							// field
				employeeStatusDetails.setClientRound(statusRequestDTO.getClientRound());

				statusHistoryService.trackStatusChange(statusRequestDTO, employeeId);
				log.info("Status change tracked for employee ID: {}", employeeId);
				break;

			}

			default:
				// Log the case of an unexpected remarks type
				log.error("Unexpected remarks type: {}", remarksType);
				throw new IllegalArgumentException("Unexpected value: " + remarksType);
			}
		} catch (ResourceNotFoundException e) {
			// Log the error if employee is not found
			log.error("Employee not found with ID: {}", employeeId, e);
			throw e; // Re-throw the exception after logging
		} catch (IllegalArgumentException e) {
			// Log the error if there's an invalid remarks type
			log.error("Invalid remarks type for employee ID: {}", employeeId, e);
			throw e; // Re-throw the exception after logging
		} catch (Exception e) {
			// Log any unexpected errors
			log.error("An unexpected error occurred while updating remarks for employee ID: {}", employeeId, e);
			throw new RuntimeException("An unexpected error occurred", e); // Re-throw as a runtime exception
		}

	}

	@Override
	public List<EmployeeStatusHistroyDTO> getListOfStatusHistoryRecords(Long employeeId) {
		log.info("Fetching status history records for employee ID: {}", employeeId);

		try {
			List<Object[]> response = employeeRepository.getListOfStatusHistoryRecordsOfEmployee(employeeId);

			if (response.isEmpty()) {
				log.warn("No status history records found for employee ID: {}", employeeId);
			}

			Map<Long, EmployeeStatusHistroyDTO> employeeMap = new HashMap<>();

			// Iterate through the response and process each row
			for (Object[] row : response) {
				Long employeeIdFromDb = (Long) row[0];

				// If the employee is not already in the map, create a new DTO
				if (!employeeMap.containsKey(employeeIdFromDb)) {
					EmployeeStatusHistroyDTO employeeDto = new EmployeeStatusHistroyDTO();
					employeeDto.setId(employeeIdFromDb);
					employeeDto.setFullName((String) row[1]);
					employeeDto.setAadhaarNumber((String) row[2]);
					employeeDto.setEmail((String) row[3]);
					employeeDto.setCreationDate((Date) row[4]);
					employeeDto.setStatusHistory(new ArrayList<>()); // Initialize status history list
					employeeMap.put(employeeIdFromDb, employeeDto);
					log.debug("Created new EmployeeStatusHistroyDTO for employee ID: {}", employeeIdFromDb);
				}

				// Create a new StatusHistory object from the current row
				StatusHistory statusHistory = new StatusHistory();
				statusHistory.setStatus((String) row[5]);
				statusHistory.setChangesDateTime((Date) row[6]);
				statusHistory.setHrName((String) row[7]);
				statusHistory.setRemarksOnEveryStages((String) row[8]);
				StatusHistoryDTO statusHistoryDTO = modelMapper.map(statusHistory, StatusHistoryDTO.class);

				// Add the status history to the employee's status history list
				employeeMap.get(employeeIdFromDb).getStatusHistory().add(statusHistoryDTO);
				log.debug("Added status history for employee ID: {} with status: {}", employeeIdFromDb,
						statusHistory.getStatus());

			}

			// Return the list of EmployeeStatusHistroyDTO values from the map
			log.info("Successfully processed status history records for employee ID: {}", employeeId);
			return new ArrayList<>(employeeMap.values());
		} catch (Exception e) {
			// Log the exception with detailed information
			log.error("Error occurred while fetching or processing status history records for employee ID {}: {}",
					employeeId, e.getMessage(), e);
			throw new RuntimeException("Error occurred while fetching status history records", e);
		}
	}

	@Override
	public List<ScheduleInterviewPageRequestDTO> getListOfEmployeesOnScheduleInterviewPage(String location, String branch) {
		// TODO Auto-generated method stub
		log.info("Request for get list of Employee on schedule Interview page from Empoloyee serviceImp");
		try {
			List<Object[]> repositoryResponse = employeeRepository.getListOfEmployeeSchedulePage(location, branch);
			List<ScheduleInterviewPageRequestDTO> response = new ArrayList<>();
			for (Object[] repositoryResponses : repositoryResponse) {
				ScheduleInterviewPageRequestDTO scheduleInterviewPageRequestDTO = new ScheduleInterviewPageRequestDTO();
				scheduleInterviewPageRequestDTO.setId((Long) repositoryResponses[0]);
				scheduleInterviewPageRequestDTO.setFullName((String) repositoryResponses[1]);
				scheduleInterviewPageRequestDTO.setEmail((String) repositoryResponses[2]);
				scheduleInterviewPageRequestDTO.setGender((String) repositoryResponses[3]);
				scheduleInterviewPageRequestDTO.setMobileNo((Long) repositoryResponses[4]);
				scheduleInterviewPageRequestDTO.setCreationDate((Date) repositoryResponses[5]);
				response.add(scheduleInterviewPageRequestDTO);
			}
			log.info("Successfully fetched {} employees for schedule interview page", response.size());
			return response.stream()
					.sorted((e1, e2) -> Long.compare(e2.getCreationDate().getTime(), e1.getCreationDate().getTime()))
					.collect(Collectors.toList());
		} catch (Exception e) {
			log.error("Error while fetching employees for schedule interview page: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to fetch schedule interview page data", e);
		}

	}

	@Override
	public List<RejectPageEmployeeDTO> getListOfEmployeeRejectedByManager(String location) {
		try {
			List<Object[]> respositoryResponse = employeeRepository.getListOfEmployeeRejectByProcessManager(location);
			List<RejectPageEmployeeDTO> proceesedResponse = new ArrayList<>();
			for (Object[] result : respositoryResponse) {
				RejectPageEmployeeDTO rejectPageEmployeeDTO = new RejectPageEmployeeDTO();
				rejectPageEmployeeDTO.setId((Long) result[0]);
				rejectPageEmployeeDTO.setFullName((String) result[1]);
				rejectPageEmployeeDTO.setEmail((String) result[2]);
				rejectPageEmployeeDTO.setJobProfile((String) result[3]);
				rejectPageEmployeeDTO.setCreationDate((Date) result[4]);

				proceesedResponse.add(rejectPageEmployeeDTO);
			}
			log.info("Successfully fetched {} employees for reject page ", proceesedResponse.size());
			return proceesedResponse.stream()
					.sorted((e1, e2) -> Long.compare(e2.getCreationDate().getTime(), e1.getCreationDate().getTime()))
					.collect(Collectors.toList());
		} catch (Exception e) {
			// TODO: handle exception
			log.error("Error while fetching employees for reject page: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to fetch rejected interview page data", e);
		}

	}

	@Override
	public void assignInterviewProcessFromRejectPage(Long employeeId, StatusRequestDTO statusRequestDTO) {

		EmployeeStatusDetails employeeStatusDetails = employeeStatusDetailsRepository.findByEmployeeId(employeeId)
				.orElseThrow(() -> new RuntimeException("EmployeeStatusDetails not found"));

		// Get the associated employee
		Employee employee = employeeStatusDetails.getEmployee();

		// Create and associate InterviewProcesses
		InterviewProcesses interviewProcesses = new InterviewProcesses();
		interviewProcesses.setEmployee(employee);

		// Update and save the employee status
		employeeStatusDetails.setProcessesStatus(statusRequestDTO.getProcessName());
		employeeStatusDetails.setLastInterviewAssign(statusRequestDTO.getProcessName());
		employeeStatusDetailsRepository.save(employeeStatusDetails);

		// Save interview process
		InterviewProcesses savedInterviewProcess = interviewProcessRepository.save(interviewProcesses);

		// Set status history
		setStatusHistoryRecoredRemarksChecks(employeeId, statusRequestDTO, savedInterviewProcess);
	}

	@Override
	public List<SelectedEmployeeDTO> getAllSelectedInterviewList(String location,String branch) {
		try {
			log.info("Getting selected employee details from repository...");
			List<Object[]> employeeObjects = employeeRepository.getSelectedEmployeeDetails(location,branch);

			List<SelectedEmployeeDTO> selectedEmployees = new ArrayList<>();

			for (Object[] result : employeeObjects) {
				SelectedEmployeeDTO selectedEmployee = new SelectedEmployeeDTO();

				try {
					selectedEmployee.setId((Long) result[0]);
					selectedEmployee.setFullName((String) result[1]);
					selectedEmployee.setEmail((String) result[2]);
					selectedEmployee.setMobileNo((Long) result[3]);
					selectedEmployee.setGender((String) result[4]);
					selectedEmployee.setCreationDate((Date) result[5]);
					selectedEmployee.setProfileScreenRemarks((String) result[9]);
					selectedEmployee.setRemarksByHr((String) result[7]);
					selectedEmployee.setRemarksByManager((String) result[8]);
					selectedEmployee.setJobProfile((String) result[6]);
					selectedEmployee.setGrade((String) result[10]);
					selectedEmployee.setCompanyType((String) result[11]);
					selectedEmployee.setDepartment((String) result[12]);
					selectedEmployee.setLastInterviewAssign((String) result[13]);
				} catch (ClassCastException | ArrayIndexOutOfBoundsException castEx) {
					log.error("Error mapping row to DTO. Row: {}, Error: {}", Arrays.toString(result),
							castEx.getMessage(), castEx);
					continue; // skip this row but continue processing others
				}

				selectedEmployees.add(selectedEmployee);
			}

			log.info("Total selected employees processed: {}", selectedEmployees.size());

			// Sort by creationDate descending
			return selectedEmployees.stream()
					.sorted((e1, e2) -> Long.compare(e2.getCreationDate().getTime(), e1.getCreationDate().getTime()))
					.collect(Collectors.toList());

		} catch (Exception e) {
			log.error("Exception occurred in getAllSelectedInterviewList: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to fetch selected employee interview data", e);
		}
	}

	@Override
	public List<ProfileScreanRejectedDTO> getListOfProfileScreaningRejected(String location,String branch) {
		try {
			log.info("Fetching profile screening rejected employee data from repository...");
			List<Object[]> repoResponse = employeeRepository.getListOfProfileScreaningPage(location,branch);
			List<ProfileScreanRejectedDTO> rejectedList = new ArrayList<>();

			for (Object[] result : repoResponse) {
				ProfileScreanRejectedDTO dto = new ProfileScreanRejectedDTO();

				try {
					dto.setId((Long) result[0]);
					dto.setFullName((String) result[1]);
					dto.setEmail((String) result[2]);
					dto.setGender((String) result[3]);
					dto.setMobileNo((Long) result[4]);
					dto.setCreationDate((Date) result[5]);
					dto.setProfileScreenRemarks((String) result[6]);
				} catch (ClassCastException | ArrayIndexOutOfBoundsException ex) {
					log.error("Error mapping profile screen rejected data. Row: {}, Error: {}", Arrays.toString(result),
							ex.getMessage(), ex);
					continue;
				}

				rejectedList.add(dto);
			}

			log.info("Total profile screening rejected employees processed: {}", rejectedList.size());

			// Sort by creationDate in descending order, if present
			return rejectedList.stream()
					.sorted((e1, e2) -> Long.compare(e2.getCreationDate() != null ? e2.getCreationDate().getTime() : 0,
							e1.getCreationDate() != null ? e1.getCreationDate().getTime() : 0))
					.collect(Collectors.toList());

		} catch (Exception e) {
			log.error("Exception occurred in getListOfProfileScreaningRejected: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to fetch profile screening rejected employee data", e);
		}
	}

	@Override
	public List<EmployeeInformationDTO> getEmployeeInformation() {
		List<EmployeeInformationDTO> result = new ArrayList<>();

		try {
			List<Object[]> repositoryResponse = employeeRepository.getRegisterEmployeeInformation();

			for (Object[] row : repositoryResponse) {
				EmployeeInformationDTO employeeInformationDTO = new EmployeeInformationDTO();

				employeeInformationDTO.setId((Long) row[0]);
				employeeInformationDTO.setFullName((String) row[1]);
				employeeInformationDTO.setEmail((String) row[2]);
				employeeInformationDTO.setInitialStatus((String) row[3]);
				employeeInformationDTO.setCreationDate((Date) row[4]);
				employeeInformationDTO.setHrStatus((String) row[5]);
				employeeInformationDTO.setManagerStatus((String) row[6]);
				employeeInformationDTO.setLastInterviewAssign((String) row[7]);
				employeeInformationDTO.setRemarksByHr((String) row[8]);
				employeeInformationDTO.setRemarksByManager((String) row[9]);
				employeeInformationDTO.setProfileScreenRemarks((String) row[10]);

				result.add(employeeInformationDTO);
			}

			log.info("Successfully fetched {} employee records", result.size());

		} catch (Exception e) {
			log.error("Error while fetching employee information", e);
			// Optionally rethrow or handle differently
		}

		return result;
	}

	@Override
	public List<EmployeeExcelReportDto> getEmployeesDumpReportData(LocalDate startDate, LocalDate endDate) {
		List<Object[]> result = employeeRepository.getEmployeeDumpData();
		List<EmployeeExcelReportDto> dtos = new ArrayList<>();

		for (Object[] row : result) {
			EmployeeExcelReportDto dto = new EmployeeExcelReportDto();

			dto.setId((Long) row[0]);
			dto.setFullName((String) row[1]);
			dto.setEmail((String) row[2]);
			dto.setJobProfile((String) row[3]);
			dto.setQualification((String) row[4]);
			dto.setMobileNo(row[5] != null ? ((Number) row[5]).longValue() : null);
			dto.setPermanentAddress((String) row[6]);
			dto.setCurrentAddress((String) row[7]);
			dto.setGender((String) row[8]);
			dto.setPreviousOrganisation((String) row[9]);
//	        dto.setDob((Date) row[10]);
			dto.setDob(convertToDate(row[10]));
			dto.setMaritalStatus((String) row[11]);
			dto.setRefferal((String) row[12]);
			dto.setAadhaarNumber((String) row[13]);
			dto.setLanguages((String) row[14]);
			dto.setExperience(row[15] != null ? ((Number) row[15]).floatValue() : null);
			dto.setSource((String) row[16]);
			dto.setSubSource((String) row[17]);

//	        dto.setCreationDate((Date) row[18]);
			dto.setCreationDate(convertToDate(row[18]));
			dto.setInitialStatus((String) row[19]);
			dto.setProcessesStatus((String) row[20]);
			dto.setHrStatus((String) row[21]);
			dto.setManagerStatus((String) row[22]);
			dto.setLastInterviewAssin((String) row[23]);
			dto.setProfileScreenRemarks((String) row[24]);
			dto.setReMarksByHr((String) row[25]);
			dto.setReMarksByManager((String) row[26]);
//	        dto.setClientRound((String) row[27]);

			dto.setStatus((String) row[27]);
			dto.setHrName((String) row[28]);
			dto.setRemarksOnEveryStages((String) row[29]);
//	        dto.setChangesDateTime((Date) row[30]);
			dto.setChangesDateTime(convertToDate(row[30]));
			if (dto.getCreationDate() != null) {
				LocalDate creationDate = dto.getCreationDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				if ((startDate == null || !creationDate.isBefore(startDate))
						&& (endDate == null || !creationDate.isAfter(endDate))) {
					dtos.add(dto);
				}
			}
		}

		return dtos;
	}

	@Override
	public ByteArrayOutputStream exportToRawExcel(List<EmployeeExcelReportDto> data) throws IOException {
		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("Employee Report");
		createRawExcelHeader(sheet);
		populateRows(sheet, data);
		return writeToOutputStream(workbook);
	}

	@Override
	public List<Object[]> getEmployeesDumpReportData(Date startDate, Date endDate) {
		List<Object[]> result = employeeRepository.getEmployeeSeqDumpData(startDate, endDate);
//		List<EmployeeExcelReportInSequenceDto> response = new ArrayList<>();
//
//		for (Object[] row : result) {
//			EmployeeExcelReportInSequenceDto dto = new EmployeeExcelReportInSequenceDto();
//			dto.setId((Long) row[0]);
//			dto.setFullName((String) row[1]);
//			dto.setQualification((String) row[2]);
//			dto.setAadhaarNumber((String) row[3]);
//			dto.setCreationDate((Date) row[4]);
//			dto.setCurrentAddress((String) row[5]);
//			dto.setDob((Date) row[6]);
//			dto.setEmail((String) row[7]);
//			if (row[8] != null) {
//				try {
//					dto.setExperience(Float.parseFloat(row[8].toString()));
//				} catch (NumberFormatException e) {
//					dto.setExperience(0f);
//				}
//			}
//			dto.setGender((String) row[9]);
//			dto.setHrStatus((String) row[10]);
//			dto.setInitialStatus((String) row[11]);
//			dto.setJobProfile((String) row[12]);
//			dto.setLanguages((String) row[13]);
//			dto.setLastInterviewAssin((String) row[14]);
//			dto.setManagerStatus((String) row[15]);
//			dto.setMaritalStatus((String) row[16]);
//			dto.setMobileNo((Long) row[17]);
//			dto.setPermanentAddress((String) row[18]);
//			dto.setPreviousOrganisation((String) row[19]);
//			dto.setProcessesStatus((String) row[20]);
//			dto.setProfileScreenRemarks((String) row[21]);
//			dto.setReMarksByHr((String) row[22]);
//			dto.setReMarksByManager((String) row[23]);
//			dto.setRefferal((String) row[24]);
//			dto.setSource((String) row[25]);
//			dto.setSubSource((String) row[26]);
//			dto.setWorkExp((String) row[27]);
//			if (row.length > 28) {
//				String[] hrNames = ((String) row[28]).split(" \\| ");
//				String[] changesDateTimes = ((String) row[29]).split(" \\| ");
//				String[] remarks = ((String) row[30]).split(" \\| ");
//				String[] statuses = ((String) row[31]).split(" \\| ");
//
//				// Ensure that these arrays have the same length
//				int maxLength = Math.max(hrNames.length,
//						Math.max(changesDateTimes.length, Math.max(remarks.length, statuses.length)));
//				List<StatusHistoryExcelDto> statusHistoryList = new ArrayList<>();
//				for (int i = 0; i < maxLength; i++) {
//					String hrName = (i < hrNames.length) ? hrNames[i] : "";
//					String changeDateTime = (i < changesDateTimes.length) ? changesDateTimes[i] : "";
//					String remark = (i < remarks.length) ? remarks[i] : "";
//					String status = (i < statuses.length) ? statuses[i] : "";
//
//					StatusHistoryExcelDto statusHistoryDto = new StatusHistoryExcelDto(hrName, changeDateTime, remark,
//							status);
//					statusHistoryList.add(statusHistoryDto);
//				}
//				dto.setStatusHistory(statusHistoryList);
//			} else {
//				dto.setStatusHistory(new ArrayList<>());
//			}
//
//			response.add(dto);
//		}
//
		return result;
	}

	@Override
	public List<ManagerPageResponseDTO> getScheduleInterviewManagerPage(String uniqueCodeProcess) {
		List<Object[]> response = employeeRepository.getScheduleInterviewManagerPage(uniqueCodeProcess);
		List<ManagerPageResponseDTO> dtoList = new ArrayList<>();

		for (Object[] obj : response) {
			ManagerPageResponseDTO dto = new ManagerPageResponseDTO();
			dto.setId((Long) obj[0]);
			dto.setFullName((String) obj[1]);
			dto.setEmail((String) obj[2]);
			dto.setJobProfile((String) obj[3]);
			dto.setMobileNo((String) obj[4]);
			dto.setCreationDate((Date) obj[5]);
			dtoList.add(dto);
		}

		return dtoList;
	}

	@Override
	public List<EmployeeDetailsOnManagerPageDTO> getAllResponseValueOnProcessType(String role, String location ,String branch) {
		log.info("Fetching employees with role: {} and location: {} and branch: {} ", role, location,branch);
		try {
			List<Object[]> employeeObjects = employeeRepository.findEmployeesByRoleAndLocation(role, location,branch);
			List<EmployeeDetailsOnManagerPageDTO> dtoList = new ArrayList<>();

			for (Object[] obj : employeeObjects) {
				EmployeeDetailsOnManagerPageDTO dto = new EmployeeDetailsOnManagerPageDTO((Long) obj[0],
						(String) obj[1], (String) obj[2], (String) obj[3], (Long) obj[4], (String) obj[5],
						(String) obj[6], (Date) obj[7]

				);
				dtoList.add(dto);
			}

			log.info("Successfully fetched {} employees", dtoList.size());
			return dtoList;

		} catch (Exception e) {
			log.error("Error fetching employee details: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to fetch employee details", e);
		}
	}

	public void exportToExcel(List<EmployeeExcelReportInSequenceDto> data, HttpServletResponse response)
			throws IOException {
		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("Employees");
		createSeqReportExcelHeader(sheet);
		createDynamicStatusHistoryHeaders(sheet, data);
		populateRowsWithEmployeeData(sheet, data);
		writeToResponse(workbook, response);
	}

	private void createSeqReportExcelHeader(Sheet sheet) {
		Row headerRow = sheet.createRow(0);
		String[] headers = { "Employee ID", "Full Name", "Qualification", "Aadhaar Number", "Creation Date",
				"Current Address", "DOB", "Email", "Experience", "Gender", "HR Status", "Initial Status", "Job Profile",
				"Languages", "Last Interview Assigned", "Manager Status", "Marital Status", "Mobile No",
				"Permanent Address", "Previous Organisation", "Processes Status", "Profile Screen Remarks",
				"Remarks by HR", "Remarks by Manager", "Referral", "Source", "Sub Source", "Work Experience" };

		// Set the fixed headers
		for (int i = 0; i < headers.length; i++) {
			headerRow.createCell(i).setCellValue(headers[i]);
		}
	}

	private void addStatusHistoryHeaders(Row headerRow, int columnOffSet, String... headers) {
		for (int i = 0; i < headers.length; i++) {
			headerRow.createCell(columnOffSet + i).setCellValue(headers[i]);
		}
	}

	private void createDynamicStatusHistoryHeaders(Sheet sheet, List<EmployeeExcelReportInSequenceDto> data) {
		int maxStatusHistorySize = data.stream().mapToInt(dto -> dto.getStatusHistory().size()).max().orElse(0);
		Row headerRow = sheet.getRow(0);
		for (int i = 0; i < maxStatusHistorySize; i++) {
			int columnOffSet = 28 + i * 4;
			int headerIndex = i < 6 ? i : 4 + (i - 6) % 2;

			switch (headerIndex) {
			case 0:
				addStatusHistoryHeaders(headerRow, columnOffSet, "Name", "Register Time and Date", "Register Remarks",
						"Register Status");
				break;
			case 1:
				addStatusHistoryHeaders(headerRow, columnOffSet, "HR Name", "HR Time and Date", "Profile Remarks",
						"HR Status");
				break;
			case 2:
				addStatusHistoryHeaders(headerRow, columnOffSet, "HR Name", "HR Time and Date",
						"Interview Schedule Remarks", "HR Status");
				break;
			case 3:
				addStatusHistoryHeaders(headerRow, columnOffSet, "Manager Name", "Manager Time and Date",
						"Manager Remarks", "Manager Status");
				break;
			case 4:
				addStatusHistoryHeaders(headerRow, columnOffSet, "HR Name", "ReScheduled Time and Date",
						"ReScheduled Remarks", "HR Status");
				break;
			case 5:
				addStatusHistoryHeaders(headerRow, columnOffSet, "Manager Name", "Manager Time and Date",
						"Manager Remarks", "Manager Status");
				break;
			}
		}
	}

	private void populateRowsWithEmployeeData(Sheet sheet, List<EmployeeExcelReportInSequenceDto> data) {
		int rowNum = 1;
		for (EmployeeExcelReportInSequenceDto dto : data) {
			Row row = sheet.createRow(rowNum++);

			// Populate fixed columns
			populateFixedColumns(row, dto);

			// Populate dynamic status history columns
			populateStatusHistoryColumns(row, dto);
		}
	}

	private void populateFixedColumns(Row row, EmployeeExcelReportInSequenceDto dto) {
		row.createCell(0).setCellValue(dto.getId());
		row.createCell(1).setCellValue(dto.getFullName());
		row.createCell(2).setCellValue(dto.getQualification());
		row.createCell(3).setCellValue(dto.getAadhaarNumber());
		row.createCell(4).setCellValue(dto.getCreationDate().toString());
		row.createCell(5).setCellValue(dto.getCurrentAddress());
		row.createCell(6).setCellValue(dto.getDob().toString());
		row.createCell(7).setCellValue(dto.getEmail());
		row.createCell(8).setCellValue(dto.getExperience());
		row.createCell(9).setCellValue(dto.getGender());
		row.createCell(10).setCellValue(dto.getHrStatus());
		row.createCell(11).setCellValue(dto.getInitialStatus());
		row.createCell(12).setCellValue(dto.getJobProfile());
		row.createCell(13).setCellValue(dto.getLanguages());
		row.createCell(14).setCellValue(dto.getLastInterviewAssin());
		row.createCell(15).setCellValue(dto.getManagerStatus());
		row.createCell(16).setCellValue(dto.getMaritalStatus());
		row.createCell(17).setCellValue(dto.getMobileNo());
		row.createCell(18).setCellValue(dto.getPermanentAddress());
		row.createCell(19).setCellValue(dto.getPreviousOrganisation());
		row.createCell(20).setCellValue(dto.getProcessesStatus());
		row.createCell(21).setCellValue(dto.getProfileScreenRemarks());
		row.createCell(22).setCellValue(dto.getReMarksByHr());
		row.createCell(23).setCellValue(dto.getReMarksByManager());
		row.createCell(24).setCellValue(dto.getRefferal());
		row.createCell(25).setCellValue(dto.getSource());
		row.createCell(26).setCellValue(dto.getSubSource());
		row.createCell(27).setCellValue(dto.getWorkExp());
	}

	private void populateStatusHistoryColumns(Row row, EmployeeExcelReportInSequenceDto dto) {
		for (int i = 0; i < dto.getStatusHistory().size(); i++) {
			StatusHistoryExcelDto statusDto = dto.getStatusHistory().get(i);
			int columnOffset = 28 + i * 4;

			row.createCell(columnOffset).setCellValue(statusDto.getHrName());
			row.createCell(columnOffset + 1).setCellValue(statusDto.getChangesDateTime());
			row.createCell(columnOffset + 2).setCellValue(statusDto.getRemarks());
			row.createCell(columnOffset + 3).setCellValue(statusDto.getStatus());
		}
	}

	private void writeToResponse(Workbook workbook, HttpServletResponse response) throws IOException {
		response.setHeader("Content-Disposition", "attachment; filename=employees_report.xlsx");
		workbook.write(response.getOutputStream());
		workbook.close();
	}

	private ByteArrayOutputStream writeToOutputStream(Workbook workbook) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		workbook.write(byteArrayOutputStream);
		workbook.close();
		return byteArrayOutputStream;
	}

	private void createRawExcelHeader(Sheet sheet) {
		// TODO Auto-generated method stub
		Row header = sheet.createRow(0);
		String[] columns = { "ID", "Full Name", "Email", "Job Profile", "Qualification", "Mobile No",
				"Permanent Address", "Current Address", "Gender", "Previous Organisation", "DOB", "Marital Status",
				"Referral", "Aadhaar Number", "Languages", "Experience", "Source", "Sub Source", "Initial Status",
				"HR Status", "Remarks by HR", "Processes Status", "Manager Status", "Remarks by Manager",
				"Creation Date", "Last Interview Assign", "Profile Screen Remarks", "Status", "HR Name",
				"Remarks on Every Stages", "Changes Date Time" };

		for (int i = 0; i < columns.length; i++) {
			header.createCell(i).setCellValue(columns[i]);
		}
	}

	private void populateRows(Sheet sheet, List<EmployeeExcelReportDto> data) {
		int rowNum = 1;
		for (EmployeeExcelReportDto dto : data) {
			Row row = sheet.createRow(rowNum++);
			populateRowWithData(row, dto);
		}
	}

	private void populateRowWithData(Row row, EmployeeExcelReportDto dto) {
		row.createCell(0).setCellValue(dto.getId());
		row.createCell(1).setCellValue(dto.getFullName());
		row.createCell(2).setCellValue(dto.getEmail());
		row.createCell(3).setCellValue(dto.getJobProfile());
		row.createCell(4).setCellValue(dto.getQualification());
		row.createCell(5).setCellValue(dto.getMobileNo() != null ? dto.getMobileNo() : 0);
		row.createCell(6).setCellValue(dto.getPermanentAddress() != null ? dto.getPermanentAddress() : "");
		row.createCell(7).setCellValue(dto.getCurrentAddress() != null ? dto.getCurrentAddress() : "");
		row.createCell(8).setCellValue(dto.getGender() != null ? dto.getGender() : "");
		row.createCell(9).setCellValue(dto.getPreviousOrganisation() != null ? dto.getPreviousOrganisation() : "");
		row.createCell(10).setCellValue(dto.getDob() != null ? dto.getDob().toString() : "N/A");
		row.createCell(11).setCellValue(dto.getMaritalStatus() != null ? dto.getMaritalStatus() : "");
		row.createCell(12).setCellValue(dto.getRefferal() != null ? dto.getRefferal() : "");
		row.createCell(13).setCellValue(dto.getAadhaarNumber() != null ? dto.getAadhaarNumber() : "");
		row.createCell(14).setCellValue(dto.getLanguages() != null ? dto.getLanguages() : "");
		row.createCell(15).setCellValue(dto.getExperience() != null ? dto.getExperience() : 0);
		row.createCell(16).setCellValue(dto.getSource() != null ? dto.getSource() : "");
		row.createCell(17).setCellValue(dto.getSubSource() != null ? dto.getSubSource() : "");
		row.createCell(18).setCellValue(dto.getInitialStatus() != null ? dto.getInitialStatus() : "");
		row.createCell(19).setCellValue(dto.getHrStatus() != null ? dto.getHrStatus() : "");
		row.createCell(20).setCellValue(dto.getReMarksByHr() != null ? dto.getReMarksByHr() : "");
		row.createCell(21).setCellValue(dto.getProcessesStatus() != null ? dto.getProcessesStatus() : "");
		row.createCell(22).setCellValue(dto.getManagerStatus() != null ? dto.getManagerStatus() : "");
		row.createCell(23).setCellValue(dto.getReMarksByManager() != null ? dto.getReMarksByManager() : "");
//			row.createCell(24).setCellValue(dto.getCreationDate());
		if (dto.getCreationDate() != null) {
			row.createCell(24).setCellValue(dto.getCreationDate());
		} else {
			row.createCell(24).setCellValue("N/A");
		}
		row.createCell(25).setCellValue(dto.getLastInterviewAssin() != null ? dto.getLastInterviewAssin() : "");
		row.createCell(26).setCellValue(dto.getProfileScreenRemarks() != null ? dto.getProfileScreenRemarks() : "");
		row.createCell(27).setCellValue(dto.getStatus() != null ? dto.getStatus() : "");
		row.createCell(28).setCellValue(dto.getHrName() != null ? dto.getHrName() : "");
		row.createCell(29).setCellValue(dto.getRemarksOnEveryStages() != null ? dto.getRemarksOnEveryStages() : "");
		row.createCell(30).setCellValue(dto.getChangesDateTime() != null ? dto.getChangesDateTime().toString() : "N/A");
	}

	@Override
	public boolean checkDuplicateEmail(String email) {
		boolean checkEmail = employeeRepository.existsByEmail(email);
		return checkEmail;
	}

	@Override
	public boolean checkDuplicateAdhaarNo(String aadharNumber) {
		boolean checkAdhaarNo = employeeRepository.existsByAadhaarNumber(aadharNumber);
		return checkAdhaarNo;
	}

	private void createEmployeeStatusDetails(Employee savedEmployeeEntity) {
		EmployeeStatusDetails statusDetails = new EmployeeStatusDetails();
		statusDetails.setEmployee(savedEmployeeEntity);
		statusDetails.setInitialStatus("Active");

		// Save EmployeeStatusDetails
		employeeStatusDetailsRepository.save(statusDetails);
		log.info("EmployeeStatusDetails created and associated with employee ID: {}", statusDetails.getId());
	}

	private String capitalizeStringAfterSpacing(String name) {
		if (name == null || name.isEmpty()) {
			return name;
		}
		return Arrays.stream(name.split(" ")).map(
				part -> part.isEmpty() ? part : part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase())
				.collect(Collectors.joining(" "));
	}

	private void updateEmployeeStatus(Employee savedEmployeeEntity) {
		StatusHistory latestStatus = statusHistoryRepository
				.findTopByEmployeeOrderByChangesDateTimeDesc(savedEmployeeEntity);
		if (latestStatus != null) {

			EmployeeStatusDetails statusDetails = employeeStatusDetailsRepository.findByEmployee(savedEmployeeEntity);

			if (statusDetails != null) {
				// Update the initial status in EmployeeStatusDetails
				statusDetails.setInitialStatus(latestStatus.getStatus());

				// Save the updated EmployeeStatusDetails entity
				employeeStatusDetailsRepository.save(statusDetails);
			} else {
				// Handle the case where EmployeeStatusDetails does not exist; possibly create a
				// new record
				EmployeeStatusDetails newStatusDetails = new EmployeeStatusDetails();
				newStatusDetails.setEmployee(savedEmployeeEntity);
				newStatusDetails.setInitialStatus(latestStatus.getStatus());
				// Set other fields as necessary

				employeeStatusDetailsRepository.save(newStatusDetails);
			}
		}
	}

	private void setStatusHistoryRecoredRemarksChecks(Long employeeId, StatusRequestDTO statusRequestDTO,
			InterviewProcesses savedInterviewProcess) {

		Employee employee = employeeRepository.findById(employeeId)
				.orElseThrow(() -> new RuntimeException("Employee not found"));
		StatusHistory statusHistory = new StatusHistory();
		statusHistory.setEmployee(employee);
		statusHistory.setInterviewProcess(savedInterviewProcess);
		statusHistory.setStatus(statusRequestDTO.getNewStatus());
		statusHistory.setHrName(statusRequestDTO.getResponseSubmitbyName());
		LocalDateTime currentDateTime = LocalDateTime.now();
		Date currentDate = Date.from(currentDateTime.atZone(ZoneId.systemDefault()).toInstant());
		statusHistory.setChangesDateTime(currentDate);
		statusHistoryRepository.save(statusHistory);
	}

	private Date convertToDate(Object obj) {
		if (obj instanceof Date) {
			return (Date) obj;
		} else if (obj instanceof String) {
			try {
				// Adjust the pattern if your date string includes time
				return new SimpleDateFormat("yyyy-MM-dd").parse((String) obj);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public EmployeeDto submitResponseForReScreeningProfile(Long employeeId, StatusRequestDTO statusRequestDTO) {
		try {
			log.info("Submitting response for re-screening for employeeId: {}", employeeId);

			// Fetch employee status details
			EmployeeStatusDetails employeeStatusDetails = employeeStatusDetailsRepository.findByEmployeeId(employeeId)
					.orElseThrow(() -> {
						log.error("EmployeeStatusDetails not found for employeeId: {}", employeeId);
						return new RuntimeException("EmployeeStatusDetails not found");
					});

			// Clear HR status
			employeeStatusDetails.setHrStatus(null);
			employeeStatusDetailsRepository.save(employeeStatusDetails);
			log.info("Cleared HR status for employeeId: {}", employeeId);

			// Update status history and remarks
			setStatusHistoryRecoredRemarksChecks(employeeId, statusRequestDTO, null);
			log.info("Status history recorded for employeeId: {}", employeeId);

			// Fetch and return updated EmployeeDto
			Employee updatedEmployee = employeeRepository.findById(employeeId).orElseThrow(() -> {
				log.error("Employee not found with ID: {}", employeeId);
				return new RuntimeException("Employee not found");
			});

			EmployeeDto employeeDto = modelMapper.map(updatedEmployee, EmployeeDto.class);
			log.info("Returning updated EmployeeDto for employeeId: {}", employeeId);

			return employeeDto;

		} catch (Exception e) {
			log.error("Error occurred while submitting response for re-screening for employeeId: {}", employeeId, e);
			throw new RuntimeException("Failed to submit response for re-screening", e);
		}
	}

	@Override
	public String getVendorNameByEmployeeEmail(String empEmail) {
		log.info("Fetching vendor name for employee email: {}", empEmail);
		String vendorName = employeeRepository.getVendorNameByEmployeeEmail(empEmail);
		if (vendorName == null || vendorName.isEmpty()) {
			log.warn("No vendor found for employee email: {}", empEmail);
			throw new EntityNotFoundException("Vendor not found for employee email: " + empEmail);
		}
		log.info("Vendor name found: {} for employee email: {}", vendorName, empEmail);
		return vendorName;
	}

	@Override
	public Long hasScreeningRemarks(String email) {
		// TODO Auto-generated method stub
		   Long result = employeeRepository.existsScreeningRemarksByEmail(email);
	        return result;
	}
}
