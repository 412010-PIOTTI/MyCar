import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

interface FaqItem {
  question: string;
  answer: string;
}

interface FaqSection {
  title: string;
  items: FaqItem[];
}

@Component({
  selector: 'app-faq',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './faq.component.html',
})
export class FaqComponent {
  openKey: string | null = null;

  readonly sections: FaqSection[] = [
    {
      title: 'Vehículos',
      items: [
        {
          question: '¿Cuántos vehículos puedo registrar?',
          answer: 'No hay límite. Podés agregar todos los vehículos que quieras desde "Mis Vehículos" y gestionar cada uno por separado.',
        },
        {
          question: '¿Por qué el kilometraje actual no se puede editar libremente?',
          answer: 'El kilometraje se actualiza automáticamente al registrar un gasto o mantenimiento con un valor mayor al actual, para que el historial siempre sea consistente. Nunca retrocede.',
        },
      ],
    },
    {
      title: 'Gastos y mantenimiento',
      items: [
        {
          question: '¿Cómo registro un gasto o un service?',
          answer: 'Entrá a "Gastos" o "Mantenimiento", elegí el vehículo y usá el botón "+ Agregar". Vas a poder cargar categoría, monto o descripción, fecha y kilometraje.',
        },
        {
          question: '¿Puedo ver un resumen consolidado de todos mis vehículos?',
          answer: 'Sí. Si tenés más de un vehículo, elegí "Todos los vehículos" en el selector de la parte superior de cada sección para ver los datos combinados.',
        },
      ],
    },
    {
      title: 'Documentos y alertas',
      items: [
        {
          question: '¿Qué documentos puedo cargar?',
          answer: 'Cédula verde, cédula azul, ITV/VTV, seguro, licencia de conducir, patente y otros. Podés adjuntar el PDF correspondiente y la fecha de vencimiento para que MyCar controle su estado.',
        },
        {
          question: '¿Cómo funcionan las alertas de vencimiento?',
          answer: 'Se generan automáticamente a partir de las fechas de vencimiento de tus documentos o de los próximos services, con aviso previo configurable. Las ves en la sección "Alertas" y también recibís un email.',
        },
      ],
    },
    {
      title: 'Transferencia de titularidad',
      items: [
        {
          question: '¿Cómo transfiero un vehículo a otra persona?',
          answer: 'Desde "Transferencias" generás un código QR de un solo uso, válido por 48 horas. La persona que lo escanea puede ver el historial del vehículo y confirmar la transferencia.',
        },
        {
          question: 'Si vendo un vehículo, ¿pierdo su historial?',
          answer: 'No. El historial de mantenimiento y el registro de la transferencia quedan guardados como respaldo, aunque el vehículo ya no aparezca en tu cuenta.',
        },
      ],
    },
    {
      title: 'Tu cuenta y tus datos',
      items: [
        {
          question: '¿Dónde veo los Términos y Condiciones?',
          answer: 'Podés leerlos en cualquier momento desde el pie de página de inicio de sesión/registro, o desde esta sección.',
        },
        {
          question: '¿Puedo eliminar mis datos personales sin dejar de usar la app?',
          answer: 'Sí. Desde "Configuración → Zona de Peligro" tenés dos opciones: desactivar tu cuenta (tus datos quedan guardados) o eliminar permanentemente tus datos personales (vehículos, gastos, mantenimientos, documentos y alertas).',
        },
        {
          question: '¿Cómo activo la verificación en dos pasos?',
          answer: 'En "Configuración → Seguridad" podés activarla; a partir de ese momento cada inicio de sesión va a pedir además un código enviado a tu email.',
        },
      ],
    },
  ];

  toggle(key: string): void {
    this.openKey = this.openKey === key ? null : key;
  }

  isOpen(key: string): boolean {
    return this.openKey === key;
  }
}
