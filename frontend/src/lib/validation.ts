import { z } from 'zod'

//mirrors backend DTO rules (RegisterRequest, LoginRequest)
const email = z.email('Please provide a valid email').max(254, 'Email must be at most 254 characters')

const password = z
  .string()
  .min(8, 'Password must be at least 8 characters long')
  .refine((value) => new TextEncoder().encode(value).length <= 72, 'Password must be at most 72 bytes')

export const loginSchema = z.object({ email, password })

export const signupSchema = z.object({
  username: z
    .string()
    .min(3, 'Username must be between 3 and 20 characters')
    .max(20, 'Username must be between 3 and 20 characters')
    .regex(/^[A-Za-z0-9_.-]+$/, "Only letters, digits, '_', '.' and '-'"),
  email,
  password,
})

export type LoginValues = z.infer<typeof loginSchema>
export type SignupValues = z.infer<typeof signupSchema>
