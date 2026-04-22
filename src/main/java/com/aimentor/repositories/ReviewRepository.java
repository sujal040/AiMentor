package com.aimentor.repositories;

import com.aimentor.entity.ReviewHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<ReviewHistory,Long> {

}
