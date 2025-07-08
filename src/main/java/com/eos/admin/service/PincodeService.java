package com.eos.admin.service;

import java.util.Optional;

import com.eos.admin.entity.PincodeDirectoryEntity;

public interface PincodeService {

	Optional<PincodeDirectoryEntity> getByPincode(String pincode);

	PincodeDirectoryEntity save(PincodeDirectoryEntity pincode);

}
