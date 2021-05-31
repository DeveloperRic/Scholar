export interface Schedule {
  start: number
  end?: number
  repeat?: ScheduleRepeat
}

export interface ScheduleRepeat {
  basis: ScheduleRepeatBasis
}

/**
 * if BASIS == day: every `interval` days
 *
 * if BASIS == week: same day of the week, every `interval` weeks
 *
 * if BASIS == month: same day of the month, every `interval` months
 *
 * if BASIS == year: same month & day of the month, every `interval` days.
 * If the day doesn't exist, the day is skipped
 */
export interface IterativeScheduleRepeat extends ScheduleRepeat {
  interval: number
}

/** e.g.) every week on days 0, 3, 6 (i.e Sunday, Wednesday, Saturday) */
export interface RelativeWeeklyScheduleRepeat extends ScheduleRepeat {
  basis: ScheduleRepeatBasis.WEEK
  days: ScheduleRepeatDay[]
}

/** e.g.) every month on the THIRD THURSDAY */
export interface RelativeMonthlyScheduleRepeat extends ScheduleRepeat {
  basis: ScheduleRepeatBasis.MONTH
  day: ScheduleRepeatDay
  week: ScheduleRepeatWeek
}

/** e.g.) every month on the THIRD THURSDAY */
export interface RelativeYearlyScheduleRepeat extends ScheduleRepeat {
  basis: ScheduleRepeatBasis.YEAR
  month: ScheduleRepeatMonth
  /** day of the month [1-31] */
  date: number
}

export enum ScheduleRepeatBasis {
  DAY = 'day',
  WEEK = 'week',
  MONTH = 'month',
  YEAR = 'year'
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

export enum ScheduleRepeatWeek {
  FIRST,
  SECOND,
  THIRD,
  FOURTH,
  /** could be the 5th or 6th week */
  LAST
}

export enum ScheduleRepeatMonth {
  JANUARY,
  FEBRUARY,
  MARCH,
  APRIL,
  MAY,
  JUNE,
  JULY,
  AUGUST,
  SEPTEMBER,
  OCTOBER,
  NOVEMBER,
  DECEMBER
}
