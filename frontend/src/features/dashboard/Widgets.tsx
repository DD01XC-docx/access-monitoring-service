import type { LucideIcon } from 'lucide-react'

import { Card, CardContent } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import type { AccessEventDto, ChartData, PointSeries } from '@/lib/api'
import { cn } from '@/lib/utils'
import { StatusBadge } from './StatusBadge'

type Tone = 'good' | 'warn' | 'bad' | 'neutral'
const TONE: Record<Tone, string> = {
  good: 'text-chart-5',
  warn: 'text-chart-3',
  bad: 'text-chart-2',
  neutral: 'text-foreground',
}

export function KpiCard({
  label,
  value,
  hint,
  icon: Icon,
  tone = 'neutral',
  loading,
}: {
  label: string
  value: string
  hint: string
  icon: LucideIcon
  tone?: Tone
  loading?: boolean
}) {
  return (
    <Card>
      <CardContent className="space-y-3">
        <div className="flex items-center justify-between text-sm text-muted-foreground">
          {label}
          <Icon className="size-4" />
        </div>
        {loading ? (
          <Skeleton className="h-8 w-24" />
        ) : (
          <p className={cn('text-3xl font-semibold tracking-tight tabular-nums', TONE[tone])}>{value}</p>
        )}
        <p className="text-xs text-muted-foreground">{hint}</p>
      </CardContent>
    </Card>
  )
}

export function TopFailedList({ data }: { data?: ChartData }) {
  const counts = data?.series[0]?.data ?? []
  const rows = (data?.categories ?? [])
    .map((account, i) => ({ account, count: counts[i] ?? 0 }))
    .filter((r) => r.count > 0)
  const max = Math.max(1, ...rows.map((r) => r.count))

  if (rows.length === 0) return <p className="py-10 text-center text-sm text-muted-foreground">No failed logins in the last 24 hours.</p>

  return (
    <ul className="space-y-3">
      {rows.map(({ account, count }) => (
        <li key={account} className="space-y-1.5">
          <div className="flex items-center justify-between gap-4 text-sm">
            <span className="truncate" title={account}>
              {account}
            </span>
            <span className="tabular-nums text-muted-foreground">{count}</span>
          </div>
          <div className="h-1.5 overflow-hidden rounded-full bg-muted">
            <div className="h-full rounded-full bg-chart-2" style={{ width: `${(count / max) * 100}%` }} />
          </div>
        </li>
      ))}
    </ul>
  )
}

export function AccountsList({ data }: { data?: PointSeries<number> }) {
  const rows = (data?.series[0]?.data ?? []).filter((p) => p.x !== 'NO DATA')
  if (rows.length === 0) return <p className="py-10 text-center text-sm text-muted-foreground">No accounts yet.</p>

  return (
    <ul className="divide-y">
      {rows.map(({ x, y }) => {
        const [role, status] = x.split('•').map((s) => s.trim())
        return (
          <li key={x} className="flex items-center justify-between py-2.5 text-sm">
            <span className="flex items-center gap-2">
              <span className="font-medium">{role}</span>
              <span className={cn('text-xs', status === 'ACTIVE' ? 'text-chart-5' : 'text-chart-3')}>{status}</span>
            </span>
            <span className="tabular-nums text-muted-foreground">{y}</span>
          </li>
        )
      })}
    </ul>
  )
}

const timeFormat = new Intl.DateTimeFormat(undefined, { dateStyle: 'short', timeStyle: 'medium' })

export function RecentEventsTable({ events }: { events?: AccessEventDto[] }) {
  if (!events?.length) return <p className="py-10 text-center text-sm text-muted-foreground">No events recorded yet.</p>

  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>Account</TableHead>
          <TableHead>Status</TableHead>
          <TableHead className="hidden sm:table-cell">IP address</TableHead>
          <TableHead className="hidden md:table-cell text-right">Duration</TableHead>
          <TableHead className="text-right">Time</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {events.map((e) => (
          <TableRow key={e.id}>
            <TableCell className="max-w-48 truncate font-medium" title={e.usernameOrEmail}>
              {e.usernameOrEmail}
            </TableCell>
            <TableCell>
              <StatusBadge status={e.status} />
            </TableCell>
            <TableCell className="hidden font-mono text-xs text-muted-foreground sm:table-cell">{e.ipAddress ?? 'n/a'}</TableCell>
            <TableCell className="hidden text-right tabular-nums text-muted-foreground md:table-cell">
              {e.durationMs != null ? `${e.durationMs} ms` : 'n/a'}
            </TableCell>
            <TableCell className="text-right text-xs text-muted-foreground tabular-nums">
              {timeFormat.format(new Date(e.createdAt))}
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  )
}
