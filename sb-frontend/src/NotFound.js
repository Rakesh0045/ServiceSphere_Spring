import React from 'react';
import { useNavigate } from 'react-router-dom';

const NotFound = () => {
    const navigate = useNavigate();

    return (
        <div style={{
            minHeight: '100vh',
            background: 'var(--dark-bg, #10101a)',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            fontFamily: "'Outfit', sans-serif",
            color: 'var(--text-primary, #f0f0f5)',
            textAlign: 'center',
            padding: '2rem',
        }}>
            <div style={{
                fontSize: '8rem',
                fontWeight: 800,
                background: 'linear-gradient(90deg, #a99eff, #f92bff)',
                WebkitBackgroundClip: 'text',
                WebkitTextFillColor: 'transparent',
                backgroundClip: 'text',
                lineHeight: 1,
                marginBottom: '1rem',
            }}>
                404
            </div>
            <h2 style={{ fontSize: '1.75rem', fontWeight: 700, margin: '0 0 0.75rem' }}>
                Page Not Found
            </h2>
            <p style={{ color: '#a0a0b0', fontSize: '1.1rem', maxWidth: '400px', marginBottom: '2rem' }}>
                The page you're looking for doesn't exist or has been moved.
            </p>
            <button
                onClick={() => navigate('/')}
                style={{
                    background: '#6a5af9',
                    color: 'white',
                    border: 'none',
                    padding: '12px 28px',
                    borderRadius: '10px',
                    fontSize: '1rem',
                    fontWeight: 600,
                    cursor: 'pointer',
                    fontFamily: 'inherit',
                    transition: 'all 0.2s',
                }}
                onMouseOver={e => e.target.style.background = '#a99eff'}
                onMouseOut={e => e.target.style.background = '#6a5af9'}
            >
                Go Home
            </button>
        </div>
    );
};

export default NotFound;