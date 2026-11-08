import { useQuery } from '@tanstack/react-query'

import { api, type ChartData, type TimeRange } from '@/lib/api'

//dashboard is a live view: poll every 30s
const live = { refetchInterval: 30_000 }

export function useDashboard(range: TimeRange) {
  return {
    activity: useQuery({ queryKey: ['activity', range], queryFn: () => api.activity(range), ...live }),
    alertLevels: useQuery({ queryKey: ['alert-levels', range], queryFn: () => api.alertLevels(range), ...live }),
    sla: useQuery({ queryKey: ['sla'], queryFn: api.sla, ...live }),
    topFailed: useQuery({ queryKey: ['top-failed'], queryFn: api.topFailed, ...live }),
    accounts: useQuery({ queryKey: ['accounts'], queryFn: api.accounts, ...live }),
    health: useQuery({ queryKey: ['health'], queryFn: api.health, ...live }),
    responseTime: useQuery({ queryKey: ['response-time'], queryFn: api.responseTime, ...live }),
    events: useQuery({ queryKey: ['events'], queryFn: api.recentEvents, ...live }),
  }
}

//{categories, series[]} -> [{label, <series name>: value}] for Recharts
export function toRows(chart: ChartData | undefined): Record<string, string | number>[] {
  if (!chart) return []
  return chart.categories.map((label, i) => {
    const row: Record<string, string | number> = { label }
    for (const s of chart.series) row[s.name] = s.data[i] ?? 0
    return row
  })
}

export function seriesTotal(chart: ChartData | undefined, name: string): number {
  return chart?.series.find((s) => s.name === name)?.data.reduce((a, b) => a + b, 0) ?? 0
}
