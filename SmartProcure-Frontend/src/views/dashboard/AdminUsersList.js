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
import { getUsers, deleteUser } from '../../api/admin';

const AdminUsersList = () => {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);

    const fetchUsers = async () => {
        setLoading(true);
        try {
            const response = await getUsers({ size: 100 }); // Fetch up to 100 for now
            setUsers(response.data.content || []);
        } catch (error) {
            console.error("Failed to fetch users", error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchUsers();
    }, []);

    const handleDelete = async (id) => {
        if (window.confirm("Are you sure you want to delete this user? This cannot be undone.")) {
            try {
                await deleteUser(id);
                fetchUsers(); // Refresh the list
            } catch (error) {
                console.error("Failed to delete user", error);
                alert("Failed to delete user. They might be tied to existing orders or requests.");
            }
        }
    };

    const getRoleBadgeColor = (role) => {
        switch (role) {
            case 'ADMIN': return 'danger';
            case 'VENDOR': return 'success';
            case 'CUSTOMER': return 'info';
            default: return 'secondary';
        }
    };

    if (loading) return <div className="text-center mt-5"><CSpinner /></div>;

    return (
        <CRow>
            <CCol xs={12}>
                <CCard className="mb-4">
                    <CCardHeader>
                        <strong>User Management</strong>
                    </CCardHeader>
                    <CCardBody>
                        <CTable align="middle" className="mb-0 border" hover responsive>
                            <CTableHead color="light">
                                <CTableRow>
                                    <CTableHeaderCell>Name</CTableHeaderCell>
                                    <CTableHeaderCell>Email</CTableHeaderCell>
                                    <CTableHeaderCell>Role</CTableHeaderCell>
                                    <CTableHeaderCell>Actions</CTableHeaderCell>
                                </CTableRow>
                            </CTableHead>
                            <CTableBody>
                                {users.map((user) => (
                                    <CTableRow key={user.id}>
                                        <CTableDataCell>
                                            <div>{user.fullName}</div>
                                        </CTableDataCell>
                                        <CTableDataCell>
                                            <div>{user.email}</div>
                                        </CTableDataCell>
                                        <CTableDataCell>
                                            <CBadge color={getRoleBadgeColor(user.role)}>{user.role}</CBadge>
                                        </CTableDataCell>
                                        <CTableDataCell>
                                            <CButton
                                                color="danger"
                                                variant="outline"
                                                size="sm"
                                                title="Delete User"
                                                onClick={() => handleDelete(user.id)}
                                                disabled={user.role === 'ADMIN'} // Prevent deleting other admins blindly
                                            >
                                                <CIcon icon={cilTrash} />
                                            </CButton>
                                        </CTableDataCell>
                                    </CTableRow>
                                ))}
                                {users.length === 0 && (
                                    <CTableRow>
                                        <CTableDataCell colSpan="4" className="text-center">No users found.</CTableDataCell>
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

export default AdminUsersList;
