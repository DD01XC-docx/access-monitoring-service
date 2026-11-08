import { ShieldHalf } from 'lucide-react'

import { cn } from '@/lib/utils'

export function Logo({ className }: { className?: string }) {
  return (
    <div className={cn('flex items-center gap-2 font-semibold tracking-tight', className)}>
      <span className="flex size-8 items-center justify-center rounded-md bg-primary/15 ring-1 ring-primary/40">
        <ShieldHalf className="size-4 text-primary" />
      </span>
      <span>
        AMC<span className="text-muted-foreground font-normal"> / access monitor</span>
      </span>
    </div>
  )
}
