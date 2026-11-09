export type DealStatus = 'SCHEDULED' | 'OPEN' | 'ACHIEVED' | 'CLOSED' | 'CANCELLED';

export interface DealResponse {
  id: string;
  productId: string;
  title: string;
  originalPrice: number;
  dealPrice: number;
  discountRate: number;
  targetQuantity: number;
  maxCapacity: number;
  currentQuantity: number;
  achievementRate: number;
  status: DealStatus;
  startsAt: string;
  endsAt: string;
  isAchieved: boolean;
}

export interface JoinDealRequest {
  quantity: number;
}

export interface ProblemDetail {
  type: string;
  title: string;
  status: number;
  detail: string;
  errorCode?: string;
  timestamp?: string;
}
