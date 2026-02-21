import api from './axios';

export const getAdminDashboardStats = () => {
    return api.get('/dashboard/stats');
};
