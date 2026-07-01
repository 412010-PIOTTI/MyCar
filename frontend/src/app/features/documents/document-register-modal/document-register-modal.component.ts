import {
  Component,
  DestroyRef,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  SimpleChanges,
  inject,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { DocumentService } from '../../../core/services/document.service';
import {
  CreateDocumentResponse,
  DocumentResponse,
  DocumentType,
} from '../../../core/models/document.model';

interface TypeOption {
  value: DocumentType;
  label: string;
  icon: string;
}

@Component({
  selector: 'app-document-register-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './document-register-modal.component.html',
})
export class DocumentRegisterModalComponent implements OnChanges {
  @Input({ required: true }) vehicleId!: number;
  @Input() existingDocuments: DocumentResponse[] = [];
  @Input() prefilledType: DocumentType | null = null;

  @Output() close = new EventEmitter<void>();
  @Output() documentCreated = new EventEmitter<CreateDocumentResponse>();

  private fb             = inject(FormBuilder);
  private documentService = inject(DocumentService);
  private destroyRef     = inject(DestroyRef);

  readonly typeOptions: TypeOption[] = [
    { value: 'CEDULA_VERDE', label: 'Cédula Verde',          icon: '📄' },
    { value: 'CEDULA_AZUL',  label: 'Cédula Azul',           icon: '📋' },
    { value: 'ITV',          label: 'ITV',                   icon: '🔄' },
    { value: 'SEGURO',       label: 'Póliza de Seguro',       icon: '🛡️' },
    { value: 'LICENCIA',     label: 'Licencia de Conducir',   icon: '🪪' },
    { value: 'PATENTE',      label: 'Patente',                icon: '📝' },
    { value: 'OTRO',         label: 'Otro',                   icon: '📌' },
  ];

  form = this.fb.group({
    type:            ['' as DocumentType | '', Validators.required],
    referenceNumber: ['', Validators.maxLength(100)],
    issueDate:       [''],
    expiryDate:      [''],
    notes:           ['', Validators.maxLength(300)],
  });

  step: 'form' | 'confirm' = 'form';
  conflictDoc: DocumentResponse | null = null;
  loading      = false;
  generalError = '';
  selectedFile: File | null = null;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['prefilledType'] && this.prefilledType) {
      this.form.patchValue({ type: this.prefilledType });
      this.step = 'form';
    }
  }

  fieldError(name: string): string {
    const ctrl = this.form.get(name);
    if (!ctrl?.invalid || !ctrl.touched) return '';
    if (ctrl.errors?.['serverError'])  return ctrl.errors['serverError'];
    if (ctrl.errors?.['required'])     return 'Este campo es obligatorio.';
    if (ctrl.errors?.['maxlength'])    return `Máximo ${ctrl.errors['maxlength'].requiredLength} caracteres.`;
    return '';
  }

  getTypeLabel(type: DocumentType): string {
    return this.typeOptions.find(t => t.value === type)?.label ?? type;
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedFile = input.files?.[0] ?? null;
  }

  onSubmit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }

    const selectedType = this.form.value.type as DocumentType;
    const conflict = this.existingDocuments.find(d => d.type === selectedType);

    if (conflict && this.step === 'form') {
      this.conflictDoc = conflict;
      this.step = 'confirm';
      return;
    }

    this.callApi();
  }

  confirmReplace(): void {
    this.callApi();
  }

  backToForm(): void {
    this.step = 'form';
    this.conflictDoc = null;
  }

  private callApi(): void {
    this.loading      = true;
    this.generalError = '';
    const v = this.form.value;

    this.documentService
      .createDocument(this.vehicleId, {
        type:            v.type as DocumentType,
        referenceNumber: v.referenceNumber || null,
        issueDate:       v.issueDate || null,
        expiryDate:      v.expiryDate || null,
        notes:           v.notes || null,
      })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (result) => this.uploadFileIfSelected(result),
        error: (err) => {
          this.loading = false;
          this.step = 'form';
          if (err.status === 400 && err.error?.errors) {
            const errors = err.error.errors as Record<string, string>;
            Object.keys(errors).forEach(f => this.form.get(f)?.setErrors({ serverError: errors[f] }));
          } else {
            this.generalError = 'No se pudo guardar el documento. Intentá de nuevo.';
          }
        },
      });
  }

  private uploadFileIfSelected(result: CreateDocumentResponse): void {
    if (!this.selectedFile) {
      this.loading = false;
      this.documentCreated.emit(result);
      return;
    }

    this.documentService
      .uploadFile(this.vehicleId, result.document.id, this.selectedFile)
      .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => (this.loading = false)))
      .subscribe({
        next: (document) => this.documentCreated.emit({ document, warning: result.warning }),
        error: () => this.documentCreated.emit({
          document: result.document,
          warning: 'El documento se guardó, pero no se pudo adjuntar el PDF. Podés adjuntarlo después desde la lista.',
        }),
      });
  }

  onBackdropClick(): void {
    if (!this.loading) this.close.emit();
  }
}
