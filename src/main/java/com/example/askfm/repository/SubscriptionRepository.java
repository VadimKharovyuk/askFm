package com.example.askfm.repository;

import com.example.askfm.model.Subscription;
import com.example.askfm.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    boolean existsBySubscriberAndSubscribedTo(User subscriber, User subscribedTo);
    void deleteBySubscriberAndSubscribedTo(User subscriber, User subscribedTo);
    List<Subscription> findBySubscriber(User subscriber);
    List<Subscription> findBySubscribedTo(User subscribedTo);
    long countBySubscriber(User subscriber);
    long countBySubscribedTo(User subscribedTo);

    @Query("SELECT s.subscriber.id FROM Subscription s WHERE s.subscribedTo.id = :creatorId")
    List<Long> findSubscriberIdsBySubscribedToId(@Param("creatorId") Long creatorId);

    @Query("SELECT s.subscriber.username FROM Subscription s WHERE s.subscribedTo.username = :username")
    List<String> findSubscriberUsernamesByUsername(@Param("username") String username);
}