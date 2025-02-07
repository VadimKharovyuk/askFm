package com.example.askfm.repository;

import com.example.askfm.model.Notification;
import org.hibernate.type.descriptor.converter.spi.JpaAttributeConverter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Получение количества непрочитанных уведомлений
    long countByUserUsernameAndIsReadFalse(String username);

    // Получение списка уведомлений пользователя, отсортированных по дате создания
    List<Notification> findByUserUsernameOrderByCreatedAtDesc(String username);

    // Опционально можно добавить метод для получения только непрочитанных уведомлений
    List<Notification> findByUserUsernameAndIsReadFalseOrderByCreatedAtDesc(String username);

    // Если нужна пагинация
    Page<Notification> findByUserUsername(String username, Pageable pageable);

    // Удаление прочитанных уведомлений старше определенной даты
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.isRead = true AND n.createdAt < :date")
    void deleteOldReadNotifications(@Param("date") LocalDateTime date);
}
