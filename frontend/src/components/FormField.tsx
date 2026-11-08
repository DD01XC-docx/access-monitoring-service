import type { ComponentProps } from 'react'
import type { FieldError, UseFormRegisterReturn } from 'react-hook-form'

import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'

type Props = {
  id: string
  label: string
  registration: UseFormRegisterReturn
  error?: FieldError
} & Omit<ComponentProps<typeof Input>, 'id'>

export function FormField({ id, label, registration, error, ...inputProps }: Props) {
  return (
    <div className="space-y-2">
      <Label htmlFor={id}>{label}</Label>
      <Input
        id={id}
        aria-invalid={!!error}
        aria-describedby={error ? `${id}-error` : undefined}
        {...inputProps}
        {...registration}
      />
      {error && (
        <p id={`${id}-error`} className="text-xs text-destructive">
          {error.message}
        </p>
      )}
    </div>
  )
}
