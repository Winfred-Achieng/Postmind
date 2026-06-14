import { Link } from 'react-router-dom'
import StatusBadge from './StatusBadge'

const PREVIEW_LENGTH = 140

export default function PostCard({ post }) {
  const preview = post.content.length > PREVIEW_LENGTH
    ? post.content.slice(0, PREVIEW_LENGTH) + '…'
    : post.content

  const createdAt = new Date(post.createdAt).toLocaleString('en-GB', {
    day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit'
  })

  return (
    <div className="post-card">
      <div className={`post-card-accent accent-${post.status}`} />
      <div className="post-card-body">
        <div className="post-source">{post.trendTitle}</div>
        <div className="post-content">{preview}</div>
        <div className="post-footer">
          <span className="post-meta">{createdAt}</span>
        </div>
      </div>
      <div className="post-card-right">
        <StatusBadge status={post.status} />
        {post.status === 'DRAFT' && (
          <Link className="review-link" to={`/posts/${post.id}`}>Review</Link>
        )}
      </div>
    </div>
  )
}
