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
    CDropdownItem
} from '@coreui/react'
import { getAvailableRequests, getMyQuotations, submitQuotation, getVendorOrders, updateOrderStatus } from '../../api/procurement'

const VendorDashboard = () => {
    const [availableRequests, setAvailableRequests] = useState([])
    const [myQuotations, setMyQuotations] = useState([])
    const [orders, setOrders] = useState([])
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

            const [requestsRes, quotesRes, ordersRes] = await Promise.all([
                getAvailableRequests(reqParams),
                getMyQuotations(),
                getVendorOrders(orderParams)
            ]);
            setAvailableRequests(requestsRes.data.content || requestsRes.data);
            setMyQuotations(quotesRes.data);
            setOrders(ordersRes.data.content || ordersRes.data);
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
                        <CCardBody>
                            <CTable hover responsive>
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
                                                        <CBadge color="success">Quoted</CBadge>
                                                    ) : (
                                                        <CButton color="primary" size="sm" onClick={() => handleOpenQuoteModal(req)}>
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
                        <CCardBody>
                            <CTable hover responsive>
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
                                                <CBadge color={quote.status === 'ACCEPTED' ? 'success' : quote.status === 'REJECTED' ? 'danger' : 'secondary'}>
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
                        <CCardBody>
                            <CTable hover responsive>
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
                                                <CBadge color={order.status === 'DELIVERED' ? 'success' : order.status === 'SHIPPED' ? 'info' : 'warning'}>
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
