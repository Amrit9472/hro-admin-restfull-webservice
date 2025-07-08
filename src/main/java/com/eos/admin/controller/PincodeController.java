package com.eos.admin.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eos.admin.entity.PincodeDirectoryEntity;
import com.eos.admin.service.PincodeService;

@RestController
@RequestMapping("/api/pincode")
@CrossOrigin("*")
public class PincodeController {

    private PincodeService pincodeService;
    
    

    public PincodeController(PincodeService pincodeService) {
		super();
		this.pincodeService = pincodeService;
	}

	@GetMapping("/{pincode}")
    public ResponseEntity<?> getByPincode(@PathVariable("pincode") String pincode) {
        Optional<PincodeDirectoryEntity> pincodeOpt = pincodeService.getByPincode(pincode);

        if (pincodeOpt.isPresent()) {
        	PincodeDirectoryEntity pin = pincodeOpt.get();
            return ResponseEntity.ok(Map.of(
                "district", pin.getDistrict(),
                "state", pin.getStateName()
            ));
        } else {
            return ResponseEntity.status(404).body(Map.of("message", "Pincode not found"));
        }
    }

    @PostMapping
    public ResponseEntity<?> insertPincode(@RequestBody PincodeDirectoryEntity newPincode) {
    	PincodeDirectoryEntity saved = pincodeService.save(newPincode);
        return ResponseEntity.ok(saved);
    }
}
