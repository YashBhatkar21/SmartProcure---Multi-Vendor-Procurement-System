import React from 'react'
import CIcon from '@coreui/icons-react'
import { cilSpeedometer, cilPeople, cilBuilding, cilTask } from '@coreui/icons'
import { CNavItem, CNavTitle } from '@coreui/react'

export const getNav = (role) => {
  const nav = [
    {
      component: CNavItem,
      name: 'Dashboard',
      to: '/dashboard',
      icon: <CIcon icon={cilSpeedometer} customClassName="nav-icon" />,
    }
  ]
  if (role === 'ADMIN') {
    nav.push({ component: CNavTitle, name: 'Admin Actions' })
    nav.push({ component: CNavItem, name: 'Users', to: '/admin/users', icon: <CIcon icon={cilPeople} customClassName="nav-icon" /> })
    nav.push({ component: CNavItem, name: 'Vendors', to: '/admin/vendors', icon: <CIcon icon={cilBuilding} customClassName="nav-icon" /> })
  }
  if (role === 'CUSTOMER') {
    nav.push({ component: CNavTitle, name: 'Customer Actions' })
    nav.push({ component: CNavItem, name: 'My Requests', to: '/customer/requests', icon: <CIcon icon={cilTask} customClassName="nav-icon" /> })
  }
  if (role === 'VENDOR') {
    nav.push({ component: CNavTitle, name: 'Vendor Actions' })
    nav.push({ component: CNavItem, name: 'My Profile', to: '/vendor/profile', icon: <CIcon icon={cilBuilding} customClassName="nav-icon" /> })
  }
  return nav
}


