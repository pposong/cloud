package com.cloud.member.dto;

import com.cloud.member.entity.Member;
import lombok.Getter;

@Getter
public class MemberResponse {

    private final Long id;
    private final String memberName;
    private final int age;
    private final String mbti;

    public MemberResponse(Member member) {
        this.id = member.getId();
        this.memberName = member.getMemberName();
        this.age = member.getAge();
        this.mbti = member.getMbti();
    }
}
