import React, { useState } from 'react';
import { toast } from 'react-toastify';

const PaymentButton = ({ booking, axiosWithAuth, onPaymentSuccess }) => {
    const [paying, setPaying] = useState(false);

    const handlePay = async () => {
        if (!booking?.id || !axiosWithAuth) {
            toast.error('Unable to process payment right now.');
            return;
        }

        setPaying(true);
        try {
            await axiosWithAuth.put(`/bookings/${booking.id}/status`, { status: 'Paid' });
            toast.success('Payment successful.');
            if (onPaymentSuccess) {
                onPaymentSuccess();
            }
        } catch (error) {
            toast.error(error.response?.data?.message || 'Payment failed. Please try again.');
        } finally {
            setPaying(false);
        }
    };

    return (
        <button
            type="button"
            className="btn btn-primary"
            onClick={handlePay}
            disabled={paying}
            style={{ minWidth: '140px' }}
        >
            {paying ? 'Processing...' : 'Pay Now'}
        </button>
    );
};

export default PaymentButton;
