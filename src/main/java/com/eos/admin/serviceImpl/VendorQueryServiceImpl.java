package com.eos.admin.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eos.admin.dto.VendorQueryDTO;
import com.eos.admin.dto.VendorQueryInformationDTO;
import com.eos.admin.entity.VendorQuery;
import com.eos.admin.enums.VendorStatusType;
import com.eos.admin.repository.VendorQueryRepository;
import com.eos.admin.service.QueryService;

import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VendorQueryServiceImpl implements QueryService {

	@Autowired
	private VendorQueryRepository vendorQueryRepository;

	@Override
	public void saveQuery(VendorQueryDTO queryDTO) {
		VendorQuery query = new VendorQuery(queryDTO.getVendorEmail(), queryDTO.getQueryText());
		vendorQueryRepository.save(query);
	}

	@Override
	public List<VendorQueryDTO> getQueriesByVendorEmail(String vendorEmail) {
	    List<VendorQuery> vendorQueries = vendorQueryRepository.findByVendorEmail(vendorEmail);

	    return vendorQueries.stream()
	            .map(query -> new VendorQueryDTO(
	                query.getId(),
	                query.getVendorEmail(),
	                query.getQueryText(),
	                query.getVendorQueryStatus(),
	                query.getInProgressRemark(),
	                query.getClosedRemark()
	            ))
	            .collect(Collectors.toList());
	}

	@Override
	public List<VendorQueryInformationDTO> getAllVendorQueries() {

		log.info("Fetching all vendor queries from repository");
		try {
			List<VendorQuery> repoResponse = vendorQueryRepository.findAll();

			Collections.reverse(repoResponse); // For LIFO order

			log.info("Successfully fetched {} vendor queries", repoResponse.size());

			return repoResponse.stream().map(vendorQuery -> new VendorQueryInformationDTO(vendorQuery.getId(),
					vendorQuery.getVendorEmail(), vendorQuery.getQueryText(), vendorQuery.getVendorQueryStatus(),vendorQuery.getInProgressRemark(),vendorQuery.getClosedRemark())).collect(Collectors.toList());

		} catch (Exception e) {
			log.error("Error occurred while fetching vendor queries", e);
			throw new RuntimeException("Unable to retrieve vendor queries at this time");
		}
	}

	@Override
	 public VendorQuery updateStatus(Long id, VendorStatusType newStatus ,String remark) {
        log.info("Attempting to update status of VendorQuery with id {} to {}", id, newStatus);

        VendorQuery query = vendorQueryRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("VendorQuery not found with id {}", id);
                    return new RuntimeException("VendorQuery not found with id " + id);
                });

        query.setVendorQueryStatus(newStatus);
        if(newStatus.equals(VendorStatusType.INPROGRESS)) {
        	query.setInProgressRemark(remark);
        }else {
        	query.setClosedRemark(remark);
        }
        VendorQuery updated = vendorQueryRepository.save(query);

        log.info("Successfully updated VendorQuery id {} to status {}", id, newStatus);
        return updated;
    }
	
	
}
