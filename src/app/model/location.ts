
export interface Location {
  name: string
  coordinates?: [number, number]
  url?: string
}

export interface ExamLocation extends Location {
  seat?: string
}
