package com.rental.camp.community.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class CommunityPostUpdateRequestDto {

    private Long userId; // 수정 요청을 보낸 사용자 ID
    private String title; // 수정할 제목
    private String content; // 수정할 내용
    private List<String> imagesToDelete; // 삭제할 이미지 경로 리스트
    private List<MultipartFile> newImages; // 추가할 새 이미지 파일 리스트

    public CommunityPostUpdateRequestDto() {}

}