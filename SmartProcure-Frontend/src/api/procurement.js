import api from './axios';

export const getMyRequests = () => {
    return api.get('/procurement-requests/me');
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

export const getAvailableRequests = () => {
    return api.get('/procurement-requests/available');
};

export const getMyQuotations = () => {
    return api.get('/quotations/me');
};

export const submitQuotation = (quotationData) => {
    return api.post('/quotations', quotationData);
};
