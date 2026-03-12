package com.cloud.member.controller;

import com.cloud.member.dto.MemberRequest;
import com.cloud.member.dto.MemberResponse;
import com.cloud.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/api/members")
    public ResponseEntity<MemberResponse> createMember(@RequestBody MemberRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(memberService.save(request));
    }

    @GetMapping("/api/members/{memberId}")
    public ResponseEntity<MemberResponse> getMember(@PathVariable Long memberId) {
        log.info("[API - LOG] GET /api/members/{} 요청", memberId);
        return ResponseEntity.status(HttpStatus.OK).body(memberService.getOne(memberId));
    }

    @PostMapping("/api/members/{memberId}/profile-image")
    public ResponseEntity<Void> uploadImage(@PathVariable Long memberId, @RequestParam MultipartFile file) throws IOException {
        memberService.uploadProfileImage(memberId, file);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/api/members/{memberId}/profile-image")
    public ResponseEntity<String> getUrl(@PathVariable Long memberId) {
        return ResponseEntity.status(HttpStatus.OK).body(memberService.getProfileImageUrl(memberId));
    }

}
