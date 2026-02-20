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
import { getMyRequests, createRequest, getQuotationsForRequest, acceptQuotation, rejectQuotation } from '../../api/procurement'

const CustomerDashboard = () => {
    const [requests, setRequests] = useState([])
    const [createModalVisible, setCreateModalVisible] = useState(false)
    const [quotationsModalVisible, setQuotationsModalVisible] = useState(false)
    const [activeRequest, setActiveRequest] = useState(null)
    const [quotations, setQuotations] = useState([])
    const [newRequest, setNewRequest] = useState({ title: '', description: '', budget: '', dueDate: '' })

    const fetchRequests = async () => {
        try {
            const { data } = await getMyRequests();
            setRequests(data);
        } catch (error) {
            console.error('Error fetching requests', error);
        }
    }

    useEffect(() => {
        fetchRequests()
    }, [])

    const handleCreateRequest = async (e) => {
        e.preventDefault();
        try {
            // Add 'Z' so the backend can parse the ISO extended format to Instant properly.
            const payload = {
                title: newRequest.title,
                description: newRequest.description,
                budget: parseFloat(newRequest.budget),
                dueDate: new Date(newRequest.dueDate).toISOString()
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
            alert('Quotation accepted!');
            setQuotationsModalVisible(false);
            fetchRequests();
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
                            <CButton color="primary" onClick={() => setCreateModalVisible(true)}>Create Request</CButton>
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
