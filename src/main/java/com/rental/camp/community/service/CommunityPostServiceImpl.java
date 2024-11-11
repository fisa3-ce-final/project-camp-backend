package com.rental.camp.community.service;

import com.rental.camp.community.dto.CommentResponseDto;
import com.rental.camp.community.dto.CommunityPostRequestDto;
import com.rental.camp.community.dto.CommunityPostResponseDto;
import com.rental.camp.community.dto.CommunityPostUpdateRequestDto;
import com.rental.camp.community.dto.PageResponseDto;
import com.rental.camp.community.model.CommunityLike;
import com.rental.camp.community.model.CommunityPost;
import com.rental.camp.community.model.CommunityPostImage;
import com.rental.camp.community.model.type.CommunityPostCategory;
import com.rental.camp.community.repository.CommentRepository;
import com.rental.camp.community.repository.CommunityLikeRepository;
import com.rental.camp.community.repository.CommunityPostImageRepository;
import com.rental.camp.community.repository.CommunityPostRepository;
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
    private final CommunityLikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final CommentService commentService;

    public CommunityPostServiceImpl(
            CommunityPostRepository postRepository,
            CommunityPostImageRepository imageRepository,
            CommunityLikeRepository likeRepository,
            CommentRepository commentRepository,
            CommentService commentService) {

        this.postRepository = postRepository;
        this.imageRepository = imageRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
        this.commentService = commentService;
    }

    @Transactional
    public CommunityPostResponseDto createPost(CommunityPostRequestDto requestDto) throws Exception {
        CommunityPost post = initializeCommunityPost(requestDto);
        CommunityPost savedPost = postRepository.save(post);

        List<String> imagePaths = saveImages(requestDto.getImages(), savedPost.getId());

        return new CommunityPostResponseDto(savedPost, imagePaths, new ArrayList<>());
    }

    private CommunityPost initializeCommunityPost(CommunityPostRequestDto requestDto) {
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

        if ("REVIEW".equals(requestDto.getCategory()) && requestDto.getRating() != null) {
            post.setRating(requestDto.getRating());
        }
        return post;
    }

    private List<String> saveImages(List<MultipartFile> images, Long postId) throws Exception {
        List<String> imagePaths = new ArrayList<>();
        if (images == null || images.isEmpty()) return imagePaths;

        for (int i = 0; i < images.size(); i++) {
            String imagePath = saveImageFile(images.get(i));
            imagePaths.add(imagePath);

            CommunityPostImage postImage = new CommunityPostImage();
            postImage.setCommunityPostId(postId);
            postImage.setImagePath(imagePath);
            postImage.setImageOrder(i + 1);
            postImage.setIsDeleted(false);
            postImage.setCreatedAt(LocalDateTime.now());
            postImage.setUpdatedAt(LocalDateTime.now());

            imageRepository.save(postImage);
        }
        return imagePaths;
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

        return "/uploads/images/" + fileName;
    }

    @Transactional
    public CommunityPostResponseDto updatePost(Long postId, CommunityPostUpdateRequestDto updateRequestDto, List<MultipartFile> newImages) throws Exception {
        CommunityPost post = validateOwnership(postId, updateRequestDto.getUserId());
        updatePostDetails(post, updateRequestDto);

        deleteImages(updateRequestDto.getImagesToDelete());
        List<String> newImagePaths = saveImages(newImages, postId);

        return new CommunityPostResponseDto(post, newImagePaths, new ArrayList<>());
    }

    private CommunityPost validateOwnership(Long postId, Long userId) {
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("수정 권한이 없습니다.");
        }
        return post;
    }

    private void updatePostDetails(CommunityPost post, CommunityPostUpdateRequestDto updateRequestDto) {
        if (updateRequestDto.getTitle() != null) post.setTitle(updateRequestDto.getTitle());
        if (updateRequestDto.getContent() != null) post.setContent(updateRequestDto.getContent());
        post.setUpdatedAt(LocalDateTime.now());
    }

    private void deleteImages(List<String> imagesToDelete) {
        if (imagesToDelete == null || imagesToDelete.isEmpty()) return;
        imagesToDelete.forEach(this::deleteImage);
    }

    private void deleteImage(String imagePath) {
        imageRepository.findByImagePath(imagePath).ifPresent(image -> {
            imageRepository.delete(image);
            try {
                Files.deleteIfExists(Paths.get("uploads/images/" + imagePath));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Transactional
    public void softDeletePost(Long postId, Long userId) throws AccessDeniedException {
        CommunityPost post = validateOwnership(postId, userId);
        post.setIsDeleted(true);
        postRepository.save(post);

        imageRepository.findByCommunityPostIdAndIsDeletedFalse(postId)
                .forEach(image -> {
                    image.setIsDeleted(true);
                    imageRepository.save(image);
                });

        commentRepository.findCustomCommentsByCommunityPostIdAndIsDeletedFalse(postId, PageRequest.of(0, Integer.MAX_VALUE))
                .forEach(comment -> {
                    comment.setIsDeleted(true);
                    commentRepository.save(comment);
                });
    }

    public CommunityPostResponseDto getPostDetail(Long id, int page, int size) {
        CommunityPost post = postRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."));

        List<String> imagePaths = retrieveImagePaths(id);
        Page<CommentResponseDto> commentPage = commentService.getCommentsByPostId(id, page, size);
        return new CommunityPostResponseDto(post, imagePaths, commentPage.getContent());
    }

    public PageResponseDto getFreePosts(int page, int size) {
        return getPostsByCategory(CommunityPostCategory.FREE, page, size);
    }

    @Override
    public PageResponseDto getReviewPosts(int page, int size) {
        return getPostsByCategory(CommunityPostCategory.REVIEW, page, size);
    }

    private PageResponseDto getPostsByCategory(CommunityPostCategory category, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CommunityPost> postPage = postRepository.findByCategoryAndIsDeletedFalse(category, pageRequest);

        List<CommunityPostResponseDto> posts = postPage.getContent().stream()
                .map(post -> new CommunityPostResponseDto(post, retrieveImagePaths(post.getId()), new ArrayList<>()))
                .collect(Collectors.toList());

        return new PageResponseDto(posts, postPage);
    }

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

        Optional<CommunityLike> existingLike = likeRepository.findByPostIdAndUserId(postId, userId);

        if (existingLike.isPresent()) {
            if (post.getLikes() > 0) {
                post.setLikes(post.getLikes() - 1);
            }
            likeRepository.deleteByPostIdAndUserId(postId, userId);
            return false;
        } else {
            post.setLikes(post.getLikes() + 1);
            likeRepository.save(new CommunityLike(postId, userId));
            return true;
        }
    }

    @Override
    public List<CommunityPostResponseDto> searchPosts(String searchParam) {
        return postRepository.searchPosts(searchParam);
    }
}