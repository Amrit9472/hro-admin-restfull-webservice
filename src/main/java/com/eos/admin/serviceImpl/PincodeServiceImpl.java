package com.eos.admin.serviceImpl;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.eos.admin.entity.PincodeDirectoryEntity;
import com.eos.admin.repository.PincodeRepository;
import com.eos.admin.service.PincodeService;

@Service
public class PincodeServiceImpl implements PincodeService {

	private PincodeRepository repository;

	public PincodeServiceImpl(PincodeRepository repository) {
		super();
		this.repository = repository;
	}

	@Override
	public Optional<PincodeDirectoryEntity> getByPincode(String pincode) {
		return repository.findByPincode(pincode);
	}

	@Override
	public PincodeDirectoryEntity save(PincodeDirectoryEntity pincode) {
		return repository.save(pincode);
	}
}
