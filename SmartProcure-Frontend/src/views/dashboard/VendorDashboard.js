import React, { useState, useEffect } from 'react'
import {
    CButton,
    CCard,
    CCardBody,
    CCardHeader,
    CCol,
    CForm,
    CFormInput,
    CFormTextarea,
    CRow,
    CTable,
    CTableBody,
    CTableDataCell,
    CTableHead,
    CTableHeaderCell,
    CTableRow,
    CModal,
    CModalBody,
    CModalFooter,
    CModalHeader,
    CModalTitle,
    CBadge,
    CDropdown,
    CDropdownToggle,
    CDropdownMenu,
    CDropdownItem,
    CWidgetStatsC,
    CSpinner,
    CButtonToolbar
} from '@coreui/react'
import { CChartLine } from '@coreui/react-chartjs'
import CIcon from '@coreui/icons-react'
import { cilMoney, cilCart, cilGraph, cilCheckCircle } from '@coreui/icons'
import { getAvailableRequests, getMyQuotations, submitQuotation, getVendorOrders, updateOrderStatus } from '../../api/procurement'
import { getVendorDashboardStats } from '../../api/dashboard'

const VendorDashboard = () => {
    const [availableRequests, setAvailableRequests] = useState([])
    const [myQuotations, setMyQuotations] = useState([])
    const [orders, setOrders] = useState([])
    const [stats, setStats] = useState(null)
    const [quoteModalVisible, setQuoteModalVisible] = useState(false)
    const [activeRequest, setActiveRequest] = useState(null)
    const [newQuote, setNewQuote] = useState({ quotedAmount: '', terms: '', validUntil: '' })

    // Filters for Available Requests
    const [requestSearch, setRequestSearch] = useState('');
    const [requestStatus, setRequestStatus] = useState('');

    // Filters for Orders
    const [orderSearch, setOrderSearch] = useState('');
    const [orderStatus, setOrderStatus] = useState('');

    const fetchData = async () => {
        try {
            const reqParams = {};
            if (requestSearch) reqParams.search = requestSearch;
            if (requestStatus) reqParams.status = requestStatus;

            const orderParams = {};
            if (orderSearch) orderParams.search = orderSearch;
            if (orderStatus) orderParams.status = orderStatus;

            const [requestsRes, quotesRes, ordersRes, statsRes] = await Promise.all([
                getAvailableRequests(reqParams),
                getMyQuotations(),
                getVendorOrders(orderParams),
                getVendorDashboardStats()
            ]);
            setAvailableRequests(requestsRes.data.content || requestsRes.data);
            setMyQuotations(quotesRes.data);
            setOrders(ordersRes.data.content || ordersRes.data);
            setStats(statsRes.data);
        } catch (error) {
            console.error('Error fetching data', error);
        }
    }

    useEffect(() => {
        fetchData()
    }, [requestSearch, requestStatus, orderSearch, orderStatus])

    const handleOpenQuoteModal = (req) => {
        setActiveRequest(req);
        setQuoteModalVisible(true);
    }

    const handleSubmitQuote = async (e) => {
        e.preventDefault();
        try {
            const payload = {
                requestId: activeRequest.id,
                quotedAmount: parseFloat(newQuote.quotedAmount),
                terms: newQuote.terms,
                validUntil: newQuote.validUntil ? new Date(newQuote.validUntil).toISOString() : null
            };
            await submitQuotation(payload);
            setQuoteModalVisible(false);
            setNewQuote({ quotedAmount: '', terms: '', validUntil: '' });
            fetchData();
            alert('Quotation submitted successfully!');
        } catch (error) {
            console.error('Error submitting quote', error);
            alert(error.response?.data?.message || 'Failed to submit quote. You may have already submitted one.');
        }
    }

    const handleUpdateStatus = async (orderId, status) => {
        try {
            await updateOrderStatus(orderId, status);
            alert(`Order status updated to ${status}`);
            fetchData();
        } catch (error) {
            console.error('Error updating status', error);
            alert('Failed to update order status');
        }
    }

    return (
        <>
            {/* Vendor Analytics Section */}
            {stats ? (
                <>
                    <CRow className="mb-4">
                        <CCol xs={12} sm={6} lg={3}>
                            <CWidgetStatsC
                                className="mb-3 hover-lift h-100"
                                icon={<CIcon icon={cilMoney} height={36} />}
                                color="success"
                                inverse
                                progress={{ value: 100 }}
                                text="Total Revenue Generated"
                                title="Revenue"
                                value={`$${stats.totalRevenueGenerated || 0}`}
                            />
                        </CCol>
                        <CCol xs={12} sm={6} lg={3}>
                            <CWidgetStatsC
                                className="mb-3 hover-lift h-100"
                                icon={<CIcon icon={cilCart} height={36} />}
                                color="info"
                                inverse
                                progress={{ value: 100 }}
                                text="Orders Completed"
                                title="Completed"
                                value={stats.ordersCompleted || 0}
                            />
                        </CCol>
                        <CCol xs={12} sm={6} lg={3}>
                            <CWidgetStatsC
                                className="mb-3 hover-lift h-100"
                                icon={<CIcon icon={cilGraph} height={36} />}
                                color="warning"
                                inverse
                                progress={{ value: 100 }}
                                text="Pending Payments from system"
                                title="Pending Payments"
                                value={`$${stats.pendingPayments || 0}`}
                            />
                        </CCol>
                        <CCol xs={12} sm={6} lg={3}>
                            <CWidgetStatsC
                                className="mb-3 hover-lift h-100"
                                icon={<CIcon icon={cilCheckCircle} height={36} />}
                                color="primary"
                                inverse
                                progress={{ value: stats.performanceScore || 0 }}
                                text="Conversion rate (Orders / Quotes)"
                                title="Performance Score"
                                value={`${stats.performanceScore || 0}%`}
                            />
                        </CCol>
                    </CRow>
                    <CRow className="mb-4">
                        <CCol xs={12}>
                            <CCard className="shadow-sm border-0">
                                <CCardHeader><strong>Monthly Revenue Trend</strong></CCardHeader>
                                <CCardBody>
                                    <CChartLine
                                        data={{
                                            labels: stats.monthlyRevenue?.map(m => m.label) || [],
                                            datasets: [
                                                {
                                                    label: 'Revenue',
                                                    backgroundColor: 'rgba(50, 31, 219, 0.2)',
                                                    borderColor: '#321fdb',
                                                    pointBackgroundColor: '#321fdb',
                                                    pointBorderColor: '#fff',
                                                    data: stats.monthlyRevenue?.map(m => m.value) || [],
                                                    fill: true,
                                                },
                                            ],
                                        }}
                                        options={{ maintainAspectRatio: false, plugins: { legend: { display: false } } }}
                                        style={{ height: '300px' }}
                                    />
                                </CCardBody>
                            </CCard>
                        </CCol>
                    </CRow>
                </>
            ) : <CSpinner className="mb-4 d-block mx-auto" />}

            <CRow>
                <CCol xs={12} lg={6}>
                    <CCard className="mb-4 hover-lift shadow-sm border-0">
                        <CCardHeader className="d-flex justify-content-between align-items-center">
                            <strong>Available Requests</strong>
                            <div className="d-flex justify-content-end gap-2">
                                <CFormInput placeholder="Search request title..." value={requestSearch} onChange={e => setRequestSearch(e.target.value)} size="sm" style={{ maxWidth: '150px' }} />
                                <select className="form-select form-select-sm" style={{ maxWidth: '120px' }} value={requestStatus} onChange={e => setRequestStatus(e.target.value)}>
                                    <option value="">Status: Open</option>
                                    <option value="IN_PROGRESS">In Progress</option>
                                    <option value="COMPLETED">Completed</option>
                                    <option value="CANCELLED">Cancelled</option>
                                </select>
                            </div>
                        </CCardHeader>
                        <CCardBody className="p-4">
                            <CTable hover striped responsive className="mb-0">
                                <CTableHead>
                                    <CTableRow>
                                        <CTableHeaderCell>Title</CTableHeaderCell>
                                        <CTableHeaderCell>Budget</CTableHeaderCell>
                                        <CTableHeaderCell>Due Date</CTableHeaderCell>
                                        <CTableHeaderCell>Action</CTableHeaderCell>
                                    </CTableRow>
                                </CTableHead>
                                <CTableBody>
                                    {availableRequests.map((req) => {
                                        const hasQuoted = myQuotations.some(q => q.requestId === req.id);
                                        return (
                                            <CTableRow key={req.id}>
                                                <CTableDataCell>{req.title}</CTableDataCell>
                                                <CTableDataCell>${req.budget}</CTableDataCell>
                                                <CTableDataCell>{new Date(req.dueDate).toLocaleDateString()}</CTableDataCell>
                                                <CTableDataCell>
                                                    {hasQuoted ? (
                                                        <CBadge className="px-3 py-2" color="secondary">Quoted</CBadge>
                                                    ) : (
                                                        <CButton className="btn-premium" size="sm" onClick={() => handleOpenQuoteModal(req)}>
                                                            Quote
                                                        </CButton>
                                                    )}
                                                </CTableDataCell>
                                            </CTableRow>
                                        )
                                    })}
                                    {availableRequests.length === 0 && (
                                        <CTableRow><CTableDataCell colSpan="4" className="text-center">No open requests.</CTableDataCell></CTableRow>
                                    )}
                                </CTableBody>
                            </CTable>
                        </CCardBody>
                    </CCard>
                </CCol>

                <CCol xs={12} lg={6}>
                    <CCard className="mb-4 hover-lift shadow-sm border-0">
                        <CCardHeader><strong>My Quotations</strong></CCardHeader>
                        <CCardBody className="p-4">
                            <CTable hover striped responsive className="mb-0">
                                <CTableHead>
                                    <CTableRow>
                                        <CTableHeaderCell>Request ID</CTableHeaderCell>
                                        <CTableHeaderCell>Amount</CTableHeaderCell>
                                        <CTableHeaderCell>Status</CTableHeaderCell>
                                    </CTableRow>
                                </CTableHead>
                                <CTableBody>
                                    {myQuotations.map((quote) => (
                                        <CTableRow key={quote.id}>
                                            <CTableDataCell>{quote.requestId}</CTableDataCell>
                                            <CTableDataCell>${quote.quotedAmount}</CTableDataCell>
                                            <CTableDataCell>
                                                <CBadge className="px-3 py-2 text-white" style={{
                                                    backgroundColor: quote.status === 'ACCEPTED' ? 'var(--success-emerald)' :
                                                        quote.status === 'REJECTED' ? '#ef4444' : '#64748b'
                                                }}>
                                                    {quote.status}
                                                </CBadge>
                                            </CTableDataCell>
                                        </CTableRow>
                                    ))}
                                    {myQuotations.length === 0 && (
                                        <CTableRow><CTableDataCell colSpan="3" className="text-center">No quotations submitted.</CTableDataCell></CTableRow>
                                    )}
                                </CTableBody>
                            </CTable>
                        </CCardBody>
                    </CCard>
                </CCol>
            </CRow>

            <CRow>
                <CCol xs={12}>
                    <CCard className="mb-4 hover-lift shadow-sm border-0">
                        <CCardHeader className="d-flex justify-content-between align-items-center">
                            <strong>My Active Purchase Orders</strong>
                            <div className="d-flex justify-content-end gap-2">
                                <CFormInput placeholder="Search order #..." value={orderSearch} onChange={e => setOrderSearch(e.target.value)} size="sm" style={{ maxWidth: '200px' }} />
                                <select className="form-select form-select-sm" style={{ maxWidth: '150px' }} value={orderStatus} onChange={e => setOrderStatus(e.target.value)}>
                                    <option value="">All Statuses</option>
                                    <option value="PROCESSING">Processing</option>
                                    <option value="SHIPPED">Shipped</option>
                                    <option value="DELIVERED">Delivered</option>
                                    <option value="CANCELLED">Cancelled</option>
                                </select>
                            </div>
                        </CCardHeader>
                        <CCardBody className="p-4">
                            <CTable hover striped responsive className="mb-0">
                                <CTableHead>
                                    <CTableRow>
                                        <CTableHeaderCell>Order #</CTableHeaderCell>
                                        <CTableHeaderCell>Request Title</CTableHeaderCell>
                                        <CTableHeaderCell>Customer Name</CTableHeaderCell>
                                        <CTableHeaderCell>Total Amount</CTableHeaderCell>
                                        <CTableHeaderCell>Status</CTableHeaderCell>
                                        <CTableHeaderCell>Created At</CTableHeaderCell>
                                        <CTableHeaderCell>Update Status</CTableHeaderCell>
                                    </CTableRow>
                                </CTableHead>
                                <CTableBody>
                                    {orders.map((order) => (
                                        <CTableRow key={order.id}>
                                            <CTableDataCell><strong>{order.orderNumber}</strong></CTableDataCell>
                                            <CTableDataCell>{order.requestTitle}</CTableDataCell>
                                            <CTableDataCell>{order.customerName}</CTableDataCell>
                                            <CTableDataCell>${order.totalAmount}</CTableDataCell>
                                            <CTableDataCell>
                                                <CBadge className="px-3 py-2 text-white" style={{
                                                    backgroundColor: order.status === 'DELIVERED' ? 'var(--success-emerald)' :
                                                        order.status === 'SHIPPED' ? '#0ea5e9' :
                                                            order.status === 'PROCESSING' ? '#f59e0b' : '#ef4444'
                                                }}>
                                                    {order.status}
                                                </CBadge>
                                            </CTableDataCell>
                                            <CTableDataCell>{new Date(order.createdAt).toLocaleDateString()}</CTableDataCell>
                                            <CTableDataCell>
                                                <CDropdown variant="btn-group">
                                                    <CDropdownToggle color="primary" size="sm">Action</CDropdownToggle>
                                                    <CDropdownMenu>
                                                        <CDropdownItem onClick={() => handleUpdateStatus(order.id, 'PROCESSING')}>Mark as Processing</CDropdownItem>
                                                        <CDropdownItem onClick={() => handleUpdateStatus(order.id, 'SHIPPED')}>Mark as Shipped</CDropdownItem>
                                                        <CDropdownItem onClick={() => handleUpdateStatus(order.id, 'DELIVERED')}>Mark as Delivered</CDropdownItem>
                                                    </CDropdownMenu>
                                                </CDropdown>
                                            </CTableDataCell>
                                        </CTableRow>
                                    ))}
                                    {orders.length === 0 && (
                                        <CTableRow><CTableDataCell colSpan="7" className="text-center">No active orders yet.</CTableDataCell></CTableRow>
                                    )}
                                </CTableBody>
                            </CTable>
                        </CCardBody>
                    </CCard>
                </CCol>
            </CRow>

            {/* Payments Render Section */}
            {stats && stats.recentPaymentsReceived && (
                <CRow>
                    <CCol xs={12}>
                        <CCard className="mb-4 hover-lift shadow-sm border-0">
                            <CCardHeader>
                                <strong>Recent Payments Received</strong>
                            </CCardHeader>
                            <CCardBody className="p-4">
                                <CTable hover striped responsive className="mb-0">
                                    <CTableHead>
                                        <CTableRow>
                                            <CTableHeaderCell>Transaction ID</CTableHeaderCell>
                                            <CTableHeaderCell>Order #</CTableHeaderCell>
                                            <CTableHeaderCell>Amount</CTableHeaderCell>
                                            <CTableHeaderCell>Method</CTableHeaderCell>
                                            <CTableHeaderCell>Status</CTableHeaderCell>
                                            <CTableHeaderCell>Date</CTableHeaderCell>
                                        </CTableRow>
                                    </CTableHead>
                                    <CTableBody>
                                        {stats.recentPaymentsReceived.map((payment) => (
                                            <CTableRow key={payment.id}>
                                                <CTableDataCell>
                                                    <code className="text-secondary">{payment.transactionId}</code>
                                                </CTableDataCell>
                                                <CTableDataCell>{payment.orderNumber}</CTableDataCell>
                                                <CTableDataCell><strong>${payment.amount}</strong></CTableDataCell>
                                                <CTableDataCell>{payment.paymentMethod || 'N/A'}</CTableDataCell>
                                                <CTableDataCell>
                                                    <CBadge className="px-3 py-2 text-white" style={{
                                                        backgroundColor: payment.status === 'SUCCESS' ? 'var(--success-emerald)' :
                                                            payment.status === 'PENDING' ? '#f59e0b' : '#ef4444'
                                                    }}>
                                                        {payment.status}
                                                    </CBadge>
                                                </CTableDataCell>
                                                <CTableDataCell>{payment.paidAt ? new Date(payment.paidAt).toLocaleString() : 'N/A'}</CTableDataCell>
                                            </CTableRow>
                                        ))}
                                        {stats.recentPaymentsReceived.length === 0 && (
                                            <CTableRow><CTableDataCell colSpan="6" className="text-center">No payments received yet.</CTableDataCell></CTableRow>
                                        )}
                                    </CTableBody>
                                </CTable>
                            </CCardBody>
                        </CCard>
                    </CCol>
                </CRow>
            )}

            {/* Quote Modal */}
            <CModal visible={quoteModalVisible} onClose={() => setQuoteModalVisible(false)}>
                <CModalHeader>
                    <CModalTitle>Submit Quote for: {activeRequest?.title}</CModalTitle>
                </CModalHeader>
                <CForm onSubmit={handleSubmitQuote}>
                    <CModalBody>
                        <div className="mb-3">
                            <CFormInput type="number" label="Quoted Amount ($)" value={newQuote.quotedAmount} onChange={(e) => setNewQuote({ ...newQuote, quotedAmount: e.target.value })} required step="0.01" />
                        </div>
                        <div className="mb-3">
                            <CFormTextarea label="Terms" rows={3} value={newQuote.terms} onChange={(e) => setNewQuote({ ...newQuote, terms: e.target.value })} placeholder="Optional terms or messages" />
                        </div>
                        <div className="mb-3">
                            <CFormInput type="date" label="Valid Until" value={newQuote.validUntil} onChange={(e) => setNewQuote({ ...newQuote, validUntil: e.target.value })} />
                        </div>
                    </CModalBody>
                    <CModalFooter>
                        <CButton color="secondary" onClick={() => setQuoteModalVisible(false)}>Cancel</CButton>
                        <CButton color="primary" type="submit">Submit</CButton>
                    </CModalFooter>
                </CForm>
            </CModal>
        </>
    )
}

export default VendorDashboard
