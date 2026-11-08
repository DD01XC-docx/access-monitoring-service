import { Activity, ArrowRight, Lock, ShieldCheck } from 'lucide-react'
import { Link } from 'react-router-dom'

import { Logo } from '@/components/layout/Logo'
import { buttonVariants } from '@/components/ui/button'
import { getSession } from '@/lib/session'
import { cn } from '@/lib/utils'

const FEATURES = [
  { icon: Activity, title: 'Live analytics', text: 'Login activity by minute, hour and day with SLA tracking.' },
  { icon: Lock, title: 'Brute-force lockout', text: 'Per-account lockout with audit events for every attempt.' },
  { icon: ShieldCheck, title: 'Audit trail', text: 'Every success, failure and lock is recorded and searchable.' },
]

//temporary landing until the full marketing pages are migrated
export function LandingPage() {
  const signedIn = !!getSession()

  return (
    <div className="relative min-h-svh overflow-hidden">
      <div className="bg-grid pointer-events-none absolute inset-0 opacity-40" />
      <div className="pointer-events-none absolute -top-40 left-1/2 size-[36rem] -translate-x-1/2 rounded-full bg-primary/15 blur-3xl" />

      <header className="relative mx-auto flex h-16 max-w-6xl items-center px-4 sm:px-6">
        <Logo />
        <nav className="ml-auto flex items-center gap-2">
          {signedIn ? (
            <Link to="/dashboard" className={buttonVariants({ size: 'sm' })}>
              Open console
            </Link>
          ) : (
            <>
              <Link to="/login" className={buttonVariants({ variant: 'ghost', size: 'sm' })}>
                Sign in
              </Link>
              <Link to="/signup" className={buttonVariants({ size: 'sm' })}>
                Get started
              </Link>
            </>
          )}
        </nav>
      </header>

      <main className="relative mx-auto max-w-6xl px-4 pt-20 pb-24 sm:px-6 sm:pt-28">
        <section className="mx-auto max-w-3xl text-center">
          <span className="inline-flex items-center gap-2 rounded-full border bg-card px-3 py-1 text-xs text-muted-foreground">
            <span className="size-1.5 rounded-full bg-primary" />
            Access monitoring for small teams
          </span>
          <h1 className="mt-6 text-4xl font-semibold tracking-tight text-balance sm:text-6xl">
            See every login.
            <span className="block text-muted-foreground">Stop the bad ones.</span>
          </h1>
          <p className="mx-auto mt-6 max-w-xl text-pretty text-muted-foreground">
            AMC records authentication activity, locks brute-force attempts and turns the audit trail into a live
            security dashboard.
          </p>
          <div className="mt-10 flex flex-wrap items-center justify-center gap-3">
            <Link to={signedIn ? '/dashboard' : '/signup'} className={cn(buttonVariants({ size: 'lg' }), 'gap-2')}>
              {signedIn ? 'Open console' : 'Create free account'}
              <ArrowRight className="size-4" />
            </Link>
            {!signedIn && (
              <Link to="/login" className={buttonVariants({ variant: 'outline', size: 'lg' })}>
                Sign in
              </Link>
            )}
          </div>
        </section>

        <section className="mt-24 grid gap-4 sm:grid-cols-3">
          {FEATURES.map(({ icon: Icon, title, text }) => (
            <div key={title} className="rounded-xl border bg-card/60 p-6 backdrop-blur">
              <span className="flex size-9 items-center justify-center rounded-md bg-primary/15 ring-1 ring-primary/40">
                <Icon className="size-4 text-primary" />
              </span>
              <h3 className="mt-4 font-medium">{title}</h3>
              <p className="mt-1 text-sm text-muted-foreground">{text}</p>
            </div>
          ))}
        </section>
      </main>
    </div>
  )
}
