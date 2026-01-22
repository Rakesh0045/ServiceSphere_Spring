import React from 'react';
import './ServiceDetailModal.css';

const StarIcon = ({ className }) => <svg className={className} xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon></svg>;

const StarRating = ({ rating, count }) => {
    if (count === 0 || !rating) {
        return <div className="detail-rating-text">No reviews yet</div>;
    }
    const fullStars = Math.round(rating);
    return (
        <div className="detail-star-rating">
            {[...Array(5)].map((_, i) => <StarIcon key={i} className={i < fullStars ? "star-filled" : "star-empty"} />)}
            <span className="detail-rating-text">{parseFloat(rating).toFixed(1)} ({count} {count === 1 ? 'review' : 'reviews'})</span>
        </div>
    );
};


const ServiceDetailModal = ({ service, onClose }) => {
    if (!service) return null;

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content detail-modal" onClick={e => e.stopPropagation()}>
                <button onClick={onClose} className="close-btn">&times;</button>
                <img 
                    src={service.image_url || `https://placehold.co/600x400/191925/a99eff?text=${service.service_name.split(' ').map(w => w[0]).join('')}`} 
                    alt={service.service_name} 
                    className="detail-modal-img" 
                />
                <div className="detail-modal-content">
                    <div className="detail-modal-header">
                        <h2>{service.service_name}</h2>
                        <p className="detail-modal-price">₹{service.price ? Number(service.price).toLocaleString('en-IN') : 'N/A'}</p>
                    </div>
                    <StarRating rating={service.average_rating} count={service.review_count} />
                    <p className="detail-modal-description">
                        {service.description || "No description provided."}
                    </p>
                    <div className="detail-modal-meta">
                        <p><strong>Category:</strong> {service.category}</p>
                        <p><strong>Location:</strong> {service.location}</p>
                        <p><strong>Provider:</strong> {service.provider_name}</p>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ServiceDetailModal;