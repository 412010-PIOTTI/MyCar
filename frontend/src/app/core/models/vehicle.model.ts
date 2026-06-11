export interface Vehicle {
  id: number;
  plate: string;
  brand: string;
  model: string;
  year: number;
  color: string | null;
  currentKm: number;
  createdAt: string;
}

export interface CreateVehicleRequest {
  plate: string;
  brand: string;
  model: string;
  year: number;
  color?: string | null;
  initialKm: number;
}

export interface UpdateVehicleRequest {
  plate?: string | null;
  brand?: string | null;
  model?: string | null;
  year?: number | null;
  color?: string | null;
}
