import { Activity, Lock, ShieldCheck } from 'lucide-react'
import type { ReactNode } from 'react'

import { Logo } from './Logo'

const FEATURES = [
  { icon: Activity, text: 'Real-time access analytics' },
  { icon: Lock, text: 'Brute-force lockout per account' },
  { icon: ShieldCheck, text: 'Audit trail for every login' },
]

export function AuthLayout({ title, subtitle, children }: { title: string; subtitle: string; children: ReactNode }) {
  return (
    <div className="grid min-h-svh lg:grid-cols-2">
      <aside className="relative hidden overflow-hidden border-r bg-card lg:flex lg:flex-col lg:justify-between lg:p-10">
        <div className="bg-grid pointer-events-none absolute inset-0 opacity-40" />
        <div className="pointer-events-none absolute -top-32 -left-32 size-96 rounded-full bg-primary/20 blur-3xl" />
        <Logo className="relative" />
        <div className="relative space-y-6">
          <h2 className="text-3xl font-semibold tracking-tight">
            Know who touches your systems.
            <span className="block text-muted-foreground">Before it becomes an incident.</span>
          </h2>
          <ul className="space-y-3">
            {FEATURES.map(({ icon: Icon, text }) => (
              <li key={text} className="flex items-center gap-3 text-sm text-muted-foreground">
                <span className="flex size-8 items-center justify-center rounded-md border bg-background">
                  <Icon className="size-4 text-primary" />
                </span>
                {text}
              </li>
            ))}
          </ul>
        </div>
        <p className="relative text-xs text-muted-foreground">Access Monitoring Console</p>
      </aside>

      <main className="flex items-center justify-center px-4 py-10">
        <div className="w-full max-w-sm space-y-6">
          <Logo className="lg:hidden" />
          <div className="space-y-1">
            <h1 className="text-2xl font-semibold tracking-tight">{title}</h1>
            <p className="text-sm text-muted-foreground">{subtitle}</p>
          </div>
          {children}
        </div>
      </main>
    </div>
  )
}
