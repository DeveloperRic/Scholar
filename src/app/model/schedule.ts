
export interface Schedule {
  start: number
  end: number
  repeat?: ScheduleRepeat
}

export interface ScheduleRepeat {
  type: ScheduleRepeatType
  basis: ScheduleRepeatBasis
  days: ScheduleRepeatDay[]
  weeks: number[]
  months: number[]
}

export enum ScheduleRepeatType {
  ITERATIVE,
  RELATIVE
}

export enum ScheduleRepeatBasis {
  DAILY,
  WEEKLY,
  MONTHLY,
  YEARLY
}

export enum ScheduleRepeatDay {
  SUNDAY,
  MONDAY,
  TUESDAY,
  WEDNESDAY,
  THURSDAY,
  FRIDAY,
  SATURDAY
}
