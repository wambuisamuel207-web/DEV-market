# DevMarket Web Frontend (Companion to Android App)

This directory contains the standalone **Web Dashboard** for DevMarket. It connects directly to your existing Supabase database (`https://dulnugmywpcfuyshtapb.supabase.co`) and PayPal escrow workflows, allowing clients and developers to manage contracts and milestones on both desktop browsers and mobile Android devices.

---

## 🚀 Quick Start (Local Run)

1. Navigate to the `web` folder:
   ```bash
   cd web
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Run the development server:
   ```bash
   npm run dev
   ```
4. Open [http://localhost:5173](http://localhost:5173) in your browser.

---

## 🌐 Deploy to Vercel / Netlify

1. Push your repository to GitHub.
2. In Vercel or Netlify, set the **Root Directory** to `web`.
3. Add these Environment Variables (optional, defaults are already configured in code):
   - `VITE_SUPABASE_URL`: `https://dulnugmywpcfuyshtapb.supabase.co`
   - `VITE_SUPABASE_ANON_KEY`: `your-supabase-anon-key`
4. Click **Deploy**.

---

## ⚡ Create a Companion Web App in Google AI Studio

1. In Google AI Studio, click **New Project** and select a **Web (React / Vite)** template.
2. Copy `src/App.tsx`, `src/types.ts`, and `src/supabaseClient.ts` into the web project.
3. Both apps will instantly sync projects, escrow funding, deliverables submissions, and milestone releases in real time!
