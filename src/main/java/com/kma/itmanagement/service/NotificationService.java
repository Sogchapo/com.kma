package com.kma.itmanagement.service;

import com.kma.itmanagement.model.Notification;
import com.kma.itmanagement.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void sendNotification(String username, String title, String message) {
        Notification notification = new Notification(username, title, message);
        notificationRepository.save(notification);
    }

    public List<Notification> getUserNotifications(String username) {
        return notificationRepository.findByRecipientUsernameOrderByCreatedAtDesc(username);
    }

    public long getUnreadCount(String username) {
        return notificationRepository.countByRecipientUsernameAndIsReadFalse(username);
    }

    public void markAsRead(Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }
}