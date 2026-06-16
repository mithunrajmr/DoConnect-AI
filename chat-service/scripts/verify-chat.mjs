import { createHmac } from 'node:crypto'

const secret = process.env.JWT_SECRET ?? 'change-this-development-secret-to-at-least-32-characters'
const baseUrl = process.env.CHAT_BASE_URL ?? 'http://localhost:8090'
const wsUrl = process.env.CHAT_WS_URL ?? 'ws://localhost:8090/ws'

function base64Url(input) {
  return Buffer.from(input)
    .toString('base64')
    .replaceAll('+', '-')
    .replaceAll('/', '_')
    .replaceAll('=', '')
}

function sign(unsignedToken) {
  return createHmac('sha256', secret)
    .update(unsignedToken)
    .digest('base64')
    .replaceAll('+', '-')
    .replaceAll('/', '_')
    .replaceAll('=', '')
}

function createToken() {
  const now = Math.floor(Date.now() / 1000)
  const header = base64Url(JSON.stringify({ alg: 'HS256', typ: 'JWT' }))
  const payload = base64Url(JSON.stringify({
    sub: 'chat.verify@example.com',
    userId: 9001,
    name: 'Chat Verifier',
    role: 'USER',
    iat: now,
    exp: now + 3600,
  }))
  const unsignedToken = `${header}.${payload}`
  return `${unsignedToken}.${sign(unsignedToken)}`
}

function stompFrame(command, headers = {}, body = '') {
  const headerLines = Object.entries(headers).map(([key, value]) => `${key}:${value}`)
  return `${command}\n${headerLines.join('\n')}\n\n${body}\0`
}

function parseStompBody(frame) {
  const separator = frame.indexOf('\n\n')
  if (separator < 0) return ''
  return frame.slice(separator + 2).replace(/\0$/, '')
}

async function getHistory(token) {
  const response = await fetch(`${baseUrl}/api/chat/messages?limit=5`, {
    headers: { Authorization: `Bearer ${token}` },
  })
  if (!response.ok) {
    throw new Error(`History request failed: ${response.status} ${await response.text()}`)
  }
  return response.json()
}

async function verifyWebSocket(token) {
  return new Promise((resolve, reject) => {
    const ws = new WebSocket(wsUrl, ['v12.stomp'])
    const timeout = setTimeout(() => {
      ws.close()
      reject(new Error('Timed out waiting for STOMP broadcast'))
    }, 10000)

    ws.addEventListener('open', () => {
      ws.send(stompFrame('CONNECT', {
        'accept-version': '1.2',
        host: 'localhost',
        Authorization: `Bearer ${token}`,
      }))
    })

    ws.addEventListener('message', (event) => {
      const frame = String(event.data)
      if (frame.startsWith('CONNECTED')) {
        ws.send(stompFrame('SUBSCRIBE', {
          id: 'sub-0',
          destination: '/topic/chat/global',
          ack: 'auto',
        }))
        ws.send(stompFrame('SEND', {
          destination: '/app/chat.send',
          'content-type': 'application/json',
        }, JSON.stringify({ content: `verification message ${Date.now()}` })))
        return
      }

      if (frame.startsWith('MESSAGE')) {
        clearTimeout(timeout)
        ws.close()
        resolve(JSON.parse(parseStompBody(frame)))
      }
    })

    ws.addEventListener('error', (error) => {
      clearTimeout(timeout)
      reject(error)
    })
  })
}

const token = createToken()
const before = await getHistory(token)
const broadcast = await verifyWebSocket(token)
const after = await getHistory(token)

console.log(JSON.stringify({
  restHistoryBeforeCount: before.length,
  broadcast,
  restHistoryAfterCount: after.length,
  persistedInHistory: after.some((message) => message.content === broadcast.content && message.senderId === 9001),
}, null, 2))
