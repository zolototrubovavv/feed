package com.github.zolototrubovavv.feed.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.github.zolototrubovavv.feed.entity.Category;

public interface CategoryRepository extends JpaRepository<Category,Long> {
    
    @Query("""
    SELECT COUNT(c) > 0
    FROM Category c
    WHERE LOWER(c.name) = LOWER(:name)
      AND (:id IS NULL OR c.id != :id)
    """)
    boolean existsByNameIgnoreCase(@Param("name") String name, @Param("id") Long id);
}
