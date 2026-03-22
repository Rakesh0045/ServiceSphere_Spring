import React, { useState, useEffect, useRef, useCallback } from 'react';
import { formatDistanceToNow } from 'date-fns';
import './NotificationBell.css';

const BellIcon = () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24"
        fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
        <path d="M13.73 21a2 2 0 0 1-3.46 0" />
    </svg>
);

const CheckIcon = () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24"
        fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <polyline points="20 6 9 17 4 12" />
    </svg>
);

const NOTIFICATION_ICONS = {
    BOOKING_CREATED: '📅',
    BOOKING_CONFIRMED: '✅',
    BOOKING_COMPLETED: '🎉',
    BOOKING_CANCELLED: '❌',
    REVIEW_RECEIVED: '⭐',
    DEFAULT: '🔔'
};

const NotificationBell = ({ axiosWithAuth }) => {
    const [notifications, setNotifications] = useState([]);
    const [unreadCount, setUnreadCount] = useState(0);
    const [isOpen, setIsOpen] = useState(false);
    const [loading, setLoading] = useState(false);
    const dropdownRef = useRef(null);
    const pollRef = useRef(null);

    const fetchUnreadCount = useCallback(async () => {
        try {
            const res = await axiosWithAuth.get('/notifications/unread-count');
            setUnreadCount(res.data.count);
        } catch (err) {
            // Silently fail — don't block UI
        }
    }, [axiosWithAuth]);

    const fetchNotifications = useCallback(async () => {
        setLoading(true);
        try {
            const res = await axiosWithAuth.get('/notifications');
            setNotifications(res.data);
            setUnreadCount(res.data.filter(n => !n.isRead).length);
        } catch (err) {
            // Silently fail
        } finally {
            setLoading(false);
        }
    }, [axiosWithAuth]);

    useEffect(() => {
        // Fetch count on mount
        fetchUnreadCount();

        // Poll for new notifications every 30 seconds
        pollRef.current = setInterval(fetchUnreadCount, 30000);
        return () => clearInterval(pollRef.current);
    }, [fetchUnreadCount]);

    // Close dropdown when clicking outside
    useEffect(() => {
        const handleClickOutside = (e) => {
            if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
                setIsOpen(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const handleOpen = () => {
        if (!isOpen) {
            fetchNotifications();
        }
        setIsOpen(prev => !prev);
    };

    const handleMarkRead = async (id, e) => {
        e.stopPropagation();
        try {
            await axiosWithAuth.put(`/notifications/${id}/read`);
            setNotifications(prev =>
                prev.map(n => n.id === id ? { ...n, isRead: true } : n)
            );
            setUnreadCount(prev => Math.max(0, prev - 1));
        } catch (err) { /* silent */ }
    };

    const handleMarkAllRead = async () => {
        try {
            await axiosWithAuth.put('/notifications/read-all');
            setNotifications(prev => prev.map(n => ({ ...n, isRead: true })));
            setUnreadCount(0);
        } catch (err) { /* silent */ }
    };

    return (
        <div className="notification-bell-wrapper" ref={dropdownRef}>
            <button className="bell-btn" onClick={handleOpen} aria-label="Notifications">
                <BellIcon />
                {unreadCount > 0 && (
                    <span className="bell-badge">{unreadCount > 99 ? '99+' : unreadCount}</span>
                )}
            </button>

            {isOpen && (
                <div className="notification-dropdown">
                    <div className="notif-header">
                        <span className="notif-title">Notifications</span>
                        {unreadCount > 0 && (
                            <button className="mark-all-btn" onClick={handleMarkAllRead}>
                                <CheckIcon /> Mark all read
                            </button>
                        )}
                    </div>

                    <div className="notif-list">
                        {loading ? (
                            <div className="notif-empty">Loading...</div>
                        ) : notifications.length === 0 ? (
                            <div className="notif-empty">
                                <span style={{ fontSize: '2rem' }}>🔔</span>
                                <p>No notifications yet</p>
                            </div>
                        ) : (
                            notifications.map(n => (
                                <div
                                    key={n.id}
                                    className={`notif-item ${!n.isRead ? 'unread' : ''}`}
                                >
                                    <span className="notif-icon">
                                        {NOTIFICATION_ICONS[n.notificationType] || NOTIFICATION_ICONS.DEFAULT}
                                    </span>
                                    <div className="notif-body">
                                        <p className="notif-item-title">{n.title}</p>
                                        <p className="notif-item-msg">{n.message}</p>
                                        <p className="notif-time">
                                            {formatDistanceToNow(new Date(n.createdAt), { addSuffix: true })}
                                        </p>
                                    </div>
                                    {!n.isRead && (
                                        <button
                                            className="mark-read-btn"
                                            onClick={(e) => handleMarkRead(n.id, e)}
                                            title="Mark as read"
                                        >
                                            <CheckIcon />
                                        </button>
                                    )}
                                </div>
                            ))
                        )}
                    </div>
                </div>
            )}
        </div>
    );
};

export default NotificationBell;