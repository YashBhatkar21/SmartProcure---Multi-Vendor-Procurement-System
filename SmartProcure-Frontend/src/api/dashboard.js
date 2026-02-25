import api from './axios';

export const getAdminDashboardStats = () => {
    return api.get('/dashboard/stats');
};

export const getAdvancedAdminDashboard = () => {
    return api.get('/dashboard/admin/advanced');
};

export const getVendorDashboardStats = () => {
    return api.get('/dashboard/vendor/advanced');
};
