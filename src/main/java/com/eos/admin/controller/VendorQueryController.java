package com.eos.admin.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eos.admin.dto.VendorQueryDTO;
import com.eos.admin.dto.VendorQueryInformationDTO;
import com.eos.admin.entity.VendorQuery;
import com.eos.admin.enums.VendorStatusType;
import com.eos.admin.repository.VendorRepository;
import com.eos.admin.service.QueryService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/candidates")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@Slf4j
public class VendorQueryController {


    @Autowired
    private QueryService queryService;
    
    @Autowired
    private VendorRepository vendorRepository;

    @PostMapping("/raisequery")
    public ResponseEntity<String> raiseQuery(@RequestBody VendorQueryDTO queryDTO) {
        try {
            // Log the received queryDTO
            log.info("Received a new query to raise: {}", queryDTO);

            queryService.saveQuery(queryDTO);

            // Log the successful query raising
            log.info("Query raised successfully for vendor: {}", queryDTO.getVendorEmail());

            return ResponseEntity.ok("Query raised successfully!");
        } catch (Exception e) {
            // Log the exception when the query cannot be raised
            log.error("Failed to raise query: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to raise query: " + e.getMessage());
        }
    }

    @GetMapping("/getByVendorEmail")
    public ResponseEntity<List<VendorQueryDTO>> getQueriesByVendorEmail(@RequestParam("vendorEmail") String vendorEmail) {
        try {

            // Log the request for queries by vendor email
            log.info("Fetching queries for vendor email: {}", vendorEmail);

            List<VendorQueryDTO> queries = queryService.getQueriesByVendorEmail(vendorEmail);

            if (queries.isEmpty()) {
                // Log if no queries are found
                log.warn("No queries found for vendor email: {}", vendorEmail);
                return ResponseEntity.noContent().build();
            }

            // Log the number of queries found
            log.info("Found {} queries for vendor email: {}", queries.size(), vendorEmail);

            return ResponseEntity.ok(queries);
        } catch (Exception e) {
            // Log the exception if there's an error fetching the queries
            log.error("Error fetching queries for vendor email {}: {}", vendorEmail, e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }
    
    @GetMapping("/getAllQueryForAdmin")
    public ResponseEntity<List<VendorQueryInformationDTO>> getAllVendorQueries() {
        log.info("Received request to get all vendor queries");

        try {
            List<VendorQueryInformationDTO> queries = queryService.getAllVendorQueries();
            log.info("Returning {} vendor queries", queries.size());
            return ResponseEntity.ok(queries);
        } catch (Exception e) {
            log.error("Failed to retrieve vendor queries", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateVendorQueryStatus(
            @PathVariable("id") Long id,
            @RequestBody VendorQueryDTO vendorQueryDTO ) {
        log.info("Received request to update status for VendorQuery id {}: {}", id, vendorQueryDTO);

       
        try {
        	VendorStatusType newStatus = vendorQueryDTO.getVendorQueryStatus();
            String remark =  vendorQueryDTO.getRemark();
        
            VendorQuery updated = queryService.updateStatus(id, newStatus,remark);
            log.info("Status updated successfully for VendorQuery id {}", id);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid status value received: {}", vendorQueryDTO.getVendorQueryStatus());
            return ResponseEntity.badRequest().body("Invalid status value");
        }
    }
  
    @GetMapping("/vendor-statuses")
    public VendorStatusType[] getAllStatuses() {
        return VendorStatusType.values();
    }

}
