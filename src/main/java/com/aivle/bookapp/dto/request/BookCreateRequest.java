package com.aivle.bookapp.dto.request;

import com.aivle.bookapp.domain.Book;
import com.aivle.bookapp.domain.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BookCreateRequest {

    @NotBlank
    private String title;

    @NotNull
    private User author;

    private String publisher;
    private String content;
    private String tags;
    private String coverImageUrl;
    private Integer likeCount;
    private String createdAt;
    private String updatedAt;

    public Book toEntity() {
        return Book.builder()
                .title(title)
                .author(author)
                .publisher(publisher)
                .content(content)
                .tags(tags)
                .coverImageUrl(coverImageUrl)
                .likeCount(likeCount)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
