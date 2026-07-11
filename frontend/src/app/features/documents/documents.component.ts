import { Component, DestroyRef, ElementRef, OnInit, ViewChild, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize, forkJoin } from 'rxjs';
import { VehicleService } from '../../core/services/vehicle.service';
import { DocumentService } from '../../core/services/document.service';
import { Vehicle } from '../../core/models/vehicle.model';
import {
  CreateDocumentResponse,
  DocumentResponse,
  DocumentStatus,
  DocumentSummaryResponse,
  DocumentType,
} from '../../core/models/document.model';
import { DocumentRegisterModalComponent } from './document-register-modal/document-register-modal.component';
import { ConfirmActionModalComponent } from '../../shared/components/confirm-action-modal/confirm-action-modal.component';

interface TypeConfig {
  label: string;
  icon: string;
}

@Component({
  selector: 'app-documents',
  standalone: true,
  imports: [CommonModule, DocumentRegisterModalComponent, ConfirmActionModalComponent],
  templateUrl: './documents.component.html',
})
export class DocumentsComponent implements OnInit {
  private destroyRef      = inject(DestroyRef);
  private vehicleService  = inject(VehicleService);
  private documentService = inject(DocumentService);

  @ViewChild('fileInput') fileInputRef!: ElementRef<HTMLInputElement>;
  attachTargetDocId: number | null = null;
  attachTargetVehicleId: number | null = null;
  fileActionLoadingId: number | null = null;

  // ── Vehicle selector ──────────────────────────────────────────────────────
  vehicles: Vehicle[]             = [];
  selectedVehicle: Vehicle | null = null;
  vehiclesLoading = true;

  // ── Data ──────────────────────────────────────────────────────────────────
  documents: DocumentResponse[]            = [];
  summary: DocumentSummaryResponse | null  = null;
  loading   = false;
  loadError = false;

  // ── Register modal ────────────────────────────────────────────────────────
  showRegisterModal = false;
  prefilledType: DocumentType | null = null;

  // ── Delete confirm ────────────────────────────────────────────────────────
  showDeleteConfirm = false;
  deleteTargetId: number | null = null;
  deleteTargetVehicleId: number | null = null;
  deleteLoading     = false;
  deleteError       = '';

  // ── Toast ─────────────────────────────────────────────────────────────────
  toastMessage = '';
  private toastTimer: ReturnType<typeof setTimeout> | null = null;

  // ── Type config ───────────────────────────────────────────────────────────
  readonly typeConfig: Record<DocumentType, TypeConfig> = {
    CEDULA_VERDE: { label: 'Cédula Verde',         icon: '📄' },
    CEDULA_AZUL:  { label: 'Cédula Azul',          icon: '📋' },
    ITV:          { label: 'ITV',                  icon: '🔄' },
    SEGURO:       { label: 'Póliza de Seguro',      icon: '🛡️' },
    LICENCIA:     { label: 'Licencia de Conducir',  icon: '🪪' },
    PATENTE:      { label: 'Patente',               icon: '📝' },
    OTRO:         { label: 'Otro documento',        icon: '📌' },
  };

  /** True when showing aggregate data for all vehicles (no specific one selected). */
  get allVehiclesMode(): boolean {
    return this.selectedVehicle === null && this.vehicles.length > 0;
  }

  ngOnInit(): void {
    this.vehicleService
      .getVehicles()
      .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => (this.vehiclesLoading = false)))
      .subscribe({
        next: (vehicles) => {
          this.vehicles = vehicles;
          if (vehicles.length === 1) this.selectedVehicle = vehicles[0];
          if (vehicles.length > 0) this.loadAll();
        },
        error: () => { this.loadError = true; },
      });
  }

  onVehicleChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    this.selectedVehicle = value
      ? (this.vehicles.find(v => v.id === Number(value)) ?? null)
      : null;
    this.loadAll();
  }

  loadAll(): void {
    this.loading   = true;
    this.loadError = false;

    if (this.selectedVehicle) {
      const id = this.selectedVehicle.id;
      forkJoin({
        documents: this.documentService.getDocuments(id),
        summary:   this.documentService.getSummary(id),
      })
        .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => (this.loading = false)))
        .subscribe({
          next: ({ documents, summary }) => {
            this.documents = documents;
            this.summary   = summary;
          },
          error: () => (this.loadError = true),
        });
    } else {
      forkJoin(this.vehicles.map((v) => this.documentService.getDocuments(v.id)))
        .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => (this.loading = false)))
        .subscribe({
          next: (results) => {
            this.documents = results.flat();
            this.summary   = this.buildSummary(this.documents);
          },
          error: () => (this.loadError = true),
        });
    }
  }

  getVehicleLabel(vehicleId: number): string {
    const v = this.vehicles.find((v) => v.id === vehicleId);
    return v ? `${v.brand} ${v.model} · ${v.plate}` : '';
  }

  /** Recomputes the compliance summary client-side from a flattened multi-vehicle document list. */
  private buildSummary(docs: DocumentResponse[]): DocumentSummaryResponse {
    let vigente = 0, porVencer = 0, vencido = 0, sinFecha = 0;
    let nextExpiring: DocumentSummaryResponse['nextExpiring'] = null;
    let nextDaysLeft = Infinity;

    for (const doc of docs) {
      switch (doc.status) {
        case 'VIGENTE':    vigente++; break;
        case 'POR_VENCER': porVencer++; break;
        case 'VENCIDO':    vencido++; break;
        case 'SIN_FECHA':  sinFecha++; break;
      }
      if ((doc.status === 'VIGENTE' || doc.status === 'POR_VENCER') && doc.expiryDate) {
        const daysLeft = this.daysUntil(doc.expiryDate);
        if (daysLeft < nextDaysLeft) {
          nextDaysLeft = daysLeft;
          nextExpiring = { type: doc.type, label: this.getTypeLabel(doc.type), expiryDate: doc.expiryDate, daysLeft };
        }
      }
    }

    const total = docs.length;
    const compliancePercentage = total === 0 ? 100 : Math.round((vigente * 100) / total);
    return { total, vigente, porVencer, vencido, sinFecha, compliancePercentage, nextExpiring };
  }

  private daysUntil(dateStr: string): number {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const target = new Date(dateStr + 'T00:00:00');
    return Math.round((target.getTime() - today.getTime()) / 86_400_000);
  }

  // ── Register modal ────────────────────────────────────────────────────────

  openRegisterModal(prefill: DocumentType | null = null): void {
    this.prefilledType    = prefill;
    this.showRegisterModal = true;
  }

  onDocumentCreated(result: CreateDocumentResponse): void {
    this.showRegisterModal = false;
    this.prefilledType     = null;
    this.loadAll();
    if (result.warning) this.showToast(result.warning);
  }

  // ── Delete ────────────────────────────────────────────────────────────────

  confirmDelete(docId: number, vehicleId: number): void {
    this.deleteTargetId        = docId;
    this.deleteTargetVehicleId = vehicleId;
    this.deleteError    = '';
    this.showDeleteConfirm = true;
  }

  onDeleteConfirmed(): void {
    if (!this.deleteTargetVehicleId || !this.deleteTargetId) return;
    this.deleteLoading = true;
    this.deleteError   = '';

    this.documentService
      .deleteDocument(this.deleteTargetVehicleId, this.deleteTargetId)
      .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => (this.deleteLoading = false)))
      .subscribe({
        next: () => {
          this.showDeleteConfirm = false;
          this.deleteTargetId    = null;
          this.deleteTargetVehicleId = null;
          this.loadAll();
        },
        error: () => (this.deleteError = 'No se pudo eliminar el documento. Intentá de nuevo.'),
      });
  }

  onDeleteCancelled(): void {
    this.showDeleteConfirm = false;
    this.deleteTargetId    = null;
    this.deleteTargetVehicleId = null;
    this.deleteError       = '';
  }

  // ── PDF attach / view ────────────────────────────────────────────────────

  triggerAttach(docId: number, vehicleId: number): void {
    this.attachTargetDocId        = docId;
    this.attachTargetVehicleId    = vehicleId;
    this.fileInputRef.nativeElement.value = '';
    this.fileInputRef.nativeElement.click();
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    const docId = this.attachTargetDocId;
    const vehicleId = this.attachTargetVehicleId;
    if (!file || !docId || !vehicleId) return;

    this.fileActionLoadingId = docId;
    this.documentService
      .uploadFile(vehicleId, docId, file)
      .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => (this.fileActionLoadingId = null)))
      .subscribe({
        next: () => this.loadAll(),
        error: () => this.showToast('No se pudo adjuntar el PDF. Intentá de nuevo.'),
      });
  }

  viewFile(docId: number, vehicleId: number): void {
    this.fileActionLoadingId = docId;
    this.documentService
      .downloadFile(vehicleId, docId)
      .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => (this.fileActionLoadingId = null)))
      .subscribe({
        next: (blob) => {
          const url = URL.createObjectURL(new Blob([blob], { type: 'application/pdf' }));
          window.open(url, '_blank');
          setTimeout(() => URL.revokeObjectURL(url), 60_000);
        },
        error: () => this.showToast('No se pudo abrir el PDF. Intentá de nuevo.'),
      });
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  getTypeLabel(type: DocumentType): string {
    return this.typeConfig[type]?.label ?? type;
  }

  getTypeIcon(type: DocumentType): string {
    return this.typeConfig[type]?.icon ?? '📌';
  }

  statusBadgeClass(status: DocumentStatus): string {
    switch (status) {
      case 'VIGENTE':    return 'bg-emerald-100 text-emerald-700';
      case 'POR_VENCER': return 'bg-amber-100 text-amber-700';
      case 'VENCIDO':    return 'bg-red-100 text-red-700';
      case 'SIN_FECHA':  return 'bg-gray-100 text-gray-500';
    }
  }

  statusLabel(status: DocumentStatus): string {
    switch (status) {
      case 'VIGENTE':    return '● Vigente';
      case 'POR_VENCER': return '● Por Vencer';
      case 'VENCIDO':    return '● Vencido';
      case 'SIN_FECHA':  return '● Sin fecha';
    }
  }

  cardBorderClass(status: DocumentStatus): string {
    switch (status) {
      case 'VIGENTE':    return 'border border-surface-border';
      case 'POR_VENCER': return 'border border-amber-300';
      case 'VENCIDO':    return 'border-2 border-red-400 bg-red-50';
      case 'SIN_FECHA':  return 'border border-surface-border';
    }
  }

  dateColorClass(status: DocumentStatus): string {
    switch (status) {
      case 'VIGENTE':    return 'text-emerald-700';
      case 'POR_VENCER': return 'text-amber-700';
      case 'VENCIDO':    return 'text-red-700 font-bold';
      case 'SIN_FECHA':  return 'text-text-muted';
    }
  }

  formatDate(dateStr: string | null): string {
    if (!dateStr) return '—';
    const [year, month, day] = dateStr.split('-');
    const months = ['ENE','FEB','MAR','ABR','MAY','JUN','JUL','AGO','SEP','OCT','NOV','DIC'];
    return `${parseInt(day, 10)} ${months[parseInt(month, 10) - 1]} ${year}`;
  }

  trackById(_: number, doc: DocumentResponse): number { return doc.id; }

  private showToast(message: string): void {
    this.toastMessage = message;
    if (this.toastTimer) clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => (this.toastMessage = ''), 5000);
  }
}
