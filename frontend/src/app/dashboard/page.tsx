"use client";

import { DashboardShell, ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { ProfilePageContent } from "@/components/profile/ProfilePageContent";

export default function DashboardPage() {
  return (
    <ProtectedRoute>
      <DashboardShell title="Profile">
        <ProfilePageContent />
      </DashboardShell>
    </ProtectedRoute>
  );
}
