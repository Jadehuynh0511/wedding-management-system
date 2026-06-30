# Wedding Repository Frontend

Frontend application for the Wedding Management System.

This repository contains the `Next.js 14` admin-facing web app that will be used for:

- system login and protected dashboard flows
- catalog management screens
- wedding booking and lookup flows
- invoice, cancellation, and reporting pages

At the current milestone (`M0`), this repository already includes:

- project structure for Next.js 14 + React 18 + TypeScript
- Tailwind CSS setup
- shadcn/ui-ready configuration
- basic landing shell and UI primitives
- ESLint and Prettier configuration

## Tech Stack

- Next.js 14
- React 18
- TypeScript
- Tailwind CSS
- shadcn/ui
- Radix UI Slot
- class-variance-authority
- clsx
- tailwind-merge
- Lucide React

## Architecture

This frontend should follow a **scalable feature-based architecture** built for Next.js App Router.

Recommended approach:

- route layer for page composition
- feature layer for business interactions
- entity layer for reusable domain-specific UI and models
- shared layer for generic UI, utilities, config, and API client code

Why this architecture is selected:

- the project will grow into many management screens, not just a few pages
- business areas like auth, catalog, booking, billing, and reporting should evolve independently
- feature-based structure scales better than placing everything under a single `components` folder
- it keeps reusable UI separate from domain-specific logic
- it works naturally with Next.js 14 server/client component boundaries

Recommended dependency direction:

```text
app/routes -> features -> entities -> shared
widgets -> features/entities/shared
shared -> no upper layer
```

## Target Frontend Structure

Target structure for future milestones:

```text
src/
  app/
    (public)/
    (dashboard)/
    api/
    layout.tsx
    globals.css
  widgets/
    app-shell/
    sidebar/
    topbar/
  features/
    auth/
    hall-management/
    booking-form/
    booking-search/
    invoice-generation/
    report-monthly/
    system-settings/
  entities/
    user/
    hall/
    hall-type/
    session/
    food/
    service/
    booking/
    invoice/
    parameter/
  shared/
    ui/
    lib/
    api/
    config/
    constants/
    hooks/
    types/
  styles/
```

How these layers are intended to work:

- `app`: routes, layouts, metadata, page composition
- `widgets`: large UI blocks reused across multiple pages
- `features`: user actions and business interactions
- `entities`: domain-focused models, selectors, and reusable UI fragments
- `shared/ui`: generic UI primitives such as button, input, dialog, table
- `shared/api`: API client, request helpers, and endpoint wrappers
- `shared/lib`: utility helpers
- `shared/types`: shared TypeScript types

## What Lives Here

- `src/app`: App Router pages and layouts
- `src/components`: reusable UI and layout components
- `src/lib`: shared utilities
- `public`: static assets
- `tailwind.config.ts`: Tailwind theme setup
- `components.json`: shadcn/ui configuration
- `package.json`: scripts and dependencies

## Prerequisites

Install these tools before running the frontend:

- Node.js 20+
- npm 10+

Recommended:

- use the version from `.nvmrc`

Quick verification:

```bash
node -v
npm -v
```

## Local Setup

### 1. Install dependencies

```bash
npm install
```

### 2. Start the development server

```bash
npm run dev
```

The app will start at:

- `http://localhost:3000`

### 3. Run quality checks

```bash
npm run lint
npm run build
```

## Backend Connection

The frontend is expected to call the backend at:

- `http://localhost:8082`

The backend already allows CORS from `http://localhost:3000`.

When API service code is added in later milestones, keep FE and BE ports aligned:

- frontend: `3000`
- backend: `8082`

How to read the current scaffold:

- `src/app/layout.tsx`: root HTML layout for the whole app
- `src/app/page.tsx`: current landing page for the scaffold
- `src/app/globals.css`: global Tailwind layers and CSS variables
- `src/app/(dashboard)`: reserved area for future dashboard routes
- `src/components/layout`: app-level structural components
- `src/components/ui`: reusable UI primitives
- `src/lib/utils.ts`: shared helper functions such as class merging

This current scaffold is still the `M0` foundation.
As the project grows, the repository should be reorganized gradually toward the target architecture above.

## Styling and UI Conventions

- Tailwind CSS is the main styling approach
- `components.json` prepares the repo for shadcn/ui components
- `cn()` in `src/lib/utils.ts` merges conditional class names safely
- current UI shell already uses a custom visual direction instead of a plain default scaffold

## Scripts

Available scripts from `package.json`:

- `npm run dev`: start local dev server
- `npm run build`: production build
- `npm run start`: run built app
- `npm run lint`: run ESLint
- `npm run format`: check Prettier formatting
- `npm run format:write`: rewrite files with Prettier

## Branching Strategy

- `main`: production-ready branch
- `develop`: integration branch
- `feature/*`: short-lived feature branches

Examples:

- `feature/m0-frontend-structure`
- `feature/m1-auth-shell`
- `feature/m2-catalog-pages`
