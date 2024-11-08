package com.rental.camp.community.service;

import com.rental.camp.community.dto.CommunityPostRequestDto;
import com.rental.camp.community.dto.CommunityPostResponseDto;
import com.rental.camp.community.dto.CommunityPostUpdateRequestDto;
import com.rental.camp.community.dto.PageResponseDto;
import com.rental.camp.community.model.CommunityLike;
import com.rental.camp.community.model.CommunityPost;
import com.rental.camp.community.model.CommunityPostImage;
import com.rental.camp.community.model.type.CommunityPostCategory;
import com.rental.camp.community.repository.CommunityLikeRepository;
import com.rental.camp.community.repository.CommunityPostImageRepository;
import com.rental.camp.community.repository.CommunityPostRepository;
import com.rental.camp.community.repository.CommunityPostRepositoryCustom;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommunityPostServiceImpl implements CommunityPostService {

    private final CommunityPostRepository postRepository;
    private final CommunityPostImageRepository imageRepository;
    private final CommunityLikeRepository communityLikeRepository;
//    private final CommunityPostRepositoryCustom customRepository;

    public CommunityPostServiceImpl(CommunityPostRepository postRepository, CommunityPostImageRepository imageRepository, CommunityLikeRepository communityLikeRepository/*, CommunityPostRepositoryCustom customRepository*/) {
        this.postRepository = postRepository;
        this.imageRepository = imageRepository;
        this.communityLikeRepository = communityLikeRepository;
//        this.customRepository = customRepository;
    }

    @Transactional
    public CommunityPostResponseDto createPost(CommunityPostRequestDto requestDto) throws Exception {
        // CommunityPost 엔티티에 기본 정보 저장
        CommunityPost post = new CommunityPost();
        post.setUserId(requestDto.getUserId());
        post.setTitle(requestDto.getTitle());
        post.setContent(requestDto.getContent());
        post.setCategory(CommunityPostCategory.valueOf(requestDto.getCategory()));
        post.setLikes(0);
        post.setViewCount(0);
        post.setIsDeleted(false);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());

        // 후기 글인 경우, rating 값 설정
        if ("REVIEW".equals(requestDto.getCategory()) && requestDto.getRating() != null) {
            post.setRating(requestDto.getRating());
        }

        // CommunityPost 엔티티 저장
        CommunityPost savedPost = postRepository.save(post);

        // 이미지 파일 저장 및 이미지 경로 리스트 생성
        List<String> imagePaths = new ArrayList<>();
        List<MultipartFile> images = requestDto.getImages();
        if (images != null && !images.isEmpty()) {
            for (int i = 0; i < images.size(); i++) {
                String imagePath = saveImageFile(images.get(i));
                imagePaths.add(imagePath);  // 경로를 리스트에 추가

                CommunityPostImage postImage = new CommunityPostImage();
                postImage.setCommunityPostId(savedPost.getId());
                postImage.setImagePath(imagePath);
                postImage.setImageOrder(i + 1);
                postImage.setIsDeleted(false);
                postImage.setCreatedAt(LocalDateTime.now());
                postImage.setUpdatedAt(LocalDateTime.now());

                imageRepository.save(postImage);
            }
        }

        // 응답 DTO 생성 및 반환
        return new CommunityPostResponseDto(savedPost, imagePaths);
    }

    public String saveImageFile(MultipartFile file) throws Exception {
        String folderPath = "uploads/images/";
        Path uploadPath = Paths.get(folderPath);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.write(filePath, file.getBytes());

//        return filePath.toString();
        return "/uploads/images/" + fileName;
    }

    @Transactional
    public CommunityPostResponseDto updatePost(Long postId, CommunityPostUpdateRequestDto updateRequestDto, List<MultipartFile> newImages) throws Exception {
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 사용자가 작성자인지 확인
        if (!post.getUserId().equals(updateRequestDto.getUserId())) {
            throw new RuntimeException("수정 권한이 없습니다.");
        }

        // 제목 및 내용 업데이트
        if (updateRequestDto.getTitle() != null) {
            post.setTitle(updateRequestDto.getTitle());
        }
        if (updateRequestDto.getContent() != null) {
            post.setContent(updateRequestDto.getContent());
        }
        post.setUpdatedAt(LocalDateTime.now());

        // 1. 기존 이미지 삭제
        List<String> imagesToDelete = updateRequestDto.getImagesToDelete();
        if (imagesToDelete != null && !imagesToDelete.isEmpty()) {
            for (String imagePath : imagesToDelete) {
                Optional<CommunityPostImage> optionalImage = imageRepository.findByImagePath(imagePath);
                if (optionalImage.isPresent()) {  // 값이 존재하는지 확인
                    CommunityPostImage image = optionalImage.get();  // 실제 객체 가져오기
                    imageRepository.delete(image); // DB에서 이미지 정보 삭제
                    try {
                        Files.deleteIfExists(Paths.get("uploads/images/" + imagePath)); // 파일 시스템에서 이미지 삭제
                    } catch (IOException e) {
                        e.printStackTrace(); // 파일 삭제 중 예외 발생 시 로그 출력
                    }
                }
            }
        }

        // 2. 새 이미지 추가
        int existingImageCount = imageRepository.countByCommunityPostId(postId); // 기존 이미지 개수 조회
        List<String> newImagePaths = new ArrayList<>();
        if (newImages != null && !newImages.isEmpty()) {
            for (int i = 0; i < newImages.size(); i++) {
                String newPath = saveImageFile(newImages.get(i)); // 새 이미지 저장

                // 새로운 image_order는 기존 이미지 수 + i + 1
                CommunityPostImage newImage = new CommunityPostImage();
                newImage.setCommunityPostId(postId); // 게시글 ID 설정
                newImage.setImagePath(newPath);
                newImage.setImageOrder(existingImageCount + i + 1); // 기존 이미지 개수 + 1부터 시작
                newImage.setCreatedAt(LocalDateTime.now());
                newImage.setUpdatedAt(LocalDateTime.now());
                newImage.setIsDeleted(false);

                // DB에 새 이미지 정보 저장
                imageRepository.save(newImage);
                newImagePaths.add(newPath);
            }
        }

        // 수정된 게시글 및 이미지 경로 반환
        return new CommunityPostResponseDto(post, newImagePaths);
    }

    @Transactional
    public void softDeletePost(Long postId, Long userId) throws AccessDeniedException {
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 사용자 검증: 작성자 또는 관리자만 삭제 가능
        if (!post.getUserId().equals(userId)) {
            throw new AccessDeniedException("삭제 권한이 없습니다.");
        }

        // 게시글의 is_deleted 상태 변경
        post.setIsDeleted(true);
        postRepository.save(post);

        // 관련된 이미지들도 소프트 삭제 처리
        List<CommunityPostImage> images = imageRepository.findByCommunityPostIdAndIsDeletedFalse(postId);
        for (CommunityPostImage image : images) {
            image.setIsDeleted(true);
            imageRepository.save(image); // 변경 사항 저장
        }
    }

//    @Transactional
//    public void deletePost(Long postId, Long userId) throws AccessDeniedException {
//        CommunityPost post = postRepository.findById(postId)
//                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
//
//        // 사용자 검증: 작성자 또는 관리자만 삭제 가능
//        if (!post.getUserId().equals(userId)) {
//            throw new AccessDeniedException("삭제 권한이 없습니다.");
//        }
//
//        // 게시글 관련 이미지 삭제
//        List<CommunityPostImage> images = imageRepository.findByCommunityPostId(postId);
//        for (CommunityPostImage image : images) {
//            imageRepository.delete(image);
//            try {
//                Files.deleteIfExists(Paths.get("uploads/images/" + image.getImagePath()));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        // 게시글 삭제
//        postRepository.delete(post);
//    }

    public CommunityPostResponseDto getPostDetail(Long id) {
        // is_deleted가 false인 게시글만 조회
        CommunityPost post = postRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."));

        List<String> imagePaths = imageRepository.findByCommunityPostIdAndIsDeletedFalse(id).stream()
                .map(CommunityPostImage::getImagePath)
                .collect(Collectors.toList());

        return new CommunityPostResponseDto(post, imagePaths);
    }

    public PageResponseDto getFreePosts(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CommunityPost> postPage = postRepository.findByCategoryAndIsDeletedFalse(CommunityPostCategory.FREE, pageRequest);

        List<CommunityPostResponseDto> posts = postPage.getContent().stream()
                .map(post -> {
                    List<String> imagePaths = retrieveImagePaths(post.getId()); // 이미지 경로 불러오기
                    return new CommunityPostResponseDto(post, imagePaths); // CommunityPostResponseDto로 매핑
                })
                .collect(Collectors.toList());

        return new PageResponseDto(posts, postPage); // CommunityPostResponseDto 목록을 전달
    }

    // 이미지 경로를 가져오는 메서드
    public List<String> retrieveImagePaths(Long postId) {
        return imageRepository.findByCommunityPostId(postId)
                .stream()
                .map(CommunityPostImage::getImagePath)
                .collect(Collectors.toList());
    }

    @Transactional
    public boolean toggleLike(Long postId, Long userId) {
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        Optional<CommunityLike> existingLike = communityLikeRepository.findByPostIdAndUserId(postId, userId);

        if (existingLike.isPresent()) {
            // 이미 좋아요를 누른 상태면 취소
            post.setLikes(post.getLikes() - 1);
            communityLikeRepository.deleteByPostIdAndUserId(postId, userId);
            return false; // 좋아요 취소 상태 반환
        } else {
            // 좋아요 추가
            post.setLikes(post.getLikes() + 1);
            communityLikeRepository.save(new CommunityLike(postId, userId));
            return true; // 좋아요 추가 상태 반환
        }


    }
    @Override
    public PageResponseDto getReviewPosts(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CommunityPost> postPage = postRepository.findByCategoryAndIsDeletedFalse(CommunityPostCategory.REVIEW, pageRequest);

        List<CommunityPostResponseDto> posts = postPage.getContent().stream()
                .map(post -> {
                    List<String> imagePaths = retrieveImagePaths(post.getId()); // 이미지 경로 불러오기
                    return new CommunityPostResponseDto(post, imagePaths); // CommunityPostResponseDto로 매핑
                })
                .collect(Collectors.toList());

        return new PageResponseDto(posts, postPage); // PageResponseDto 객체로 반환
    }



    @Override
    public List<CommunityPostResponseDto> searchPosts(String searchParam) {
        return postRepository.searchPosts(searchParam);
    }
}