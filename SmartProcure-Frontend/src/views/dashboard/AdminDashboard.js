import React from 'react'
import {
    CCard,
    CCardBody,
    CCol,
    CRow,
    CWidgetStatsC
} from '@coreui/react'
import CIcon from '@coreui/icons-react'
import { cilPeople, cilBriefcase, cilCart } from '@coreui/icons'

const AdminDashboard = () => {
    return (
        <CRow>
            <CCol xs={12} sm={6} lg={4}>
                <CWidgetStatsC
                    className="mb-3"
                    icon={<CIcon icon={cilPeople} height={36} />}
                    color="primary"
                    inverse
                    progress={{ value: 100 }}
                    text="Manage all registered users in the system"
                    title="Users"
                    value="User Management"
                />
            </CCol>
            <CCol xs={12} sm={6} lg={4}>
                <CWidgetStatsC
                    className="mb-3"
                    icon={<CIcon icon={cilBriefcase} height={36} />}
                    color="info"
                    inverse
                    progress={{ value: 100 }}
                    text="Oversee all submitted procurement requests"
                    title="Procurement"
                    value="Requests"
                />
            </CCol>
            <CCol xs={12} sm={6} lg={4}>
                <CWidgetStatsC
                    className="mb-3"
                    icon={<CIcon icon={cilCart} height={36} />}
                    color="warning"
                    inverse
                    progress={{ value: 100 }}
                    text="Monitor system operations and logs"
                    title="System"
                    value="Dashboard Stats"
                />
            </CCol>
        </CRow>
    )
}

export default AdminDashboard
