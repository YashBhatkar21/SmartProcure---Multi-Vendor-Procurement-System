import React, { useState, useEffect } from 'react'
import {
    CCard,
    CCardBody,
    CCol,
    CRow,
    CWidgetStatsC,
    CSpinner
} from '@coreui/react'
import CIcon from '@coreui/icons-react'
import { cilPeople, cilCart, cilMoney, cilList } from '@coreui/icons'
import { useNavigate } from 'react-router-dom'
import { getAdminDashboardStats } from '../../api/dashboard'

const AdminDashboard = () => {
    const navigate = useNavigate();
    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchStats = async () => {
            try {
                const response = await getAdminDashboardStats();
                setStats(response.data);
            } catch (error) {
                console.error("Failed to fetch dashboard stats", error);
            } finally {
                setLoading(false);
            }
        };
        fetchStats();
    }, []);

    if (loading) {
        return <div className="text-center mt-5"><CSpinner /></div>;
    }

    return (
        <CRow>
            <CCol xs={12} sm={6} lg={3}>
                <div onClick={() => navigate('/admin/vendors')} style={{ cursor: 'pointer' }} className="hover-lift h-100">
                    <CWidgetStatsC
                        className="mb-3"
                        icon={<CIcon icon={cilPeople} height={36} />}
                        color="primary"
                        inverse
                        progress={{ value: 100 }}
                        text="Manage vendors in the system"
                        title="Active Vendors"
                        value={stats?.activeVendors || 0}
                    />
                </div>
            </CCol>
            <CCol xs={12} sm={6} lg={3}>
                <div onClick={() => navigate('/admin/requests')} style={{ cursor: 'pointer' }} className="hover-lift h-100">
                    <CWidgetStatsC
                        className="mb-3"
                        icon={<CIcon icon={cilList} height={36} />}
                        color="info"
                        inverse
                        progress={{ value: 100 }}
                        text="Submitted procurement requests"
                        title="Procurement Requests"
                        value={stats?.totalProcurementRequests || 0}
                    />
                </div>
            </CCol>
            <CCol xs={12} sm={6} lg={3}>
                <div onClick={() => navigate('/admin/orders')} style={{ cursor: 'pointer' }} className="hover-lift h-100">
                    <CWidgetStatsC
                        className="mb-3"
                        icon={<CIcon icon={cilCart} height={36} />}
                        color="warning"
                        inverse
                        progress={{ value: 100 }}
                        text="Total orders processed"
                        title="Total Orders"
                        value={stats?.totalOrders || 0}
                    />
                </div>
            </CCol>
            <CCol xs={12} sm={6} lg={3}>
                <div style={{ cursor: 'pointer' }} className="hover-lift h-100">
                    <CWidgetStatsC
                        className="mb-3"
                        icon={<CIcon icon={cilMoney} height={36} />}
                        color="success"
                        inverse
                        progress={{ value: 100 }}
                        text="Total revenue value"
                        title="Total Revenue"
                        value={`$${stats?.totalRevenue || 0}`}
                    />
                </div>
            </CCol>
        </CRow>
    )
}

export default AdminDashboard
