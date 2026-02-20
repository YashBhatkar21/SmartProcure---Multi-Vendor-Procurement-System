import React from 'react'
import { Navigate } from 'react-router-dom'
import AdminDashboard from './AdminDashboard'
import CustomerDashboard from './CustomerDashboard'
import VendorDashboard from './VendorDashboard'

const Dashboard = () => {
  const role = localStorage.getItem('role')
  if (role === 'ADMIN') return <AdminDashboard />
  if (role === 'CUSTOMER') return <CustomerDashboard />
  if (role === 'VENDOR') return <VendorDashboard />

  return <Navigate to="/login" />
}

export default Dashboard
