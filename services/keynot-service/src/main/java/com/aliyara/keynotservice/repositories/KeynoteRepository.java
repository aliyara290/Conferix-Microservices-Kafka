package com.aliyara.keynotservice.repositories;

import com.aliyara.keynotservice.entities.Keynote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeynoteRepository extends JpaRepository<Keynote, Long> {
}
