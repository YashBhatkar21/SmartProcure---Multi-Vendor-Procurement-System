import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import {
  CButton,
  CCard,
  CCardBody,
  CCardGroup,
  CCol,
  CContainer,
  CForm,
  CFormInput,
  CInputGroup,
  CInputGroupText,
  CRow,
  CSpinner,
  CAlert
} from '@coreui/react'
import CIcon from '@coreui/icons-react'
import { cilLockLocked, cilUser } from '@coreui/icons'
import api from '../../../api/axios'

const Login = () => {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const navigate = useNavigate()

  const handleLogin = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError('')
    try {
      const response = await api.post('/auth/login', { email, password })
      localStorage.setItem('token', response.data.accessToken)
      // Assuming the backend sends the role in the response object as 'role'
      if (response.data.role) {
        localStorage.setItem('role', response.data.role)
      }
      navigate('/dashboard', { replace: true })
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed. Please check your credentials.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-bg min-vh-100 d-flex flex-row align-items-center">
      <CContainer>
        <CRow className="justify-content-center">
          <CCol md={6} lg={5}>
            <CCard className="p-4 glass-card border-0">
              <CCardBody>
                <CForm onSubmit={handleLogin}>
                  <div className="text-center mb-4">
                    <h1 className="fw-bold" style={{ color: 'var(--primary-indigo)' }}>SmartProcure</h1>
                    <p className="text-body-secondary mt-2">Sign in to your account</p>
                  </div>
                  {error && <CAlert color="danger">{error}</CAlert>}
                  <CInputGroup className="mb-3">
                    <CInputGroupText className="bg-white">
                      <CIcon icon={cilUser} />
                    </CInputGroupText>
                    <CFormInput
                      type="email"
                      placeholder="Email"
                      autoComplete="username"
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      required
                      className="border-start-0 ps-0"
                    />
                  </CInputGroup>
                  <CInputGroup className="mb-4">
                    <CInputGroupText className="bg-white">
                      <CIcon icon={cilLockLocked} />
                    </CInputGroupText>
                    <CFormInput
                      type="password"
                      placeholder="Password"
                      autoComplete="current-password"
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      required
                      className="border-start-0 ps-0"
                    />
                  </CInputGroup>
                  <div className="d-grid mb-3">
                    <CButton className="btn-premium" type="submit" disabled={loading}>
                      {loading ? <CSpinner size="sm" /> : 'Log In'}
                    </CButton>
                  </div>
                  <div className="text-center text-muted">
                    <small>
                      Don't have an account?{' '}
                      <Link to="/register" style={{ color: 'var(--primary-indigo)' }} className="text-decoration-none fw-bold">
                        Register Now
                      </Link>
                    </small>
                  </div>
                </CForm>
              </CCardBody>
            </CCard>
          </CCol>
        </CRow>
      </CContainer>
    </div>
  )
}

export default Login

