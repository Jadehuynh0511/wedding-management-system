import type { Metadata } from "next";
import { Playfair_Display, Plus_Jakarta_Sans } from "next/font/google";
import "./globals.css";

const display = Playfair_Display({
  subsets: ["latin", "vietnamese"],
  variable: "--font-display"
});

const sans = Plus_Jakarta_Sans({
  subsets: ["latin", "vietnamese"],
  variable: "--font-sans"
});

export const metadata: Metadata = {
  title: "Wedding Management",
  description: "Admin dashboard for wedding hall operations"
};

export default function RootLayout({
  children
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="vi">
      <body className={`${display.variable} ${sans.variable} font-sans`}>
        {children}
      </body>
    </html>
  );
}
