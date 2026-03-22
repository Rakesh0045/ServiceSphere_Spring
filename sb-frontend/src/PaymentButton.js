import React, { useState } from 'react';
import { toast } from 'react-toastify';

/**
 * PaymentButton
 * Handles the complete Razorpay checkout flow.
 * Props:
 *   booking         - booking object (.id, .service_name, .price, .provider_name)
 *   axiosWithAuth   - authenticated axios instance
 *   onPaymentSuccess - callback after successful payment
 */
const PaymentButton = ({ booking, axiosWithAuth, onPaymentSuccess }) => {
    const [loading, setLoading] = useState(false);

    const loadRazorpayScript = () =>
        new Promise((resolve) => {
            if (window.Razorpay) { resolve(true); return; }
            if (document.getElementById('razorpay-script')) {
                // Already injected but not ready yet — wait
                const check = setInterval(() => {
                    if (window.Razorpay) { clearInterval(check); resolve(true); }
                }, 100);
                setTimeout(() => { clearInterval(check); resolve(!!window.Razorpay); }, 5000);
                return;
            }
            const script = document.createElement('script');
            script.id = 'razorpay-script';
            script.src = 'https://checkout.razorpay.com/v1/checkout.js';
            script.onload = () => resolve(true);
            script.onerror = () => resolve(false);
            document.body.appendChild(script);
        });

    const handlePayment = async () => {
        if (loading) return;
        setLoading(true);

        try {
            // 1. Load Razorpay SDK
            const loaded = await loadRazorpayScript();
            if (!loaded) {
                toast.error("Failed to load payment gateway. Check your internet connection.");
                setLoading(false);
                return;
            }

            // 2. Create order on backend
            let data;
            try {
                const res = await axiosWithAuth.post('/payments/create-order', {
                    bookingId: booking.id,
                });
                data = res.data;
            } catch (err) {
                const msg = err.response?.data?.message || 'Failed to create payment order.';
                toast.error(msg);
                setLoading(false);
                return;
            }

            // Guard: don't open Razorpay if we got bad data
            if (!data?.keyId || !data?.orderId) {
                toast.error('Payment configuration error. Please contact support.');
                setLoading(false);
                return;
            }

            // 3. Get user info for prefill
            const user = JSON.parse(localStorage.getItem('user') || '{}');

            // 4. Open Razorpay checkout
            const options = {
                key: data.keyId,
                amount: data.amount,
                currency: data.currency,
                name: 'ServiceSphere',
                description: `Payment for ${data.serviceName}`,
                order_id: data.orderId,
                prefill: {
                    name: user.name || '',
                    email: user.email || '',
                },
                theme: { color: '#6a5af9' },
                handler: async (response) => {
                    // 5. Verify signature on backend
                    try {
                        await axiosWithAuth.post('/payments/verify', {
                            razorpayOrderId: response.razorpay_order_id,
                            razorpayPaymentId: response.razorpay_payment_id,
                            razorpaySignature: response.razorpay_signature,
                        });
                        toast.success('🎉 Payment successful! Your slot is secured.');
                        setLoading(false);
                        if (onPaymentSuccess) onPaymentSuccess();
                    } catch (err) {
                        toast.error(err.response?.data?.message || 'Payment verification failed.');
                        setLoading(false);
                    }
                },
                modal: {
                    ondismiss: () => {
                        toast.info('Payment cancelled.');
                        setLoading(false);
                    },
                    escape: true,
                    backdropclose: false,
                },
            };

            const rzp = new window.Razorpay(options);
            rzp.on('payment.failed', (resp) => {
                toast.error(`Payment failed: ${resp.error.description}`);
                setLoading(false);
            });
            rzp.open();

        } catch (err) {
            toast.error('Something went wrong. Please try again.');
            setLoading(false);
        }
    };

    return (
        <button
            onClick={handlePayment}
            disabled={loading}
            style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: '0.5rem',
                background: loading
                    ? 'rgba(16,185,129,0.35)'
                    : 'linear-gradient(90deg, #10b981, #059669)',
                color: 'white',
                border: 'none',
                borderRadius: '8px',
                padding: '10px 20px',
                fontWeight: 700,
                fontSize: '0.9rem',
                cursor: loading ? 'not-allowed' : 'pointer',
                fontFamily: 'inherit',
                transition: 'all 0.2s ease',
                boxShadow: loading ? 'none' : '0 4px 12px rgba(16,185,129,0.3)',
            }}
        >
            {loading ? (
                <>
                    <span style={{
                        width: 14, height: 14,
                        border: '2px solid rgba(255,255,255,0.35)',
                        borderTopColor: 'white',
                        borderRadius: '50%',
                        display: 'inline-block',
                        animation: 'rz-spin 0.8s linear infinite',
                    }} />
                    Processing...
                </>
            ) : (
                <>💳 Pay ₹{Number(booking.price).toLocaleString('en-IN')}</>
            )}
            <style>{`@keyframes rz-spin { to { transform: rotate(360deg); } }`}</style>
        </button>
    );
};

export default PaymentButton;