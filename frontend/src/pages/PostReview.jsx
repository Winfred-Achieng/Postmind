import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import { getPost, decidePost } from '../api/client'
import StatusBadge from '../components/StatusBadge'

const DECISION_LABEL = {
  APPROVED:  'Approved — will be published within 5 minutes.',
  REJECTED:  'Rejected — this post will not be published.',
  PUBLISHED: 'Published to Twitter/X.',
}

export default function PostReview() {
  const { id } = useParams()
  const [post, setPost] = useState(null)
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    getPost(id)
      .then(setPost)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }, [id])

  async function handleDecision(decision) {
    setSubmitting(true)
    setError(null)
    try {
      await decidePost(id, decision)
      setPost(await getPost(id))
    } catch (e) {
      setError(e.message)
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) return (
    <div className="page">
      <div className="state-message"><span className="state-icon">⏳</span>Loading…</div>
    </div>
  )

  if (!post) return (
    <div className="page">
      <div className="state-message"><span className="state-icon">⚠️</span>{error}</div>
    </div>
  )

  const isDraft = post.status === 'DRAFT'
  const isDecided = !isDraft

  const createdAt = new Date(post.createdAt).toLocaleString('en-GB', {
    weekday: 'short', day: 'numeric', month: 'short',
    hour: '2-digit', minute: '2-digit'
  })

  return (
    <div className="page">
      <Link className="back-link" to="/">← Back to queue</Link>

      <div className="review-card">
        <div className="review-card-header">
          <div>
            <div className="review-trend-label">Trending on Reddit</div>
            <div className="review-trend-value">{post.trendTitle}</div>
          </div>
          <StatusBadge status={post.status} />
        </div>

        <div className="review-card-body">
          <div className="review-content">{post.content}</div>

          {isDecided && (
            <div className={`decision-banner ${post.status}`}>
              {DECISION_LABEL[post.status] ?? post.status}
            </div>
          )}
        </div>

        <div className="review-card-footer">
          <span className="post-meta">{createdAt}</span>

          {isDraft && (
            <div className="review-actions">
              <button
                className="btn btn-reject"
                disabled={submitting}
                onClick={() => handleDecision('REJECTED')}
              >
                Reject
              </button>
              <button
                className="btn btn-approve"
                disabled={submitting}
                onClick={() => handleDecision('APPROVED')}
              >
                {submitting ? 'Saving…' : 'Approve & Queue'}
              </button>
            </div>
          )}
        </div>
      </div>

      {error && <div className="error-text">{error}</div>}
    </div>
  )
}
