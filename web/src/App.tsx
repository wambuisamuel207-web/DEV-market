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
  Database,
  MessageSquare,
  Check
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

  const [activeTab, setActiveTab] = useState<'work_review' | 'communication' | 'escrow' | 'dashboard' | 'legal'>('work_review');
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

  // 1-on-1 Communication Portal State
  const [isCommunicationPortalOpen, setIsCommunicationPortalOpen] = useState(false);
  const [messages, setMessages] = useState<Array<{ id: string; sender: string; role: 'client' | 'developer'; text: string; time: string }>>([
    { id: '1', sender: 'Sarah Jenkins', role: 'client', text: 'Hi Alex, I have funded Milestone 1 & 2 into escrow with delayed disbursement. Please submit your work deliverables once ready.', time: '10:15 AM' },
    { id: '2', sender: 'Alex Rivera', role: 'developer', text: 'Thanks Sarah! The architecture specification and smart contract code are ready. I have submitted the repository links for your review.', time: '11:42 AM' },
    { id: '3', sender: 'Sarah Jenkins', role: 'client', text: 'Great! Inspecting the test coverage and demo link now.', time: '1:05 PM' }
  ]);
  const [chatInputText, setChatInputText] = useState('');

  // PayPal Details Modal State
  const [isPayPalModalOpen, setIsPayPalModalOpen] = useState(false);
  const [paypalEmailInput, setPaypalEmailInput] = useState(currentUser.email);
  const [paypalMerchantInput, setPaypalMerchantInput] = useState(currentUser.paypal_merchant_id || 'PMR-CLIENT-98');

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
      setPaypalEmailInput('client@devmarket.io');
      setPaypalMerchantInput('PMR-CLIENT-98');
    } else if (newRole === 'developer') {
      setCurrentUser({
        id: 'usr_dev_1',
        email: 'alex.rivera@devmarket.io',
        full_name: 'Alex Rivera (Lead Editor & Dev)',
        role: 'developer',
        paypal_connected: true,
        paypal_merchant_id: 'PMR-DEV-PAYPAL-44'
      });
      setPaypalEmailInput('alex.rivera@devmarket.io');
      setPaypalMerchantInput('PMR-DEV-PAYPAL-44');
    } else {
      setCurrentUser({
        id: 'usr_admin_1',
        email: 'samuelgitau76@gmail.com',
        full_name: 'Samuel Gitau (Platform Admin)',
        role: 'admin',
        paypal_connected: true,
        paypal_merchant_id: 'PMR-ADMIN-01'
      });
      setPaypalEmailInput('samuelgitau76@gmail.com');
      setPaypalMerchantInput('PMR-ADMIN-01');
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

    try {
      await supabase.from('milestones').update({ status: 'FUNDED' }).eq('id', mls.id);
    } catch (e) {
      console.warn('Supabase sync skipped');
    }
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
    showBanner(`Deliverables submitted for: "${isSubmittingDeliverable.title}". Sent to client for review.`);
    setIsSubmittingDeliverable(null);
    setDeliverableNotes('');
    setDeliverableUrl('');

    try {
      await supabase.from('milestones').update({
        status: 'UNDER_REVIEW',
        submission_notes: deliverableNotes,
        submission_url: deliverableUrl
      }).eq('id', isSubmittingDeliverable.id);
    } catch (e) {
      console.warn('Supabase sync skipped');
    }
  };

  const handleReleaseFunds = async (mls: Milestone) => {
    if (currentUser.role !== 'client' && currentUser.role !== 'admin') {
      showBanner('Error: Only Clients can approve deliverables and release funds.');
      return;
    }
    const updated = milestones.map(m => m.id === mls.id ? { ...m, status: 'RELEASED' as const, released_at: Date.now() } : m);
    setMilestones(updated);
    const devNet = mls.amount * 0.9;
    const fee = mls.amount * 0.1;
    showBanner(`Approved! Released $${devNet} to developer PayPal account. $${fee} retained in platform commission.`);

    try {
      await supabase.from('milestones').update({ status: 'RELEASED' }).eq('id', mls.id);
    } catch (e) {
      console.warn('Supabase sync skipped');
    }
  };

  const handleOpenDispute = async () => {
    if (!isDisputeModalOpen) return;
    const updated = milestones.map(m => m.id === isDisputeModalOpen.id ? { ...m, status: 'DISPUTED' as const } : m);
    setMilestones(updated);
    showBanner(`Dispute lodged for "${isDisputeModalOpen.title}". Sent to platform admin for mediation.`);
    setIsDisputeModalOpen(null);
    setDisputeReason('');

    try {
      await supabase.from('milestones').update({ status: 'DISPUTED' }).eq('id', isDisputeModalOpen.id);
    } catch (e) {
      console.warn('Supabase sync skipped');
    }
  };

  // Send message in 1-on-1 portal
  const handleSendMessage = () => {
    if (!chatInputText.trim()) return;
    const newMsg = {
      id: String(Date.now()),
      sender: currentUser.full_name,
      role: (currentUser.role === 'developer' ? 'developer' : 'client') as 'client' | 'developer',
      text: chatInputText.trim(),
      time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    };
    setMessages(prev => [...prev, newMsg]);
    setChatInputText('');
    showBanner('Message posted to Public 1-on-1 Portal');
  };

  // Save PayPal Details
  const handleSavePayPalDetails = () => {
    setCurrentUser(prev => ({
      ...prev,
      email: paypalEmailInput,
      paypal_merchant_id: paypalMerchantInput,
      paypal_connected: true
    }));
    setIsPayPalModalOpen(false);
    showBanner(`PayPal details saved: ${paypalEmailInput} (${paypalMerchantInput})`);
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

      {/* Header */}
      <header className="border-b border-slate-800 bg-slate-900/90 backdrop-blur sticky top-0 z-40 px-6 py-3 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="flex items-center gap-2 cursor-pointer" onClick={() => setActiveTab('work_review')}>
            <div className="w-8 h-8 rounded-lg bg-slate-950 border border-emerald-500 flex items-center justify-center text-emerald-400">
              <ShieldCheck className="w-5 h-5" />
            </div>
            <div>
              <div className="flex items-center gap-1.5">
                <span className="font-black text-sm tracking-tight text-white">DevMarket</span>
                <span className="w-1.5 h-1.5 rounded-full bg-emerald-500"></span>
              </div>
              <div className="text-[10px] text-slate-400 leading-tight">Delayed Disbursement Escrow</div>
            </div>
          </div>

          {/* Navigation Tabs */}
          <nav className="hidden md:flex items-center gap-1 ml-4">
            <button
              onClick={() => setActiveTab('work_review')}
              className={`px-3 py-1.5 rounded-md text-xs font-medium transition ${
                activeTab === 'work_review' ? 'bg-sky-600 text-white' : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              Work Showcase & Review
            </button>
            <button
              onClick={() => setActiveTab('communication')}
              className={`px-3 py-1.5 rounded-md text-xs font-medium transition ${
                activeTab === 'communication' ? 'bg-emerald-600 text-white' : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              1-on-1 Chat Portal
            </button>
            <button
              onClick={() => setActiveTab('escrow')}
              className={`px-3 py-1.5 rounded-md text-xs font-medium transition ${
                activeTab === 'escrow' ? 'bg-indigo-600 text-white' : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              Escrow & Approvals
            </button>
            <button
              onClick={() => setActiveTab('dashboard')}
              className={`px-3 py-1.5 rounded-md text-xs font-medium transition ${
                activeTab === 'dashboard' ? 'bg-slate-800 text-white' : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              Pipeline Overview
            </button>
            <button
              onClick={() => setActiveTab('legal')}
              className={`px-3 py-1.5 rounded-md text-xs font-medium transition ${
                activeTab === 'legal' ? 'bg-slate-800 text-white' : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              Legal & Policy
            </button>
          </nav>
        </div>

        {/* User Role Switcher, PayPal Status & Profile */}
        <div className="flex items-center gap-3">
          {/* PayPal Status Button */}
          <button
            onClick={() => setIsPayPalModalOpen(true)}
            className="hidden sm:flex items-center gap-1.5 px-2.5 py-1 rounded-lg bg-slate-900 border border-sky-500/40 text-[11px] text-sky-400 hover:bg-slate-800 transition"
          >
            <DollarSign className="w-3.5 h-3.5" />
            <span>PayPal Setup</span>
          </button>

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
              Developer
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
              <div className="text-[10px] text-emerald-400 font-mono">
                {currentUser.role === 'client' ? 'Client Authority' : 'Dev Contractor'}
              </div>
            </div>
          </div>
        </div>
      </header>

      {/* Main Body Content */}
      <main className="flex-1 max-w-7xl w-full mx-auto p-6 space-y-6">

        {/* PAYPAL GUIDANCE BANNER (Answers: 'if i am needed to fill paypal details tell me') */}
        <div className="bg-slate-900 border border-sky-500/30 rounded-xl p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-3 shadow-sm">
          <div className="flex items-start gap-3">
            <div className="w-9 h-9 rounded-lg bg-sky-500/15 flex items-center justify-center text-sky-400 shrink-0 mt-0.5">
              <HelpCircle className="w-5 h-5" />
            </div>
            <div>
              <div className="text-xs font-bold text-white flex items-center gap-2">
                <span>Do you need to fill PayPal details?</span>
                <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-emerald-500/15 text-emerald-400 border border-emerald-500/30">
                  {currentUser.role === 'developer' ? 'YES (For 90% Payouts)' : 'YES (For Escrow Funding)'}
                </span>
              </div>
              <p className="text-xs text-slate-300 mt-1 leading-relaxed">
                {currentUser.role === 'developer'
                  ? 'Developers must enter their PayPal email or Merchant ID to automatically receive delayed disbursement milestone earnings upon client review approval.'
                  : 'Clients need a linked PayPal account or Merchant ID to fund escrow contracts held securely with delayed disbursement.'}
              </p>
            </div>
          </div>
          <button
            onClick={() => setIsPayPalModalOpen(true)}
            className="px-3.5 py-1.5 bg-sky-600 hover:bg-sky-500 text-white text-xs font-bold rounded-lg transition whitespace-nowrap"
          >
            Manage PayPal Details
          </button>
        </div>

        {/* PANEL 1: WORK SHOWCASE & DELIVERABLES REVIEW */}
        {activeTab === 'work_review' && (
          <div className="space-y-4">
            <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 space-y-3">
              <div className="flex items-center justify-between">
                <div>
                  <h2 className="text-base font-bold text-white flex items-center gap-2">
                    <FileCode className="w-5 h-5 text-sky-400" />
                    Work Showcase & Deliverables Review
                  </h2>
                  <p className="text-xs text-slate-400 mt-0.5">
                    Developers showcase their code, demos, and release notes • Clients review and approve
                  </p>
                </div>
                <div className={`px-2.5 py-1 rounded text-xs font-bold border ${
                  currentUser.role === 'client'
                    ? 'bg-sky-500/15 text-sky-400 border-sky-500/40'
                    : 'bg-amber-500/15 text-amber-400 border-amber-500/40'
                }`}>
                  {currentUser.role === 'client' ? 'Active Reviewer (Client)' : 'Showcase Mode (Dev)'}
                </div>
              </div>

              {/* STRICT PERMISSION ENFORCEMENT NOTICE */}
              <div className={`p-3 rounded-lg border text-xs flex items-center gap-2.5 ${
                currentUser.role === 'client'
                  ? 'bg-sky-950/40 border-sky-500/30 text-sky-300'
                  : 'bg-amber-950/40 border-amber-500/30 text-amber-300'
              }`}>
                {currentUser.role === 'client' ? (
                  <CheckCircle2 className="w-4 h-4 shrink-0 text-sky-400" />
                ) : (
                  <Lock className="w-4 h-4 shrink-0 text-amber-400" />
                )}
                <span>
                  {currentUser.role === 'client'
                    ? 'Review Authority: As a Client, you have exclusive permission to inspect code deliverables, approve release, or request revisions.'
                    : 'Developer Restriction: You can showcase your work below. Developers CANNOT review or approve their own work; only clients have review authority.'}
                </span>
              </div>
            </div>

            {/* Milestones Deliverables Cards */}
            <div className="space-y-4">
              {milestones.map((m) => (
                <div key={m.id} className="bg-slate-900 border border-slate-800 rounded-xl p-5 space-y-4">
                  <div className="flex items-center justify-between">
                    <div>
                      <span className="text-[10px] font-mono text-slate-500 block">Milestone #{m.order_index}</span>
                      <h3 className="font-bold text-sm text-white">{m.title}</h3>
                      <div className="text-xs text-emerald-400 font-semibold mt-0.5">
                        ${m.amount} Total • Net Dev Payout: ${m.amount * 0.9}
                      </div>
                    </div>

                    <div>
                      {m.status === 'UNDER_REVIEW' && (
                        <span className="px-2.5 py-1 rounded text-xs font-bold bg-amber-500/15 text-amber-400 border border-amber-500/30">
                          UNDER CLIENT REVIEW
                        </span>
                      )}
                      {m.status === 'RELEASED' && (
                        <span className="px-2.5 py-1 rounded text-xs font-bold bg-sky-500/15 text-sky-400 border border-sky-500/30">
                          APPROVED & RELEASED
                        </span>
                      )}
                      {m.status === 'FUNDED' && (
                        <span className="px-2.5 py-1 rounded text-xs font-bold bg-emerald-500/15 text-emerald-400 border border-emerald-500/30">
                          ESCROW SECURED (IN PROGRESS)
                        </span>
                      )}
                      {m.status === 'UNFUNDED' && (
                        <span className="px-2.5 py-1 rounded text-xs font-bold bg-slate-800 text-slate-400 border border-slate-700">
                          UNFUNDED
                        </span>
                      )}
                    </div>
                  </div>

                  {/* Deliverable submission box */}
                  {(m.submission_notes || m.submission_url) ? (
                    <div className="bg-slate-950 border border-slate-800 rounded-lg p-3 text-xs space-y-1.5">
                      <div className="text-slate-300 font-bold flex items-center gap-1.5">
                        <FileCode className="w-3.5 h-3.5 text-sky-400" />
                        Submitted Deliverables Showcase:
                      </div>
                      <p className="text-slate-300">{m.submission_notes}</p>
                      {m.submission_url && (
                        <a
                          href={m.submission_url}
                          target="_blank"
                          rel="noreferrer"
                          className="inline-flex items-center gap-1 text-sky-400 hover:underline font-mono text-[11px]"
                        >
                          <ExternalLink className="w-3 h-3" />
                          {m.submission_url}
                        </a>
                      )}
                    </div>
                  ) : (
                    <div className="bg-slate-950 border border-slate-800/60 rounded-lg p-3 text-xs text-slate-500 italic">
                      No deliverables submitted yet for this milestone.
                    </div>
                  )}

                  {/* Actions & Role Permissions */}
                  <div className="pt-2 border-t border-slate-800 flex items-center justify-between">
                    <div className="text-xs text-slate-400">
                      Disbursement: 90% Developer / 10% Platform Fee
                    </div>

                    {currentUser.role === 'client' ? (
                      /* CLIENT REVIEW ACTIONS */
                      <div className="flex items-center gap-2">
                        {m.status === 'UNDER_REVIEW' && (
                          <>
                            <button
                              onClick={() => handleReleaseFunds(m)}
                              className="px-4 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-bold rounded-lg transition flex items-center gap-1.5 shadow"
                            >
                              <CheckCircle2 className="w-3.5 h-3.5" />
                              Approve Deliverable & Release Escrow
                            </button>
                            <button
                              onClick={() => {
                                const notes = prompt('Enter revision feedback for developer:');
                                if (notes) {
                                  const updated = milestones.map(item => item.id === m.id ? { ...item, status: 'FUNDED' as const, submission_notes: `Revision Requested: ${notes}` } : item);
                                  setMilestones(updated);
                                  showBanner('Revision requested. Developer notified.');
                                }
                              }}
                              className="px-3 py-1.5 bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-medium rounded-lg"
                            >
                              Request Revision
                            </button>
                          </>
                        )}
                        {m.status === 'UNFUNDED' && (
                          <button
                            onClick={() => handleFundEscrow(m)}
                            className="px-3 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-bold rounded-lg"
                          >
                            Fund Escrow (${m.amount})
                          </button>
                        )}
                      </div>
                    ) : (
                      /* DEVELOPER PERSPECTIVE: CANNOT REVIEW WORK! */
                      <div className="flex items-center gap-2">
                        {m.status === 'FUNDED' && (
                          <button
                            onClick={() => setIsSubmittingDeliverable(m)}
                            className="px-4 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-bold rounded-lg transition flex items-center gap-1.5"
                          >
                            <Send className="w-3.5 h-3.5" />
                            Submit Work Deliverables
                          </button>
                        )}
                        {m.status === 'UNDER_REVIEW' && (
                          <span className="inline-flex items-center gap-1.5 text-xs text-amber-400 bg-amber-950/40 border border-amber-500/30 px-3 py-1 rounded-lg">
                            <Lock className="w-3.5 h-3.5" />
                            Client Review Only: Developer cannot review deliverables.
                          </span>
                        )}
                      </div>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* PANEL 2: 1-ON-1 COMMUNICATION & PUBLIC PORTAL */}
        {activeTab === 'communication' && (
          <div className="space-y-4">
            <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 space-y-3">
              <div className="flex items-center justify-between">
                <div>
                  <h2 className="text-base font-bold text-white flex items-center gap-2">
                    <MessageSquare className="w-5 h-5 text-emerald-400" />
                    1-on-1 Direct Communication Portal
                  </h2>
                  <p className="text-xs text-slate-400 mt-0.5">
                    One-on-one contact channel between Client (Sarah Jenkins) and Developer (Alex Rivera)
                  </p>
                </div>
                <span className="px-2.5 py-1 rounded text-xs font-bold bg-emerald-500/15 text-emerald-400 border border-emerald-500/30">
                  PUBLICLY AUDITABLE
                </span>
              </div>

              {/* Public Portal Transparency Guarantee */}
              <div className="p-3 bg-slate-950 border border-slate-800 rounded-lg text-xs text-slate-300 flex items-center gap-2">
                <ShieldCheck className="w-4 h-4 text-sky-400 shrink-0" />
                <span>
                  Transparency Policy: All 1-on-1 communication in this portal is auditable by platform mediators to ensure contract compliance, prevent off-platform contract evasion, and secure escrow disbursements.
                </span>
              </div>

              <div className="flex items-center justify-between pt-2">
                <div className="text-xs text-slate-400">
                  Participants: Sarah Jenkins (Client) ↔ Alex Rivera (Developer)
                </div>
                <button
                  onClick={() => setIsCommunicationPortalOpen(true)}
                  className="px-4 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-xs rounded-lg flex items-center gap-1.5 shadow"
                >
                  <ExternalLink className="w-3.5 h-3.5" />
                  Open Dedicated 1-on-1 Portal
                </button>
              </div>
            </div>

            {/* Inline Message Feed */}
            <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 space-y-4">
              <h3 className="text-xs font-bold text-white uppercase tracking-wider">Public Transcript Feed</h3>

              <div className="space-y-3 max-h-96 overflow-y-auto pr-1">
                {messages.map((msg) => (
                  <div
                    key={msg.id}
                    className={`p-3.5 rounded-xl border text-xs max-w-xl ${
                      msg.role === 'client'
                        ? 'bg-sky-950/40 border-sky-500/30 ml-auto'
                        : 'bg-slate-950 border-slate-800 mr-auto'
                    }`}
                  >
                    <div className="flex items-center justify-between gap-3 mb-1">
                      <span className={`font-bold text-[11px] ${msg.role === 'client' ? 'text-sky-400' : 'text-emerald-400'}`}>
                        {msg.sender} ({msg.role.toUpperCase()})
                      </span>
                      <span className="text-[10px] text-slate-500">{msg.time}</span>
                    </div>
                    <p className="text-slate-200 leading-relaxed">{msg.text}</p>
                  </div>
                ))}
              </div>

              {/* Message Composer */}
              <div className="pt-2 border-t border-slate-800 flex items-center gap-2">
                <input
                  type="text"
                  placeholder="Type a public message to send to the 1-on-1 portal..."
                  value={chatInputText}
                  onChange={(e) => setChatInputText(e.target.value)}
                  onKeyDown={(e) => { if (e.key === 'Enter') handleSendMessage(); }}
                  className="flex-1 bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-xs text-white focus:outline-none focus:border-emerald-500"
                />
                <button
                  onClick={handleSendMessage}
                  disabled={!chatInputText.trim()}
                  className="px-4 py-2 bg-emerald-600 hover:bg-emerald-500 disabled:opacity-50 text-white font-bold text-xs rounded-lg flex items-center gap-1.5"
                >
                  <Send className="w-3.5 h-3.5" />
                  Send to Portal
                </button>
              </div>
            </div>
          </div>
        )}

        {/* PANEL 3: ESCROW & FINANCIAL APPROVALS */}
        {activeTab === 'escrow' && (
          <div className="space-y-4">
            <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 space-y-3">
              <div className="flex items-center justify-between">
                <div>
                  <h2 className="text-base font-bold text-white flex items-center gap-2">
                    <Lock className="w-5 h-5 text-indigo-400" />
                    Escrow & Financial Approvals Panel
                  </h2>
                  <p className="text-xs text-slate-400 mt-0.5">
                    Both Client and Developer see all locked funds • Only Client has approval authority
                  </p>
                </div>
                <div className={`px-2.5 py-1 rounded text-xs font-bold border ${
                  currentUser.role === 'client'
                    ? 'bg-emerald-500/15 text-emerald-400 border-emerald-500/40'
                    : 'bg-slate-800 text-slate-300 border-slate-700'
                }`}>
                  {currentUser.role === 'client' ? 'CLIENT APPROVER' : 'DEVELOPER (READ-ONLY)'}
                </div>
              </div>

              <div className={`p-3 rounded-lg border text-xs flex items-center gap-2 ${
                currentUser.role === 'client'
                  ? 'bg-emerald-950/40 border-emerald-500/30 text-emerald-300'
                  : 'bg-slate-950 border-slate-800 text-slate-400'
              }`}>
                <ShieldCheck className="w-4 h-4 shrink-0 text-emerald-400" />
                <span>
                  {currentUser.role === 'client'
                    ? 'Approval Rights: As the client, you are authorized to release milestone payouts once work satisfies contract terms.'
                    : 'Read-Only Transparency: Escrow funds are secured under PayPal delayed disbursement. Disbursement can only be authorized by the client.'}
                </span>
              </div>
            </div>

            {/* Escrow Metrics Cards */}
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
              <div className="bg-slate-900 border border-slate-800 rounded-xl p-4">
                <div className="text-slate-400 text-xs font-medium">Total Locked in Escrow</div>
                <div className="text-2xl font-black text-emerald-400 mt-1">${totalEscrowHeld.toLocaleString()}</div>
                <div className="text-[10px] text-slate-500">PayPal Delayed Disbursement Hold</div>
              </div>

              <div className="bg-slate-900 border border-slate-800 rounded-xl p-4">
                <div className="text-slate-400 text-xs font-medium">Developer Net (90%)</div>
                <div className="text-2xl font-black text-sky-400 mt-1">${(totalEscrowHeld * 0.9).toLocaleString()}</div>
                <div className="text-[10px] text-slate-500">Disbursed on client approval</div>
              </div>

              <div className="bg-slate-900 border border-slate-800 rounded-xl p-4">
                <div className="text-slate-400 text-xs font-medium">Total Payouts Released</div>
                <div className="text-2xl font-black text-white mt-1">${totalReleased.toLocaleString()}</div>
                <div className="text-[10px] text-emerald-400">Completed disbursements</div>
              </div>
            </div>

            {/* Escrow Milestones Table */}
            <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 space-y-3">
              <h3 className="text-xs font-bold text-white uppercase tracking-wider">Milestone Escrow Ledger</h3>

              <div className="space-y-3">
                {milestones.map((m) => (
                  <div key={m.id} className="bg-slate-950 border border-slate-800 rounded-xl p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                    <div>
                      <div className="font-bold text-sm text-white">{m.title}</div>
                      <div className="text-xs text-slate-400 mt-1">
                        Amount: <strong className="text-white">${m.amount}</strong> • Dev Net: <span className="text-emerald-400">${m.amount * 0.9}</span> • Fee: ${m.amount * 0.1}
                      </div>
                    </div>

                    <div className="flex items-center gap-3">
                      <span className={`px-2.5 py-1 rounded text-xs font-bold border ${
                        m.status === 'FUNDED' ? 'bg-emerald-500/15 text-emerald-400 border-emerald-500/30' :
                        m.status === 'UNDER_REVIEW' ? 'bg-amber-500/15 text-amber-400 border-amber-500/30' :
                        m.status === 'RELEASED' ? 'bg-sky-500/15 text-sky-400 border-sky-500/30' :
                        'bg-slate-800 text-slate-400 border-slate-700'
                      }`}>
                        {m.status}
                      </span>

                      {/* APPROVAL PERMISSION: ONLY CLIENT CAN APPROVE */}
                      {currentUser.role === 'client' ? (
                        <>
                          {m.status === 'UNDER_REVIEW' && (
                            <button
                              onClick={() => handleReleaseFunds(m)}
                              className="px-3.5 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-xs rounded-lg transition"
                            >
                              Client Approve & Release
                            </button>
                          )}
                          {m.status === 'UNFUNDED' && (
                            <button
                              onClick={() => handleFundEscrow(m)}
                              className="px-3.5 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-xs rounded-lg"
                            >
                              Fund via PayPal
                            </button>
                          )}
                        </>
                      ) : (
                        m.status === 'UNDER_REVIEW' && (
                          <span className="text-[11px] text-slate-400 italic">
                            🔒 Client Approval Authority Only
                          </span>
                        )
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* PANEL 4: DASHBOARD PIPELINE OVERVIEW */}
        {activeTab === 'dashboard' && (
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

            {/* Filter and Search Bar */}
            <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3">
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
              {activeMilestones.map((milestone) => (
                <div
                  key={milestone.id}
                  className="bg-slate-900 border border-slate-800 rounded-xl p-5 space-y-4 shadow-sm"
                >
                  <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
                    <div className="flex items-center gap-3">
                      <span className="text-xs font-mono font-bold text-slate-500">#{milestone.order_index}</span>
                      <h3 className="font-bold text-sm text-white">{milestone.title}</h3>
                    </div>
                    <div>
                      <span className="px-2.5 py-1 rounded text-xs font-bold bg-slate-800 text-slate-300 border border-slate-700">
                        {milestone.status}
                      </span>
                    </div>
                  </div>
                  {milestone.description && (
                    <p className="text-xs text-slate-400">{milestone.description}</p>
                  )}
                  <div className="text-xs text-emerald-400 font-semibold">
                    ${milestone.amount} Escrow (90% Dev: ${milestone.amount * 0.9})
                  </div>
                </div>
              ))}
            </div>
          </>
        )}

        {/* LEGAL VIEW */}
        {activeTab === 'legal' && (
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
            </div>
          </div>
        )}
      </main>

      {/* DEDICATED 1-ON-1 COMMUNICATION PORTAL MODAL */}
      {isCommunicationPortalOpen && (
        <div className="fixed inset-0 bg-black/75 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-700 rounded-2xl max-w-2xl w-full h-[80vh] flex flex-col p-6 shadow-2xl">
            <div className="flex items-center justify-between pb-4 border-b border-slate-800">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-emerald-600 flex items-center justify-center text-white">
                  <MessageSquare className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="font-bold text-sm text-white flex items-center gap-2">
                    1-on-1 Public Communication Portal
                    <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-emerald-500/20 text-emerald-400 border border-emerald-500/40">
                      PUBLIC RECORD
                    </span>
                  </h3>
                  <p className="text-xs text-slate-400">Sarah Jenkins (Client) ↔ Alex Rivera (Developer)</p>
                </div>
              </div>
              <button
                onClick={() => setIsCommunicationPortalOpen(false)}
                className="text-slate-400 hover:text-white text-xs px-2 py-1 bg-slate-800 rounded-lg"
              >
                Close
              </button>
            </div>

            {/* Transparency Notice */}
            <div className="my-3 p-3 bg-slate-950 border border-slate-800 rounded-xl text-xs text-slate-300 flex items-center gap-2">
              <ShieldCheck className="w-4 h-4 text-emerald-400 shrink-0" />
              <span>
                Auditable Public Log: All messages in this 1-on-1 channel are recorded for escrow protection and mediation.
              </span>
            </div>

            {/* Chat Messages Stream */}
            <div className="flex-1 overflow-y-auto space-y-3 p-2 bg-slate-950 rounded-xl border border-slate-800/80 my-2">
              {messages.map((m) => (
                <div
                  key={m.id}
                  className={`p-3 rounded-xl border text-xs max-w-lg ${
                    m.role === 'client'
                      ? 'bg-sky-950/40 border-sky-500/30 ml-auto'
                      : 'bg-slate-900 border-slate-800 mr-auto'
                  }`}
                >
                  <div className="flex items-center justify-between gap-2 mb-1">
                    <span className={`font-bold text-[11px] ${m.role === 'client' ? 'text-sky-400' : 'text-emerald-400'}`}>
                      {m.sender} ({m.role.toUpperCase()})
                    </span>
                    <span className="text-[10px] text-slate-500">{m.time}</span>
                  </div>
                  <p className="text-slate-200">{m.text}</p>
                </div>
              ))}
            </div>

            {/* Input Composer */}
            <div className="pt-2 flex items-center gap-2">
              <input
                type="text"
                value={chatInputText}
                onChange={(e) => setChatInputText(e.target.value)}
                onKeyDown={(e) => { if (e.key === 'Enter') handleSendMessage(); }}
                placeholder="Type your message into this auditable portal..."
                className="flex-1 bg-slate-950 border border-slate-800 rounded-lg px-3 py-2.5 text-xs text-white focus:outline-none focus:border-emerald-500"
              />
              <button
                onClick={handleSendMessage}
                disabled={!chatInputText.trim()}
                className="px-4 py-2.5 bg-emerald-600 hover:bg-emerald-500 disabled:opacity-50 text-white font-bold text-xs rounded-lg flex items-center gap-1.5"
              >
                <Send className="w-4 h-4" />
                Send
              </button>
            </div>
          </div>
        </div>
      )}

      {/* PAYPAL DETAILS MODAL */}
      {isPayPalModalOpen && (
        <div className="fixed inset-0 bg-black/75 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-700 rounded-2xl max-w-md w-full p-6 space-y-4 shadow-2xl">
            <div className="flex items-center justify-between">
              <h3 className="font-bold text-sm text-white flex items-center gap-2">
                <DollarSign className="w-4 h-4 text-sky-400" />
                PayPal Details & Account Setup
              </h3>
              <button
                onClick={() => setIsPayPalModalOpen(false)}
                className="text-slate-400 hover:text-white text-xs px-2 py-1 bg-slate-800 rounded-lg"
              >
                Close
              </button>
            </div>

            {/* Answer explanation */}
            <div className="bg-slate-950 border border-slate-800 rounded-xl p-3.5 text-xs space-y-2">
              <div className="font-bold text-white flex items-center gap-1.5">
                <HelpCircle className="w-3.5 h-3.5 text-sky-400" />
                When are you needed to fill PayPal details?
              </div>
              <p className="text-slate-300 leading-relaxed">
                • <strong>Developers:</strong> Required to receive your 90% payout disbursement when the client approves work.<br />
                • <strong>Clients:</strong> Required to authorize and fund milestones into delayed disbursement escrow.
              </p>
            </div>

            <div className="space-y-3 text-xs">
              <div>
                <label className="text-slate-400 block mb-1">PayPal Account Email</label>
                <input
                  type="email"
                  value={paypalEmailInput}
                  onChange={(e) => setPaypalEmailInput(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg p-2.5 text-xs text-white focus:outline-none focus:border-sky-500"
                />
              </div>

              <div>
                <label className="text-slate-400 block mb-1">PayPal Merchant / Partner ID</label>
                <input
                  type="text"
                  value={paypalMerchantInput}
                  onChange={(e) => setPaypalMerchantInput(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg p-2.5 text-xs text-white focus:outline-none focus:border-sky-500"
                />
              </div>

              <div className="pt-2 flex items-center justify-end gap-2">
                <button
                  onClick={() => setIsPayPalModalOpen(false)}
                  className="px-3.5 py-2 bg-slate-800 text-slate-300 text-xs rounded-lg hover:bg-slate-700"
                >
                  Cancel
                </button>
                <button
                  onClick={handleSavePayPalDetails}
                  className="px-4 py-2 bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-xs rounded-lg"
                >
                  Save & Connect PayPal
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

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
