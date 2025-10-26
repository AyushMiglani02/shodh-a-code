import type { Metadata } from 'next'
import './globals.css'

export const metadata: Metadata = {
  title: 'Shodh-a-Code',
  description: 'Online Programming Contest Platform',
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  )
}
