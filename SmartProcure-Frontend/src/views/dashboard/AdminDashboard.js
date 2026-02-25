import React, { useState, useEffect } from 'react'
import {
    CCard,
    CCardBody,
    CCardHeader,
    CCol,
    CRow,
    CWidgetStatsC,
    CSpinner
} from '@coreui/react'
import { CChartLine, CChartBar, CChartPie } from '@coreui/react-chartjs'
import CIcon from '@coreui/icons-react'
import { cilPeople, cilCart, cilMoney, cilList } from '@coreui/icons'
import { useNavigate } from 'react-router-dom'
import { getAdvancedAdminDashboard } from '../../api/dashboard'

const AdminDashboard = () => {
    const navigate = useNavigate();
    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchStats = async () => {
            try {
                const response = await getAdvancedAdminDashboard();
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

    if (!stats) {
        return <div className="text-center mt-5 text-danger">Failed to load data.</div>;
    }

    // Prepare chart data
    const monthlyRevenueLabels = stats.monthlyRevenue?.map(item => item.label) || [];
    const monthlyRevenueData = stats.monthlyRevenue?.map(item => item.value) || [];

    const purchaseLabels = stats.purchasesPerCustomer?.map(item => item.label) || [];
    const purchaseData = stats.purchasesPerCustomer?.map(item => item.value) || [];

    const statusLabels = stats.orderStatusDistribution?.map(item => item.label) || [];
    const statusData = stats.orderStatusDistribution?.map(item => item.value) || [];

    // Merge vendor activity labels (since quotes vs orders vendors might differ slightly)
    const vendorMap = new Map();
    stats.vendorActivityQuotations?.forEach(v => {
        if (!vendorMap.has(v.label)) vendorMap.set(v.label, { quotes: 0, orders: 0 });
        vendorMap.get(v.label).quotes = v.value;
    });
    stats.vendorActivityOrders?.forEach(v => {
        if (!vendorMap.has(v.label)) vendorMap.set(v.label, { quotes: 0, orders: 0 });
        vendorMap.get(v.label).orders = v.value;
    });

    const vendorActivityLabels = Array.from(vendorMap.keys());
    const vendorQuotesData = vendorActivityLabels.map(label => vendorMap.get(label).quotes);
    const vendorOrdersData = vendorActivityLabels.map(label => vendorMap.get(label).orders);

    return (
        <>
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
                            value={stats.activeVendors || 0}
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
                            value={stats.totalProcurementRequests || 0}
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
                            value={stats.totalOrders || 0}
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
                            value={`$${stats.totalRevenue || 0}`}
                        />
                    </div>
                </CCol>
            </CRow>

            <CRow className="mt-4">
                <CCol xs={12} lg={8}>
                    <CCard className="mb-4">
                        <CCardHeader>Total Revenue (Monthly Trend)</CCardHeader>
                        <CCardBody>
                            <CChartLine
                                data={{
                                    labels: monthlyRevenueLabels,
                                    datasets: [
                                        {
                                            label: 'Revenue',
                                            backgroundColor: 'rgba(50, 31, 219, 0.2)',
                                            borderColor: '#321fdb',
                                            pointBackgroundColor: '#321fdb',
                                            pointBorderColor: '#fff',
                                            data: monthlyRevenueData,
                                            fill: true,
                                        },
                                    ],
                                }}
                                options={{
                                    maintainAspectRatio: false,
                                    plugins: { legend: { display: false } }
                                }}
                                style={{ height: '300px' }}
                            />
                        </CCardBody>
                    </CCard>
                </CCol>
                <CCol xs={12} lg={4}>
                    <CCard className="mb-4">
                        <CCardHeader>Order Status Distribution</CCardHeader>
                        <CCardBody>
                            <CChartPie
                                data={{
                                    labels: statusLabels,
                                    datasets: [
                                        {
                                            data: statusData,
                                            backgroundColor: ['#41B883', '#E46651', '#00D8FF', '#DD1B16', '#F9B115'],
                                        },
                                    ],
                                }}
                                options={{
                                    maintainAspectRatio: false,
                                }}
                                style={{ height: '300px' }}
                            />
                        </CCardBody>
                    </CCard>
                </CCol>
            </CRow>

            <CRow>
                <CCol xs={12} lg={6}>
                    <CCard className="mb-4">
                        <CCardHeader>Purchases per Customer</CCardHeader>
                        <CCardBody>
                            <CChartBar
                                data={{
                                    labels: purchaseLabels,
                                    datasets: [
                                        {
                                            label: 'Orders',
                                            backgroundColor: '#39f',
                                            data: purchaseData,
                                        },
                                    ],
                                }}
                                options={{
                                    maintainAspectRatio: false,
                                    plugins: { legend: { display: false } }
                                }}
                                style={{ height: '300px' }}
                            />
                        </CCardBody>
                    </CCard>
                </CCol>
                <CCol xs={12} lg={6}>
                    <CCard className="mb-4">
                        <CCardHeader>Vendor Activity</CCardHeader>
                        <CCardBody>
                            <CChartBar
                                data={{
                                    labels: vendorActivityLabels,
                                    datasets: [
                                        {
                                            label: 'Quotes Submitted',
                                            backgroundColor: '#f9b115',
                                            data: vendorQuotesData,
                                        },
                                        {
                                            label: 'Orders Completed',
                                            backgroundColor: '#2eb85c',
                                            data: vendorOrdersData,
                                        }
                                    ],
                                }}
                                options={{
                                    maintainAspectRatio: false,
                                }}
                                style={{ height: '300px' }}
                            />
                        </CCardBody>
                    </CCard>
                </CCol>
            </CRow>
        </>
    )
}

export default AdminDashboard
