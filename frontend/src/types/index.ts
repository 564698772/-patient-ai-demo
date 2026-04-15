export interface AnalysisRequest {
  symptoms: string
}

export interface AnalysisResponse {
  department: string | null
  reason: string | null
  isEmergency: boolean
  fallback: boolean
  fallbackMessage: string | null
}

export interface HospitalRequest {
  latitude: number
  longitude: number
  department?: string | null
}

export interface HospitalDto {
  name: string
  address: string
  distance: number | null
  departments: string[]
}

export interface ErrorResponse {
  code: string
  message: string
  fallback: boolean
}

export type LocationState =
  | { status: 'idle' }
  | { status: 'requesting' }
  | { status: 'granted'; lat: number; lon: number }
  | { status: 'denied'; reason: string }

export type QueryState =
  | { phase: 'idle' }
  | { phase: 'loading' }
  | { phase: 'done'; analysis: AnalysisResponse; hospitals: HospitalDto[] }
  | { phase: 'error'; message: string }
