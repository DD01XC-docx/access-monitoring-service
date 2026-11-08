import { CircleCheck, Gauge, Server, ShieldAlert } from 'lucide-react'
import { useState } from 'react'

import { ActivityChart, AlertLevelsChart, hasPoints, ResponseTimeChart } from '@/features/dashboard/Charts'
import { seriesTotal, useDashboard } from '@/features/dashboard/data'
import { Panel } from '@/features/dashboard/Panel'
import { AccountsList, KpiCard, RecentEventsTable, TopFailedList } from '@/features/dashboard/Widgets'
import type { TimeRange } from '@/lib/api'
import { cn } from '@/lib/utils'

const RANGES: { value: TimeRange; label: string }[] = [
  { value: '1h', label: '1H' },
  { value: '24h', label: '24H' },
  { value: '7d', label: '7D' },
]
const RANGE_TEXT: Record<TimeRange, string> = { '1h': 'last hour', '24h': 'last 24 hours', '7d': 'last 7 days' }

export function DashboardPage() {
  const [range, setRange] = useState<TimeRange>('24h')
  const d = useDashboard(range)

  const sla = d.sla.data?.series[0]
  const successful = seriesTotal(d.activity.data, 'Successful')
  const failed = seriesTotal(d.activity.data, 'Failed')
  const health = d.health.data ?? []
  const up = health.filter((h) => h.status === 'UP').length
  const updatedAt = d.activity.dataUpdatedAt ? new Date(d.activity.dataUpdatedAt).toLocaleTimeString() : null

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div className="space-y-1">
          <h1 className="text-2xl font-semibold tracking-tight">Security overview</h1>
          <p className="flex items-center gap-2 text-sm text-muted-foreground">
            <span className="relative flex size-2">
              <span className="absolute inline-flex size-full animate-ping rounded-full bg-chart-5 opacity-60" />
              <span className="relative inline-flex size-2 rounded-full bg-chart-5" />
            </span>
            Live, refreshes every 30s{updatedAt && ` · updated ${updatedAt}`}
          </p>
        </div>
        <div role="tablist" aria-label="Time range" className="inline-flex rounded-lg border bg-card p-1">
          {RANGES.map(({ value, label }) => (
            <button
              key={value}
              role="tab"
              aria-selected={range === value}
              onClick={() => setRange(value)}
              className={cn(
                'rounded-md px-3 py-1 text-sm font-medium text-muted-foreground transition-colors hover:text-foreground',
                range === value && 'bg-secondary text-foreground shadow-sm',
              )}
            >
              {label}
            </button>
          ))}
        </div>
      </div>

      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <KpiCard
          label="Login success rate"
          value={sla != null ? `${sla}%` : '–'}
          hint="Successful share of all logins"
          icon={Gauge}
          tone={sla == null ? 'neutral' : sla >= 95 ? 'good' : sla >= 80 ? 'warn' : 'bad'}
          loading={d.sla.isPending}
        />
        <KpiCard
          label="Successful logins"
          value={successful.toLocaleString()}
          hint={RANGE_TEXT[range]}
          icon={CircleCheck}
          tone="good"
          loading={d.activity.isPending}
        />
        <KpiCard
          label="Failed logins"
          value={failed.toLocaleString()}
          hint={RANGE_TEXT[range]}
          icon={ShieldAlert}
          tone={failed > 0 ? 'bad' : 'neutral'}
          loading={d.activity.isPending}
        />
        <KpiCard
          label="Systems up"
          value={health.length ? `${up}/${health.length}` : '–'}
          hint={health.map((h) => `${h.x} ${h.status}`).join(' · ') || 'Database checks'}
          icon={Server}
          tone={health.length && up === health.length ? 'good' : 'bad'}
          loading={d.health.isPending}
        />
      </div>

      <div className="grid gap-4 lg:grid-cols-3">
        <Panel
          title="Access activity"
          description={`Successful and failed logins, ${RANGE_TEXT[range]}`}
          className="lg:col-span-2"
          loading={d.activity.isPending}
          error={d.activity.isError}
          empty={!hasPoints(d.activity.data)}
        >
          <ActivityChart data={d.activity.data} />
        </Panel>
        <Panel
          title="Top failed accounts"
          description="Most failed logins, last 24 hours"
          loading={d.topFailed.isPending}
          error={d.topFailed.isError}
        >
          <TopFailedList data={d.topFailed.data} />
        </Panel>
      </div>

      <div className="grid gap-4 lg:grid-cols-3">
        <Panel
          title="Alert levels"
          description={`Failure intensity, ${RANGE_TEXT[range]}`}
          className="lg:col-span-2"
          loading={d.alertLevels.isPending}
          error={d.alertLevels.isError}
          empty={!hasPoints(d.alertLevels.data)}
        >
          <AlertLevelsChart data={d.alertLevels.data} />
        </Panel>
        <div className="grid gap-4">
          <Panel
            title="Response time"
            description="Login latency by hour"
            loading={d.responseTime.isPending}
            error={d.responseTime.isError}
            empty={!d.responseTime.data?.series[0]?.data.length}
          >
            <ResponseTimeChart data={d.responseTime.data} />
          </Panel>
          <Panel title="Accounts" description="By role and status" loading={d.accounts.isPending} error={d.accounts.isError}>
            <AccountsList data={d.accounts.data} />
          </Panel>
        </div>
      </div>

      <Panel
        title="Recent events"
        description="Last 10 authentication events"
        loading={d.events.isPending}
        error={d.events.isError}
      >
        <RecentEventsTable events={d.events.data} />
      </Panel>
    </div>
  )
}
