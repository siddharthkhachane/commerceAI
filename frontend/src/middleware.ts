import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";

const protectedRoutes = ["/dashboard", "/cart", "/checkout", "/orders"];
const authRoutes = ["/login", "/register"];

export function middleware(request: NextRequest) {
  const token = request.cookies.get("auth-token")?.value;
  const { pathname } = request.nextUrl;

  const isAdminLogin = pathname === "/admin/login";
  const isAdminRoute = pathname.startsWith("/admin") && !isAdminLogin;
  const isProtected = protectedRoutes.some((route) => pathname.startsWith(route));
  const isAuthRoute = authRoutes.includes(pathname);

  if (isAdminRoute && !token) {
    const loginUrl = new URL("/admin/login", request.url);
    return NextResponse.redirect(loginUrl);
  }

  if (isAdminLogin && token) {
    return NextResponse.redirect(new URL("/admin", request.url));
  }

  if (isProtected && !token) {
    const loginUrl = new URL("/login", request.url);
    loginUrl.searchParams.set("from", pathname);
    return NextResponse.redirect(loginUrl);
  }

  if (isAuthRoute && token) {
    return NextResponse.redirect(new URL("/", request.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    "/dashboard/:path*",
    "/admin/:path*",
    "/cart",
    "/checkout",
    "/orders/:path*",
    "/login",
    "/register",
  ],
};
