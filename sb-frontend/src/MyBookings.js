import React, { useState, useEffect, useMemo, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { format } from 'date-fns';
import { ToastContainer, toast } from 'react-toastify';
import RatingModal from './RatingModal';
import ConfirmToast from './ConfirmToast';
import PaymentButton from './PaymentButton';
import './MyBookings.css';

const API_BASE = "http://localhost:8070/api";

const StarIcon = ({ className }) => (
    <svg className={className} xmlns="http://www.w3.org/2000/svg" width="16" height="16"
        viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"
        strokeLinecap="round" strokeLinejoin="round">
        <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
    </svg>
);

const StarRatingDisplay = ({ rating }) => (
    <div className="star-rating-display">
        {[...Array(5)].map((_, index) => (
            <StarIcon key={index} className={index < rating ? 'star-filled' : 'star-empty'} />
        ))}
    </div>
);

const ArrowLeftIcon = () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24"
        fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <line x1="19" y1="12" x2="5" y2="12" />
        <polyline points="12 19 5 12 12 5" />
    </svg>
);

const MyBookings = () => {
    const [bookings, setBookings] = useState([]);
    const [loading, setLoading] = useState(true);
    const [user, setUser] = useState(null);
    const [isRatingModalOpen, setIsRatingModalOpen] = useState(false);
    const [selectedBooking, setSelectedBooking] = useState(null);
    const [cancellingId, setCancellingId] = useState(null);
    const navigate = useNavigate();

    const token = localStorage.getItem("token");
    const axiosWithAuth = useMemo(() => axios.create({
        baseURL: API_BASE,
        headers: { Authorization: `Bearer ${token}` }
    }), [token]);

    const fetchBookings = useCallback(async () => {
        setLoading(true);
        try {
            const res = await axiosWithAuth.get('/bookings');
            setBookings(res.data);
        } catch (error) {
            toast.error("Failed to fetch your bookings.");
        } finally {
            setLoading(false);
        }
    }, [axiosWithAuth]);

    useEffect(() => {
        if (!token) { navigate('/login'); return; }
        const currentUser = JSON.parse(localStorage.getItem("user"));
        setUser(currentUser);
        fetchBookings();
    }, [token, navigate, fetchBookings]);

    const handleCancelBooking = async (bookingId) => {
        if (!window.confirm("Are you sure you want to cancel this booking?")) return;
        setCancellingId(bookingId);
        try {
            await axiosWithAuth.put(`/bookings/${bookingId}/status`, { status: 'Cancelled' });
            toast.success("Booking cancelled successfully.");
            fetchBookings();
        } catch (err) {
            toast.error(err.response?.data?.message || "Failed to cancel booking.");
        } finally {
            setCancellingId(null);
        }
    };

    const handleOpenRatingModal = (booking) => {
        setSelectedBooking(booking);
        setIsRatingModalOpen(true);
    };

    const handleCloseRatingModal = () => {
        setIsRatingModalOpen(false);
        setSelectedBooking(null);
    };

    const getStatusClass = (status) => status ? status.toLowerCase() : '';
    const statusEmoji = { Pending: '⏳', Confirmed: '✅', Paid: '💰', Completed: '🎉', Cancelled: '❌' };

    const handleBackClick = () => {
        navigate(user?.role === 'Service Provider' ? '/provider' : '/customer');
    };

    const handleExportCSV = () => {
        if (!bookings.length) {
            toast.info('No bookings to export.');
            return;
        }

        const headers = [
            'Booking ID',
            'Service',
            'Customer/Provider',
            'Date Time',
            'Price',
            'Status'
        ];

        const rows = bookings.map((booking) => [
            booking.id,
            booking.service_name || '',
            user?.role === 'Customer' ? (booking.provider_name || '') : (booking.customer_name || ''),
            booking.booking_start_time ? format(new Date(booking.booking_start_time), "yyyy-MM-dd HH:mm") : '',
            booking.price ?? '',
            booking.status || ''
        ]);

        const csvContent = [headers, ...rows]
            .map((row) => row.map((cell) => `"${String(cell).replace(/"/g, '""')}"`).join(','))
            .join('\n');

        const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', 'my-bookings.csv');
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        URL.revokeObjectURL(url);
    };

    return (
        <>
            <div className="my-bookings-page">
                <ToastContainer theme="dark" position="bottom-right" />
                <header className="bookings-header">
                    <div className="logo" onClick={() => navigate('/')} style={{ cursor: 'pointer' }}>
                        ServiceSphere
                    </div>
                    <button className="back-btn" onClick={handleBackClick}>
                        <ArrowLeftIcon />
                        <span>Back to Dashboard</span>
                    </button>
                </header>

                <main className="bookings-container">
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem', flexWrap: 'wrap', gap: '1rem' }}>
                        <h1 style={{ margin: 0 }}>My Bookings</h1>
                        {bookings.length > 0 && (
                            <button
                                className="back-btn"
                                onClick={handleExportCSV}
                                style={{ color: '#2ecc71', borderColor: 'rgba(46,204,113,0.3)', background: 'rgba(46,204,113,0.08)' }}
                            >
                                ⬇ Export CSV
                            </button>
                        )}
                    </div>
                    <p>Here is a list of all your scheduled appointments and past services.</p>

                    <div className="bookings-list">
                        {loading ? (
                            <div className="loader-container"><div className="loader"></div></div>
                        ) : bookings.length > 0 ? (
                            bookings.map(booking => (
                                <div key={booking.id} className="booking-card">
                                    <div className="booking-card-header">
                                        <h3>{booking.service_name}</h3>
                                        <span className={`booking-status ${getStatusClass(booking.status)}`}>
                                            {statusEmoji[booking.status] || ''} {booking.status}
                                        </span>
                                    </div>

                                    <div className="booking-card-body">
                                        <p>
                                            <strong>
                                                {user?.role === 'Customer' ? 'Provider' : 'Customer'}:
                                            </strong>{' '}
                                            {user?.role === 'Customer' ? booking.provider_name : booking.customer_name}
                                        </p>
                                        <p>
                                            <strong>Date & Time:</strong>{' '}
                                            {format(new Date(booking.booking_start_time), "EEEE, MMMM d, yyyy 'at' h:mm a")}
                                        </p>
                                        <p><strong>Price:</strong> ₹{booking.price || 'N/A'}</p>
                                    </div>

                                    {/* --- Customer: Cancel pending booking --- */}
                                    {booking.status === 'Pending' && user?.role === 'Customer' && (
                                        <div className="booking-card-footer">
                                            <button
                                                className="btn"
                                                style={{
                                                    background: 'rgba(231,76,60,0.12)',
                                                    color: '#e74c3c',
                                                    border: '1px solid rgba(231,76,60,0.3)',
                                                    fontWeight: 600,
                                                    cursor: cancellingId === booking.id ? 'not-allowed' : 'pointer',
                                                    opacity: cancellingId === booking.id ? 0.6 : 1,
                                                }}
                                                onClick={() => handleCancelBooking(booking.id)}
                                                disabled={cancellingId === booking.id}
                                            >
                                                {cancellingId === booking.id ? 'Cancelling...' : '✕ Cancel Booking'}
                                            </button>
                                        </div>
                                    )}

                                    {/* --- Confirmed: show Pay button (customer) --- */}
                                    {booking.status === 'Confirmed' && user?.role === 'Customer' && (
                                        <div className="booking-card-footer">
                                            <p style={{ margin: '0 0 0.75rem', fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
                                                Your booking is confirmed! Complete payment to secure your slot.
                                            </p>
                                            <PaymentButton
                                                booking={booking}
                                                axiosWithAuth={axiosWithAuth}
                                                onPaymentSuccess={fetchBookings}
                                            />
                                        </div>
                                    )}

                                    {/* --- Paid: show payment confirmed badge --- */}
                                    {booking.status === 'Paid' && (
                                        <div className="booking-card-footer">
                                            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', background: 'rgba(16,185,129,0.1)', border: '1px solid rgba(16,185,129,0.25)', borderRadius: '8px', padding: '0.75rem 1rem', color: '#10b981', fontWeight: 600 }}>
                                                💰 Payment completed — your slot is secured!
                                            </div>
                                        </div>
                                    )}

                                    {/* --- Completed: show review or prompt --- */}
                                    {booking.status === 'Completed' && (
                                        <div className="booking-card-footer">
                                            {booking.review_id ? (
                                                <div className="review-display">
                                                    <h4>{user?.role === 'Customer' ? 'Your Review' : 'Customer Review'}:</h4>
                                                    <StarRatingDisplay rating={booking.rating} />
                                                    {booking.comment && (
                                                        <p className="review-comment">"{booking.comment}"</p>
                                                    )}
                                                </div>
                                            ) : (
                                                user?.role === 'Customer' && (
                                                    <button
                                                        className="btn btn-primary"
                                                        onClick={() => handleOpenRatingModal(booking)}
                                                    >
                                                        ⭐ Rate & Review
                                                    </button>
                                                )
                                            )}
                                        </div>
                                    )}
                                </div>
                            ))
                        ) : (
                            <div className="no-bookings">
                                <p>You have no bookings yet.</p>
                                <button className="btn" onClick={() => navigate('/customer')}>
                                    Explore Services
                                </button>
                            </div>
                        )}
                    </div>
                </main>
            </div>

            {isRatingModalOpen && (
                <RatingModal
                    booking={selectedBooking}
                    onClose={handleCloseRatingModal}
                    axiosWithAuth={axiosWithAuth}
                    onReviewSubmit={fetchBookings}
                />
            )}
        </>
    );
};

export default MyBookings;