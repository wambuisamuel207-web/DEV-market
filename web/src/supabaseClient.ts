import { createClient } from '@supabase/supabase-js';

// Supabase Instance Configuration
const SUPABASE_URL = import.meta.env.VITE_SUPABASE_URL || 'https://dulnugmywpcfuyshtapb.supabase.co';
const SUPABASE_ANON_KEY = import.meta.env.VITE_SUPABASE_ANON_KEY || 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImR1bG51Z215d3BjZnV5c2h0YXBiIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDI2MzcxNDcsImV4cCI6MjA1ODIxMzE0N30.rQ219J57h7M3mO9o7K4L3t_z7zJ1kX7fX9N6n8s2v-g';

export const supabase = createClient(SUPABASE_URL, SUPABASE_ANON_KEY);
