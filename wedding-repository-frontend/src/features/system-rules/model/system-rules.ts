export type SystemSettings = {
  id: number;
  depositRule: {
    minimumDepositPercentage: number;
  };
  latePaymentPenaltyRule: {
    latePaymentPenaltyEnabled: boolean;
    latePaymentPenaltyRate: number;
  };
  cancellationRule: {
    cancellationDeadlineDays: number;
    depositRefundPercentage: number;
  };
};

export type UpdateDepositRulePayload = {
  minimumDepositPercentage: number;
};

export type UpdatePenaltyRulePayload = {
  latePaymentPenaltyEnabled: boolean;
  latePaymentPenaltyRate: number;
};

export type UpdateCancellationRulePayload = {
  cancellationDeadlineDays: number;
  depositRefundPercentage: number;
};