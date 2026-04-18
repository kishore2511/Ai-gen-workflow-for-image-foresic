package com.defense.forensic.controller;

import com.defense.forensic.dto.AnalysisResultDto;
import com.defense.forensic.service.AnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {
    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping("/upload")
    public ResponseEntity<AnalysisResultDto> upload(Authentication authentication,
                                                    @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(analysisService.processUpload(authentication.getName(), file));
    }

    @GetMapping("/history")
    public ResponseEntity<List<AnalysisResultDto>> history(Authentication authentication) {
        return ResponseEntity.ok(analysisService.findHistory(authentication.getName()));
    }
}
