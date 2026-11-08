import { cn } from '@/lib/utils'

//one color per access event status; unknown statuses fall back to neutral
const STYLES: Record<string, { label: string; className: string }> = {
  SUCCESS: { label: 'Success', className: 'bg-chart-5/15 text-chart-5 ring-chart-5/30' },
  FAILED: { label: 'Failed', className: 'bg-chart-2/15 text-chart-2 ring-chart-2/30' },
  LOCKED: { label: 'Locked', className: 'bg-chart-3/15 text-chart-3 ring-chart-3/30' },
  REGISTER_FAILED: { label: 'Signup rejected', className: 'bg-chart-4/15 text-chart-4 ring-chart-4/30' },
}

export function StatusBadge({ status }: { status: string }) {
  const style = STYLES[status] ?? { label: status, className: 'bg-muted text-muted-foreground ring-border' }
  return (
    <span className={cn('inline-flex items-center gap-1.5 rounded-full px-2 py-0.5 text-xs font-medium ring-1', style.className)}>
      <span className="size-1.5 rounded-full bg-current" />
      {style.label}
    </span>
  )
}
