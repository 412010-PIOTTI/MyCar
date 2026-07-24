export interface ChatRequest {
  vehicleId: number;
  message: string;
}

export interface ChatResponse {
  reply: string;
  manualGrounded: boolean;
  vehicleId: number;
}

export interface ChatMessage {
  role: 'user' | 'assistant';
  text: string;
  manualGrounded?: boolean;
  timestamp: Date;
}
