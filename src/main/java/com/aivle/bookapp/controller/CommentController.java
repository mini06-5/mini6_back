package com.aivle.bookapp.controller;

import com.aivle.bookapp.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/books/{bookId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getComments(
            @PathVariable Long bookId,
            @RequestParam(value = "sort", required = false, defaultValue = "likes") String sort) {
        return ResponseEntity.ok(commentService.getCommentsByBook(bookId, sort));
    }

    @PostMapping
    public ResponseEntity<String> createComment(
            @PathVariable Long bookId,
            @AuthenticationPrincipal String userId,
            @RequestBody Map<String, String> requestBody) {
        String content = requestBody.get("content");
        commentService.createComment(bookId, userId, content);

        return ResponseEntity.ok("댓글이 성공적으로 등록되었습니다.");
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal String userId) {

        commentService.deleteComment(commentId, userId);
        return ResponseEntity.ok("댓글이 성공적으로 삭제되었습니다.");
    }

    @PostMapping("/{commentId}/like") // 주소: /books/{bookId}/comments/{commentId}/like
    public ResponseEntity<Map<String, Object>> toggleLike(
            @PathVariable Long commentId,
            @AuthenticationPrincipal String userId) {

        boolean isLiked = commentService.toggleCommentLike(commentId, userId);

        Map<String, Object> response = new HashMap<>();
        response.put("liked", isLiked);
        response.put("message", isLiked ? "좋아요를 눌렀습니다." : "좋아요를 취소했습니다.");

        return ResponseEntity.ok(response);
    }
}
