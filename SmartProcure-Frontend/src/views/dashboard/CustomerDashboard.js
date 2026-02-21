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
    CBadge
} from '@coreui/react'
import { getMyRequests, createRequest, getQuotationsForRequest, acceptQuotation, getCustomerOrders } from '../../api/procurement'

const CustomerDashboard = () => {
    const [requests, setRequests] = useState([])
    const [createModalVisible, setCreateModalVisible] = useState(false)
    const [quotationsModalVisible, setQuotationsModalVisible] = useState(false)
    const [activeRequest, setActiveRequest] = useState(null)
    const [quotations, setQuotations] = useState([])
    const [orders, setOrders] = useState([])
    const [newRequest, setNewRequest] = useState({ title: '', description: '', budget: '', dueDate: '' })

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

    useEffect(() => {
        fetchRequests()
    }, [requestSearch, requestStatus])

    useEffect(() => {
        fetchOrders()
    }, [orderSearch, orderStatus])

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

    return (
        <>
            <CRow>
                <CCol xs={12}>
                    <CCard className="mb-4">
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
                        <CCardBody>
                            <CTable hover responsive>
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
                                                <CBadge color={req.status === 'OPEN' ? 'success' : 'secondary'}>{req.status}</CBadge>
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
                    <CCard className="mb-4">
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
                        <CCardBody>
                            <CTable hover responsive>
                                <CTableHead>
                                    <CTableRow>
                                        <CTableHeaderCell>Order #</CTableHeaderCell>
                                        <CTableHeaderCell>Request Title</CTableHeaderCell>
                                        <CTableHeaderCell>Vendor</CTableHeaderCell>
                                        <CTableHeaderCell>Total Amount</CTableHeaderCell>
                                        <CTableHeaderCell>Status</CTableHeaderCell>
                                        <CTableHeaderCell>Created At</CTableHeaderCell>
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
                                                <CBadge color={order.status === 'DELIVERED' ? 'success' : order.status === 'SHIPPED' ? 'info' : 'warning'}>
                                                    {order.status}
                                                </CBadge>
                                            </CTableDataCell>
                                            <CTableDataCell>{new Date(order.createdAt).toLocaleDateString()}</CTableDataCell>
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
        </>
    )
}

export default CustomerDashboard
