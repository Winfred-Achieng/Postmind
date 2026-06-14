import { useState, useEffect } from 'react'
import { getPosts } from '../api/client'
import PostCard from '../components/PostCard'

const FILTERS = ['ALL', 'DRAFT', 'APPROVED', 'REJECTED', 'PUBLISHED']

export default function Dashboard() {
  const [posts, setPosts] = useState([])
  const [activeFilter, setActiveFilter] = useState('ALL')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    setLoading(true)
    setError(null)
    getPosts(activeFilter === 'ALL' ? null : activeFilter)
      .then(setPosts)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }, [activeFilter])

  const drafts = posts.filter(p => p.status === 'DRAFT').length

  return (
    <div className="page">
      <div className="page-header">
        <h1 className="page-title">Post Queue</h1>
        <div className="page-count">
          {loading ? 'Loading…' : `${posts.length} post${posts.length !== 1 ? 's' : ''}${drafts > 0 ? ` · ${drafts} awaiting review` : ''}`}
        </div>
      </div>

      <div className="filter-row">
        {FILTERS.map((f) => (
          <button
            key={f}
            className={`filter-tab ${activeFilter === f ? 'active' : ''}`}
            onClick={() => setActiveFilter(f)}
          >
            {f}
          </button>
        ))}
      </div>

      {loading && (
        <div className="state-message">
          <span className="state-icon">⏳</span>
          Loading posts…
        </div>
      )}

      {error && (
        <div className="state-message">
          <span className="state-icon">⚠️</span>
          {error}
        </div>
      )}

      {!loading && !error && posts.length === 0 && (
        <div className="state-message">
          <span className="state-icon">📭</span>
          No {activeFilter !== 'ALL' ? activeFilter.toLowerCase() : ''} posts yet.
          {activeFilter === 'ALL' && <><br />The scheduler runs every hour to fetch Reddit trends.</>}
        </div>
      )}

      {!loading && !error && posts.map((post) => (
        <PostCard key={post.id} post={post} />
      ))}
    </div>
  )
}
