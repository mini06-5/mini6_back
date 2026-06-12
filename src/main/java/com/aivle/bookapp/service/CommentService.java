package com.aivle.bookapp.service;

import com.aivle.bookapp.domain.Book;
import com.aivle.bookapp.domain.Comment;
import com.aivle.bookapp.domain.CommentLike;
import com.aivle.bookapp.domain.User;
import com.aivle.bookapp.repository.BookRepository;
import com.aivle.bookapp.repository.CommentLikeRepository;
import com.aivle.bookapp.repository.CommentRepository;
import com.aivle.bookapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
    private final CommentRepository commentRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final CommentLikeRepository commentLikeRepository;

    public List<Map<String, Object>> getCommentsByBook(Long bookId, String sort) {
        List<Comment> comments;

        if ("likes".equalsIgnoreCase(sort)) {
            comments = commentRepository.findByBookIdOrderByLikesDesc(bookId); // 좋아요순
        } else {
            comments = commentRepository.findByBookIdOrderByCreatedAtDesc(bookId); // 기본값: 최신순
        }

        return comments.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("content", c.getContent());
            map.put("createdAt", c.getCreatedAt());
            map.put("nickname", c.getAuthor().getNickname());
            map.put("userId", c.getAuthor().getUserId());

            int likeCount = commentLikeRepository.countByCommentId(c.getId());
            map.put("likeCount", likeCount);
            return map;
         }).collect(Collectors.toList());
    }

    @Transactional
    public void createComment(Long bookId, String userId, String content) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 도서입니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        Comment comment = Comment.builder()
                .book(book)
                .author(user)
                .content(content)
                .build();
        commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, String userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        if (!comment.getAuthor().getUserId().equals(userId)) {
            throw new IllegalStateException("본인이 작성한 댓글만 삭제할 수 있습니다.");
        }
        commentLikeRepository.deleteByCommentId(commentId);
        commentRepository.delete(comment);
    }

    @Transactional
    public boolean toggleCommentLike(Long commentId, String userId) {
        // 1. 이미 좋아요를 눌렀는지 확인
        Optional<CommentLike> alreadyLike = commentLikeRepository.findByCommentIdAndUserUserId(commentId, userId);

        if (alreadyLike.isPresent()) {
            // 1-1. 이미 있다면? 취소(삭제) 처리
            commentLikeRepository.delete(alreadyLike.get());
            return false; // 좋아요 해제됨을 의미
        } else {
            // 1-2. 없다면? 새로 등록
            Comment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

            CommentLike commentLike = CommentLike.builder()
                    .comment(comment)
                    .user(user)
                    .build();

            commentLikeRepository.save(commentLike);
            return true; // 좋아요 등록됨을 의미
        }
    }
}
