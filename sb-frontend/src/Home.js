import React from 'react';
import { Link, useNavigate } from "react-router-dom";
import { 
    ShieldCheck, 
    Calendar, 
    Star, 
    Search,
    ArrowRight,
    Users
} from "lucide-react";
import "./Home.css"; 

const Home = () => {
    const navigate = useNavigate();

    const features = [
        {
            icon: <Search size={32} />,
            title: "Find Local Services",
            description: "Discover trusted service providers in your area with advanced filtering options."
        },
        {
            icon: <Calendar size={32} />,
            title: "Easy Booking",
            description: "Schedule appointments instantly with our real-time availability checking system."
        },
        {
            icon: <Star size={32} />,
            title: "Verified Reviews",
            description: "Read authentic reviews from verified customers to make informed decisions."
        },
        {
            icon: <ShieldCheck size={32} />,
            title: "Secure & Reliable",
            description: "All providers are vetted for quality and payments are securely processed."
        }
    ];

    const serviceCategories = [
        { name: "Plumbing", icon: "🔧", count: "240+ providers" },
        { name: "House Cleaning", icon: "✨", count: "180+ providers" },
        { name: "Electrical", icon: "⚡", count: "150+ providers" },
        { name: "Tutoring", icon: "📚", count: "300+ providers" },
        { name: "Appliance Repair", icon: "🔨", count: "160+ providers" },
        { name: "Gardening", icon: "🌿", count: "130+ providers" },
    ];

    const testimonials = [
        {
            name: "Sarah J.",
            role: "Homeowner",
            content: "Found an amazing plumber through ServiceSphere. The booking process was seamless and the service was top-notch!",
            rating: 5
        },
        {
            name: "Mike C.",
            role: "Service Provider",
            content: "As an electrician, ServiceSphere has helped me grow my business significantly. The platform is easy to use and brings quality customers.",
            rating: 5
        },
        {
            name: "Emily D.",
            role: "Busy Parent",
            content: "ServiceSphere saved me so much time finding reliable tutoring services for my kids. Highly recommended!",
            rating: 5
        }
    ];

    const handleGetStarted = () => {
        navigate('/signup');
    };

    return (
        <div className="landing-container">
            {/* --- Header --- */}
            <header className="main-header">
                <nav className="main-nav">
                    <Link to="/" className="logo">
                        <svg width="32" height="32" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                            <path d="M2 17L12 22L22 17" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                            <path d="M2 12L12 17L22 12" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                        </svg>
                        <span>ServiceSphere</span>
                    </Link>
                    <div className="nav-auth-links">
                        <Link to="/login" className="btn btn-secondary">Login</Link>
                        <Link to="/signup" className="btn btn-primary">Sign Up</Link>
                    </div>
                </nav>
            </header>

            {/* --- Hero Section --- */}
            <main className="hero-section">
                <div className="hero-content container">
                    <h1 className="hero-heading">
                        Find & Book <span className="gradient-text">Trusted Local Services</span> In Minutes
                    </h1>
                    <p className="hero-tagline">
                        Connect with verified professionals for all your needs. From home repairs to tutoring, find the best with real reviews.
                    </p>
                    <div className="hero-cta-group">
                        <button onClick={handleGetStarted} className="btn btn-hero-primary">
                            <Search size={20} style={{ marginRight: '4px' }} /> 
                            <span>Find Services</span>
                            <ArrowRight size={20} />
                        </button>
                        <button onClick={() => navigate('/signup?role=provider')} className="btn btn-hero-secondary">
                            <Users size={20} style={{ marginRight: '4px' }} /> 
                            Become a Provider
                        </button>
                    </div>
                </div>
            </main>

            {/* --- Stats Section --- */}
            <section className="stats-section container">
                <div className="stat-item">
                    <div className="stat-value">1,200+</div>
                    <div className="stat-label">Verified Providers</div>
                </div>
                <div className="stat-item">
                    <div className="stat-value">15,000+</div>
                    <div className="stat-label">Services Completed</div>
                </div>
                <div className="stat-item">
                    <div className="stat-value">4.9★</div>
                    <div className="stat-label">Average Rating</div>
                </div>
            </section>

            {/* --- Features Section --- */}
            <section className="features-section" id="features">
                <div className="container">
                    <div className="section-header">
                        <h2 className="section-title">Why Choose <span className="gradient-text">ServiceSphere?</span></h2>
                        <p className="section-subtitle">We make finding and booking local services simple, safe, and reliable.</p>
                    </div>
                    <div className="features-grid">
                        {features.map((feature, index) => (
                            <div key={index} className="feature-card" style={{ "--delay": index + 1 }}>
                                <div className="feature-icon">{feature.icon}</div>
                                <h3>{feature.title}</h3>
                                <p>{feature.description}</p>
                            </div>
                        ))}
                    </div>
                </div>
            </section>
            
            {/* --- How It Works --- */}
            <section className="how-it-works-section">
                <div className="container">
                    <div className="section-header">
                        <h2 className="section-title">How It Works</h2>
                        <p className="section-subtitle">Get the help you need in three simple steps.</p>
                    </div>
                    <div className="how-it-works-grid">
                        <div className="step-card">
                            <div className="step-number">1</div>
                            <h3 className="step-title">Search & Compare</h3>
                            <p className="step-description">Browse local pros, read verified reviews, and compare prices.</p>
                        </div>
                        <div className="step-card">
                            <div className="step-number">2</div>
                            <h3 className="step-title">Book Instantly</h3>
                            <p className="step-description">Select your preferred time slot and book with just a few clicks.</p>
                        </div>
                        <div className="step-card">
                            <div className="step-number">3</div>
                            <h3 className="step-title">Get It Done</h3>
                            <p className="step-description">Enjoy quality service from a trusted professional and pay securely online.</p>
                        </div>
                    </div>
                </div>
            </section>

            {/* --- Service Categories --- */}
            <section className="categories-section">
                <div className="container">
                    <div className="section-header">
                        <h2 className="section-title">Popular Service Categories</h2>
                        <p className="section-subtitle">Find the right professional for any job, big or small.</p>
                    </div>
                    <div className="categories-grid">
                        {serviceCategories.map((category, index) => (
                            <div key={index} className="category-card">
                                <span className="category-icon">{category.icon}</span>
                                <div>
                                    <h3 className="category-name">{category.name}</h3>
                                    <p className="category-count">{category.count}</p>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            </section>
            
            {/* --- Testimonials --- */}
            <section className="testimonials-section">
                <div className="container">
                    <div className="section-header">
                        <h2 className="section-title">What Our Users Say</h2>
                        <p className="section-subtitle">Thousands of happy customers and successful service providers.</p>
                    </div>
                    <div className="testimonials-grid">
                        {testimonials.map((testimonial, index) => (
                            <div key={index} className="testimonial-card">
                                <div className="testimonial-rating">
                                    {[...Array(testimonial.rating)].map((_, i) => (
                                        <Star key={i} size={20} className="star-icon" />
                                    ))}
                                </div>
                                <p className="testimonial-content">"{testimonial.content}"</p>
                                <div className="testimonial-author">
                                    <div className="author-name">{testimonial.name}</div>
                                    <div className="author-role">{testimonial.role}</div>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            </section>

            {/* --- CTA Section --- */}
            <section className="cta-section">
                <div className="container">
                    {/* MODIFIED: Content is now wrapped in a card for the new design */}
                    <div className="cta-card">
                        <div className="cta-content">
                            <h2 className="cta-title">Ready to Get Started?</h2>
                            <p className="cta-subtitle">Join thousands of satisfied customers and grow your business with ServiceSphere.</p>
                            <div className="hero-cta-group">
                                <button onClick={handleGetStarted} className="btn btn-hero-primary">Find Services Now</button>
                                <button onClick={() => navigate('/signup?role=provider')} className="btn btn-hero-secondary">Become a Provider</button>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            {/* --- Footer --- */}
            <footer className="main-footer">
                <div className="container footer-content">
                    <div className="footer-brand">
                         <div className="logo">
                            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/><path d="M2 17L12 22L22 17" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/><path d="M2 12L12 17L22 12" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/></svg>
                            <span>ServiceSphere</span>
                        </div>
                        <p className="footer-tagline">Connecting communities with trusted local services.</p>
                    </div>
                    <div className="footer-links">
                        <h4>For Customers</h4>
                        <ul>
                            <li><a href="#features">How It Works</a></li>
                            <li><Link to="/login">Find Services</Link></li>
                            <li><a href="/#">Safety</a></li>
                        </ul>
                    </div>
                    <div className="footer-links">
                        <h4>For Providers</h4>
                        <ul>
                            <li><Link to="/signup?role=provider">Join as Provider</Link></li>
                            <li><a href="/#">Success Stories</a></li>
                            <li><a href="/#">Support</a></li>
                        </ul>
                    </div>
                    <div className="footer-links">
                        <h4>Company</h4>
                        <ul>
                            <li><a href="/#">About Us</a></li>
                            <li><a href="/#">Contact</a></li>
                            <li><a href="/#">Privacy Policy</a></li>
                        </ul>
                    </div>
                </div>
                <div className="footer-bottom">
                    <p>&copy; {new Date().getFullYear()} ServiceSphere. All rights reserved.</p>
                </div>
            </footer>
        </div>
    );
};

export default Home;