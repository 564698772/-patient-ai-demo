import type { AnalysisRequest, AnalysisResponse, HospitalRequest, HospitalDto } from '../types'

const BASE_URL = '/api'

async function post<T>(path: string, body: unknown): Promise<T> {
  const response = await fetch(`${BASE_URL}${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  })

  if (!response.ok) {
    const err = await response.json().catch(() => ({ message: '请求失败' }))
    throw new Error(err.message || `HTTP ${response.status}`)
  }

  return response.json() as Promise<T>
}

export function analyzeSymptoms(req: AnalysisRequest): Promise<AnalysisResponse> {
  return post<AnalysisResponse>('/analyze', req)
}

export function findNearbyHospitals(req: HospitalRequest): Promise<HospitalDto[]> {
  return post<HospitalDto[]>('/hospitals', req)
}
