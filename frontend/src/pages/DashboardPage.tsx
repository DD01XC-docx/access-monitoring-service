import { Card, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'

//placeholder until the dashboard step
export function DashboardPage() {
  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-semibold tracking-tight">Dashboard</h1>
      <Card>
        <CardHeader>
          <CardTitle>Coming next</CardTitle>
          <CardDescription>Charts, SLA, top failed accounts and recent logs.</CardDescription>
        </CardHeader>
      </Card>
    </div>
  )
}
