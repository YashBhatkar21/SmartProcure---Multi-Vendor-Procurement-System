import axios from 'axios';

const API_URL = 'http://localhost:8081/api/payments';

// Set up Axios interceptor for JWT
axios.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

export const initiatePayment = (orderId, method = 'CARD') => {
    return axios.post(`${API_URL}/initiate/${orderId}?method=${method}`);
};

export const confirmPayment = (transactionId, isSuccess, paymentMethod = 'CARD') => {
    return axios.post(`${API_URL}/confirm`, {
        transactionId,
        isSuccess,
        paymentMethod
    });
};

export const getVendorPayments = () => {
    return axios.get(`${API_URL}/vendor`);
};

export const getCustomerPayments = () => {
    return axios.get(`${API_URL}/customer`);
};
