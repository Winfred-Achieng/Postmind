const BASE = '/api'

async function request(path, options = {}) {
  const res = await fetch(`${BASE}${path}`, {
    headers: { 'Content-Type': 'application/json', ...options.headers },
    ...options,
  })
  if (!res.ok) {
    const err = await res.json().catch(() => ({ message: res.statusText }))
    throw new Error(err.message || `Request failed: ${res.status}`)
  }
  if (res.status === 204) return null
  return res.json()
}

export const getPosts = (status) =>
  request(status ? `/posts?status=${status}` : '/posts')

export const getPost = (id) =>
  request(`/posts/${id}`)

export const decidePost = (postId, decision) =>
  request(`/posts/${postId}/approval`, {
    method: 'POST',
    body: JSON.stringify({ decision }),
  })
