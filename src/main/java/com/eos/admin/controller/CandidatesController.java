package com.eos.admin.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eos.admin.dto.CandidatesDTO;
import com.eos.admin.exception.DuplicateRecordException;
import com.eos.admin.serviceImpl.CandidatesServiceImpl;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/candi")
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
public class CandidatesController {

	private final CandidatesServiceImpl candidatesService;

	@Autowired
	public CandidatesController(CandidatesServiceImpl candidatesService) {
		super();
		this.candidatesService = candidatesService;
	}

	/**
	 * @PostMapping public ResponseEntity<CandidatesDTO>
	 *              createCandidate(@RequestBody CandidatesDTO candidatesDTO) {
	 * 
	 *              try { if (candidatesDTO == null) { log.warn("Candidate request
	 *              is null {}", candidatesDTO); return new ResponseEntity<>(null,
	 *              HttpStatus.BAD_REQUEST); } CandidatesDTO request =
	 *              candidatesService.saveCandidates(candidatesDTO); return new
	 *              ResponseEntity<>(request, HttpStatus.CREATED); } catch
	 *              (Exception e) { log.error("Error occurred while creating
	 *              candidate: {}", e.getMessage(), e); return new
	 *              ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR); } }
	 */
	@PostMapping("/bulk")
	public ResponseEntity<Map<String, String>> createCandidateMultiple(@RequestBody List<CandidatesDTO> candidatesDTO) {

		try {
			// Validate input
			if (candidatesDTO == null || candidatesDTO.isEmpty()) {
				log.warn("Candidate request is null or empty {}", candidatesDTO);
				return new ResponseEntity<>(Map.of("message", "No candidates provided"), HttpStatus.BAD_REQUEST);
			}

			// Call service to save candidates
			candidatesService.saveCandidatesMultiple(candidatesDTO);

			// Return success message
			return new ResponseEntity<>(Map.of("message", "Candidates have been successfully created."),
					HttpStatus.CREATED);

		} catch (DuplicateRecordException ex) {
			log.error("Duplicate record error: {}", ex.getMessage());
			throw ex; // Let this exception propagate to be handled elsewhere (maybe globally)
		} catch (Exception e) {
			log.error("Error occurred while creating candidates: {}", e.getMessage(), e);
			return new ResponseEntity<>(Map.of("message", "An internal error occurred while processing the request."),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/candidates-sended/{venId}/{vendorEmail}")
	public ResponseEntity<List<CandidatesDTO>> getCandidatesByVendor(@PathVariable("venId") String venId,
			@PathVariable("vendorEmail") String vendorEmail) {

		log.info("API Request received for venId: {}, vendorEmail: {}", venId, vendorEmail);

		List<CandidatesDTO> candidates = candidatesService.getCandidatesByVendor(venId, vendorEmail);

		return ResponseEntity.ok(candidates);
	}

	@GetMapping("/byDateRange")
	public ResponseEntity<List<CandidatesDTO>> getCandidatesByDateRange(
			@RequestParam(name = "startDate", required = false) String startDate,
			@RequestParam(name = "endDate", required = false) String endDate,
			@RequestParam(name = "email") String vendorEmail) {

		try {
			List<CandidatesDTO> candidates;

			if (startDate != null && endDate != null) {
				// Parse and filter by date
				LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE);
				LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE);

				LocalDateTime startOfDay = start.atStartOfDay();
				LocalDateTime endOfDay = end.atTime(23, 59, 59);

				candidates = candidatesService.findBySubmittedDateBetweenAndVendorEmail(startOfDay, endOfDay,
						vendorEmail);
			
			} else {
				candidates = candidatesService.findAllByVendorEmailLifo(vendorEmail);
			}

			return new ResponseEntity<>(candidates, HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error fetching candidates: {}", e.getMessage(), e);
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/manager-status")
	public ResponseEntity<String> getManagerStatus(@RequestParam(name = "email") String email) {
		try {
			String managerStatus = candidatesService.getManagerStatusByEmail(email);

			if (managerStatus != null) {
				return new ResponseEntity<>(managerStatus, HttpStatus.OK);
			} else {
				return new ResponseEntity<>("Manager status not found for the provided email.", HttpStatus.NOT_FOUND);
			}

		} catch (Exception e) {
			log.error("Error occurred while fetching manager status for email: {}", e.getMessage(), e);
			return new ResponseEntity<>("Error occurred while fetching manager status.",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
