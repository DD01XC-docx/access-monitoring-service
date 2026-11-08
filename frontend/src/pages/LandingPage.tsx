import {
  Activity,
  ArrowRight,
  BellRing,
  Container,
  Fingerprint,
  KeyRound,
  Lock,
  LogIn,
  ScrollText,
  ShieldCheck,
  Timer,
  type LucideIcon,
} from 'lucide-react'
import type { ReactNode } from 'react'
import { Link } from 'react-router-dom'

import { Logo } from '@/components/layout/Logo'
import { buttonVariants } from '@/components/ui/button'
import { DashboardPreview } from '@/features/landing/DashboardPreview'
import { getSession } from '@/lib/session'
import { cn } from '@/lib/utils'

const FEATURES: { icon: LucideIcon; title: string; text: string }[] = [
  { icon: Activity, title: 'Live analytics', text: 'Successful and failed logins by minute, hour or day, refreshed every 30 seconds.' },
  { icon: Lock, title: 'Brute-force lockout', text: 'Five failed attempts lock an account for 15 minutes, with a Retry-After hint for clients.' },
  { icon: ScrollText, title: 'Full audit trail', text: 'Every success, failure, lock and rejected signup is stored with IP address and latency.' },
  { icon: BellRing, title: 'Alert levels', text: 'Failure intensity is graded from low to critical so spikes stand out at a glance.' },
  { icon: Timer, title: 'Latency insight', text: 'Median and interquartile login latency per hour to catch slow authentication early.' },
  { icon: Container, title: 'Self-hosted', text: 'One Docker Compose command runs the app and PostgreSQL. Your data stays on your servers.' },
]

const SECURITY: { icon: LucideIcon; title: string; text: string }[] = [
  { icon: Fingerprint, title: 'No account enumeration', text: 'Unknown emails, wrong passwords and disabled accounts get the same response in the same time.' },
  { icon: KeyRound, title: 'BCrypt, done right', text: 'Passwords are capped at 72 bytes, the BCrypt input limit, so nothing is silently truncated.' },
  { icon: ShieldCheck, title: 'Race-free lockout', text: 'Per-account PostgreSQL advisory locks stop parallel requests from slipping past the limit.' },
]

const STEPS = [
  { title: 'Deploy', text: 'Start the stack with Docker Compose. PostgreSQL and the app come up together.' },
  { title: 'Sign in', text: 'Users authenticate with email and password and receive a signed JWT.' },
  { title: 'Watch', text: 'Every attempt lands in the audit log and the dashboard updates live.' },
]

function Section({ id, eyebrow, title, text, children }: { id: string; eyebrow: string; title: string; text: string; children: ReactNode }) {
  return (
    <section id={id} className="scroll-mt-20 py-20 sm:py-24">
      <div className="mx-auto max-w-2xl text-center">
        <p className="text-sm font-medium text-primary">{eyebrow}</p>
        <h2 className="mt-2 text-3xl font-semibold tracking-tight text-balance sm:text-4xl">{title}</h2>
        <p className="mt-4 text-pretty text-muted-foreground">{text}</p>
      </div>
      <div className="mt-14">{children}</div>
    </section>
  )
}

function IconTile({ icon: Icon }: { icon: LucideIcon }) {
  return (
    <span className="flex size-9 items-center justify-center rounded-md bg-primary/15 ring-1 ring-primary/40">
      <Icon className="size-4 text-primary" />
    </span>
  )
}

export function LandingPage() {
  const signedIn = !!getSession()
  const primary = signedIn ? { to: '/dashboard', label: 'Open console' } : { to: '/signup', label: 'Create free account' }

  return (
    <div className="relative overflow-x-clip">
      <div className="bg-grid pointer-events-none absolute inset-x-0 top-0 h-[48rem] opacity-40" />
      <div className="pointer-events-none absolute -top-40 left-1/2 size-[40rem] -translate-x-1/2 rounded-full bg-primary/15 blur-3xl" />

      <header className="sticky top-0 z-40 border-b border-transparent bg-background/70 backdrop-blur supports-[backdrop-filter]:bg-background/50">
        <div className="mx-auto flex h-16 max-w-6xl items-center gap-6 px-4 sm:px-6">
          <Logo />
          <nav className="hidden items-center gap-6 text-sm text-muted-foreground md:flex">
            <a href="#features" className="hover:text-foreground">Features</a>
            <a href="#security" className="hover:text-foreground">Security</a>
            <a href="#deploy" className="hover:text-foreground">Deploy</a>
          </nav>
          <div className="ml-auto flex items-center gap-2">
            {!signedIn && (
              <Link to="/login" className={buttonVariants({ variant: 'ghost', size: 'sm' })}>
                Sign in
              </Link>
            )}
            <Link to={primary.to} className={buttonVariants({ size: 'sm' })}>
              {signedIn ? 'Open console' : 'Get started'}
            </Link>
          </div>
        </div>
      </header>

      <main className="relative mx-auto max-w-6xl px-4 sm:px-6">
        {/* hero */}
        <section className="grid items-center gap-12 pt-16 pb-20 lg:grid-cols-[1fr_1.1fr] lg:pt-24">
          <div>
            <span className="inline-flex items-center gap-2 rounded-full border bg-card px-3 py-1 text-xs text-muted-foreground">
              <span className="size-1.5 rounded-full bg-primary" />
              Self-hosted access monitoring
            </span>
            <h1 className="mt-6 text-4xl font-semibold tracking-tight text-balance sm:text-5xl lg:text-6xl">
              See every login.
              <span className="block text-muted-foreground">Stop the bad ones.</span>
            </h1>
            <p className="mt-6 max-w-lg text-pretty text-lg text-muted-foreground">
              AMC records every authentication attempt, locks brute-force attacks and turns the audit trail into a live
              security dashboard.
            </p>
            <div className="mt-10 flex flex-wrap gap-3">
              <Link to={primary.to} className={cn(buttonVariants({ size: 'lg' }), 'gap-2')}>
                {primary.label}
                <ArrowRight className="size-4" />
              </Link>
              <a href="#deploy" className={buttonVariants({ variant: 'outline', size: 'lg' })}>
                Run it yourself
              </a>
            </div>
          </div>
          <DashboardPreview />
        </section>

        {/* stats strip: real product limits, not marketing numbers */}
        <dl className="grid grid-cols-2 gap-px overflow-hidden rounded-xl border bg-border sm:grid-cols-4">
          {[
            ['30 s', 'dashboard refresh'],
            ['5 / 15 min', 'lockout threshold'],
            ['72 bytes', 'BCrypt-safe passwords'],
            ['1 command', 'to deploy'],
          ].map(([value, label]) => (
            <div key={label} className="bg-card px-6 py-5">
              <dt className="text-xs text-muted-foreground">{label}</dt>
              <dd className="mt-1 text-xl font-semibold tracking-tight">{value}</dd>
            </div>
          ))}
        </dl>

        <Section
          id="features"
          eyebrow="Features"
          title="Everything you need to watch authentication"
          text="From the first login to the thousandth failed attempt, AMC keeps the full picture in one place."
        >
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {FEATURES.map(({ icon, title, text }) => (
              <div key={title} className="rounded-xl border bg-card/60 p-6 transition-colors hover:border-primary/40">
                <IconTile icon={icon} />
                <h3 className="mt-4 font-medium">{title}</h3>
                <p className="mt-1.5 text-sm text-muted-foreground">{text}</p>
              </div>
            ))}
          </div>
        </Section>

        <Section
          id="security"
          eyebrow="Security"
          title="Hardened where attackers look first"
          text="The login endpoint is the front door. These are the details that keep it shut."
        >
          <div className="grid gap-4 lg:grid-cols-3">
            {SECURITY.map(({ icon, title, text }) => (
              <div key={title} className="relative overflow-hidden rounded-xl border bg-card p-6">
                <div className="pointer-events-none absolute -top-16 -right-16 size-40 rounded-full bg-primary/10 blur-2xl" />
                <IconTile icon={icon} />
                <h3 className="mt-4 font-medium">{title}</h3>
                <p className="mt-1.5 text-sm text-muted-foreground">{text}</p>
              </div>
            ))}
          </div>
        </Section>

        <Section
          id="how"
          eyebrow="How it works"
          title="From deploy to insight in three steps"
          text="No agents to install and no external services to sign up for."
        >
          <ol className="grid gap-4 md:grid-cols-3">
            {STEPS.map(({ title, text }, i) => (
              <li key={title} className="rounded-xl border bg-card/60 p-6">
                <span className="flex size-8 items-center justify-center rounded-full border font-mono text-sm text-primary">
                  {i + 1}
                </span>
                <h3 className="mt-4 font-medium">{title}</h3>
                <p className="mt-1.5 text-sm text-muted-foreground">{text}</p>
              </li>
            ))}
          </ol>
        </Section>

        <Section
          id="deploy"
          eyebrow="Deploy"
          title="Up and running in one command"
          text="Generate a signing key once, then let Docker Compose build and start everything."
        >
          <div className="mx-auto max-w-2xl overflow-hidden rounded-xl border bg-card">
            <div className="flex items-center gap-2 border-b px-4 py-2.5 text-xs text-muted-foreground">
              <span className="size-2 rounded-full bg-chart-5" />
              terminal
            </div>
            <pre className="overflow-x-auto p-5 font-mono text-sm leading-7">
              <code>
                <span className="text-muted-foreground"># one-time: create a JWT signing key</span>
                {'\n'}
                <span className="text-primary">$</span> printf 'JWT_SECRET=%s\n' "$(openssl rand -base64 32)" {'>'} .env
                {'\n\n'}
                <span className="text-muted-foreground"># build and start the app with PostgreSQL</span>
                {'\n'}
                <span className="text-primary">$</span> docker compose up --build
              </code>
            </pre>
          </div>
        </Section>

        {/* final CTA */}
        <section className="relative mb-20 overflow-hidden rounded-2xl border bg-card px-6 py-16 text-center sm:px-12">
          <div className="bg-grid pointer-events-none absolute inset-0 opacity-30" />
          <div className="pointer-events-none absolute -bottom-32 left-1/2 size-96 -translate-x-1/2 rounded-full bg-primary/20 blur-3xl" />
          <div className="relative">
            <h2 className="text-3xl font-semibold tracking-tight text-balance sm:text-4xl">Know who is knocking.</h2>
            <p className="mx-auto mt-4 max-w-md text-muted-foreground">
              Create an account and watch your first login appear on the dashboard.
            </p>
            <Link to={primary.to} className={cn(buttonVariants({ size: 'lg' }), 'mt-8 gap-2')}>
              <LogIn className="size-4" />
              {primary.label}
            </Link>
          </div>
        </section>
      </main>

      <footer className="border-t">
        <div className="mx-auto flex max-w-6xl flex-wrap items-center justify-between gap-4 px-4 py-8 text-sm text-muted-foreground sm:px-6">
          <Logo />
          <p>Spring Boot · PostgreSQL · React</p>
        </div>
      </footer>
    </div>
  )
}
