import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import Dashboard from './pages/Dashboard'
import PostReview from './pages/PostReview'

function TopNav() {
  return (
    <nav className="topnav">
      <span className="topnav-logo">PostMind</span>
      <div className="topnav-dot" />
      <span className="topnav-sub">Reddit → AI → Twitter</span>
    </nav>
  )
}

export default function App() {
  return (
    <BrowserRouter>
      <TopNav />
      <Routes>
        <Route path="/" element={<Dashboard />} />
        <Route path="/posts/:id" element={<PostReview />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  )
}
