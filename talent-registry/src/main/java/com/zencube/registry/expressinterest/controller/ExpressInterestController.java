package com.zencube.registry.expressinterest.controller;

import com.zencube.registry.enterprise.entity.EnterpriseAccount;
import com.zencube.registry.enterprise.repository.EnterpriseAccountRepository;
import com.zencube.registry.expressinterest.dto.BookmarkRequest;
import com.zencube.registry.expressinterest.dto.FormalRequestResponse;
import com.zencube.registry.expressinterest.dto.InterestResponse;
import com.zencube.registry.expressinterest.exception.InterestException;
import com.zencube.registry.expressinterest.service.ExpressInterestService;
import com.zencube.registry.security.model.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/interests")
@RequiredArgsConstructor
@Tag(name = "Enterprise Interests", description = "Enterprise candidate bookmarking and formal requests")
public class ExpressInterestController {

    private final ExpressInterestService expressInterestService;
    private final EnterpriseAccountRepository enterpriseAccountRepository;

    private UUID getEnterpriseId(CustomUserDetails userDetails) {
        return enterpriseAccountRepository.findByUserId(userDetails.getUserId())
                .map(EnterpriseAccount::getId)
                .orElseThrow(() -> new InterestException("Authenticated user does not have an active Enterprise Account."));
    }

    @PostMapping("/bookmark")
    @PreAuthorize("hasRole('ENTERPRISE_ADMIN') or hasRole('ENTERPRISE_RECRUITER')")
    @Operation(summary = "Bookmark Candidate", description = "Bookmark a student, optionally against a specific opening. Requires ENTERPRISE.")
    public ResponseEntity<InterestResponse> bookmarkCandidate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody BookmarkRequest request) {
        
        UUID enterpriseId = getEnterpriseId(userDetails);
        InterestResponse response = expressInterestService.bookmark(enterpriseId, request.studentId(), request.openingId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{interestId}/formal-request")
    @PreAuthorize("hasRole('ENTERPRISE_ADMIN') or hasRole('ENTERPRISE_RECRUITER')")
    @Operation(summary = "Escalate to Formal Request", description = "Escalate a bookmarked candidate to a formal request. Requires ENTERPRISE.")
    public ResponseEntity<FormalRequestResponse> createFormalRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID interestId) {
        
        UUID enterpriseId = getEnterpriseId(userDetails);
        FormalRequestResponse response = expressInterestService.formalRequest(enterpriseId, interestId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/bookmarks")
    @PreAuthorize("hasRole('ENTERPRISE_ADMIN') or hasRole('ENTERPRISE_RECRUITER')")
    @Operation(summary = "Get My Bookmarks", description = "List all bookmarked candidates for the enterprise. Requires ENTERPRISE.")
    public ResponseEntity<Page<InterestResponse>> getMyBookmarks(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable) {
        
        UUID enterpriseId = getEnterpriseId(userDetails);
        return ResponseEntity.ok(expressInterestService.getMyBookmarks(enterpriseId, pageable));
    }

    @GetMapping("/formal-requests")
    @PreAuthorize("hasRole('ENTERPRISE_ADMIN') or hasRole('ENTERPRISE_RECRUITER')")
    @Operation(summary = "Get My Formal Requests", description = "List all formal requests made by the enterprise. Requires ENTERPRISE.")
    public ResponseEntity<Page<FormalRequestResponse>> getMyFormalRequests(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable) {
        
        UUID enterpriseId = getEnterpriseId(userDetails);
        return ResponseEntity.ok(expressInterestService.getMyFormalRequests(enterpriseId, pageable));
    }
}
