// src/AdminDashboard.js

import React, { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import './AdminDashboard.css';
import {
    PieChart, Pie, Cell, BarChart, Bar, XAxis, YAxis, CartesianGrid,
    Tooltip, ResponsiveContainer, RadialBarChart, RadialBar
} from 'recharts';

const API_BASE = "http://localhost:8070/api";

// --- Icon Components ---
const UsersIcon = () => <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>;
const ServicesIcon = () => <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M3 3v18h18" /><path d="m19 9-5 5-4-4-3 3" /></svg>;
const BookingsIcon = () => <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M8 2v4" /><path d="M16 2v4" /><rect width="18" height="18" x="3" y="4" rx="2" /><path d="M3 10h18" /></svg>;
const ProvidersIcon = () => <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" /><circle cx="9" cy="7" r="4" /><path d="M22 21v-2a4 4 0 0 0-3-3.87" /><path d="M16 3.13a4 4 0 0 1 0 7.75" /></svg>;
const PowerIcon = () => <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M18.36 6.64a9 9 0 1 1-12.73 0"></path><line x1="12" y1="2" x2="12" y2="12"></line></svg>;
const DollarIcon = () => <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="12" y1="1" x2="12" y2="23"></line><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path></svg>;
const TrendingUpIcon = () => <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="23 6 13.5 15.5 8.5 10.5 1 18"></polyline><polyline points="17 6 23 6 23 12"></polyline></svg>;
const ClockIcon = () => <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>;

const COLORS = {
    Pending: '#f59e0b',
    Confirmed: '#3b82f6',
    Completed: '#10b981',
    Cancelled: '#ef4444'
};

const CHART_COLORS = ['#6a5af9', '#8b7aff', '#a99eff', '#c7bdff', '#e5e0ff'];

const StatCard = ({ icon, title, value, color, subtitle }) => (
    <div className="admin-stat-card" style={{ borderLeftColor: color }}>
        <div className="admin-stat-icon" style={{ color }}>{icon}</div>
        <div className="admin-stat-info">
            <span className="admin-stat-title">{title}</span>
            <span className="admin-stat-value">{value || 0}</span>
            {subtitle && <span className="admin-stat-subtitle">{subtitle}</span>}
        </div>
    </div>
);

const CustomTooltip = ({ active, payload }) => {
    if (active && payload && payload.length) {
        return (
            <div className="custom-tooltip">
                <p className="tooltip-label">{payload[0].name}</p>
                <p className="tooltip-value">{typeof payload[0].value === 'number' ? payload[0].value.toLocaleString() : payload[0].value}</p>
            </div>
        );
    }
    return null;
};

// Custom label for pie chart
const renderCustomLabel = ({ cx, cy, midAngle, innerRadius, outerRadius, percent }) => {
    if (percent < 0.05) return null;
    const RADIAN = Math.PI / 180;
    const radius = innerRadius + (outerRadius - innerRadius) * 0.5;
    const x = cx + radius * Math.cos(-midAngle * RADIAN);
    const y = cy + radius * Math.sin(-midAngle * RADIAN);

    return (
        <text x={x} y={y} fill="white" textAnchor={x > cx ? 'start' : 'end'} dominantBaseline="central" fontSize="14" fontWeight="600">
            {`${(percent * 100).toFixed(0)}%`}
        </text>
    );
};

// Simple list-based visualization for top items
const TopItemsList = ({ data, type }) => {
    const maxValue = Math.max(...data.map(item => item.value));

    return (
        <div className="top-items-list">
            {data.map((item, index) => (
                <div key={index} className="top-item">
                    <div className="top-item-header">
                        <span className="top-item-rank">#{index + 1}</span>
                        <span className="top-item-name">{item.name}</span>
                        <span className="top-item-value">{item.value.toLocaleString()}</span>
                    </div>
                    <div className="top-item-bar-container">
                        <div
                            className="top-item-bar"
                            style={{
                                width: `${(item.value / maxValue) * 100}%`,
                                background: `linear-gradient(90deg, ${CHART_COLORS[index % CHART_COLORS.length]} 0%, ${CHART_COLORS[(index + 1) % CHART_COLORS.length]} 100%)`
                            }}
                        />
                    </div>
                </div>
            ))}
        </div>
    );
};

const AdminDashboard = () => {
    const [activeTab, setActiveTab] = useState('dashboard');
    const [stats, setStats] = useState({});
    const [revenue, setRevenue] = useState({});
    const [statusBreakdown, setStatusBreakdown] = useState({});
    const [topCategories, setTopCategories] = useState([]);
    const [topServices, setTopServices] = useState([]);
    const [topProviders, setTopProviders] = useState([]);
    const [recentActivities, setRecentActivities] = useState([]);
    const [services, setServices] = useState([]);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    const token = localStorage.getItem("token");
    const axiosWithAuth = useMemo(() => axios.create({ baseURL: API_BASE, headers: { Authorization: `Bearer ${token}` } }), [token]);

    const fetchData = async () => {
        try {
            const [statsRes, servicesRes] = await Promise.all([
                axiosWithAuth.get('/admin/stats'),
                axiosWithAuth.get('/admin/services')
            ]);

            setStats(statsRes.data.stats || {});
            setRevenue(statsRes.data.revenue || {});
            setStatusBreakdown(statsRes.data.bookingStatusBreakdown || {});
            setTopCategories(statsRes.data.topCategories || []);
            setTopServices(statsRes.data.topServices || []);
            setTopProviders(statsRes.data.topProviders || []);
            setRecentActivities(statsRes.data.recentActivities || []);
            setServices(servicesRes.data || []);
        } catch (err) {
            toast.error("Failed to fetch admin data. You may not have access.");
            if (err.response?.status === 403) navigate('/login');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        const currentUser = JSON.parse(localStorage.getItem("user"));
        if (!token || !currentUser || currentUser.role !== 'Admin') {
            toast.error("Access Denied.");
            navigate('/login');
        } else {
            fetchData();
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [token, navigate]);

    const handleServiceStatus = async (serviceId, status) => {
        try {
            await axiosWithAuth.put(`/admin/services/${serviceId}/status`, { status });
            toast.success(`Service has been ${status.toLowerCase()}.`);
            fetchData();
        } catch (error) {
            toast.error("Failed to update service status.");
        }
    };

    const handleSignOut = () => {
        localStorage.removeItem("token");
        localStorage.removeItem("user");
        navigate('/login');
    };

    const formatCurrency = (amount) => {
        return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(amount || 0);
    };

    const formatDate = (dateString) => {
        return new Date(dateString).toLocaleString('en-IN', {
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    // Prepare data for visualizations
    const statusChartData = Object.entries(statusBreakdown).map(([name, value]) => ({ name, value }));
    const categoriesListData = topCategories.slice(0, 5).map(cat => ({ name: cat.category, value: cat.bookingCount }));
    const servicesListData = topServices.slice(0, 5).map(srv => ({ name: srv.serviceName, value: srv.bookingCount }));
    const providersListData = topProviders.slice(0, 5).map(p => ({ name: p.providerName, value: p.totalEarnings }));

    return (
        <div className="admin-dashboard">
            <ToastContainer theme="dark" position="bottom-right" />
            <header className="admin-header">
                <div className="header-content">
                    <h1>Admin Dashboard</h1>
                    <p className="header-subtitle">Comprehensive Business Analytics</p>
                </div>
                <button onClick={handleSignOut} className="signout-btn">
                    <PowerIcon /> Sign Out
                </button>
            </header>
            <main className="admin-content">
                {loading ? (
                    <div className="loader-container"><div className="loader"></div></div>
                ) : (
                    <>
                        <div className="tabs">
                            <button className={`tab-btn ${activeTab === 'dashboard' ? 'active' : ''}`} onClick={() => setActiveTab('dashboard')}>
                                Analytics
                            </button>
                            <button className={`tab-btn ${activeTab === 'moderation' ? 'active' : ''}`} onClick={() => setActiveTab('moderation')}>
                                Service Moderation
                            </button>
                        </div>

                        {activeTab === 'dashboard' && (
                            <>
                                {/* Core Metrics */}
                                <section className="stats-section">
                                    <StatCard icon={<UsersIcon />} title="Total Users" value={stats.total_users} color="#6a5af9" />
                                    <StatCard icon={<ProvidersIcon />} title="Service Providers" value={stats.total_providers} color="#f92bff" />
                                    <StatCard icon={<ServicesIcon />} title="Total Services" value={stats.total_services} color="#10b981" />
                                    <StatCard icon={<BookingsIcon />} title="Total Bookings" value={stats.total_bookings} color="#f59e0b" />
                                </section>

                                {/* Revenue Metrics */}
                                <section className="stats-section">
                                    <StatCard
                                        icon={<DollarIcon />}
                                        title="Total Revenue"
                                        value={formatCurrency(revenue.total_revenue)}
                                        color="#10b981"
                                        subtitle="From completed bookings"
                                    />
                                    <StatCard
                                        icon={<TrendingUpIcon />}
                                        title="Avg Booking Value"
                                        value={formatCurrency(revenue.average_booking_value)}
                                        color="#8b5cf6"
                                    />
                                    <StatCard
                                        icon={<ClockIcon />}
                                        title="Pending Revenue"
                                        value={formatCurrency(revenue.pending_revenue)}
                                        color="#f59e0b"
                                        subtitle="Potential income"
                                    />
                                    <StatCard
                                        icon={<BookingsIcon />}
                                        title="Completed"
                                        value={stats.completed_bookings}
                                        color="#06b6d4"
                                    />
                                </section>

                                {/* Charts Section */}
                                <section className="charts-section">
                                    {/* Booking Status Pie Chart */}
                                    <div className="chart-card">
                                        <h3 className="chart-title">Booking Status Distribution</h3>
                                        <ResponsiveContainer width="100%" height={320}>
                                            <PieChart>
                                                <Pie
                                                    data={statusChartData}
                                                    cx="50%"
                                                    cy="50%"
                                                    labelLine={false}
                                                    label={renderCustomLabel}
                                                    outerRadius={110}
                                                    fill="#8884d8"
                                                    dataKey="value"
                                                >
                                                    {statusChartData.map((entry, index) => (
                                                        <Cell key={`cell-${index}`} fill={COLORS[entry.name] || '#6a5af9'} />
                                                    ))}
                                                </Pie>
                                                <Tooltip content={<CustomTooltip />} />
                                            </PieChart>
                                        </ResponsiveContainer>
                                        <div className="chart-legend">
                                            {statusChartData.map((entry, index) => (
                                                <div key={index} className="legend-item">
                                                    <span className="legend-dot" style={{ backgroundColor: COLORS[entry.name] }}></span>
                                                    <span className="legend-label">{entry.name}</span>
                                                    <span className="legend-value">{entry.value}</span>
                                                </div>
                                            ))}
                                        </div>
                                    </div>

                                    {/* Top Categories List */}
                                    <div className="chart-card">
                                        <h3 className="chart-title">Top Categories</h3>
                                        {categoriesListData.length > 0 ? (
                                            <TopItemsList data={categoriesListData} type="bookings" />
                                        ) : (
                                            <div className="empty-chart">No data available</div>
                                        )}
                                    </div>
                                </section>

                                {/* Top Services & Providers */}
                                <section className="charts-section">
                                    {/* Top Services List */}
                                    <div className="chart-card">
                                        <h3 className="chart-title">Top Services</h3>
                                        {servicesListData.length > 0 ? (
                                            <TopItemsList data={servicesListData} type="bookings" />
                                        ) : (
                                            <div className="empty-chart">No data available</div>
                                        )}
                                    </div>

                                    {/* Top Providers List */}
                                    <div className="chart-card">
                                        <h3 className="chart-title">Top Providers by Earnings</h3>
                                        {providersListData.length > 0 ? (
                                            <TopItemsList data={providersListData} type="earnings" />
                                        ) : (
                                            <div className="empty-chart">No data available</div>
                                        )}
                                    </div>
                                </section>

                                {/* Recent Activities */}
                                <section className="admin-panel">
                                    <h3 className="panel-header">Recent Activities</h3>
                                    <div className="table-wrapper">
                                        <table className="admin-table">
                                            <thead>
                                                <tr><th>ID</th><th>Customer</th><th>Service</th><th>Provider</th><th>Status</th><th>Time</th><th>Amount</th></tr>
                                            </thead>
                                            <tbody>
                                                {recentActivities.length > 0 ? recentActivities.slice(0, 10).map((activity, index) => (
                                                    <tr key={index} className="activity-row">
                                                        <td className="booking-id">#{activity.bookingId}</td>
                                                        <td>{activity.customerName}</td>
                                                        <td className="service-name">{activity.serviceName}</td>
                                                        <td>{activity.providerName}</td>
                                                        <td><span className={`status-badge ${activity.status?.toLowerCase()}`}>{activity.status}</span></td>
                                                        <td className="time-cell">{formatDate(activity.bookingTime)}</td>
                                                        <td className="amount-cell">{formatCurrency(activity.price)}</td>
                                                    </tr>
                                                )) : (
                                                    <tr><td colSpan="7" className="empty-cell">No recent activities.</td></tr>
                                                )}
                                            </tbody>
                                        </table>
                                    </div>
                                </section>
                            </>
                        )}

                        {activeTab === 'moderation' && (
                            <section className="admin-panel">
                                <h3 className="panel-header">Service Listing Moderation</h3>
                                <div className="table-wrapper">
                                    <table className="admin-table">
                                        <thead>
                                            <tr><th>Service</th><th>Provider</th><th>Category</th><th>Price</th><th>Status</th><th>Actions</th></tr>
                                        </thead>
                                        <tbody>
                                            {services.map(s => (
                                                <tr key={s.id}>
                                                    <td className="service-name">{s.service_name}</td>
                                                    <td>{s.provider_name}</td>
                                                    <td><span className="category-badge">{s.category}</span></td>
                                                    <td className="price-cell">₹{s.price}</td>
                                                    <td><span className={`status-badge ${s.status?.toLowerCase()}`}>{s.status}</span></td>
                                                    <td className="actions-cell">
                                                        {s.status === 'Pending' ? (
                                                            <>
                                                                <button className="btn-admin-action approve" onClick={() => handleServiceStatus(s.id, 'Approved')}>Approve</button>
                                                                <button className="btn-admin-action reject" onClick={() => handleServiceStatus(s.id, 'Rejected')}>Reject</button>
                                                            </>
                                                        ) : (
                                                            <span className="no-actions-text">—</span>
                                                        )}
                                                    </td>
                                                </tr>
                                            ))}
                                        </tbody>
                                    </table>
                                </div>
                            </section>
                        )}
                    </>
                )}
            </main>
        </div>
    );
};

export default AdminDashboard;