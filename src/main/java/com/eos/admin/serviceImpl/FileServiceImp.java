package com.eos.admin.serviceImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.eos.admin.enums.FileType;
import com.eos.admin.exception.FileAlreadyExistsException;
import com.eos.admin.service.FileService;

@Service
public class FileServiceImp implements FileService {

	@Value("${project.file.upload-dir}")
	private String path;

	@Override
	public List<String> uploadImage(String path, List<MultipartFile> files, String aadhaarNumber) throws IOException {
		List<String> fileNames = new ArrayList();

		if (files == null || files.isEmpty()) {
			throw new IOException("No files provided");
		}

		File directory = new File(path);
		if (!directory.exists()) {
			if (!directory.mkdirs()) {
				throw new IOException("Failed to create directory: " + path);
			}
		}

		int count = 1;
		for (MultipartFile file : files) {
			String name = file.getOriginalFilename();
			if (name == null || name.isEmpty()) {
				throw new IOException("Invalid file name");
			}

			String fileExtension = name.substring(name.lastIndexOf("."));
			String fileName = aadhaarNumber + "_" + count + fileExtension; 
			String filePath = path + File.separator + fileName;
			Path destinationPath = Paths.get(filePath);
			
			if(Files.exists(destinationPath)) {
				throw new FileAlreadyExistsException("File already exists : " + fileName);
			}

			try {
				Files.copy(file.getInputStream(), Paths.get(filePath));
				fileNames.add(fileName); // Add the file name to the result list
				count++; // Increment the count for the next file
			} catch (IOException e) {
				e.printStackTrace();
				throw new IOException("Failed to upload file", e);
			}
		}

		return fileNames;
	}
	
//	@Override
//	public List<String> uploadImage2(String path, List<MultipartFile> files, String aadhaarNumber) throws IOException {
//	    List<String> fileNames = new ArrayList<>();
//
//	    if (files == null || files.isEmpty()) {
//	        throw new IOException("No files provided");
//	    }
//
//	    File directory = new File(path);
//	    if (!directory.exists() && !directory.mkdirs()) {
//	        throw new IOException("Failed to create directory: " + path);
//	    }
//
//	    int uploadedFileCount = 1; // Only increment this when a file is actually uploaded
//
//	    for (MultipartFile file : files) {
//	        String originalName = file.getOriginalFilename();
//
//	        if (originalName == null || originalName.isEmpty() || file.isEmpty()) {
//	            fileNames.add(null); // Maintain position
//	            continue;
//	        }
//
//	        String fileExtension = originalName.substring(originalName.lastIndexOf("."));
//	        String fileName = aadhaarNumber + "_" + uploadedFileCount + fileExtension;
//	        Path destinationPath = Paths.get(path, fileName);
//
//	        try {
//	            Files.copy(file.getInputStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
//	            fileNames.add(fileName); // Maintain order for UI divs
//	            uploadedFileCount++;     // Only increase if actual file is uploaded
//	        } catch (IOException e) {
//	            e.printStackTrace();
//	            throw new IOException("Failed to upload file: " + fileName, e);
//	        }
//	    }
//
//	    return fileNames;
//	}
//	@Override
//	public List<String> uploadImage2(String path, List<MultipartFile> files, String aadhaarNumber) throws IOException {
//	    List<String> fileNames = Arrays.asList(null, null, null, null); // For fileName1 to fileName4
//
//	    if (files == null || files.isEmpty()) {
//	        throw new IOException("No files provided");
//	    }
//
//	    // Filter out null or empty files
//	    List<MultipartFile> validFiles = new ArrayList<>();
//	    for (MultipartFile file : files) {
//	        if (file != null && !file.isEmpty() && file.getOriginalFilename() != null && !file.getOriginalFilename().isEmpty()) {
//	            validFiles.add(file);
//	        }
//	    }
//
//	    if (validFiles.isEmpty()) {
//	        throw new IOException("No valid files to upload");
//	    }
//
//	    File directory = new File(path);
//	    if (!directory.exists() && !directory.mkdirs()) {
//	        throw new IOException("Failed to create directory: " + path);
//	    }
//
//	    int fileIndex = 0;
//	    for (int i = 0; i < validFiles.size(); i++) {
//	        MultipartFile file = validFiles.get(i);
//	        String originalName = file.getOriginalFilename();
//	        String fileExtension = originalName.substring(originalName.lastIndexOf("."));
//
//	        int nameIndex;
//	        if (i == 0) {
//	            nameIndex = 1; // First valid file
//	        } else if (i == validFiles.size() - 1) {
//	            nameIndex = 4; // Last valid file
//	        } else {
//	            nameIndex = ++fileIndex + 1; // Middle files as 2, 3...
//	        }
//
//	        // Adjust if only 1 file: it should be both first and last (1 and 4 same file)
//	        if (validFiles.size() == 1) {
//	            nameIndex = 4; // As per your rule, a single file is treated as last
//	            fileNames.set(0, aadhaarNumber + "_1" + fileExtension); // Also save as first
//	            Path firstPath = Paths.get(path, aadhaarNumber + "_1" + fileExtension);
//	            Files.copy(file.getInputStream(), firstPath, StandardCopyOption.REPLACE_EXISTING);
//	        }
//
//	        String finalFileName = aadhaarNumber + "_" + nameIndex + fileExtension;
//	        Path destinationPath = Paths.get(path, finalFileName);
//
//	        Files.copy(file.getInputStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
//	        fileNames.set(nameIndex - 1, finalFileName); // Indexing from 0
//	    }
//
//	    return fileNames;
//	}

	@Override
	public List<String> uploadImage2(String path, List<MultipartFile> files, String aadhaarNumber) throws IOException {
	    // Initialize with 4 nulls, but as a mutable list
	    List<String> fileNames = new ArrayList<>(Arrays.asList(null, null, null, null));

	    if (files == null || files.isEmpty()) {
	        // No files provided, just return list of nulls or handle as needed
	        return fileNames; // or throw exception if you prefer
	    }

	    // Filter out null or empty files
	    List<MultipartFile> validFiles = new ArrayList<>();
	    for (MultipartFile file : files) {
	        if (file != null && !file.isEmpty() && file.getOriginalFilename() != null && !file.getOriginalFilename().isEmpty()) {
	            validFiles.add(file);
	        }
	    }

	    if (validFiles.isEmpty()) {
	        // No valid files, return the list as is (all nulls)
	        return fileNames;
	    }

	    // Create directory if it doesn't exist
	    File directory = new File(path);
	    if (!directory.exists() && !directory.mkdirs()) {
	        throw new IOException("Failed to create directory: " + path);
	    }

	    int fileIndex = 0; // For middle files
	    for (int i = 0; i < validFiles.size(); i++) {
	        MultipartFile file = validFiles.get(i);
	        String originalName = file.getOriginalFilename();
	        String fileExtension = originalName.substring(originalName.lastIndexOf(".")); // assumes filename has extension

	        int nameIndex;
	        if (i == 0 && validFiles.size() == 1) {
	            // Single valid file: treat as both first and last
	            nameIndex = 4; // last
	            // Save as first too
	            String finalFileName1 = aadhaarNumber + "_1" + fileExtension;
	            Path firstPath = Paths.get(path, finalFileName1);
	            Files.copy(file.getInputStream(), firstPath, StandardCopyOption.REPLACE_EXISTING);
	            fileNames.set(0, finalFileName1);
	        }

	        if (i == 0 && validFiles.size() > 1) {
	            // First file
	            nameIndex = 1;
	        } else if (i == validFiles.size() - 1) {
	            // Last file
	            nameIndex = 4;
	        } else {
	            // Middle files
	            nameIndex = ++fileIndex + 1; // 2, 3
	        }

	        String finalFileName = aadhaarNumber + "_" + nameIndex + fileExtension;
	        Path destinationPath = Paths.get(path, finalFileName);
	        Files.copy(file.getInputStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
	        // Save filename in list
	        fileNames.set(nameIndex - 1, finalFileName);
	    }

	    return fileNames;
	}

//	@Override
//	public String uploadSingleImages(String path, MultipartFile file, FileType fileType) throws IOException {
//	    if (file == null || file.isEmpty() || file.getOriginalFilename() == null || file.getOriginalFilename().isEmpty()) {
//	        throw new IllegalArgumentException("Invalid file provided.");
//	    }
//
//	    // Create directory if it doesn't exist
//	    File directory = new File(path);
//	    if (!directory.exists() && !directory.mkdirs()) {
//	        throw new IOException("Failed to create directory: " + path);
//	    }
//
//	    // Map FileType to suffix number
//	    int suffix;
//	    switch (fileType) {
//	        case PAN:
//	            suffix = 2;
//	            break;
//	        case GST:
//	            suffix = 3;
//	            break;
//	        case MSME:
//	            suffix = 4;
//	            break;
//	        case CHEQUE:
//	            suffix = 1;
//	            break;
//	        default:
//	            throw new IllegalArgumentException("Unsupported file type: " + fileType);
//	    }
//
//	    // Get file extension
//	    String fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.'));
//
//	    // Use timestamp or UUID as unique file identifier
//	    String fileIdentifier = fileType.name().toLowerCase() + "_" + System.currentTimeMillis(); // Or use UUID.randomUUID()
//
//	    // Construct final filename
//	    String finalFileName = fileIdentifier + "_" + suffix + fileExtension;
//
//	    // Save the file
//	    Path destinationPath = Paths.get(path, finalFileName);
//	    Files.copy(file.getInputStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
//
//	    return finalFileName;
//	}

	@Override
	public String uploadSingleImages(String path, MultipartFile file, String fileNo, FileType fileType) throws IOException {
	    if (file == null || file.isEmpty() || file.getOriginalFilename() == null || file.getOriginalFilename().isEmpty()) {
	        throw new IllegalArgumentException("Invalid file provided.");
	    }

	    // Create directory if it doesn't exist
	    File directory = new File(path);
	    if (!directory.exists() && !directory.mkdirs()) {
	        throw new IOException("Failed to create directory: " + path);
	    }

	    // Determine suffix based on fileType
	    int index;
	    switch (fileType) {
	        case PAN:
	            index = 1;
	            break;
	        case GST:
	            index = 2;
	            break;
	        case MSME:
	            index = 3;
	            break;
	        case CHEQUE:
	            index = 4;
	            break;
	        default:
	            throw new IllegalArgumentException("Unsupported file type: " + fileType);
	    }

	    String fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.'));
	    String finalFileName = fileNo + "_" + index + fileExtension;

	    Path destinationPath = Paths.get(path, finalFileName);
	    Files.copy(file.getInputStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

	    return finalFileName;
	}

}
