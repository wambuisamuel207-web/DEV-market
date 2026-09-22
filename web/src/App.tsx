import React, { useState, useEffect } from 'react';
import { supabase } from './supabaseClient';
import { User, Project, Milestone, EscrowTransaction, Dispute } from './types';
import {
  ShieldCheck,
  Lock,
  CheckCircle2,
  Clock,
  AlertTriangle,
  ExternalLink,
  ChevronRight,
  Plus,
  RefreshCw,
  Search,
  DollarSign,
  FileCode,
  Sliders,
  Send,
  HelpCircle,
  Eye,
  LogOut,
  Database
} from 'lucide-react';

export default function App() {
  // Current user state
  const [currentUser, setCurrentUser] = useState<User>({
    id: 'usr_client_1',
    email: 'client@devmarket.io',
    full_name: 'Sarah Jenkins (TechVentures)',
    role: 'client',
    paypal_connected: true,
    paypal_merchant_id: 'PMR-CLIENT-98'
  });

  const [activeTab, setActiveTab] = useState<'dashboard' | 'projects' | 'disputes' | 'legal'>('dashboard');
  const [projects, setProjects] = useState<Project[]>([]);
  const [milestones, setMilestones] = useState<Milestone[]>([]);
  const [selectedProjectId, setSelectedProjectId] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');

  // Modals & form state
  const [isSubmittingDeliverable, setIsSubmittingDeliverable] = useState<Milestone | null>(null);
  const [deliverableNotes, setDeliverableNotes] = useState('');
  const [deliverableUrl, setDeliverableUrl] = useState('');
  const [isFundingMilestone, setIsFundingMilestone] = useState<Milestone | null>(null);
  const [isDisputeModalOpen, setIsDisputeModalOpen] = useState<Milestone | null>(null);
  const [disputeReason, setDisputeReason] = useState('');

  // Notification banner
  const [bannerMessage, setBannerMessage] = useState<string | null>(null);

  // Load data from Supabase
  const loadSupabaseData = async () => {
    setLoading(true);
    try {
      const { data: prjData, error: prjErr } = await supabase.from('projects').select('*');
      const { data: mlsData, error: mlsErr } = await supabase.from('milestones').select('*').order('order_index');

      if (prjData && prjData.length > 0) {
        setProjects(prjData);
        if (!selectedProjectId) setSelectedProjectId(prjData[0].id);
      } else {
        // Fallback default seed for instant web preview
        const fallbackProjects: Project[] = [
          {
            id: 'prj_seed_01',
            title: 'Fintech Cloud Escrow Integration',
            description: 'Complete PayPal v2 Delayed Disbursement backend with automatic escrow fund holding and dispute mediation.',
            client_id: 'usr_client_1',
            developer_id: 'usr_dev_1',
            total_budget: 3500,
            status: 'ACTIVE',
            created_at: Date.now() - 86400000 * 5
          }
        ];
        setProjects(fallbackProjects);
        setSelectedProjectId(fallbackProjects[0].id);
      }

      if (mlsData && mlsData.length > 0) {
        setMilestones(mlsData);
      } else {
        // Fallback milestones
        setMilestones([
          {
            id: 'mls_01',
            project_id: 'prj_seed_01',
            title: 'Architecture Specification & Security Plan',
            description: 'Define threat model, webhook HMAC verification, and PayPal vault credentials.',
            amount: 800,
            commission_amount: 80,
            status: 'RELEASED',
            order_index: 1,
            submission_notes: 'Completed security audit and OAuth architecture diagrams.',
            submission_url: 'https://github.com/example/docs/security.md'
          },
          {
            id: 'mls_02',
            project_id: 'prj_seed_01',
            title: 'Smart Contract / PostgREST Webhook Sync',
            description: 'Implement backend listeners for CHECKOUT.ORDER.APPROVED and capture execution.',
            amount: 1200,
            commission_amount: 120,
            status: 'UNDER_REVIEW',
            order_index: 2,
            submission_notes: 'Webhook listener live on AWS Lambda with signature checking.',
            submission_url: 'https://github.com/example/escrow-webhook'
          },
          {
            id: 'mls_03',
            project_id: 'prj_seed_01',
            title: 'QA & Admin Dispute Mediation Module',
            description: 'End-to-end stress testing with simulated disputed payouts and partial settlement.',
            amount: 1500,
            commission_amount: 150,
            status: 'FUNDED',
            order_index: 3
          }
        ]);
      }
    } catch (e) {
      console.error('Error fetching Supabase data:', e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadSupabaseData();
  }, []);

  // Quick Role Switching for demo testing
  const switchRole = (newRole: 'client' | 'developer' | 'admin') => {
    if (newRole === 'client') {
      setCurrentUser({
        id: 'usr_client_1',
        email: 'client@devmarket.io',
        full_name: 'Sarah Jenkins (TechVentures)',
        role: 'client',
        paypal_connected: true,
        paypal_merchant_id: 'PMR-CLIENT-98'
      });
    } else if (newRole === 'developer') {
      setCurrentUser({
        id: 'usr_dev_1',
        email: 'alex.rivera@devmarket.io',
        full_name: 'Alex Rivera (Lead Editor & Dev)',
        role: 'developer',
        paypal_connected: true,
        paypal_merchant_id: 'PMR-DEV-PAYPAL-44'
      });
    } else {
      setCurrentUser({
        id: 'usr_admin_1',
        email: 'samuelgitau76@gmail.com',
        full_name: 'Samuel Gitau (Platform Admin)',
        role: 'admin',
        paypal_connected: true,
        paypal_merchant_id: 'PMR-ADMIN-01'
      });
    }
    showBanner(`Switched perspective to ${newRole.toUpperCase()}`);
  };

  const showBanner = (msg: string) => {
    setBannerMessage(msg);
    setTimeout(() => setBannerMessage(null), 4000);
  };

  // Milestone Actions
  const handleFundEscrow = async (mls: Milestone) => {
    const updated = milestones.map(m => m.id === mls.id ? { ...m, status: 'FUNDED' as const } : m);
    setMilestones(updated);
    setIsFundingMilestone(null);
    showBanner(`Secured $${mls.amount} in PayPal Escrow (Delayed Disbursement Hold active).`);

    // Sync to Supabase
    await supabase.from('milestones').update({ status: 'FUNDED' }).eq('id', mls.id);
  };

  const handleSubmitDeliverable = async () => {
    if (!isSubmittingDeliverable) return;
    const updated = milestones.map(m =>
      m.id === isSubmittingDeliverable.id
        ? {
            ...m,
            status: 'UNDER_REVIEW' as const,
            submission_notes: deliverableNotes,
            submission_url: deliverableUrl,
            submitted_at: Date.now()
          }
        : m
    );
    setMilestones(updated);
    showBanner(`Deliverables submitted for milestone: "${isSubmittingDeliverable.title}". Sent to client for review.`);
    setIsSubmittingDeliverable(null);
    setDeliverableNotes('');
    setDeliverableUrl('');

    // Sync to Supabase
    await supabase.from('milestones').update({
      status: 'UNDER_REVIEW',
      submission_notes: deliverableNotes,
      submission_url: deliverableUrl
    }).eq('id', isSubmittingDeliverable.id);
  };

  const handleReleaseFunds = async (mls: Milestone) => {
    const updated = milestones.map(m => m.id === mls.id ? { ...m, status: 'RELEASED' as const, released_at: Date.now() } : m);
    setMilestones(updated);
    const devNet = mls.amount * 0.9;
    const fee = mls.amount * 0.1;
    showBanner(`Approved! Released $${devNet} to developer PayPal account. $${fee} retained in platform commission.`);

    // Sync to Supabase
    await supabase.from('milestones').update({ status: 'RELEASED' }).eq('id', mls.id);
  };

  const handleOpenDispute = async () => {
    if (!isDisputeModalOpen) return;
    const updated = milestones.map(m => m.id === isDisputeModalOpen.id ? { ...m, status: 'DISPUTED' as const } : m);
    setMilestones(updated);
    showBanner(`Dispute lodged for "${isDisputeModalOpen.title}". Sent to platform admin for mediation.`);
    setIsDisputeModalOpen(null);
    setDisputeReason('');

    // Sync to Supabase
    await supabase.from('milestones').update({ status: 'DISPUTED' }).eq('id', isDisputeModalOpen.id);
  };

  // Filtered milestones
  const activeMilestones = milestones.filter(m => {
    const matchesProject = selectedProjectId ? m.project_id === selectedProjectId : true;
    const matchesStatus = statusFilter === 'ALL' || m.status === statusFilter;
    const matchesSearch = !searchQuery || m.title.toLowerCase().includes(searchQuery.toLowerCase());
    return matchesProject && matchesStatus && matchesSearch;
  });

  const totalEscrowHeld = milestones.filter(m => m.status === 'FUNDED' || m.status === 'UNDER_REVIEW').reduce((acc, m) => acc + m.amount, 0);
  const totalReleased = milestones.filter(m => m.status === 'RELEASED').reduce((acc, m) => acc + m.amount, 0);

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col font-sans">
      {/* Top Banner Notice */}
      {bannerMessage && (
        <div className="bg-emerald-600 text-white text-xs font-semibold py-2 px-4 text-center flex items-center justify-center gap-2 animate-pulse">
          <CheckCircle2 className="w-4 h-4" />
          <span>{bannerMessage}</span>
        </div>
      )}

      {/* Main Navigation Header */}
      <header className="border-b border-slate-800 bg-slate-900/90 backdrop-blur sticky top-0 z-40 px-6 py-3.5 flex items-center justify-between">
        <div className="flex items-center gap-6">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-lg bg-emerald-500/20 border border-emerald-500 flex items-center justify-center text-emerald-400 font-black">
              DM
            </div>
            <div>
              <div className="font-extrabold text-sm tracking-tight text-white flex items-center gap-2">
                DevMarket Web
                <span className="text-[10px] px-1.5 py-0.5 rounded bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 font-bold uppercase">
                  Connected to Supabase
                </span>
              </div>
              <div className="text-[11px] text-slate-400">Freelance PayPal Escrow & Project Hub</div>
            </div>
          </div>

          {/* Navigation Links */}
          <nav className="hidden md:flex items-center gap-1 ml-4">
            <button
              onClick={() => setActiveTab('dashboard')}
              className={`px-3 py-1.5 rounded-md text-xs font-medium transition ${
                activeTab === 'dashboard' ? 'bg-slate-800 text-white' : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              Pipeline Dashboard
            </button>
            <button
              onClick={() => setActiveTab('legal')}
              className={`px-3 py-1.5 rounded-md text-xs font-medium transition ${
                activeTab === 'legal' ? 'bg-slate-800 text-white' : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              Privacy & Legal
            </button>
          </nav>
        </div>

        {/* User Role Switcher & Profile */}
        <div className="flex items-center gap-3">
          <div className="flex items-center bg-slate-950 p-1 rounded-lg border border-slate-800 text-xs">
            <button
              onClick={() => switchRole('client')}
              className={`px-2.5 py-1 rounded font-medium transition ${
                currentUser.role === 'client' ? 'bg-sky-600 text-white' : 'text-slate-400 hover:text-white'
              }`}
            >
              Client
            </button>
            <button
              onClick={() => switchRole('developer')}
              className={`px-2.5 py-1 rounded font-medium transition ${
                currentUser.role === 'developer' ? 'bg-emerald-600 text-white' : 'text-slate-400 hover:text-white'
              }`}
            >
              Editor / Dev
            </button>
            <button
              onClick={() => switchRole('admin')}
              className={`px-2.5 py-1 rounded font-medium transition ${
                currentUser.role === 'admin' ? 'bg-purple-600 text-white' : 'text-slate-400 hover:text-white'
              }`}
            >
              Admin
            </button>
          </div>

          <div className="hidden sm:flex items-center gap-2 pl-2 border-l border-slate-800">
            <div className="w-7 h-7 rounded-full bg-slate-800 border border-slate-700 flex items-center justify-center text-xs font-bold text-slate-300">
              {currentUser.full_name[0]}
            </div>
            <div className="text-right">
              <div className="text-xs font-semibold text-slate-200 leading-tight">{currentUser.full_name}</div>
              <div className="text-[10px] text-emerald-400 font-mono">PayPal Verified</div>
            </div>
          </div>
        </div>
      </header>

      {/* Main Body Content */}
      <main className="flex-1 max-w-7xl w-full mx-auto p-6 space-y-6">
        {activeTab === 'dashboard' ? (
          <>
            {/* Top Stat Summary Cards */}
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
              <div className="bg-slate-900 border border-emerald-500/30 rounded-xl p-4 flex items-center justify-between">
                <div>
                  <div className="text-xs font-medium text-slate-400 flex items-center gap-1.5">
                    <Lock className="w-3.5 h-3.5 text-emerald-400" />
                    Funds in Escrow Hold
                  </div>
                  <div className="text-2xl font-black text-emerald-400 mt-1">${totalEscrowHeld.toLocaleString()}</div>
                  <div className="text-[11px] text-slate-500">PayPal delayed disbursement</div>
                </div>
                <div className="w-10 h-10 rounded-full bg-emerald-500/10 flex items-center justify-center text-emerald-400">
                  <ShieldCheck className="w-5 h-5" />
                </div>
              </div>

              <div className="bg-slate-900 border border-slate-800 rounded-xl p-4 flex items-center justify-between">
                <div>
                  <div className="text-xs font-medium text-slate-400 flex items-center gap-1.5">
                    <Clock className="w-3.5 h-3.5 text-amber-400" />
                    Deliverables In Review
                  </div>
                  <div className="text-2xl font-black text-amber-400 mt-1">
                    {milestones.filter(m => m.status === 'UNDER_REVIEW').length}
                  </div>
                  <div className="text-[11px] text-slate-500">Awaiting client inspection</div>
                </div>
                <div className="w-10 h-10 rounded-full bg-amber-500/10 flex items-center justify-center text-amber-400">
                  <Clock className="w-5 h-5" />
                </div>
              </div>

              <div className="bg-slate-900 border border-slate-800 rounded-xl p-4 flex items-center justify-between">
                <div>
                  <div className="text-xs font-medium text-slate-400 flex items-center gap-1.5">
                    <CheckCircle2 className="w-3.5 h-3.5 text-sky-400" />
                    Total Payouts Released
                  </div>
                  <div className="text-2xl font-black text-sky-400 mt-1">${totalReleased.toLocaleString()}</div>
                  <div className="text-[11px] text-slate-500">Disbursed to developers</div>
                </div>
                <div className="w-10 h-10 rounded-full bg-sky-500/10 flex items-center justify-center text-sky-400">
                  <DollarSign className="w-5 h-5" />
                </div>
              </div>
            </div>

            {/* Escrow Guarantee Banner */}
            <div className="bg-gradient-to-r from-emerald-950/40 via-slate-900 to-slate-900 border border-emerald-500/20 rounded-xl p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
              <div className="flex items-start gap-3">
                <ShieldCheck className="w-5 h-5 text-emerald-400 shrink-0 mt-0.5" />
                <div className="text-xs">
                  <span className="font-bold text-white">PayPal Delayed Disbursement Protection:</span>
                  <span className="text-slate-300 ml-1">
                    Clients pre-fund work into escrow. Developers begin work guaranteed that payment is locked. Funds are only disbursed once deliverables are approved.
                  </span>
                </div>
              </div>
              <button
                onClick={loadSupabaseData}
                className="self-start sm:self-auto flex items-center gap-1.5 px-3 py-1.5 bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs rounded-lg transition border border-slate-700"
              >
                <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
                Sync Cloud
              </button>
            </div>

            {/* Filter and Search Bar */}
            <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3">
              {/* Status Chips */}
              <div className="flex items-center gap-1.5 overflow-x-auto pb-1 sm:pb-0">
                {['ALL', 'FUNDED', 'UNDER_REVIEW', 'UNFUNDED', 'RELEASED', 'DISPUTED'].map((st) => (
                  <button
                    key={st}
                    onClick={() => setStatusFilter(st)}
                    className={`px-3 py-1.5 rounded-lg text-xs font-semibold whitespace-nowrap transition ${
                      statusFilter === st
                        ? 'bg-slate-800 text-emerald-400 border border-emerald-500/40'
                        : 'bg-slate-900 text-slate-400 border border-slate-800 hover:text-slate-200'
                    }`}
                  >
                    {st === 'ALL' ? 'All Milestones' : st.replace('_', ' ')}
                  </button>
                ))}
              </div>

              {/* Search input */}
              <div className="relative w-full sm:w-64">
                <Search className="w-4 h-4 text-slate-500 absolute left-3 top-2.5" />
                <input
                  type="text"
                  placeholder="Filter milestones..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="w-full bg-slate-900 border border-slate-800 rounded-lg pl-9 pr-3 py-1.5 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-emerald-500"
                />
              </div>
            </div>

            {/* Milestones Pipeline List */}
            <div className="space-y-3">
              {activeMilestones.length === 0 ? (
                <div className="bg-slate-900/60 border border-slate-800 rounded-xl p-8 text-center text-slate-400 text-xs">
                  No milestones found matching your filter criteria.
                </div>
              ) : (
                activeMilestones.map((milestone) => (
                  <div
                    key={milestone.id}
                    className="bg-slate-900 border border-slate-800 hover:border-slate-700 rounded-xl p-5 transition space-y-4 shadow-sm"
                  >
                    <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
                      <div className="flex items-center gap-3">
                        <span className="text-xs font-mono font-bold text-slate-500">#{milestone.order_index}</span>
                        <h3 className="font-bold text-sm text-white">{milestone.title}</h3>
                      </div>

                      {/* Status Badges */}
                      <div>
                        {milestone.status === 'FUNDED' && (
                          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-md text-[11px] font-bold bg-emerald-500/15 text-emerald-400 border border-emerald-500/30">
                            <Lock className="w-3 h-3" /> ESCROW SECURED
                          </span>
                        )}
                        {milestone.status === 'UNDER_REVIEW' && (
                          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-md text-[11px] font-bold bg-amber-500/15 text-amber-400 border border-amber-500/30">
                            <Clock className="w-3 h-3" /> UNDER CLIENT REVIEW
                          </span>
                        )}
                        {milestone.status === 'RELEASED' && (
                          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-md text-[11px] font-bold bg-sky-500/15 text-sky-400 border border-sky-500/30">
                            <CheckCircle2 className="w-3 h-3" /> PAYOUT RELEASED
                          </span>
                        )}
                        {milestone.status === 'UNFUNDED' && (
                          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-md text-[11px] font-bold bg-slate-800 text-slate-400 border border-slate-700">
                            UNFUNDED
                          </span>
                        )}
                        {milestone.status === 'DISPUTED' && (
                          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-md text-[11px] font-bold bg-rose-500/15 text-rose-400 border border-rose-500/30">
                            <AlertTriangle className="w-3 h-3" /> DISPUTED
                          </span>
                        )}
                      </div>
                    </div>

                    {milestone.description && (
                      <p className="text-xs text-slate-400 leading-relaxed">{milestone.description}</p>
                    )}

                    {/* Deliverable details if present */}
                    {milestone.submission_notes && (
                      <div className="bg-slate-950 border border-slate-800 rounded-lg p-3 text-xs space-y-1">
                        <div className="text-[11px] font-bold text-slate-300 flex items-center gap-1.5">
                          <FileCode className="w-3.5 h-3.5 text-sky-400" />
                          Submitted Deliverables by Developer:
                        </div>
                        <div className="text-slate-400">{milestone.submission_notes}</div>
                        {milestone.submission_url && (
                          <a
                            href={milestone.submission_url}
                            target="_blank"
                            rel="noreferrer"
                            className="inline-flex items-center gap-1 text-sky-400 hover:underline pt-1 text-[11px]"
                          >
                            <span>View External Deliverables</span>
                            <ExternalLink className="w-3 h-3" />
                          </a>
                        )}
                      </div>
                    )}

                    {/* Financial split strip & Action Buttons */}
                    <div className="pt-3 border-t border-slate-800/80 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                      <div className="flex items-center gap-5 text-xs">
                        <div>
                          <span className="text-slate-500 text-[10px] block">TOTAL ESCROW</span>
                          <span className="font-bold text-white text-sm">${milestone.amount}</span>
                        </div>
                        <div>
                          <span className="text-slate-500 text-[10px] block">DEV NET (90%)</span>
                          <span className="font-bold text-emerald-400 text-sm">${milestone.amount * 0.9}</span>
                        </div>
                        <div>
                          <span className="text-slate-500 text-[10px] block">FEE (10%)</span>
                          <span className="font-semibold text-slate-400 text-sm">${milestone.amount * 0.1}</span>
                        </div>
                      </div>

                      {/* Role Actions */}
                      <div className="flex items-center gap-2 flex-wrap">
                        {/* Client Actions */}
                        {currentUser.role === 'client' && (
                          <>
                            {milestone.status === 'UNFUNDED' && (
                              <button
                                onClick={() => handleFundEscrow(milestone)}
                                className="px-3.5 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-bold rounded-lg transition shadow-sm flex items-center gap-1.5"
                              >
                                <Lock className="w-3.5 h-3.5" />
                                Fund Escrow with PayPal (${milestone.amount})
                              </button>
                            )}

                            {milestone.status === 'UNDER_REVIEW' && (
                              <>
                                <button
                                  onClick={() => handleReleaseFunds(milestone)}
                                  className="px-3.5 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-bold rounded-lg transition shadow-sm flex items-center gap-1.5"
                                >
                                  <CheckCircle2 className="w-3.5 h-3.5" />
                                  Approve & Release Funds
                                </button>
                                <button
                                  onClick={() => setIsDisputeModalOpen(milestone)}
                                  className="px-3 py-1.5 bg-slate-800 hover:bg-slate-700 text-rose-400 text-xs font-medium rounded-lg transition border border-rose-500/20"
                                >
                                  Open Dispute
                                </button>
                              </>
                            )}
                          </>
                        )}

                        {/* Developer Actions */}
                        {currentUser.role === 'developer' && (
                          <>
                            {milestone.status === 'FUNDED' && (
                              <button
                                onClick={() => setIsSubmittingDeliverable(milestone)}
                                className="px-3.5 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-bold rounded-lg transition shadow-sm flex items-center gap-1.5"
                              >
                                <Send className="w-3.5 h-3.5" />
                                Submit Work Deliverables
                              </button>
                            )}
                            {milestone.status === 'UNFUNDED' && (
                              <span className="text-[11px] text-slate-500 italic">Awaiting client PayPal funding</span>
                            )}
                          </>
                        )}

                        {/* Admin Actions */}
                        {currentUser.role === 'admin' && milestone.status === 'DISPUTED' && (
                          <div className="flex items-center gap-2">
                            <button
                              onClick={() => handleReleaseFunds(milestone)}
                              className="px-3 py-1.5 bg-purple-600 hover:bg-purple-500 text-white text-xs font-bold rounded-lg transition"
                            >
                              Settle to Developer
                            </button>
                            <button
                              onClick={() => {
                                const updated = milestones.map(m => m.id === milestone.id ? { ...m, status: 'UNFUNDED' as const } : m);
                                setMilestones(updated);
                                showBanner(`Refunded $${milestone.amount} back to client PayPal.`);
                              }}
                              className="px-3 py-1.5 bg-slate-800 text-slate-200 text-xs font-medium rounded-lg hover:bg-slate-700"
                            >
                              Refund Client
                            </button>
                          </div>
                        )}
                      </div>
                    </div>
                  </div>
                ))
              )}
            </div>
          </>
        ) : (
          /* Legal & Privacy Policy View */
          <div className="bg-slate-900 border border-slate-800 rounded-xl p-8 max-w-4xl mx-auto space-y-6 text-xs text-slate-300">
            <div>
              <h2 className="text-xl font-bold text-white">DevMarket Legal & Privacy Policy</h2>
              <p className="text-slate-500 text-[11px] mt-1">Effective Date: September 2026</p>
            </div>

            <div className="space-y-4">
              <section className="space-y-1.5">
                <h3 className="text-sm font-semibold text-white">1. Delayed Disbursement & PayPal Escrow Protection</h3>
                <p>
                  DevMarket utilizes PayPal v2 Orders with Delayed Disbursement intent. Funds authorized by the Client are securely retained in an escrow account until milestones are marked as submitted and explicitly accepted by the Client or resolved via administrative arbitration.
                </p>
              </section>

              <section className="space-y-1.5">
                <h3 className="text-sm font-semibold text-white">2. Platform Commission & Payouts</h3>
                <p>
                  A standardized 10% platform facilitation fee is retained upon milestone release to maintain dispute arbitration, webhook guarantees, and fraud prevention. 90% of the funds are disbursed directly to the developer's connected PayPal account.
                </p>
              </section>

              <section className="space-y-1.5">
                <h3 className="text-sm font-semibold text-white">3. Information We Collect & Data Retention</h3>
                <p>
                  We store user identification details, PayPal Merchant IDs, project deliverables, and transaction hash logs in accordance with PCI-DSS guidelines. We never store raw credit card numbers or banking passwords.
                </p>
              </section>
            </div>
          </div>
        )}
      </main>

      {/* Deliverable Submission Modal */}
      {isSubmittingDeliverable && (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 max-w-md w-full space-y-4">
            <h3 className="font-bold text-sm text-white">Submit Work Deliverables</h3>
            <p className="text-xs text-slate-400">
              Provide notes and a link to your deliverables (e.g. GitHub repository, Google Drive, Figma, or Loom video) for client review.
            </p>

            <div className="space-y-2">
              <label className="text-[11px] font-semibold text-slate-300">Deliverable Notes & Summary</label>
              <textarea
                rows={3}
                value={deliverableNotes}
                onChange={(e) => setDeliverableNotes(e.target.value)}
                placeholder="Explain the changes made and tests performed..."
                className="w-full bg-slate-950 border border-slate-800 rounded-lg p-2.5 text-xs text-white focus:outline-none focus:border-emerald-500"
              />
            </div>

            <div className="space-y-2">
              <label className="text-[11px] font-semibold text-slate-300">Deliverable URL (optional)</label>
              <input
                type="url"
                value={deliverableUrl}
                onChange={(e) => setDeliverableUrl(e.target.value)}
                placeholder="https://github.com/org/repo/pull/12"
                className="w-full bg-slate-950 border border-slate-800 rounded-lg p-2.5 text-xs text-white focus:outline-none focus:border-emerald-500"
              />
            </div>

            <div className="flex items-center justify-end gap-2 pt-2">
              <button
                onClick={() => setIsSubmittingDeliverable(null)}
                className="px-3 py-1.5 bg-slate-800 text-slate-300 text-xs rounded-lg hover:bg-slate-700"
              >
                Cancel
              </button>
              <button
                onClick={handleSubmitDeliverable}
                disabled={!deliverableNotes.trim()}
                className="px-4 py-1.5 bg-emerald-600 hover:bg-emerald-500 disabled:opacity-50 text-white font-bold text-xs rounded-lg"
              >
                Send for Review
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Dispute Modal */}
      {isDisputeModalOpen && (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 max-w-md w-full space-y-4">
            <h3 className="font-bold text-sm text-white flex items-center gap-2">
              <AlertTriangle className="w-4 h-4 text-rose-500" />
              Open Escrow Dispute
            </h3>
            <p className="text-xs text-slate-400">
              Escrow funds for "{isDisputeModalOpen.title}" will be frozen immediately and transferred to admin arbitration.
            </p>

            <div className="space-y-2">
              <label className="text-[11px] font-semibold text-slate-300">Reason for Dispute</label>
              <textarea
                rows={3}
                value={disputeReason}
                onChange={(e) => setDisputeReason(e.target.value)}
                placeholder="Describe missing deliverables or requirements..."
                className="w-full bg-slate-950 border border-slate-800 rounded-lg p-2.5 text-xs text-white focus:outline-none focus:border-rose-500"
              />
            </div>

            <div className="flex items-center justify-end gap-2 pt-2">
              <button
                onClick={() => setIsDisputeModalOpen(null)}
                className="px-3 py-1.5 bg-slate-800 text-slate-300 text-xs rounded-lg hover:bg-slate-700"
              >
                Cancel
              </button>
              <button
                onClick={handleOpenDispute}
                disabled={!disputeReason.trim()}
                className="px-4 py-1.5 bg-rose-600 hover:bg-rose-500 disabled:opacity-50 text-white font-bold text-xs rounded-lg"
              >
                File Dispute
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Footer */}
      <footer className="border-t border-slate-800 py-4 px-6 text-center text-[11px] text-slate-500">
        DevMarket Multiplatform • Android App & Web Frontend synchronized via Supabase Cloud
      </footer>
    </div>
  );
}
