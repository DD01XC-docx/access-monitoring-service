import type { ReactNode } from 'react'

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { cn } from '@/lib/utils'

type Props = {
  title: string
  description?: string
  action?: ReactNode
  loading?: boolean
  error?: boolean
  empty?: boolean
  className?: string
  children: ReactNode
}

//card wrapper with loading / error / empty states shared by all widgets
export function Panel({ title, description, action, loading, error, empty, className, children }: Props) {
  return (
    <Card className={cn('flex flex-col', className)}>
      <CardHeader className="flex flex-row items-start justify-between gap-4">
        <div className="space-y-1">
          <CardTitle className="text-base">{title}</CardTitle>
          {description && <CardDescription>{description}</CardDescription>}
        </div>
        {action}
      </CardHeader>
      <CardContent className="flex-1">
        {loading ? (
          <Skeleton className="h-full min-h-40 w-full" />
        ) : error ? (
          <State text="Could not load data." tone="error" />
        ) : empty ? (
          <State text="No data for this period yet." />
        ) : (
          children
        )}
      </CardContent>
    </Card>
  )
}

function State({ text, tone }: { text: string; tone?: 'error' }) {
  return (
    <div
      className={cn(
        'flex h-full min-h-40 items-center justify-center rounded-md border border-dashed text-sm',
        tone === 'error' ? 'text-destructive' : 'text-muted-foreground',
      )}
    >
      {text}
    </div>
  )
}
