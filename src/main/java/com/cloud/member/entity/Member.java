package com.cloud.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String memberName;
    private int age;
    private String mbti;
    private String profileImageUrl;

    public Member(String memberName, int age, String mbti) {
        this.memberName = memberName;
        this.age = age;
        this.mbti = mbti;
    }

    public void updateProfileImageUrl(String url) {
        this.profileImageUrl = url;
    }
}
