export type PaymentStatus = 'PENDING' | 'COMPLETED' | 'FAILED' | 'REFUNDED';

export interface PaymentResponse {
  id: string;
  dealId: string;
  userId: string;
  amount: number;
  status: PaymentStatus;
  idempotencyKey: string;
  pgTransactionId?: string;
  failureReason?: string;
  createdAt: string;
  updatedAt: string;
}

export interface RefundRequest {
  reason: string;
}
