import React, { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { toast, ToastContainer } from 'react-toastify';
import BookingModal from './BookingModal';
import ServiceDetailModal from './ServiceDetailModal';
import './WishlistPage.css';

const API_BASE = "http://localhost:8070/api";

const HeartIcon = ({ filled }) => (
    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24"
        fill={filled ? "currentColor" : "none"} stroke="currentColor" strokeWidth="2"
        strokeLinecap="round" strokeLinejoin="round">
        <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
    </svg>
);

const ArrowLeftIcon = () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24"
        fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <line x1="19" y1="12" x2="5" y2="12" /><polyline points="12 19 5 12 12 5" />
    </svg>
);

const StarIcon = ({ filled }) => (
    <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24"
        fill={filled ? "#f39c12" : "none"} stroke="#f39c12" strokeWidth="2">
        <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
    </svg>
);

const WishlistPage = () => {
    const [wishlist, setWishlist] = useState([]);
    const [loading, setLoading] = useState(true);
    const [selectedService, setSelectedService] = useState(null);
    const [isBookingModalOpen, setIsBookingModalOpen] = useState(false);
    const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);
    const navigate = useNavigate();

    const token = localStorage.getItem('token');
    const axiosWithAuth = useMemo(() => axios.create({
        baseURL: API_BASE,
        headers: { Authorization: `Bearer ${token}` }
    }), [token]);

    const fetchWishlist = async () => {
        setLoading(true);
        try {
            const res = await axiosWithAuth.get('/wishlist');
            setWishlist(res.data);
        } catch {
            toast.error("Failed to load wishlist.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (!token) { navigate('/login'); return; }
        fetchWishlist();
    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [token]);

    const handleRemove = async (serviceId, serviceName) => {
        try {
            await axiosWithAuth.post(`/wishlist/${serviceId}/toggle`);
            toast.success(`"${serviceName}" removed from wishlist.`);
            setWishlist(prev => prev.filter(s => s.id !== serviceId));
        } catch {
            toast.error("Failed to remove from wishlist.");
        }
    };

    return (
        <div className="wishlist-page">
            <ToastContainer theme="dark" position="bottom-right" />
            <header className="wishlist-header">
                <div className="logo" onClick={() => navigate('/')} style={{ cursor: 'pointer' }}>ServiceSphere</div>
                <button className="back-btn" onClick={() => navigate('/customer')}>
                    <ArrowLeftIcon /> Back to Services
                </button>
            </header>

            <main className="wishlist-container">
                <div className="wishlist-title-row">
                    <h1>❤️ My Wishlist</h1>
                    <span className="wishlist-count">{wishlist.length} saved</span>
                </div>

                {loading ? (
                    <div className="loader-container"><div className="loader"></div></div>
                ) : wishlist.length === 0 ? (
                    <div className="wishlist-empty">
                        <span style={{ fontSize: '4rem' }}>💔</span>
                        <h3>Your wishlist is empty</h3>
                        <p>Save services you like by clicking the heart icon on any service card.</p>
                        <button className="btn btn-primary" onClick={() => navigate('/customer')}>
                            Browse Services
                        </button>
                    </div>
                ) : (
                    <div className="wishlist-grid">
                        {wishlist.map(service => (
                            <div key={service.wishlist_id} className="wishlist-card">
                                <div className="wishlist-card-img-wrap" onClick={() => { setSelectedService(service); setIsDetailModalOpen(true); }}>
                                    <img
                                        src={service.image_url || `https://placehold.co/400x200/191925/a99eff?text=${service.service_name?.charAt(0)}`}
                                        alt={service.service_name}
                                        className="wishlist-card-img"
                                    />
                                    <button
                                        className="heart-btn active"
                                        onClick={e => { e.stopPropagation(); handleRemove(service.id, service.service_name); }}
                                        title="Remove from wishlist"
                                    >
                                        <HeartIcon filled={true} />
                                    </button>
                                </div>
                                <div className="wishlist-card-body">
                                    <div className="wishlist-card-top">
                                        <h3>{service.service_name}</h3>
                                        <span className="wishlist-price">₹{Number(service.price).toLocaleString('en-IN')}</span>
                                    </div>
                                    <div className="wishlist-rating">
                                        {[...Array(5)].map((_, i) => (
                                            <StarIcon key={i} filled={i < Math.round(service.average_rating)} />
                                        ))}
                                        <span>{service.review_count > 0 ? `${parseFloat(service.average_rating).toFixed(1)} (${service.review_count})` : 'No reviews'}</span>
                                    </div>
                                    <p className="wishlist-meta">📍 {service.location || 'Not specified'} &nbsp;·&nbsp; 🏷️ {service.category}</p>
                                    <p className="wishlist-provider">By {service.provider_name}</p>
                                </div>
                                <div className="wishlist-card-footer">
                                    {service.availability === 'Available'
                                        ? <button className="btn btn-primary btn-sm" onClick={() => { setSelectedService(service); setIsBookingModalOpen(true); }}>Book Now</button>
                                        : <span className="unavailable-badge">Unavailable</span>
                                    }
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </main>

            {isBookingModalOpen && <BookingModal service={selectedService} onClose={() => setIsBookingModalOpen(false)} axiosWithAuth={axiosWithAuth} />}
            {isDetailModalOpen && <ServiceDetailModal service={selectedService} onClose={() => setIsDetailModalOpen(false)} />}
        </div>
    );
};

export default WishlistPage;