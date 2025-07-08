package com.eos.admin.serviceImpl;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eos.admin.dto.CandidatesDTO;
import com.eos.admin.entity.CandidatesEntity;
import com.eos.admin.entity.Employee;
import com.eos.admin.entity.OurEmployees;
import com.eos.admin.exception.CandidateNotFoundException;
import com.eos.admin.exception.DuplicateRecordException;
import com.eos.admin.repository.CandidatesRepository;
import com.eos.admin.repository.EmployeeRepository;
import com.eos.admin.repository.OurEmployeeRepository;
import com.eos.admin.service.CandidatesService;
import com.eos.admin.service.EmployeeService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CandidatesServiceImpl implements CandidatesService {

    private final CandidatesRepository candidatesRepository;
    private final ModelMapper modelMapper;
    private final EmployeeRepository employeeRepository;
    private final OurEmployeeRepository ourEmployeeRepository;
    private final EmployeeService employeeService;
    @Autowired
    public CandidatesServiceImpl(CandidatesRepository candidatesRepository, ModelMapper modelMapper
    		,EmployeeRepository employeeRepository,OurEmployeeRepository ourEmployeeRepository,EmployeeService employeeService) {
        this.candidatesRepository = candidatesRepository;
        this.modelMapper = modelMapper;
        this.employeeRepository = employeeRepository;
        this.ourEmployeeRepository= ourEmployeeRepository;
        this.employeeService = employeeService;
    }

    @Override
    public CandidatesDTO saveCandidates(CandidatesDTO candidatesDTO) {
        // Check if candidate with same email and scheme already exists
        Optional<CandidatesEntity> existingCandidate = candidatesRepository.findByCandiEmailAndScheme(candidatesDTO.getCandiEmail(), candidatesDTO.getScheme());
        if (existingCandidate.isPresent()) {
            throw new RuntimeException("Candidate with the same email and scheme already exists.");
        }

        // Map DTO to Entity and save it
        CandidatesEntity candidatesEntity = modelMapper.map(candidatesDTO, CandidatesEntity.class);
        CandidatesEntity savedEntity = candidatesRepository.save(candidatesEntity);

        // Convert back the saved entity to DTO and return
        return modelMapper.map(savedEntity, CandidatesDTO.class);
    }

    @Override
    public List<CandidatesDTO> saveCandidatesMultiple(List<CandidatesDTO> candidatesDTOList) {
        // Validate each candidate for duplicate email and scheme
        for (CandidatesDTO candidateDTO : candidatesDTOList) {
            Optional<CandidatesEntity> existingCandidate = candidatesRepository.findByCandiEmailAndScheme(candidateDTO.getCandiEmail(), candidateDTO.getScheme());
            if (existingCandidate.isPresent()) {
               throw new DuplicateRecordException("Candidate with the same email and scheme already exists.");
            
            }
        }

        // Convert DTOs to Entities
//        List<CandidatesEntity> entities = candidatesDTOList.stream()
//                .map(dto -> modelMapper.map(dto, CandidatesEntity.class))
//                .collect(Collectors.toList());
        List<CandidatesEntity> entities = candidatesDTOList.stream()
        	    .map(dto -> {
        	        CandidatesEntity entity = modelMapper.map(dto, CandidatesEntity.class);
        	        entity.setId(null); // Ensure it's treated as a new entity
        	        return entity;
        	    })
        	    .collect(Collectors.toList());


        // Save all entities
        List<CandidatesEntity> savedEntities = candidatesRepository.saveAll(entities);

        // Convert back to DTOs and return
        return savedEntities.stream()
                .map(entity -> modelMapper.map(entity, CandidatesDTO.class))
                .collect(Collectors.toList());
    }

//    @Override
//    public List<CandidatesDTO> findBySubmittedDateBetweenAndVendorEmail(LocalDateTime start, LocalDateTime end, String vendorEmail) {
//        List<CandidatesEntity> entities = candidatesRepository.findBySubmittedDateBetweenAndVendorEmail(start, end, vendorEmail);
//        return entities.stream()
//                .map(entity -> modelMapper.map(entity, CandidatesDTO.class))
//                .collect(Collectors.toList());
//    }
//    @Override
//    public List<CandidatesDTO> findBySubmittedDateBetweenAndVendorEmail(LocalDateTime start, LocalDateTime end, String vendorEmail) {
//        List<CandidatesEntity> candidates = candidatesRepository.findBySubmittedDateBetweenAndVendorEmail(start, end, vendorEmail);
//
//        // Extract candidate emails
//        Set<String> candidateEmails = candidates.stream()
//            .map(CandidatesEntity::getCandiEmail)
//            .filter(Objects::nonNull)
//            .collect(Collectors.toSet());
//
//        // Fetch employees by candidate emails
//        List<Employee> employees = employeeRepository.findByEmailIn(candidateEmails);
//
//        Map<String, Employee> emailToEmployeeMap = employees.stream()
//            .collect(Collectors.toMap(Employee::getEmail, Function.identity()));
//
//        // Fetch OurEmployees by employee IDs using nested property
//        Set<Long> employeeIds = employees.stream()
//            .map(Employee::getId)
//            .collect(Collectors.toSet());
//
//        List<OurEmployees> ourEmployeesList = ourEmployeeRepository.findByEmployeeIds((Collection<Long>) employeeIds);
//
//        Set<Long> loiEmployeeIds = ourEmployeesList.stream()
//            .map(oe -> oe.getEmployee().getId())  // <-- Correct way
//            .collect(Collectors.toSet());
//
//        return candidates.stream()
//            .map(candidate -> {
//                CandidatesDTO dto = modelMapper.map(candidate, CandidatesDTO.class);
//
//                Employee employee = emailToEmployeeMap.get(candidate.getCandiEmail());
//                boolean loiIssued = (employee != null) && loiEmployeeIds.contains(employee.getId());
//                Long appeared = employeeService.hasScreeningRemarks(dto.getCandiEmail());
//
//                dto.setLoiIssued(loiIssued);
//                dto.setAppearedForInterview(appeared);
//                return dto;
//            })
//            .collect(Collectors.toList());
//    }
    @Override
    public List<CandidatesDTO> findBySubmittedDateBetweenAndVendorEmail(LocalDateTime start, LocalDateTime end, String vendorEmail) {
        log.info("Fetching candidates between {} and {} for vendor: {}", start, end, vendorEmail);

        List<CandidatesEntity> candidates = candidatesRepository.findBySubmittedDateBetweenAndVendorEmail(start, end, vendorEmail);
        log.debug("Fetched {} candidates from repository", candidates.size());

        Set<String> candidateEmails = candidates.stream()
            .map(CandidatesEntity::getCandiEmail)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        log.debug("Extracted {} unique candidate emails", candidateEmails.size());

        List<Employee> employees = employeeRepository.findByEmailIn(candidateEmails);
        log.debug("Fetched {} employees by email", employees.size());

        Map<String, Employee> emailToEmployeeMap = employees.stream()
            .filter(emp -> emp.getEmail() != null)
            .collect(Collectors.toMap(Employee::getEmail, Function.identity(), (e1, e2) -> e1));

        Set<Long> employeeIds = employees.stream()
            .map(Employee::getId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        log.debug("Extracted {} unique employee IDs", employeeIds.size());

        List<OurEmployees> ourEmployeesList = ourEmployeeRepository.findByEmployeeIds(employeeIds);
        log.debug("Fetched {} ourEmployee records", ourEmployeesList.size());

        Set<Long> loiEmployeeIds = ourEmployeesList.stream()
            .map(oe -> {
                if (oe.getEmployee() != null) {
                    return oe.getEmployee().getId();
                }
                return null;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        List<CandidatesDTO> result = candidates.stream()
            .map(candidate -> {
                try {
                    if (candidate == null) {
                        log.warn("Skipping null candidate record");
                        return null;
                    }

                    CandidatesDTO dto = modelMapper.map(candidate, CandidatesDTO.class);

                    String email = candidate.getCandiEmail();
                    Employee employee = emailToEmployeeMap.get(email);

                    boolean loiIssued = (employee != null) && loiEmployeeIds.contains(employee.getId());
                    dto.setLoiIssued(loiIssued);

                    Long appeared = null;
                    try {
                        appeared = employeeService.hasScreeningRemarks(email);
                    } catch (Exception e) {
                        log.warn("Failed to check screening remarks for email: {}", email, e);
                    }

                    dto.setAppearedForInterview(appeared);
                    return dto;

                } catch (Exception e) {
                    log.error("Error mapping candidate to DTO: {}", candidate.getCandiEmail(), e);
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        log.info("Mapped {} candidates to DTOs", result.size());
        return result;
    }



    @Override
    public List<CandidatesDTO> findAllByVendorEmailLifo(String vendorEmail) {
        List<CandidatesEntity> candidates = candidatesRepository.findAllByVendorEmailOrderBySubmittedDateDesc(vendorEmail);

        // Extract candidate emails
        Set<String> candidateEmails = candidates.stream()
            .map(CandidatesEntity::getCandiEmail)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        // Fetch employees by candidate emails
        List<Employee> employees = employeeRepository.findByEmailIn(candidateEmails);

        Map<String, Employee> emailToEmployeeMap = employees.stream()
            .collect(Collectors.toMap(Employee::getEmail, Function.identity()));

        // Fetch OurEmployees by employee IDs using nested property
        Set<Long> employeeIds = employees.stream()
            .map(Employee::getId)
            .collect(Collectors.toSet());

        List<OurEmployees> ourEmployeesList = ourEmployeeRepository.findByEmployeeIds(employeeIds);

        Set<Long> loiEmployeeIds = ourEmployeesList.stream()
            .map(oe -> oe.getEmployee().getId())  // <-- Correct way
            .collect(Collectors.toSet());

        return candidates.stream()
            .map(candidate -> {
                CandidatesDTO dto = modelMapper.map(candidate, CandidatesDTO.class);

                Employee employee = emailToEmployeeMap.get(candidate.getCandiEmail());
                boolean loiIssued = (employee != null) && loiEmployeeIds.contains(employee.getId());
                Long appeared = employeeService.hasScreeningRemarks(dto.getCandiEmail());

                dto.setLoiIssued(loiIssued);
                dto.setAppearedForInterview(appeared);
                return dto;
            })
            .collect(Collectors.toList());
    }


    @Override
    public String getManagerStatusByEmail(String email) {
        Optional<String> managerStatus = candidatesRepository.findManagerStatusByEmail(email);

        return managerStatus.orElse(null);
    }
    
    @Override
    public List<CandidatesDTO> getCandidatesByVendor(String venId, String vendorEmail) {

        log.info("Searching candidates for venId: {} and vendorEmail: {}", venId, vendorEmail);

        List<CandidatesEntity> candidates = candidatesRepository
                .findByVenIdAndVendorEmailOrderBySubmittedDateDesc(venId, vendorEmail);

        if (candidates.isEmpty()) {
            log.warn("No candidates found for venId: {} and vendorEmail: {}", venId, vendorEmail);
            throw new CandidateNotFoundException("No candidates found for the given vendor details.");
        }

        List<CandidatesDTO> dtoList = candidates.stream()
                .map(candidate -> modelMapper.map(candidate, CandidatesDTO.class))
                .collect(Collectors.toList());

        log.info("Found {} candidates for venId: {}", dtoList.size(), venId);

        return dtoList;
    }

//    @Override
//	public List<CandidatesDTO> findAllByVendorEmailLifo(String vendorEmail) {
//	    List<CandidatesEntity> entities = candidatesRepository.findAllByVendorEmailOrderBySubmittedDateDesc(vendorEmail);
//	    return entities.stream()
//	            .map(entity -> modelMapper.map(entity, CandidatesDTO.class))
//	            .collect(Collectors.toList());
//
//	}
}
