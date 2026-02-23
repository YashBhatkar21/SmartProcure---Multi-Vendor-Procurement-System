import React, { useState, useEffect } from 'react';
import {
    CCard,
    CCardBody,
    CCardHeader,
    CCol,
    CRow,
    CTable,
    CTableBody,
    CTableDataCell,
    CTableHead,
    CTableHeaderCell,
    CTableRow,
    CBadge,
    CSpinner
} from '@coreui/react';
import { getRequests } from '../../api/admin';

const AdminRequestsList = () => {
    const [requests, setRequests] = useState([]);
    const [loading, setLoading] = useState(true);

    const fetchRequests = async () => {
        setLoading(true);
        try {
            const response = await getRequests({ size: 100 });
            setRequests(response.data.content || []);
        } catch (error) {
            console.error("Failed to fetch requests", error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchRequests();
    }, []);

    const getStatusBadgeColor = (status) => {
        switch (status) {
            case 'PENDING': return 'warning';
            case 'IN_PROGRESS': return 'info';
            case 'COMPLETED': return 'success';
            case 'CANCELLED': return 'danger';
            default: return 'secondary';
        }
    };

    if (loading) return <div className="text-center mt-5"><CSpinner /></div>;

    return (
        <CRow>
            <CCol xs={12}>
                <CCard className="mb-4">
                    <CCardHeader>
                        <strong>Procurement Requests List</strong>
                    </CCardHeader>
                    <CCardBody>
                        <CTable align="middle" className="mb-0 border" hover responsive>
                            <CTableHead color="light">
                                <CTableRow>
                                    <CTableHeaderCell>Item</CTableHeaderCell>
                                    <CTableHeaderCell>Customer Name</CTableHeaderCell>
                                    <CTableHeaderCell>Quantity</CTableHeaderCell>
                                    <CTableHeaderCell>Needed By</CTableHeaderCell>
                                    <CTableHeaderCell>Status</CTableHeaderCell>
                                </CTableRow>
                            </CTableHead>
                            <CTableBody>
                                {requests.map((req) => (
                                    <CTableRow key={req.id}>
                                        <CTableDataCell>
                                            <strong>{req.itemName}</strong>
                                        </CTableDataCell>
                                        <CTableDataCell>
                                            <div>{req.customerName || 'N/A'}</div>
                                        </CTableDataCell>
                                        <CTableDataCell>
                                            <div>{req.quantity}</div>
                                        </CTableDataCell>
                                        <CTableDataCell>
                                            <div>{new Date(req.neededByDate).toLocaleDateString()}</div>
                                        </CTableDataCell>
                                        <CTableDataCell>
                                            <CBadge color={getStatusBadgeColor(req.status)}>{req.status}</CBadge>
                                        </CTableDataCell>
                                    </CTableRow>
                                ))}
                                {requests.length === 0 && (
                                    <CTableRow>
                                        <CTableDataCell colSpan="5" className="text-center">No procurement requests found.</CTableDataCell>
                                    </CTableRow>
                                )}
                            </CTableBody>
                        </CTable>
                    </CCardBody>
                </CCard>
            </CCol>
        </CRow>
    );
};

export default AdminRequestsList;
