import { Component, DestroyRef, ElementRef, OnInit, ViewChild, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { VehicleService } from '../../core/services/vehicle.service';
import { ChatService } from '../../core/services/chat.service';
import { Vehicle } from '../../core/models/vehicle.model';
import { ChatMessage } from '../../core/models/chat.model';

interface Suggestion {
  title: string;
  subtitle: string;
}

@Component({
  selector: 'app-ai-chat',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './ai-chat.component.html',
})
export class AiChatComponent implements OnInit {
  private destroyRef = inject(DestroyRef);
  private vehicleService = inject(VehicleService);
  private chatService = inject(ChatService);

  @ViewChild('messagesContainer') messagesContainer?: ElementRef<HTMLDivElement>;

  vehicles: Vehicle[] = [];
  selectedVehicle: Vehicle | null = null;
  vehiclesLoading = true;

  // Session-only history per vehicle, kept client-side so switching vehicles doesn't lose
  // the transcript — the backend already scopes its own conversation memory per user+vehicle.
  private historyByVehicle = new Map<number, ChatMessage[]>();
  messages: ChatMessage[] = [];

  draft = '';
  sending = false;
  sendError = false;

  readonly suggestions: Suggestion[] = [
    { title: '¿Cuándo toca el cambio de aceite?', subtitle: 'Consultá el intervalo de service recomendado.' },
    { title: 'Presión de neumáticos', subtitle: 'Valores recomendados para uso normal.' },
    { title: 'Luces de advertencia', subtitle: 'Qué significa cada luz del tablero.' },
  ];

  ngOnInit(): void {
    this.vehicleService
      .getVehicles()
      .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => (this.vehiclesLoading = false)))
      .subscribe({
        next: (vehicles) => {
          this.vehicles = vehicles;
          if (vehicles.length > 0) this.selectVehicle(vehicles[0]);
        },
        error: () => {},
      });
  }

  selectVehicle(vehicle: Vehicle): void {
    this.selectedVehicle = vehicle;
    if (!this.historyByVehicle.has(vehicle.id)) {
      this.historyByVehicle.set(vehicle.id, [this.buildWelcomeMessage(vehicle)]);
    }
    this.messages = this.historyByVehicle.get(vehicle.id)!;
    this.sendError = false;
    this.scrollToBottom();
  }

  onVehicleChange(event: Event): void {
    const id = Number((event.target as HTMLSelectElement).value);
    const vehicle = this.vehicles.find((v) => v.id === id);
    if (vehicle) this.selectVehicle(vehicle);
  }

  sendSuggestion(text: string): void {
    this.draft = text;
    this.send();
  }

  send(): void {
    const text = this.draft.trim();
    if (!text || !this.selectedVehicle || this.sending) return;

    const vehicleId = this.selectedVehicle.id;
    this.messages.push({ role: 'user', text, timestamp: new Date() });
    this.draft = '';
    this.sending = true;
    this.sendError = false;
    this.scrollToBottom();

    this.chatService
      .sendMessage({ vehicleId, message: text })
      .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => (this.sending = false)))
      .subscribe({
        next: (response) => {
          this.messages.push({
            role: 'assistant',
            text: response.reply,
            manualGrounded: response.manualGrounded,
            timestamp: new Date(),
          });
          this.scrollToBottom();
        },
        error: () => {
          this.sendError = true;
        },
      });
  }

  onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.send();
    }
  }

  private buildWelcomeMessage(vehicle: Vehicle): ChatMessage {
    return {
      role: 'assistant',
      text: `¡Hola! Soy tu asistente técnico de MyCar. Preguntame lo que necesites sobre el mantenimiento de tu ${vehicle.brand} ${vehicle.model} ${vehicle.year}.`,
      timestamp: new Date(),
    };
  }

  private scrollToBottom(): void {
    setTimeout(() => {
      const el = this.messagesContainer?.nativeElement;
      if (el) el.scrollTop = el.scrollHeight;
    });
  }
}
