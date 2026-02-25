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
    CSpinner
} from '@coreui/react'
import { getMyRequests, createRequest, getQuotationsForRequest, acceptQuotation, getCustomerOrders } from '../../api/procurement'
import { initiatePayment, confirmPayment, getCustomerPayments } from '../../api/payment'

const CustomerDashboard = () => {
    const [requests, setRequests] = useState([])
    const [createModalVisible, setCreateModalVisible] = useState(false)
    const [quotationsModalVisible, setQuotationsModalVisible] = useState(false)
    const [activeRequest, setActiveRequest] = useState(null)
    const [quotations, setQuotations] = useState([])
    const [orders, setOrders] = useState([])
    const [payments, setPayments] = useState([])
    const [newRequest, setNewRequest] = useState({ title: '', description: '', budget: '', dueDate: '' })

    // Payment Modal State
    const [paymentModalVisible, setPaymentModalVisible] = useState(false)
    const [activePaymentOrder, setActivePaymentOrder] = useState(null)
    const [activeTransaction, setActiveTransaction] = useState(null)
    const [isProcessingPayment, setIsProcessingPayment] = useState(false)

    // Filters for Requests
    const [requestSearch, setRequestSearch] = useState('');
    const [requestStatus, setRequestStatus] = useState('');

    // Filters for Orders
    const [orderSearch, setOrderSearch] = useState('');
    const [orderStatus, setOrderStatus] = useState('');

    const fetchRequests = async () => {
        try {
            const params = {};
            if (requestSearch) params.search = requestSearch;
            if (requestStatus) params.status = requestStatus;

            const { data } = await getMyRequests(params);
            setRequests(data.content || data);
        } catch (error) {
            console.error('Error fetching requests', error);
        }
    }

    const fetchOrders = async () => {
        try {
            const params = {};
            if (orderSearch) params.search = orderSearch;
            if (orderStatus) params.status = orderStatus;

            const { data } = await getCustomerOrders(params);
            setOrders(data.content || data);
        } catch (error) {
            console.error('Error fetching orders', error);
        }
    }

    const fetchPayments = async () => {
        try {
            const { data } = await getCustomerPayments();
            setPayments(data);
        } catch (error) {
            console.error('Error fetching payments', error);
        }
    }

    useEffect(() => {
        fetchRequests()
    }, [requestSearch, requestStatus])

    useEffect(() => {
        fetchOrders()
    }, [orderSearch, orderStatus])

    useEffect(() => {
        fetchPayments()
    }, [])

    const handleCreateRequest = async (e) => {
        e.preventDefault();
        try {
            // Add 'Z' so the backend can parse the ISO extended format to Instant properly.
            const payload = {
                title: newRequest.title,
                description: newRequest.description,
                budget: parseFloat(newRequest.budget),
                dueDate: newRequest.dueDate ? new Date(newRequest.dueDate).toISOString() : null
            };
            await createRequest(payload);
            setCreateModalVisible(false);
            setNewRequest({ title: '', description: '', budget: '', dueDate: '' });
            fetchRequests();
        } catch (error) {
            console.error('Error creating request', error);
            alert('Failed to create request');
        }
    }

    const viewQuotations = async (request) => {
        setActiveRequest(request);
        try {
            const { data } = await getQuotationsForRequest(request.id);
            setQuotations(data);
            setQuotationsModalVisible(true);
        } catch (error) {
            console.error('Error fetching quotations', error);
        }
    }

    const handleAcceptQuotation = async (quotationId) => {
        try {
            await acceptQuotation(quotationId);
            alert('Quotation accepted! A Purchase Order has been automatically generated.');
            setQuotationsModalVisible(false);
            fetchRequests();
            fetchOrders();
        } catch (error) {
            console.error(error);
            alert('Failed to accept quotation.');
        }
    }

    const handleInitiatePayment = async (order) => {
        try {
            const { data } = await initiatePayment(order.id, 'CARD');
            setActivePaymentOrder(order);
            setActiveTransaction(data);
            setPaymentModalVisible(true);
        } catch (error) {
            console.error('Error initiating payment', error);
            alert(error.response?.data?.message || 'Failed to initiate payment.');
        }
    }

    const handleConfirmPayment = async (isSuccess) => {
        setIsProcessingPayment(true);
        try {
            await confirmPayment(activeTransaction.transactionId, isSuccess, 'CARD');
            alert(isSuccess ? 'Payment Successful!' : 'Payment Failed.');
            setPaymentModalVisible(false);
            fetchOrders();
            fetchPayments();
        } catch (error) {
            console.error('Error confirming payment', error);
            alert('An error occurred during payment processing.');
        } finally {
            setIsProcessingPayment(false);
        }
    }

    return (
        <>
            <CRow>
                <CCol xs={12}>
                    <CCard className="mb-4 hover-lift shadow-sm border-0">
                        <CCardHeader className="d-flex justify-content-between align-items-center">
                            <strong>My Procurement Requests</strong>
                            <div className="d-flex w-50 justify-content-end gap-2">
                                <CFormInput placeholder="Search request title..." value={requestSearch} onChange={e => setRequestSearch(e.target.value)} size="sm" style={{ maxWidth: '200px' }} />
                                <select className="form-select form-select-sm" style={{ maxWidth: '150px' }} value={requestStatus} onChange={e => setRequestStatus(e.target.value)}>
                                    <option value="">All Statuses</option>
                                    <option value="OPEN">Open</option>
                                    <option value="IN_PROGRESS">In Progress</option>
                                    <option value="COMPLETED">Completed</option>
                                    <option value="CANCELLED">Cancelled</option>
                                </select>
                                <CButton color="primary" size="sm" onClick={() => setCreateModalVisible(true)}>Create Request</CButton>
                            </div>
                        </CCardHeader>
                        <CCardBody className="p-4">
                            <CTable hover striped responsive className="mb-0">
                                <CTableHead>
                                    <CTableRow>
                                        <CTableHeaderCell>ID</CTableHeaderCell>
                                        <CTableHeaderCell>Title</CTableHeaderCell>
                                        <CTableHeaderCell>Budget</CTableHeaderCell>
                                        <CTableHeaderCell>Status</CTableHeaderCell>
                                        <CTableHeaderCell>Due Date</CTableHeaderCell>
                                        <CTableHeaderCell>Actions</CTableHeaderCell>
                                    </CTableRow>
                                </CTableHead>
                                <CTableBody>
                                    {requests.map((req) => (
                                        <CTableRow key={req.id}>
                                            <CTableDataCell>{req.id}</CTableDataCell>
                                            <CTableDataCell>{req.title}</CTableDataCell>
                                            <CTableDataCell>${req.budget}</CTableDataCell>
                                            <CTableDataCell>
                                                <CBadge className="px-3 py-2 text-white" style={{
                                                    backgroundColor: req.status === 'OPEN' ? 'var(--primary-indigo)' :
                                                        req.status === 'COMPLETED' ? 'var(--success-emerald)' :
                                                            req.status === 'CANCELLED' ? '#ef4444' : '#64748b'
                                                }}>
                                                    {req.status}
                                                </CBadge>
                                            </CTableDataCell>
                                            <CTableDataCell>{new Date(req.dueDate).toLocaleDateString()}</CTableDataCell>
                                            <CTableDataCell>
                                                <CButton color="info" variant="outline" size="sm" onClick={() => viewQuotations(req)}>
                                                    View Quotations
                                                </CButton>
                                            </CTableDataCell>
                                        </CTableRow>
                                    ))}
                                    {requests.length === 0 && (
                                        <CTableRow><CTableDataCell colSpan="6" className="text-center">No requests found.</CTableDataCell></CTableRow>
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
                            <div className="d-flex w-50 justify-content-end gap-2">
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
                                        <CTableHeaderCell>Vendor</CTableHeaderCell>
                                        <CTableHeaderCell>Total Amount</CTableHeaderCell>
                                        <CTableHeaderCell>Status</CTableHeaderCell>
                                        <CTableHeaderCell>Created At</CTableHeaderCell>
                                        <CTableHeaderCell>Action</CTableHeaderCell>
                                    </CTableRow>
                                </CTableHead>
                                <CTableBody>
                                    {orders.map((order) => (
                                        <CTableRow key={order.id}>
                                            <CTableDataCell><strong>{order.orderNumber}</strong></CTableDataCell>
                                            <CTableDataCell>{order.requestTitle}</CTableDataCell>
                                            <CTableDataCell>{order.vendorName}</CTableDataCell>
                                            <CTableDataCell>${order.totalAmount}</CTableDataCell>
                                            <CTableDataCell>
                                                <CBadge className="px-3 py-2 text-white" style={{
                                                    backgroundColor: order.status === 'DELIVERED' ? 'var(--success-emerald)' :
                                                        order.status === 'SHIPPED' ? '#0ea5e9' :
                                                            order.status === 'PROCESSING' ? '#f59e0b' :
                                                                order.status === 'COMPLETED' ? '#10b981' : '#ef4444'
                                                }}>
                                                    {order.status}
                                                </CBadge>
                                            </CTableDataCell>
                                            <CTableDataCell>{new Date(order.createdAt).toLocaleDateString()}</CTableDataCell>
                                            <CTableDataCell>
                                                {order.status !== 'COMPLETED' ? (
                                                    <CButton color="success" className="text-white" size="sm" onClick={() => handleInitiatePayment(order)}>
                                                        Pay Now
                                                    </CButton>
                                                ) : (
                                                    <CBadge color="success">Paid</CBadge>
                                                )}
                                            </CTableDataCell>
                                        </CTableRow>
                                    ))}
                                    {orders.length === 0 && (
                                        <CTableRow><CTableDataCell colSpan="6" className="text-center">No active orders yet.</CTableDataCell></CTableRow>
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
                        <CCardHeader>
                            <strong>My Payment History</strong>
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
                                    {payments.map((payment) => (
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
                                    {payments.length === 0 && (
                                        <CTableRow><CTableDataCell colSpan="6" className="text-center">No payment history.</CTableDataCell></CTableRow>
                                    )}
                                </CTableBody>
                            </CTable>
                        </CCardBody>
                    </CCard>
                </CCol>
            </CRow>

            {/* Create Modal */}
            <CModal visible={createModalVisible} onClose={() => setCreateModalVisible(false)}>
                <CModalHeader>
                    <CModalTitle>Create Procurement Request</CModalTitle>
                </CModalHeader>
                <CForm onSubmit={handleCreateRequest}>
                    <CModalBody>
                        <div className="mb-3">
                            <CFormInput type="text" label="Title" value={newRequest.title} onChange={(e) => setNewRequest({ ...newRequest, title: e.target.value })} required />
                        </div>
                        <div className="mb-3">
                            <CFormTextarea label="Description" rows={3} value={newRequest.description} onChange={(e) => setNewRequest({ ...newRequest, description: e.target.value })} required />
                        </div>
                        <div className="mb-3">
                            <CFormInput type="number" label="Budget ($)" value={newRequest.budget} onChange={(e) => setNewRequest({ ...newRequest, budget: e.target.value })} required step="0.01" />
                        </div>
                        <div className="mb-3">
                            <CFormInput type="date" label="Due Date" value={newRequest.dueDate} onChange={(e) => setNewRequest({ ...newRequest, dueDate: e.target.value })} required />
                        </div>
                    </CModalBody>
                    <CModalFooter>
                        <CButton color="secondary" onClick={() => setCreateModalVisible(false)}>Cancel</CButton>
                        <CButton color="primary" type="submit">Create</CButton>
                    </CModalFooter>
                </CForm>
            </CModal>

            {/* View Quotations Modal */}
            <CModal size="lg" visible={quotationsModalVisible} onClose={() => setQuotationsModalVisible(false)}>
                <CModalHeader>
                    <CModalTitle>Quotations for {activeRequest?.title}</CModalTitle>
                </CModalHeader>
                <CModalBody>
                    {quotations.length === 0 ? <p>No quotations received yet.</p> : (
                        <CTable>
                            <CTableHead>
                                <CTableRow>
                                    <CTableHeaderCell>Vendor</CTableHeaderCell>
                                    <CTableHeaderCell>Amount</CTableHeaderCell>
                                    <CTableHeaderCell>Status</CTableHeaderCell>
                                    <CTableHeaderCell>Valid Until</CTableHeaderCell>
                                    <CTableHeaderCell>Action</CTableHeaderCell>
                                </CTableRow>
                            </CTableHead>
                            <CTableBody>
                                {quotations.map(q => (
                                    <CTableRow key={q.id}>
                                        <CTableDataCell>{q.vendorName}</CTableDataCell>
                                        <CTableDataCell>${q.quotedAmount}</CTableDataCell>
                                        <CTableDataCell>{q.status}</CTableDataCell>
                                        <CTableDataCell>{new Date(q.validUntil).toLocaleDateString()}</CTableDataCell>
                                        <CTableDataCell>
                                            {q.status === 'SUBMITTED' && (
                                                <CButton color="success" size="sm" onClick={() => handleAcceptQuotation(q.id)}>Accept</CButton>
                                            )}
                                        </CTableDataCell>
                                    </CTableRow>
                                ))}
                            </CTableBody>
                        </CTable>
                    )}
                </CModalBody>
                <CModalFooter>
                    <CButton color="secondary" onClick={() => setQuotationsModalVisible(false)}>Close</CButton>
                </CModalFooter>
            </CModal>

            {/* Payment Modal (Simulated Gateway) */}
            <CModal visible={paymentModalVisible} onClose={() => !isProcessingPayment && setPaymentModalVisible(false)} alignment="center" backdrop="static">
                <CModalHeader>
                    <CModalTitle>Secure Checkout</CModalTitle>
                </CModalHeader>
                <CModalBody>
                    {activeTransaction && activePaymentOrder ? (
                        <>
                            <div className="text-center mb-4">
                                <h4>Payment Details</h4>
                                <p className="text-muted mb-0">You are about to pay</p>
                                <h2 className="text-primary mb-3">${activeTransaction.amount}</h2>
                            </div>

                            <table className="table table-borderless table-sm mb-4">
                                <tbody>
                                    <tr>
                                        <td className="text-muted">Order #</td>
                                        <td className="text-end fw-bold">{activePaymentOrder.orderNumber}</td>
                                    </tr>
                                    <tr>
                                        <td className="text-muted">Vendor</td>
                                        <td className="text-end fw-bold">{activePaymentOrder.vendorName}</td>
                                    </tr>
                                    <tr>
                                        <td className="text-muted">Requested Item</td>
                                        <td className="text-end">{activePaymentOrder.requestTitle}</td>
                                    </tr>
                                    <tr>
                                        <td className="text-muted">Transaction ID</td>
                                        <td className="text-end"><code className="bg-light p-1 rounded text-lowercase">{activeTransaction.transactionId}</code></td>
                                    </tr>
                                </tbody>
                            </table>

                            <div className="alert alert-info py-2" role="alert">
                                <small>This is a simulated payment gateway. In a real environment, you would be redirected to a provider like Stripe or PayPal.</small>
                            </div>
                        </>
                    ) : <CSpinner />}
                </CModalBody>
                <CModalFooter className="justify-content-center">
                    <CButton
                        color="success"
                        className="text-white w-100 mb-2 py-2 fw-bold"
                        onClick={() => handleConfirmPayment(true)}
                        disabled={isProcessingPayment}
                    >
                        {isProcessingPayment ? <><CSpinner size="sm" /> Processing...</> : 'Simulate Successful Payment'}
                    </CButton>
                    <CButton
                        color="danger"
                        className="text-white w-100 py-2 fw-bold"
                        onClick={() => handleConfirmPayment(false)}
                        disabled={isProcessingPayment}
                    >
                        {isProcessingPayment ? <><CSpinner size="sm" /> Processing...</> : 'Simulate Failed Payment'}
                    </CButton>
                    <CButton
                        color="light"
                        className="w-100 mt-2"
                        onClick={() => setPaymentModalVisible(false)}
                        disabled={isProcessingPayment}
                    >
                        Cancel
                    </CButton>
                </CModalFooter>
            </CModal>
        </>
    )
}

export default CustomerDashboard
