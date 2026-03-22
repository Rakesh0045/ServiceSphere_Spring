package com.kce.localservices.service;

import com.kce.localservices.entity.Notification;
import com.kce.localservices.entity.User;
import com.kce.localservices.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserService userService;

    /**
     * Create a notification for a specific user.
     */
    public void createNotification(Integer userId, String title, String message,
                                    String notificationType, Integer referenceId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setNotificationType(notificationType);
        notification.setReferenceId(referenceId);
        notification.setIsRead(false);
        notificationRepository.save(notification);
    }

    /**
     * Get all notifications for the currently authenticated user.
     */
    public List<Map<String, Object>> getNotificationsForCurrentUser() {
        User currentUser = userService.getCurrentUser();
        List<Notification> notifications = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(currentUser.getId());

        return notifications.stream().map(n -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", n.getId());
            map.put("title", n.getTitle());
            map.put("message", n.getMessage());
            map.put("isRead", n.getIsRead());
            map.put("notificationType", n.getNotificationType());
            map.put("referenceId", n.getReferenceId());
            map.put("createdAt", n.getCreatedAt());
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * Get unread notification count for current user.
     */
    public long getUnreadCount() {
        User currentUser = userService.getCurrentUser();
        return notificationRepository.countByUserIdAndIsReadFalse(currentUser.getId());
    }

    /**
     * Mark a specific notification as read.
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        User currentUser = userService.getCurrentUser();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUserId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    /**
     * Mark ALL notifications as read for current user.
     */
    @Transactional
    public void markAllAsRead() {
        User currentUser = userService.getCurrentUser();
        notificationRepository.markAllReadByUserId(currentUser.getId());
    }
}