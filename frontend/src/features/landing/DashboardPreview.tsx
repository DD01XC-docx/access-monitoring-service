import { StatusBadge } from '@/features/dashboard/StatusBadge'

//static product shot built from real UI pieces; plain SVG keeps recharts out of the landing bundle
const SUCCESS = [12, 18, 15, 22, 30, 26, 34, 41, 38, 46, 52, 49, 58, 63, 57, 66]
const FAILED = [2, 3, 2, 5, 4, 9, 14, 6, 4, 3, 5, 4, 3, 7, 4, 3]

function areaPath(values: number[], width: number, height: number, max: number) {
  const step = width / (values.length - 1)
  const points = values.map((v, i) => [i * step, height - (v / max) * height] as const)
  const line = points.map(([x, y], i) => `${i === 0 ? 'M' : 'L'}${x.toFixed(1)},${y.toFixed(1)}`).join(' ')
  return { line, area: `${line} L${width},${height} L0,${height} Z` }
}

const EVENTS = [
  { account: 'maria@acme.io', status: 'SUCCESS', ip: '10.0.4.12', time: '14:32:08' },
  { account: 'ops-bot@acme.io', status: 'FAILED', ip: '185.220.101.7', time: '14:31:55' },
  { account: 'ops-bot@acme.io', status: 'LOCKED', ip: '185.220.101.7', time: '14:31:57' },
  { account: 'jonas@acme.io', status: 'SUCCESS', ip: '10.0.4.31', time: '14:30:41' },
]

export function DashboardPreview() {
  const w = 560
  const h = 150
  const max = 70
  const ok = areaPath(SUCCESS, w, h, max)
  const bad = areaPath(FAILED, w, h, max)

  return (
    <div className="rounded-xl border bg-card/80 shadow-2xl shadow-primary/10 ring-1 ring-white/5 backdrop-blur">
      <div className="flex items-center gap-1.5 border-b px-4 py-3">
        <span className="size-2.5 rounded-full bg-chart-2/70" />
        <span className="size-2.5 rounded-full bg-chart-3/70" />
        <span className="size-2.5 rounded-full bg-chart-5/70" />
        <span className="ml-3 text-xs text-muted-foreground">Security overview · last 24 hours</span>
      </div>

      <div className="space-y-4 p-4 sm:p-5">
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
          {[
            ['Success rate', '96.4%', 'text-chart-5'],
            ['Logins', '1,284', 'text-foreground'],
            ['Failed', '47', 'text-chart-2'],
            ['Locked', '3', 'text-chart-3'],
          ].map(([label, value, tone]) => (
            <div key={label} className="rounded-lg border bg-background/60 p-3">
              <p className="text-[11px] text-muted-foreground">{label}</p>
              <p className={`mt-1 text-lg font-semibold tabular-nums ${tone}`}>{value}</p>
            </div>
          ))}
        </div>

        <div className="rounded-lg border bg-background/60 p-3">
          <svg viewBox={`0 0 ${w} ${h}`} className="h-32 w-full" preserveAspectRatio="none" aria-hidden>
            <defs>
              <linearGradient id="pv-ok" x1="0" x2="0" y1="0" y2="1">
                <stop offset="0%" stopColor="var(--chart-1)" stopOpacity="0.35" />
                <stop offset="100%" stopColor="var(--chart-1)" stopOpacity="0" />
              </linearGradient>
              <linearGradient id="pv-bad" x1="0" x2="0" y1="0" y2="1">
                <stop offset="0%" stopColor="var(--chart-2)" stopOpacity="0.35" />
                <stop offset="100%" stopColor="var(--chart-2)" stopOpacity="0" />
              </linearGradient>
            </defs>
            <path d={ok.area} fill="url(#pv-ok)" />
            <path d={ok.line} fill="none" stroke="var(--chart-1)" strokeWidth="2" vectorEffect="non-scaling-stroke" />
            <path d={bad.area} fill="url(#pv-bad)" />
            <path d={bad.line} fill="none" stroke="var(--chart-2)" strokeWidth="2" vectorEffect="non-scaling-stroke" />
          </svg>
        </div>

        <ul className="divide-y rounded-lg border bg-background/60 text-xs">
          {EVENTS.map((e, i) => (
            <li key={i} className="flex items-center gap-3 px-3 py-2">
              <span className="min-w-0 flex-1 truncate font-medium">{e.account}</span>
              <StatusBadge status={e.status} />
              <span className="hidden w-28 font-mono text-muted-foreground sm:block">{e.ip}</span>
              <span className="tabular-nums text-muted-foreground">{e.time}</span>
            </li>
          ))}
        </ul>
      </div>
    </div>
  )
}
