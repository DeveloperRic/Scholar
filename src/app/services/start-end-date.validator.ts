import { AbstractControl, FormGroup, ValidatorFn } from "@angular/forms"

/**
 * Validates 2 AbstractControls (1 start, 1 end).
 * Valid so long as the start control is <= to the end control.
 */
export class StartEndDateValidator {
  private form: FormGroup
  private startControlWasInvalid: boolean
  private endControlWasInvalid: boolean
  readonly startValidator: ValidatorFn
  readonly endValidator: ValidatorFn

  /**
   * DON'T FORGET TO CALL {@link StartEndDateValidator.setForm} SO VALIDATION CAN HAPPEN
   */
  constructor(
    private startControlNme: string,
    private endControlName: string,
    private convertInputToDate: (value: AbstractControl['value']) => Date
  ) {
    this.startValidator = this.getValidatorFn()
    this.endValidator = this.getValidatorFn()
  }

  setForm(form: FormGroup) {
    this.form = form
  }

  private getValidatorFn(): ValidatorFn {
    return control => {
      if (!this.form) return { error: 'no form' }
      const startTimeControl = this.form.get(this.startControlNme)
      const isStartControl = control === startTimeControl
      const otherTimeControl = isStartControl ? this.form.get(this.endControlName) : startTimeControl
      const callback = this.getValidateCallback(startTimeControl, otherTimeControl, isStartControl)
      if (!control.value) return callback('badTime')
      const thisControlValue: Date = this.convertInputToDate(control.value)
      const otherControlValue: Date = this.convertInputToDate(otherTimeControl.value)
      if (isStartControl && thisControlValue > otherControlValue) return callback('startTooLate')
      if (!isStartControl && thisControlValue < otherControlValue) return callback('endTooEarly')
      return callback()
    }
  }

  private getValidateCallback(
    startControl: AbstractControl,
    endControl: AbstractControl,
    isStartControl: boolean
  ): (error?: string) => ReturnType<ValidatorFn> {
    return error => {
      if (error) { // return error if any
        if (isStartControl) this.startControlWasInvalid = true
        else this.endControlWasInvalid = true
        const result = {}
        if (error) result[error] = isStartControl ? startControl.value : endControl.value
        return result
      }
      // classify the current control as valid
      if (isStartControl) this.startControlWasInvalid = false
      else this.endControlWasInvalid = false
      // schedule the other control for re-validation (it should also be valid but it must be re-checked to update the UI)
      if (this.startControlWasInvalid) startControl.setValue(startControl.value)
      if (this.endControlWasInvalid) endControl.setValue(endControl.value)
      // notify that the current control is valid
      return {}
    }
  }
}
