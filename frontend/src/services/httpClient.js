import { ORGANIZATION_ID } from '../app/config'

export async function request(baseUrl, path, options = {}) {
  const response = await fetch(`${baseUrl}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      'X-Organization-Id': ORGANIZATION_ID,
      ...(options.headers || {}),
    },
  })

  const body = await response.json().catch(() => ({}))
  if (!response.ok) {
    const fieldMessage = body.fields ? Object.values(body.fields).join(' ') : ''
    throw new Error(fieldMessage || body.message || 'Não foi possível concluir a operação.')
  }
  return body
}
