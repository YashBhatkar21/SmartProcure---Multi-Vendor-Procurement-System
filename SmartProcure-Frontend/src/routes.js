import React from 'react'

const Dashboard = React.lazy(() => import('./views/dashboard/Dashboard'))
const CustomerDashboard = React.lazy(() => import('./views/dashboard/CustomerDashboard'))
const VendorDashboard = React.lazy(() => import('./views/dashboard/VendorDashboard'))
const AdminUsersList = React.lazy(() => import('./views/dashboard/AdminUsersList'))
const AdminVendorsList = React.lazy(() => import('./views/dashboard/AdminVendorsList'))
const AdminRequestsList = React.lazy(() => import('./views/dashboard/AdminRequestsList'))

const routes = [
  { path: '/', exact: true, name: 'Home' },
  { path: '/dashboard', name: 'Dashboard', element: Dashboard },
  { path: '/customer/requests', name: 'My Profile & History', element: CustomerDashboard },
  { path: '/vendor/profile', name: 'My Profile & History', element: VendorDashboard },
  { path: '/admin/users', name: 'Users', element: AdminUsersList },
  { path: '/admin/vendors', name: 'Vendors', element: AdminVendorsList },
  { path: '/admin/requests', name: 'Procurement Requests', element: AdminRequestsList },
]

export default routes
