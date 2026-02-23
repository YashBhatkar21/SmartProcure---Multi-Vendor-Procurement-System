import axiosInstance from './axios';

export const getUsers = (params) => {
    return axiosInstance.get('/admin/users', { params });
};

export const getVendors = (params) => {
    return axiosInstance.get('/admin/vendors', { params });
};

export const deleteUser = (id) => {
    return axiosInstance.delete(`/admin/users/${id}`);
};

export const getRequests = (params) => {
    return axiosInstance.get('/procurement-requests/available', { params });
};
