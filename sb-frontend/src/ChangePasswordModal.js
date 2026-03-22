import React, { useState } from 'react';
import { toast } from 'react-toastify';

const LockIcon = () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24"
        fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
        <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
    </svg>
);

const ChangePasswordModal = ({ onClose, axiosWithAuth }) => {
    const [form, setForm] = useState({ oldPassword: '', newPassword: '', confirmPassword: '' });
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (form.newPassword !== form.confirmPassword) {
            toast.error("New passwords do not match.");
            return;
        }
        if (form.newPassword.length < 6) {
            toast.error("New password must be at least 6 characters.");
            return;
        }
        setLoading(true);
        try {
            await axiosWithAuth.post('/users/change-password', {
                oldPassword: form.oldPassword,
                newPassword: form.newPassword,
            });
            toast.success("Password changed successfully!");
            onClose();
        } catch (err) {
            toast.error(err.response?.data?.message || "Failed to change password.");
        } finally {
            setLoading(false);
        }
    };

    const inputStyle = {
        width: '100%',
        padding: '10px 10px 10px 36px',
        background: 'var(--dark-bg)',
        border: '1px solid var(--border-color)',
        borderRadius: '8px',
        color: 'var(--text-primary)',
        fontSize: '1rem',
        fontFamily: 'inherit',
        boxSizing: 'border-box',
    };

    const groupStyle = {
        display: 'flex',
        flexDirection: 'column',
        gap: '0.5rem',
        marginBottom: '1rem',
    };

    const labelStyle = {
        fontSize: '0.875rem',
        fontWeight: '500',
        color: 'var(--text-secondary)',
    };

    const iconWrap = {
        position: 'relative',
    };

    const iconPos = {
        position: 'absolute',
        left: '10px',
        top: '50%',
        transform: 'translateY(-50%)',
        color: 'var(--text-secondary)',
        pointerEvents: 'none',
    };

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content" onClick={e => e.stopPropagation()}>
                <div className="modal-header">
                    <h3>Change Password</h3>
                    <button onClick={onClose} style={{ background:'none', border:'none', color:'var(--text-secondary)', fontSize:'1.75rem', cursor:'pointer', lineHeight:1 }}>&times;</button>
                </div>
                <form onSubmit={handleSubmit}>
                    <div style={groupStyle}>
                        <label style={labelStyle}>Current Password</label>
                        <div style={iconWrap}>
                            <span style={iconPos}><LockIcon /></span>
                            <input
                                type="password"
                                style={inputStyle}
                                placeholder="Enter current password"
                                value={form.oldPassword}
                                onChange={e => setForm({ ...form, oldPassword: e.target.value })}
                                required
                            />
                        </div>
                    </div>
                    <div style={groupStyle}>
                        <label style={labelStyle}>New Password</label>
                        <div style={iconWrap}>
                            <span style={iconPos}><LockIcon /></span>
                            <input
                                type="password"
                                style={inputStyle}
                                placeholder="Min 6 characters"
                                value={form.newPassword}
                                onChange={e => setForm({ ...form, newPassword: e.target.value })}
                                required
                            />
                        </div>
                    </div>
                    <div style={groupStyle}>
                        <label style={labelStyle}>Confirm New Password</label>
                        <div style={iconWrap}>
                            <span style={iconPos}><LockIcon /></span>
                            <input
                                type="password"
                                style={inputStyle}
                                placeholder="Re-enter new password"
                                value={form.confirmPassword}
                                onChange={e => setForm({ ...form, confirmPassword: e.target.value })}
                                required
                            />
                        </div>
                    </div>
                    <div className="modal-actions">
                        <button type="button" className="btn btn-secondary" onClick={onClose}>Cancel</button>
                        <button type="submit" className="btn btn-primary" disabled={loading}>
                            {loading ? 'Saving...' : 'Change Password'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default ChangePasswordModal;