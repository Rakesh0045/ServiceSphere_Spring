import React, { useState } from "react";
import axios from "axios";
import { Link, useNavigate } from "react-router-dom";
import { ToastContainer, toast } from "react-toastify";
import { ArrowLeft, User, Mail, Lock, Briefcase, Users } from 'lucide-react';

// CSS is included directly in the component to resolve import errors.
const SignupStyles = () => (
    <style>{`
        /* --- Inherited Theme Variables & Font --- */
        @import url('https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;700;800&display=swap');

        :root {
          --primary-color: #6a5af9;
          --primary-light: #a99eff;
          --accent-color: #f92bff;
          --dark-bg: #10101a;
          --card-bg: rgba(255, 255, 255, 0.05);
          --text-primary: #f0f0f5;
          --text-secondary: #a0a0b0;
          --input-bg: rgba(255, 255, 255, 0.08);
          --border-color: rgba(255, 255, 255, 0.1);
        }

        /* --- Base & Background --- */
        .signup-bg {
          min-height: 100vh;
          width: 100vw;
          display: flex;
          justify-content: center;
          align-items: center;
          font-family: 'Outfit', sans-serif;
          background: var(--dark-bg) url("https://images.unsplash.com/photo-1504384308090-c894fdcc538d?ixlib=rb-4.0.3&auto=format&fit=crop&w=1600&q=80") no-repeat center center fixed;
          background-size: cover;
          background-blend-mode: overlay;
          padding: 2rem;
          box-sizing: border-box;
        }

        /* --- Signup Card --- */
        .signup-card {
          position: relative;
          max-width: 440px;
          width: 100%;
          padding: 3rem;
          background-color: var(--card-bg);
          border-radius: 20px;
          border: 1px solid var(--border-color);
          backdrop-filter: blur(12px);
          -webkit-backdrop-filter: blur(12px);
          box-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.37);
          text-align: center;
          animation: fadeIn 0.8s ease-in-out;
        }

        @keyframes fadeIn {
          from { opacity: 0; transform: translateY(20px); }
          to { opacity: 1; transform: translateY(0); }
        }
        
        /* --- Back to Home Button --- */
        .back-to-home {
            position: absolute;
            top: 2rem;
            left: 2rem;
            display: flex;
            align-items: center;
            gap: 0.5rem;
            color: var(--text-secondary);
            text-decoration: none;
            font-size: 0.9rem;
            font-weight: 500;
            transition: color 0.3s ease;
        }

        .back-to-home:hover {
            color: var(--primary-light);
        }

        /* --- Header & Subtitle --- */
        .signup-card h2.gradient-text {
          font-size: 2.2rem;
          font-weight: 700;
          margin-bottom: 0.75rem;
          background: linear-gradient(90deg, var(--primary-light), var(--accent-color));
          -webkit-background-clip: text;
          background-clip: text;
          -webkit-text-fill-color: transparent;
          color: var(--primary-color);
        }
        
        .signup-subtitle {
            color: var(--text-secondary);
            font-size: 1rem;
            margin-bottom: 2.5rem;
        }

        /* --- Form & Inputs --- */
        .signup-form {
          display: flex;
          flex-direction: column;
          gap: 1.5rem;
          text-align: left;
        }

        .input-group {
          position: relative;
        }
        
        .input-icon {
            position: absolute;
            top: 50%;
            left: 1rem;
            transform: translateY(-50%);
            color: var(--text-secondary);
            pointer-events: none;
            transition: color 0.3s ease;
        }

        .signup-form input {
          width: 100%;
          padding: 0.9rem 1rem 0.9rem 3rem;
          background-color: var(--input-bg);
          border: 1px solid var(--border-color);
          border-radius: 12px;
          color: var(--text-primary);
          font-size: 1rem;
          font-family: 'Outfit', sans-serif;
          transition: all 0.3s ease;
          box-sizing: border-box;
        }

        .signup-form input::placeholder {
          color: rgba(240, 240, 245, 0.4);
        }

        .signup-form input:focus {
          outline: none;
          border-color: var(--primary-color);
          box-shadow: 0 0 0 4px rgba(106, 90, 249, 0.2);
        }
        
        .signup-form input:focus + .input-icon {
            color: var(--primary-color);
        }

        /* --- Role Selection --- */
        .role-label {
            font-size: 0.9rem;
            font-weight: 500;
            color: var(--text-secondary);
            padding-left: 0.25rem;
            margin-bottom: 0.5rem;
        }
        
        .role-buttons {
          display: flex;
          gap: 1rem;
        }

        .role-btn {
          flex: 1;
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 0.5rem;
          padding: 0.8rem 1rem;
          font-size: 0.9rem;
          font-weight: 600;
          font-family: 'Outfit', sans-serif;
          border: 1px solid var(--border-color);
          border-radius: 12px;
          background: transparent;
          color: var(--text-secondary);
          cursor: pointer;
          transition: all 0.3s ease;
        }

        .role-btn:hover {
          background-color: var(--input-bg);
          border-color: var(--primary-color);
        }

        .role-btn.active-role {
          background: var(--primary-color);
          color: white;
          border-color: var(--primary-color);
          box-shadow: 0 4px 15px rgba(106, 90, 249, 0.3);
        }
        
        /* --- Submit Button & Switch Link --- */
        .submit-btn {
          width: 100%;
          padding: 1rem;
          margin-top: 1rem;
          background: linear-gradient(90deg, var(--primary-color), var(--accent-color));
          border: none;
          border-radius: 12px;
          color: white;
          font-weight: 700;
          font-size: 1rem;
          letter-spacing: 0.5px;
          cursor: pointer;
          box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
          transition: all 0.3s ease;
        }

        .submit-btn:hover {
          transform: translateY(-3px);
          box-shadow: 0 10px 25px rgba(0, 0, 0, 0.3);
        }

        .switch-text {
          font-size: 0.9rem;
          margin-top: 2rem;
          color: var(--text-secondary);
        }

        .switch-text a {
          color: var(--primary-light);
          text-decoration: none;
          font-weight: 600;
          transition: color 0.3s ease;
        }

        .switch-text a:hover {
          text-decoration: underline;
          color: var(--accent-color);
        }
        
        /* --- Responsive Design --- */
        @media (max-width: 500px) {
          .signup-card {
            padding: 2.5rem 1.5rem;
            max-width: 100%;
          }
          .back-to-home {
            top: 1.5rem;
            left: 1.5rem;
          }
          .signup-card h2.gradient-text {
            font-size: 1.8rem;
          }
        }
    `}</style>
);

const Signup = () => {
    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [role, setRole] = useState(""); 
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!role) {
            toast.error("Please select a role.", { position: "top-center" });
            return;
        }
        try {
            const res = await axios.post("http://localhost:8070/api/signup", {
                name,
                email,
                password,
                role,
            });
            toast.success(res.data.message, { position: "top-center" });
            setTimeout(() => {
                navigate("/login");
            }, 1500);
        } catch (err) {
            toast.error(err.response?.data?.message || "Signup failed", {
                position: "top-center",
            });
        }
    };

    return (
        <>
            <SignupStyles />
            <div className="signup-bg">
                <div className="signup-card">
                    <Link to="/" className="back-to-home">
                        <ArrowLeft size={20} />
                        <span>Home</span>
                    </Link>

                    <h2 className="gradient-text">Join ServiceSphere</h2>
                    <p className="signup-subtitle">Create your account to get started.</p>

                    <form className="signup-form" onSubmit={handleSubmit}>
                        <div className="input-group">
                            <User className="input-icon" size={20} />
                            <input
                                type="text"
                                value={name}
                                placeholder="Full Name"
                                onChange={(e) => setName(e.target.value)}
                                required
                            />
                        </div>
                        <div className="input-group">
                            <Mail className="input-icon" size={20} />
                            <input
                                type="email"
                                value={email}
                                placeholder="Email"
                                onChange={(e) => setEmail(e.target.value)}
                                required
                            />
                        </div>
                        <div className="input-group">
                            <Lock className="input-icon" size={20} />
                            <input
                                type="password"
                                value={password}
                                placeholder="Password"
                                onChange={(e) => setPassword(e.target.value)}
                                required
                            />
                        </div>

                        <div>
                            <div className="role-label">I am a...</div>
                            <div className="role-buttons">
                                <button
                                    type="button"
                                    className={`role-btn ${role === "Service Provider" ? "active-role" : ""}`}
                                    onClick={() => setRole("Service Provider")}
                                >
                                    <Briefcase size={18} />
                                    Provider
                                </button>
                                <button
                                    type="button"
                                    className={`role-btn ${role === "Customer" ? "active-role" : ""}`}
                                    onClick={() => setRole("Customer")}
                                >
                                    <Users size={18} />
                                    Customer
                                </button>
                            </div>
                        </div>

                        <button type="submit" className="submit-btn">Create Account</button>
                    </form>

                    <p className="switch-text">
                        Already have an account? <Link to="/login">Log In</Link>
                    </p>
                </div>
                <ToastContainer theme="dark" />
            </div>
        </>
    );
};

export default Signup;
