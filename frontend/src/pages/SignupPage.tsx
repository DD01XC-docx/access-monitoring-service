import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { Loader2, TriangleAlert } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router-dom'
import { toast } from 'sonner'

import { FormField } from '@/components/FormField'
import { AuthLayout } from '@/components/layout/AuthLayout'
import { Button } from '@/components/ui/button'
import { api, ApiError } from '@/lib/api'
import { signupSchema, type SignupValues } from '@/lib/validation'

const FIELDS = ['username', 'email', 'password'] as const

export function SignupPage() {
  const navigate = useNavigate()
  const form = useForm<SignupValues>({
    resolver: zodResolver(signupSchema),
    defaultValues: { username: '', email: '', password: '' },
  })

  const register = useMutation({
    mutationFn: api.register,
    onSuccess: () => {
      toast.success('Account created. You can sign in now.')
      navigate('/login')
    },
    onError: (error) => {
      //map backend validation errors onto form fields
      if (error instanceof ApiError && error.status === 400) {
        for (const [field, message] of Object.entries(error.fieldErrors)) {
          const target = field === 'passwordWithinBcryptLimit' ? 'password' : field
          if ((FIELDS as readonly string[]).includes(target)) {
            form.setError(target as (typeof FIELDS)[number], { message })
          }
        }
      }
    },
  })
  const { errors } = form.formState

  const formError =
    register.error instanceof ApiError
      ? register.error.status === 409
        ? 'An account with this email or username already exists.'
        : register.error.status === 400
          ? null
          : 'Something went wrong. Try again.'
      : register.isError
        ? 'Server is unreachable. Try again later.'
        : null

  return (
    <AuthLayout title="Create account" subtitle="Start monitoring access in a minute.">
      <form className="space-y-4" noValidate onSubmit={form.handleSubmit((values) => register.mutate(values))}>
        {formError && (
          <div role="alert" className="flex items-start gap-2 rounded-md border border-destructive/40 bg-destructive/10 p-3 text-sm text-destructive">
            <TriangleAlert className="mt-0.5 size-4 shrink-0" />
            {formError}
          </div>
        )}
        <FormField id="username" label="Username" autoComplete="username" placeholder="john.doe" registration={form.register('username')} error={errors.username} />
        <FormField id="email" label="Email" type="email" autoComplete="email" placeholder="you@company.com" registration={form.register('email')} error={errors.email} />
        <FormField id="password" label="Password" type="password" autoComplete="new-password" registration={form.register('password')} error={errors.password} />
        <Button type="submit" className="w-full" disabled={register.isPending}>
          {register.isPending && <Loader2 className="size-4 animate-spin" />}
          Create account
        </Button>
      </form>
      <p className="text-center text-sm text-muted-foreground">
        Already have an account?{' '}
        <Link to="/login" className="text-primary underline-offset-4 hover:underline">
          Sign in
        </Link>
      </p>
    </AuthLayout>
  )
}
