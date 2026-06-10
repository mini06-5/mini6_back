package com.aivle.bookapp;

import com.aivle.bookapp.domain.Book;
import com.aivle.bookapp.repository.BookRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@SpringBootApplication
public class BookappApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookappApplication.class, args);
	}

	// 교안 p.126: 실습용 초기 데이터 5권 등록 (프론트엔드 맞춤 확장)
	@Bean
	public CommandLineRunner init(BookRepository repo) {
		return args -> {
			if (repo.count() == 0) {
				String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

				repo.save(Book.builder()
						.title("별을 건너는 개발자")
						.author("김하린")
						.publisher("걷기출판")
						.content("우주 정거장에서 코드를 작성하는 개발자가 잃어버린 기억과 데이터를 찾아가는 이야기입니다.")
						.tags("#SF #성장 #개발자")
						.coverImageUrl("")
						.likeCount(5)
						.createdAt(now)
						.updatedAt(now)
						.build());

				repo.save(Book.builder()
						.title("달빛 아래의 자바")
						.author("이민우")
						.publisher("에이블북스")
						.content("자바 프로그래밍의 기초부터 심화까지 달빛 아래에서 조용히 읽을 수 있는 가이드라인을 제공합니다.")
						.tags("#자바 #프로그래밍 #학습")
						.coverImageUrl("")
						.likeCount(12)
						.createdAt(now)
						.updatedAt(now)
						.build());

				repo.save(Book.builder()
						.title("시간을 달리는 알고리즘")
						.author("박지성")
						.publisher("미래정보기술")
						.content("자료구조와 알고리즘이 우리 실생활에 미치는 영향과 복잡한 문제 해결 방식을 재미있게 소설로 풀어낸 책입니다.")
						.tags("#알고리즘 #소설 #IT")
						.coverImageUrl("")
						.likeCount(8)
						.createdAt(now)
						.updatedAt(now)
						.build());

				repo.save(Book.builder()
						.title("스프링의 정석")
						.author("이강산")
						.publisher("코드프레스")
						.content("스프링 부트 프레임워크를 정복하기 위한 입문서로 백엔드 개발자들의 필수 권장도서입니다.")
						.tags("#스프링 #백엔드 #추천")
						.coverImageUrl("")
						.likeCount(24)
						.createdAt(now)
						.updatedAt(now)
						.build());

				repo.save(Book.builder()
						.title("파이썬의 정석")
						.author("김에이블")
						.publisher("에이블북스")
						.content("파이썬 기초 문법부터 데이터 분석과 웹 개발 기초까지 한 권으로 다루는 종합 안내서입니다.")
						.tags("#파이썬 #데이터 #기초")
						.coverImageUrl("")
						.likeCount(15)
						.createdAt(now)
						.updatedAt(now)
						.build());
			}
		};
	}
}
