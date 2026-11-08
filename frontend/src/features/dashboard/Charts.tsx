import { Area, AreaChart, Bar, BarChart, CartesianGrid, ComposedChart, Line, XAxis, YAxis } from 'recharts'

import {
  ChartContainer,
  ChartLegend,
  ChartLegendContent,
  ChartTooltip,
  ChartTooltipContent,
  type ChartConfig,
} from '@/components/ui/chart'
import type { ChartData, PointSeries } from '@/lib/api'
import { toRows } from './data'

const axis = { tickLine: false, axisLine: false, tickMargin: 8, minTickGap: 24 } as const

const activityConfig = {
  Successful: { label: 'Successful', color: 'var(--chart-1)' },
  Failed: { label: 'Failed', color: 'var(--chart-2)' },
} satisfies ChartConfig

export function ActivityChart({ data }: { data?: ChartData }) {
  return (
    <ChartContainer config={activityConfig} className="aspect-auto h-72 w-full">
      <AreaChart data={toRows(data)} margin={{ left: -16, right: 8 }}>
        <defs>
          {Object.keys(activityConfig).map((key) => (
            <linearGradient key={key} id={`fill-${key}`} x1="0" y1="0" x2="0" y2="1">
              <stop offset="5%" stopColor={`var(--color-${key})`} stopOpacity={0.35} />
              <stop offset="95%" stopColor={`var(--color-${key})`} stopOpacity={0.02} />
            </linearGradient>
          ))}
        </defs>
        <CartesianGrid vertical={false} />
        <XAxis dataKey="label" {...axis} />
        <YAxis allowDecimals={false} {...axis} />
        <ChartTooltip content={<ChartTooltipContent indicator="line" />} />
        <ChartLegend content={<ChartLegendContent />} />
        <Area dataKey="Successful" type="monotone" stroke="var(--color-Successful)" fill="url(#fill-Successful)" strokeWidth={2} />
        <Area dataKey="Failed" type="monotone" stroke="var(--color-Failed)" fill="url(#fill-Failed)" strokeWidth={2} />
      </AreaChart>
    </ChartContainer>
  )
}

const alertConfig = {
  Low: { label: 'Low', color: 'var(--chart-5)' },
  Medium: { label: 'Medium', color: 'var(--chart-3)' },
  High: { label: 'High', color: 'var(--chart-2)' },
  Critical: { label: 'Critical', color: 'var(--chart-4)' },
} satisfies ChartConfig

export function AlertLevelsChart({ data }: { data?: ChartData }) {
  const keys = Object.keys(alertConfig) as (keyof typeof alertConfig)[]
  return (
    <ChartContainer config={alertConfig} className="aspect-auto h-72 w-full">
      <BarChart data={toRows(data)} margin={{ left: -16, right: 8 }}>
        <CartesianGrid vertical={false} />
        <XAxis dataKey="label" {...axis} />
        <YAxis allowDecimals={false} {...axis} />
        <ChartTooltip content={<ChartTooltipContent />} />
        <ChartLegend content={<ChartLegendContent />} />
        {keys.map((key, i) => (
          <Bar
            key={key}
            dataKey={key}
            stackId="alerts"
            fill={`var(--color-${key})`}
            radius={i === keys.length - 1 ? [4, 4, 0, 0] : 0}
          />
        ))}
      </BarChart>
    </ChartContainer>
  )
}

const latencyConfig = {
  median: { label: 'Median', color: 'var(--chart-1)' },
  spread: { label: 'P25 to P75', color: 'var(--chart-1)' },
} satisfies ChartConfig

type Latency = PointSeries<[number, number, number, number, number]>

export function ResponseTimeChart({ data }: { data?: Latency }) {
  //box plot -> median line with interquartile band
  const rows = (data?.series[0]?.data ?? []).map(({ x, y: [, q1, median, q3] }) => ({
    label: x,
    median: Math.round(median),
    spread: [Math.round(q1), Math.round(q3)],
  }))

  return (
    <ChartContainer config={latencyConfig} className="aspect-auto h-56 w-full">
      <ComposedChart data={rows} margin={{ left: -16, right: 8 }}>
        <CartesianGrid vertical={false} />
        <XAxis dataKey="label" {...axis} />
        <YAxis unit="ms" {...axis} />
        <ChartTooltip content={<ChartTooltipContent indicator="line" />} />
        <Area dataKey="spread" type="monotone" stroke="none" fill="var(--color-spread)" fillOpacity={0.15} />
        <Line dataKey="median" type="monotone" stroke="var(--color-median)" strokeWidth={2} dot={{ r: 3 }} />
      </ComposedChart>
    </ChartContainer>
  )
}

export function hasPoints(chart?: ChartData) {
  return !!chart && chart.series.some((s) => s.data.some((v) => v > 0))
}
