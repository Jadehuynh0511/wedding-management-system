"use client";

import { LoaderCircle, LogOut } from "lucide-react";

import { useAuth } from "@/features/auth/ui/auth-provider";
import { Button } from "@/shared/ui/button";

export function LogoutButton() {
  const { isLoggingOut, logout } = useAuth();

  return (
    <Button
      type="button"
      variant="ghost"
      className="justify-start rounded-2xl px-4"
      onClick={logout}
      disabled={isLoggingOut}
    >
      {isLoggingOut ? <LoaderCircle className="mr-2 h-4 w-4 animate-spin" /> : <LogOut className="mr-2 h-4 w-4" />}
      Đăng xuất
    </Button>
  );
}
