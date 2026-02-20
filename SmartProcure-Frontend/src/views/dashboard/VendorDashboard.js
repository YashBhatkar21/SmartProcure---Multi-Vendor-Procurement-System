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
import { getAvailableRequests, getMyQuotations, submitQuotation } from '../../api/procurement'

const VendorDashboard = () => {
    const [availableRequests, setAvailableRequests] = useState([])
    const [myQuotations, setMyQuotations] = useState([])
    const [quoteModalVisible, setQuoteModalVisible] = useState(false)
    const [activeRequest, setActiveRequest] = useState(null)
    const [newQuote, setNewQuote] = useState({ quotedAmount: '', terms: '', validUntil: '' })

    const fetchData = async () => {
        try {
            const [requestsRes, quotesRes] = await Promise.all([
                getAvailableRequests(),
                getMyQuotations()
            ]);
            setAvailableRequests(requestsRes.data);
            setMyQuotations(quotesRes.data);
        } catch (error) {
            console.error('Error fetching data', error);
        }
    }

    useEffect(() => {
        fetchData()
    }, [])

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

    return (
        <>
            <CRow>
                <CCol xs={12} lg={6}>
                    <CCard className="mb-4">
                        <CCardHeader><strong>Available Requests</strong></CCardHeader>
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
                    <CCard className="mb-4">
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
