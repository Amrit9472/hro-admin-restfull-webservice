package com.eos.admin.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eos.admin.dto.SchemeVendorDTO;
import com.eos.admin.dto.SchemesDto;
import com.eos.admin.dto.VendorCompanyNameDTO;
import com.eos.admin.entity.Schemes;
import com.eos.admin.exception.EntityNotFoundException;
import com.eos.admin.service.SchemesService;
import com.eos.admin.service.VendorInfoService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/schemes")
public class SchemeController {

	private SchemesService schemesService;
	private VendorInfoService vendorInfoService;

	public SchemeController(SchemesService schemesService,VendorInfoService vendorInfoService) {
		super();
		this.schemesService = schemesService;
		this.vendorInfoService = vendorInfoService;
	}

	@PostMapping("/create")
	public ResponseEntity<?> createScheme(@Valid @RequestBody SchemesDto dto) {
	    try {
	        Schemes created = schemesService.createSchemeWithVendorNames(dto);
	        return ResponseEntity.ok(created);
	    } catch (EntityNotFoundException e) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
	    } catch (Exception e) {
	        log.error("Unexpected error occurred", e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal error");
	    }
		
	}
	
	@GetMapping("/vendor/{vendorId}")
	public ResponseEntity<List<Schemes>> getSchemes(
	        @PathVariable(name = "vendorId") Long vendorId,
	        @RequestParam(name = "month",required = false) Integer month,
	        @RequestParam(name = "year",required = false) Integer year,
	        @RequestParam(name = "sourceType",defaultValue = "all") String sourceType) {
	    try {
	        log.info("Fetching schemes for vendorId: {}, month: {}, year: {}, sourceType: {}", vendorId, month, year, sourceType);
	        List<Schemes> schemes = schemesService.getSchemes(month, year, vendorId, sourceType);
	        return new ResponseEntity<>(schemes, HttpStatus.OK);
	    } catch (Exception e) {
	        log.error("Error fetching schemes", e);
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}

	// Get all schemes filtered by isDefault
    @GetMapping
    public ResponseEntity<List<Schemes>> getSchemesByIsDefault(@RequestParam(name = "isDefault") boolean isDefault) {
        List<Schemes> schemes = schemesService.findByIsDefault(isDefault);
        return ResponseEntity.ok(schemes);
    }

    // Get a single scheme by its name
    @GetMapping("/by-name")
    public ResponseEntity<Schemes> getSchemeByName(@RequestParam(name = "schemeName") String schemeName) {
        return schemesService.findByScheme(schemeName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    // Update an existing scheme
    @PutMapping("/{id}")
    public ResponseEntity<Schemes> updateScheme(@PathVariable("id") Long id, @RequestBody() Schemes updated) {
        return schemesService.update(id, updated)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @GetMapping("/vendor-mapping")
    public List<SchemeVendorDTO> getSchemeVendorMapping() {
        return schemesService.getSchemeVendorMapping();
    }
    
    @PostMapping("/schemes/{schemeId}/vendors")
    public ResponseEntity<?> addVendorsToScheme(
            @PathVariable("schemeId") Long schemeId,
            @RequestBody List<String> vendorNames) {
        try {
            schemesService.addVendorsToScheme(schemeId, vendorNames);
            return ResponseEntity.ok("Vendors added successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    
    @PostMapping("/schemes/{schemeId}/vendors/remove")
    public ResponseEntity<?> removeVendorFromScheme(
            @PathVariable("schemeId") Long schemeId,
            @RequestBody List<String> vendorNames) {
        try {
            schemesService.removeVendorsFromScheme(schemeId, vendorNames);
            
            Map<String, String> response = new HashMap();
            response.put("message", "Vendors removed successfully.");
            return ResponseEntity.ok(response);
//            return ResponseEntity.ok("Vendors removed successfully.");
        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        	 Map<String, String> errorResponse = new HashMap<>();
             errorResponse.put("error", e.getMessage());
             
             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }


    @GetMapping("/company-names")
    public List<VendorCompanyNameDTO> getAllVendorCompanyNamesSchemes() {
        return vendorInfoService.getAllVendorCompanyNames();
    }
}


