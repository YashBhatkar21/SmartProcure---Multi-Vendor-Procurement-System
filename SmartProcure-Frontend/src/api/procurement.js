import api from './axios';

export const getMyRequests = (params = {}) => {
    return api.get('/procurement-requests/me', { params });
};

export const createRequest = (requestData) => {
    return api.post('/procurement-requests', requestData);
};

export const getQuotationsForRequest = (requestId) => {
    return api.get(`/quotations/request/${requestId}`);
};

export const acceptQuotation = (quotationId) => {
    return api.post(`/quotations/${quotationId}/accept`);
};

export const rejectQuotation = (quotationId) => {
    return api.post(`/quotations/${quotationId}/reject`);
};

export const getAvailableRequests = (params = {}) => {
    return api.get('/procurement-requests/available', { params });
};

export const getMyQuotations = () => {
    return api.get('/quotations/me');
};

export const submitQuotation = (quotationData) => {
    return api.post('/quotations', quotationData);
};

export const getCustomerOrders = (params = {}) => {
    return api.get('/orders/customer', { params });
};

export const getVendorOrders = (params = {}) => {
    return api.get('/orders/vendor', { params });
};

export const updateOrderStatus = (orderId, status) => {
    return api.patch(`/orders/${orderId}/status?status=${status}`);
};
