import React, { useState, useEffect, useRef, useCallback } from 'react';
import axios from 'axios';
import './SearchSuggestions.css';

const API_BASE = "http://localhost:8070/api";

const SearchIcon = () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24"
        fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <circle cx="11" cy="11" r="8" /><line x1="21" y1="21" x2="16.65" y2="16.65" />
    </svg>
);

const StarIcon = () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24"
        fill="#f39c12" stroke="#f39c12" strokeWidth="1">
        <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
    </svg>
);

/**
 * SearchSuggestions
 * A smart search input that shows a live dropdown of matching services.
 *
 * Props:
 *   value          - current search string (controlled)
 *   onChange       - called with new string value
 *   onSelectService - called when user clicks a suggestion (receives full service object)
 *   placeholder    - input placeholder text
 */
const SearchSuggestions = ({ value, onChange, onSelectService, placeholder = "Search services..." }) => {
    const [suggestions, setSuggestions] = useState([]);
    const [showDropdown, setShowDropdown] = useState(false);
    const [loading, setLoading] = useState(false);
    const [activeIndex, setActiveIndex] = useState(-1);
    const wrapperRef = useRef(null);
    const debounceRef = useRef(null);

    // Close dropdown on outside click
    useEffect(() => {
        const handler = (e) => {
            if (wrapperRef.current && !wrapperRef.current.contains(e.target)) {
                setShowDropdown(false);
                setActiveIndex(-1);
            }
        };
        document.addEventListener('mousedown', handler);
        return () => document.removeEventListener('mousedown', handler);
    }, []);

    // Debounced fetch
    const fetchSuggestions = useCallback(async (query) => {
        if (query.trim().length < 2) {
            setSuggestions([]);
            setShowDropdown(false);
            return;
        }
        setLoading(true);
        try {
            const res = await axios.get(`${API_BASE}/services`, {
                params: { keyword: query, sortBy: 'rating_desc' }
            });
            // Take top 6 results
            setSuggestions((res.data || []).slice(0, 6));
            setShowDropdown(true);
        } catch {
            setSuggestions([]);
        } finally {
            setLoading(false);
        }
    }, []);

    const handleInputChange = (e) => {
        const val = e.target.value;
        onChange(val);
        setActiveIndex(-1);
        clearTimeout(debounceRef.current);
        debounceRef.current = setTimeout(() => fetchSuggestions(val), 300);
    };

    const handleSelect = (service) => {
        onChange(service.service_name);
        setShowDropdown(false);
        setActiveIndex(-1);
        if (onSelectService) onSelectService(service);
    };

    const handleKeyDown = (e) => {
        if (!showDropdown || suggestions.length === 0) return;
        if (e.key === 'ArrowDown') {
            e.preventDefault();
            setActiveIndex(prev => Math.min(prev + 1, suggestions.length - 1));
        } else if (e.key === 'ArrowUp') {
            e.preventDefault();
            setActiveIndex(prev => Math.max(prev - 1, -1));
        } else if (e.key === 'Enter' && activeIndex >= 0) {
            e.preventDefault();
            handleSelect(suggestions[activeIndex]);
        } else if (e.key === 'Escape') {
            setShowDropdown(false);
            setActiveIndex(-1);
        }
    };

    const handleFocus = () => {
        if (suggestions.length > 0) setShowDropdown(true);
    };

    const getAvailabilityColor = (av) => {
        if (av === 'Available') return '#10b981';
        if (av === 'Busy') return '#f59e0b';
        return '#ef4444';
    };

    return (
        <div className="search-suggestions-wrapper" ref={wrapperRef}>
            <div className="search-input-wrap">
                <span className="search-icon"><SearchIcon /></span>
                <input
                    type="text"
                    className="search-main-input"
                    value={value}
                    onChange={handleInputChange}
                    onKeyDown={handleKeyDown}
                    onFocus={handleFocus}
                    placeholder={placeholder}
                    autoComplete="off"
                />
                {loading && <span className="search-spinner" />}
                {value && (
                    <button className="search-clear-btn" onClick={() => { onChange(''); setSuggestions([]); setShowDropdown(false); }}>
                        ×
                    </button>
                )}
            </div>

            {showDropdown && suggestions.length > 0 && (
                <div className="suggestions-dropdown">
                    <p className="suggestions-label">Matching services</p>
                    {suggestions.map((s, i) => (
                        <div
                            key={s.id}
                            className={`suggestion-item ${i === activeIndex ? 'active' : ''}`}
                            onMouseDown={() => handleSelect(s)}
                        >
                            <img
                                src={s.image_url || `https://placehold.co/40x40/191925/a99eff?text=${s.service_name.charAt(0)}`}
                                alt=""
                                className="suggestion-img"
                            />
                            <div className="suggestion-info">
                                <p className="suggestion-name">{s.service_name}</p>
                                <p className="suggestion-meta">
                                    <span className="suggestion-category">{s.category}</span>
                                    {s.location && <span>· 📍 {s.location}</span>}
                                    {s.average_rating > 0 && (
                                        <span className="suggestion-rating">
                                            <StarIcon /> {parseFloat(s.average_rating).toFixed(1)}
                                        </span>
                                    )}
                                </p>
                            </div>
                            <div className="suggestion-right">
                                <span className="suggestion-price">₹{Number(s.price).toLocaleString('en-IN')}</span>
                                <span className="suggestion-avail" style={{ color: getAvailabilityColor(s.availability) }}>
                                    ● {s.availability}
                                </span>
                            </div>
                        </div>
                    ))}
                    <div className="suggestions-footer">
                        Press Enter or click to select
                    </div>
                </div>
            )}

            {showDropdown && value.length >= 2 && !loading && suggestions.length === 0 && (
                <div className="suggestions-dropdown">
                    <p className="suggestions-empty">No services found for "{value}"</p>
                </div>
            )}
        </div>
    );
};

export default SearchSuggestions;