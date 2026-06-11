export interface UserProfile {
  id: number;
  name: string;
  email: string;
  role: string;
  createdAt: string;
  twoFactorEnabled: boolean;
}

export interface UpdateProfileRequest {
  name: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}
