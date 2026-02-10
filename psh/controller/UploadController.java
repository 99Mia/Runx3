package org.run.runx3.psh.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;

@Controller
@Log4j2

// 브라우저에 업로드된 파일 보여주기, 다운로드 기능
public class UploadController {

    // application.properties 에 정의된 업로드 경로 주입
    @Value("${file.upload-dir}")
    private String uploadPath;
    
    // 업로드된 파일(이미지 포함)을 보여주는 메서드
    @GetMapping("/upload/display")
    @ResponseBody
    public ResponseEntity<Resource> display(String filename) {
        try {
            Resource resource = new FileSystemResource(uploadPath + "/" + filename);
            if (!resource.exists()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            HttpHeaders headers = new HttpHeaders();
            Path filePath = resource.getFile().toPath();
            headers.add("Content-Type", Files.probeContentType(filePath));

            return new ResponseEntity<>(resource, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 업로드된 파일 다운로드
    @GetMapping("/upload/download")
    @ResponseBody
    public ResponseEntity<Resource> download(String filename) {
        Resource resource = new FileSystemResource(uploadPath + "/" + filename);
        if (!resource.exists()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        String resourceName = resource.getFilename();
        HttpHeaders headers = new HttpHeaders();
        try {
            headers.add("Content-Disposition",
                    "attachment; filename=" + URLEncoder.encode(resourceName, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }



}