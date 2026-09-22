export interface User {
  id: string;
  email: string;
  full_name: string;
  role: 'admin' | 'client' | 'developer';
  paypal_merchant_id?: string;
  paypal_connected: boolean;
  avatar_url?: string;
}

export interface Project {
  id: string;
  title: string;
  description: string;
  client_id: string;
  developer_id?: string;
  total_budget: number;
  status: 'OPEN' | 'ACTIVE' | 'IN_REVIEW' | 'COMPLETED' | 'DISPUTED';
  created_at: number;
}

export interface Milestone {
  id: string;
  project_id: string;
  title: string;
  description?: string;
  amount: number;
  commission_amount: number;
  status: 'UNFUNDED' | 'FUNDED' | 'SUBMITTED' | 'UNDER_REVIEW' | 'RELEASED' | 'REFUNDED' | 'DISPUTED';
  order_index: number;
  submission_notes?: string;
  submission_url?: string;
  submitted_at?: number;
  released_at?: number;
}

export interface EscrowTransaction {
  id: string;
  milestone_id: string;
  project_id: string;
  sender_user_id: string;
  receiver_user_id?: string;
  amount: number;
  commission_retained: number;
  type: 'ESCROW_DEPOSIT' | 'PAYOUT_RELEASE' | 'COMMISSION_CAPTURE' | 'REFUND_CLIENT';
  status: 'PENDING' | 'COMPLETED' | 'FAILED' | 'HELD';
  paypal_order_id?: string;
  created_at: number;
}

export interface Dispute {
  id: string;
  milestone_id: string;
  project_id: string;
  raised_by_user_id: string;
  reason: string;
  evidence?: string;
  status: 'OPEN' | 'INVESTIGATING' | 'RESOLVED_PAY_DEV' | 'RESOLVED_REFUND_CLIENT' | 'SPLIT_SETTLED';
  admin_resolution_notes?: string;
  resolved_at?: number;
  created_at: number;
}
