import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { Loader2, TriangleAlert } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { Link, useLocation, useNavigate } from 'react-router-dom'

import { FormField } from '@/components/FormField'
import { AuthLayout } from '@/components/layout/AuthLayout'
import { Button } from '@/components/ui/button'
import { api, ApiError } from '@/lib/api'
import { saveSession } from '@/lib/session'
import { loginSchema, type LoginValues } from '@/lib/validation'

function loginErrorMessage(error: unknown): string {
  if (!(error instanceof ApiError)) return 'Server is unreachable. Try again later.'
  if (error.status === 401) return 'Incorrect email or password.'
  if (error.status === 429) {
    const minutes = Math.ceil((error.retryAfter ?? 60) / 60)
    return `Too many failed attempts. Try again in ${minutes} min.`
  }
  return 'Something went wrong. Try again.'
}

export function LoginPage() {
  const navigate = useNavigate()
  const from = (useLocation().state as { from?: string } | null)?.from ?? '/dashboard'

  const form = useForm<LoginValues>({ resolver: zodResolver(loginSchema), defaultValues: { email: '', password: '' } })
  const login = useMutation({
    mutationFn: api.login,
    onSuccess: (session) => {
      saveSession(session)
      navigate(from, { replace: true })
    },
  })
  const { errors } = form.formState

  return (
    <AuthLayout title="Sign in" subtitle="Use your work email to access the console.">
      <form className="space-y-4" noValidate onSubmit={form.handleSubmit((values) => login.mutate(values))}>
        {login.isError && (
          <div role="alert" className="flex items-start gap-2 rounded-md border border-destructive/40 bg-destructive/10 p-3 text-sm text-destructive">
            <TriangleAlert className="mt-0.5 size-4 shrink-0" />
            {loginErrorMessage(login.error)}
          </div>
        )}
        <FormField id="email" label="Email" type="email" autoComplete="email" placeholder="you@company.com" registration={form.register('email')} error={errors.email} />
        <FormField id="password" label="Password" type="password" autoComplete="current-password" registration={form.register('password')} error={errors.password} />
        <Button type="submit" className="w-full" disabled={login.isPending}>
          {login.isPending && <Loader2 className="size-4 animate-spin" />}
          Sign in
        </Button>
      </form>
      <p className="text-center text-sm text-muted-foreground">
        No account yet?{' '}
        <Link to="/signup" className="text-primary underline-offset-4 hover:underline">
          Create one
        </Link>
      </p>
    </AuthLayout>
  )
}
