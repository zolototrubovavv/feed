package com.github.zolototrubovavv.feed.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.github.zolototrubovavv.feed.entity.News;

public interface NewsRepository extends JpaRepository<News,Long> {
    List<News> findByCategory_Id(Long categoryId);
    // Page<News> findByCategory_Id(Long categoryId, Pageable pageable);

    @Query("SELECT n FROM News n WHERE n.publishedAt IS NOT NULL ORDER BY n.publishedAt DESC LIMIT 10")
    List<News> findLatest10Published();
}
