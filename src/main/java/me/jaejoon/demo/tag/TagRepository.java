package me.jaejoon.demo.tag;

import me.jaejoon.demo.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public interface TagRepository extends JpaRepository<Tag,Long> {
    Tag findByTitle(String title);
}
