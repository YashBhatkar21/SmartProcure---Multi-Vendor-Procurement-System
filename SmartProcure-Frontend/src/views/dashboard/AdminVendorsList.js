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
    CButton,
    CBadge,
    CSpinner
} from '@coreui/react';
import CIcon from '@coreui/icons-react';
import { cilTrash } from '@coreui/icons';
import { getVendors, deleteUser } from '../../api/admin';

const AdminVendorsList = () => {
    const [vendors, setVendors] = useState([]);
    const [loading, setLoading] = useState(true);

    const fetchVendors = async () => {
        setLoading(true);
        try {
            const response = await getVendors({ size: 100 });
            setVendors(response.data.content || []);
        } catch (error) {
            console.error("Failed to fetch vendors", error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchVendors();
    }, []);

    const handleDelete = async (id) => {
        if (window.confirm("Are you sure you want to delete this vendor? This will also delete their user account.")) {
            try {
                await deleteUser(id); // vendor user ID
                fetchVendors(); // Refresh the list
            } catch (error) {
                console.error("Failed to delete vendor", error);
                alert("Failed to delete vendor. They might be tied to existing orders or quotes.");
            }
        }
    };

    if (loading) return <div className="text-center mt-5"><CSpinner /></div>;

    return (
        <CRow>
            <CCol xs={12}>
                <CCard className="mb-4">
                    <CCardHeader>
                        <strong>Vendor Management</strong>
                    </CCardHeader>
                    <CCardBody>
                        <CTable align="middle" className="mb-0 border" hover responsive>
                            <CTableHead color="light">
                                <CTableRow>
                                    <CTableHeaderCell>Company Name</CTableHeaderCell>
                                    <CTableHeaderCell>Representative Name</CTableHeaderCell>
                                    <CTableHeaderCell>Email</CTableHeaderCell>
                                    <CTableHeaderCell>Phone</CTableHeaderCell>
                                    <CTableHeaderCell>Status</CTableHeaderCell>
                                    <CTableHeaderCell>Actions</CTableHeaderCell>
                                </CTableRow>
                            </CTableHead>
                            <CTableBody>
                                {vendors.map((vendorUser) => (
                                    <CTableRow key={vendorUser.id}>
                                        <CTableDataCell>
                                            <strong>{vendorUser.vendor?.companyName || 'N/A'}</strong>
                                        </CTableDataCell>
                                        <CTableDataCell>
                                            <div>{vendorUser.fullName}</div>
                                        </CTableDataCell>
                                        <CTableDataCell>
                                            <div>{vendorUser.email}</div>
                                        </CTableDataCell>
                                        <CTableDataCell>
                                            <div>{vendorUser.vendor?.contactPhone || 'N/A'}</div>
                                        </CTableDataCell>
                                        <CTableDataCell>
                                            <CBadge color="success">Active</CBadge>
                                        </CTableDataCell>
                                        <CTableDataCell>
                                            <CButton
                                                color="danger"
                                                variant="outline"
                                                size="sm"
                                                title="Delete Vendor"
                                                onClick={() => handleDelete(vendorUser.id)}
                                            >
                                                <CIcon icon={cilTrash} />
                                            </CButton>
                                        </CTableDataCell>
                                    </CTableRow>
                                ))}
                                {vendors.length === 0 && (
                                    <CTableRow>
                                        <CTableDataCell colSpan="6" className="text-center">No vendors found.</CTableDataCell>
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

export default AdminVendorsList;
