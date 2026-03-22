// src/ConfirmToast.js
// A reusable inline confirmation widget rendered inside a toast notification.
// Usage:
//   toast(({ closeToast }) => (
//     <ConfirmToast message="..." onConfirm={...} onCancel={closeToast} />
//   ), { autoClose: false, closeOnClick: false, closeButton: false });

import React from 'react';

const ConfirmToast = ({ message, onConfirm, onCancel, confirmLabel = 'Yes, proceed', cancelLabel = 'No, keep it' }) => (
    <div style={{ fontFamily: 'inherit' }}>
        <p style={{ margin: '0 0 12px', fontWeight: 600, fontSize: '0.95rem', color: '#f0f0f5' }}>
            {message}
        </p>
        <div style={{ display: 'flex', gap: '0.625rem' }}>
            <button
                onClick={onConfirm}
                style={{
                    flex: 1,
                    padding: '8px 12px',
                    background: '#e74c3c',
                    color: 'white',
                    border: 'none',
                    borderRadius: '8px',
                    fontWeight: 700,
                    fontSize: '0.85rem',
                    cursor: 'pointer',
                    fontFamily: 'inherit',
                    transition: 'opacity 0.2s',
                }}
                onMouseOver={e => e.target.style.opacity = '0.85'}
                onMouseOut={e => e.target.style.opacity = '1'}
            >
                {confirmLabel}
            </button>
            <button
                onClick={onCancel}
                style={{
                    flex: 1,
                    padding: '8px 12px',
                    background: 'rgba(255,255,255,0.08)',
                    color: '#a0a0b0',
                    border: '1px solid rgba(255,255,255,0.1)',
                    borderRadius: '8px',
                    fontWeight: 600,
                    fontSize: '0.85rem',
                    cursor: 'pointer',
                    fontFamily: 'inherit',
                    transition: 'opacity 0.2s',
                }}
                onMouseOver={e => e.target.style.opacity = '0.75'}
                onMouseOut={e => e.target.style.opacity = '1'}
            >
                {cancelLabel}
            </button>
        </div>
    </div>
);

export default ConfirmToast;