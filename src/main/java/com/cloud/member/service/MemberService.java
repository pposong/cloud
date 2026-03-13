package com.cloud.member.service;

import com.cloud.member.dto.MemberRequest;
import com.cloud.member.dto.MemberResponse;
import com.cloud.member.entity.Member;
import com.cloud.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final S3Service s3Service;

    @Transactional
    public MemberResponse save(MemberRequest request) {

        Member member = new Member(request.getMemberName(), request.getAge(), request.getMbti());

        Member savedMember = memberRepository.save(member);
        return new MemberResponse(savedMember);
    }


    @Transactional
    public MemberResponse getOne(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> {
                    log.error("[API - ERROR] 존재하지 않는 회원 ID: {}", memberId);
                    return new IllegalStateException("없는 유저 입니다.");
                }
        );

        return new MemberResponse(member);
    }

    @Transactional
    public void uploadProfileImage(Long memberId, MultipartFile file) throws IOException {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new IllegalStateException("없는 유저 입니다.")
        );

        String fileName = s3Service.uploadFile(file); // S3에 파일 업로드 → fileName 받기

        member.updateProfileImageUrl(fileName); // Member에  URL 저장
    }

    public String getProfileImageUrl(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new IllegalStateException("없는 유저 입니다.")
        );

        String fileName = member.getProfileImageUrl(); // Member에서 fileName 꺼내기

        return s3Service.getCloudFrontUrl(fileName); // getCloudFrontUrl()이 이미 String을 반환하니까 .toString() 불필요
    }
}