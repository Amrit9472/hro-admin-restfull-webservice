package com.eos.admin.service;

import java.io.IOException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.eos.admin.enums.FileType;

@Service
public interface FileService {
	 List<String> uploadImage(String path , List<MultipartFile> file,String aadhaarNumber) throws IOException;

	List<String> uploadImage2(String path, List<MultipartFile> files, String aadhaarNumber) throws IOException;

	String uploadSingleImages(String path, MultipartFile file, String fileNo, FileType fileType) throws IOException;
}
