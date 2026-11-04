package com.staynest.repository;

import com.staynest.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    Optional<Favorite> findByUserIdAndPropertyId(Long userId, Long propertyId);
    boolean existsByUserIdAndPropertyId(Long userId, Long propertyId);
    List<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId);
    void deleteByUserIdAndPropertyId(Long userId, Long propertyId);
    long countByPropertyId(Long propertyId);
}
