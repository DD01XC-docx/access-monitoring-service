import { useQuery } from '@tanstack/react-query'

import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { api } from '@/lib/api'

export function ProfilePage() {
  const { data: profile, isPending, isError } = useQuery({ queryKey: ['profile'], queryFn: api.profile })

  const rows: [string, string | undefined][] = [
    ['Email', profile?.email],
    ['Role', profile?.role],
    ['Member since', profile?.createdAt],
    ['Last login', profile?.lastLogin],
    ['Last IP', profile?.lastIp],
  ]

  return (
    <div className="mx-auto max-w-2xl space-y-6">
      <h1 className="text-2xl font-semibold tracking-tight">Profile</h1>
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-3">
            {isPending ? <Skeleton className="h-6 w-40" /> : profile?.username}
            {profile && (
              <Badge variant={profile.status === 'ACTIVE' ? 'secondary' : 'destructive'}>{profile.status}</Badge>
            )}
          </CardTitle>
          <CardDescription>Account details from the access monitoring service.</CardDescription>
        </CardHeader>
        <CardContent>
          {isError ? (
            <p className="text-sm text-destructive">Could not load profile.</p>
          ) : (
            <dl className="divide-y">
              {rows.map(([label, value]) => (
                <div key={label} className="flex items-center justify-between gap-4 py-3 text-sm">
                  <dt className="text-muted-foreground">{label}</dt>
                  <dd className="font-medium">{isPending ? <Skeleton className="h-4 w-32" /> : value}</dd>
                </div>
              ))}
            </dl>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
