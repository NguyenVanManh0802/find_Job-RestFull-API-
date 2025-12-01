package vn.manh.findJob.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vn.manh.findJob.dto.ResponseData;
import vn.manh.findJob.dto.file.ResUploadFileDTO;
import vn.manh.findJob.exception.StorageException;
import vn.manh.findJob.service.FileService;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/files")
public class FileController {
    private final FileService fileService;



    @PostMapping("")
    public ResponseEntity<ResponseData<ResUploadFileDTO>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("folder") String folder
    ) throws URISyntaxException, IOException {
        //validate
        if(file.isEmpty() || file==null)
        {
            throw new StorageException("File is empty, Please upload a difference file");
        }
        //validate extension
        String fileName = file.getOriginalFilename();
        List<String> allowedExtensions = Arrays.asList("pdf","jpg", "jpeg", "png", "doc", "docx");
        boolean isValid=allowedExtensions.stream().anyMatch(item ->fileName.toLowerCase().endsWith(item));
        if(!isValid)
            throw new StorageException("Invalid file extension");
        //create folder
        // Validate MIME type
//        String contentType = file.getContentType();
//        if (!allowedMimeTypes.contains(contentType)) {
//            return ResponseEntity.badRequest().body("Invalid file type based on MIME type.");
//        }
//
//        // Check file size
//        long maxSize = 5 * 1024 * 1024; // 5 MB in bytes
//        if (file.getSize() > maxSize) {
//            //todo
//        }

        fileService.createUploadFolder(folder);

        //save file in folder
        String  uploadFile=fileService.store(file,folder);
        ResUploadFileDTO resUploadFileDTO=new ResUploadFileDTO(uploadFile, Instant.now());
        ResponseData<ResUploadFileDTO>responseData=new ResponseData<>(
                HttpStatus.CREATED.value(),
                "Upload single file successful",
                resUploadFileDTO

        );
        return ResponseEntity.ok(responseData);
    }

}
