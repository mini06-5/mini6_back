package com.aivle.bookapp.repository;

import com.aivle.bookapp.domain.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    Optional<CommentLike> findByCommentIdAndUserUserId(Long commentId, String userId);

    int countByCommentId(Long commentId);

    void deleteByCommentId(Long commentId);
}