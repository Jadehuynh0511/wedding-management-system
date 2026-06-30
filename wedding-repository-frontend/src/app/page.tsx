import { redirect } from "next/navigation";

import { getOptionalAuthenticatedSession } from "@/features/auth/lib/server-session";
import { DEFAULT_POST_LOGIN_PATH } from "@/features/auth/model/auth-session";

export default async function HomePage() {
  const session = await getOptionalAuthenticatedSession();

  redirect(session ? DEFAULT_POST_LOGIN_PATH : "/login");
}
