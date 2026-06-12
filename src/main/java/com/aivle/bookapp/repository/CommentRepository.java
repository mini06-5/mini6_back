package com.aivle.bookapp.repository;

import com.aivle.bookapp.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByBookIdOrderByCreatedAtDesc(Long bookId);
    @Query("SELECT c FROM Comment c " +
            "LEFT JOIN CommentLike cl ON cl.comment.id = c.id " +
            "WHERE c.book.id = :bookId " +
            "GROUP BY c.id " +
            "ORDER BY COUNT(cl.id) DESC, c.createdAt DESC")
    List<Comment> findByBookIdOrderByLikesDesc(@Param("bookId") Long bookId);
}
