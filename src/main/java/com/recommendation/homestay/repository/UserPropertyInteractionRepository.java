package com.recommendation.homestay.repository;

import com.recommendation.homestay.entity.User;
import com.recommendation.homestay.entity.UserPropertyInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPropertyInteractionRepository extends JpaRepository<UserPropertyInteraction, Long> {
    
    List<UserPropertyInteraction> findByUser(User user);
    
    @Query("SELECT i FROM UserPropertyInteraction i WHERE i.user.id = :userId " +
           "ORDER BY i.createdAt DESC")
    List<UserPropertyInteraction> findRecentInteractionsByUser(@Param("userId") Long userId);

    @Query("SELECT i.property.id, COUNT(i) AS interactionCount " +
            "FROM UserPropertyInteraction i " +
            "WHERE i.user.id = :userId " +
            "GROUP BY i.property.id " +
            "ORDER BY interactionCount DESC")
    List<Object[]> findMostInteractedProperties(@Param("userId") Long userId);
}
