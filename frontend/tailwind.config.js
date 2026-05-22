/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      colors: {
        // ── MyCar Brand Palette ──────────────────────────────────
        primary: {
          DEFAULT: '#0D4D8B',   // Azul oscuro dominante (botones, nav activo, headings)
          light:   '#1A6AB5',   // Hover state
          dark:    '#083761',   // Sidebar background
        },
        secondary: {
          DEFAULT: '#2EA7E0',   // Azul celeste (acciones secundarias, links, iconos activos)
          light:   '#5BBFE8',   // Hover state
          dark:    '#1E8EC4',
        },
        success: {
          DEFAULT: '#6DBB4F',   // Verde (solo para estados de éxito, badges OK)
          light:   '#EBF7E4',   // Background de alert success
          dark:    '#4E9436',
        },
        danger: {
          DEFAULT: '#D64545',   // Rojo (errores, validaciones, alertas críticas)
          light:   '#FDEAEA',   // Background de alert danger
          dark:    '#B03030',
        },
        warning: {
          DEFAULT: '#E8A020',   // Naranja (advertencias, estados pendientes)
          light:   '#FEF6E4',   // Background de alert warning
          dark:    '#C07818',
        },
        info: {
          DEFAULT: '#2EA7E0',   // Usa secondary para info
          light:   '#E4F4FB',   // Background de alert info
        },
        // ── Neutrals & Surface ───────────────────────────────────
        sidebar: '#083761',     // Fondo del sidebar
        surface: {
          DEFAULT: '#F5F7FA',   // Background principal de la app
          card:    '#FFFFFF',   // Fondo de cards y paneles
          border:  '#D9E1E8',   // Bordes sutiles
        },
        // ── Text ─────────────────────────────────────────────────
        text: {
          primary:   '#1F2937', // Texto principal
          secondary: '#6B7280', // Texto secundario / labels
          muted:     '#9CA3AF', // Placeholders / hints
          inverse:   '#FFFFFF', // Texto sobre fondos oscuros
        },
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', '-apple-system', 'sans-serif'],
        mono: ['JetBrains Mono', 'Fira Code', 'monospace'],
      },
      fontSize: {
        'xs':   ['0.75rem',  { lineHeight: '1rem' }],
        'sm':   ['0.875rem', { lineHeight: '1.25rem' }],
        'base': ['1rem',     { lineHeight: '1.5rem' }],
        'lg':   ['1.125rem', { lineHeight: '1.75rem' }],
        'xl':   ['1.25rem',  { lineHeight: '1.75rem' }],
        '2xl':  ['1.5rem',   { lineHeight: '2rem' }],
        '3xl':  ['1.875rem', { lineHeight: '2.25rem' }],
      },
      borderRadius: {
        'sm':  '0.25rem',   // 4px  — inputs, badges
        'DEFAULT': '0.375rem', // 6px  — botones
        'md':  '0.5rem',    // 8px  — cards
        'lg':  '0.75rem',   // 12px — modales, paneles grandes
        'xl':  '1rem',      // 16px
        'full': '9999px',   // pills / avatars
      },
      boxShadow: {
        'card':  '0 1px 3px 0 rgb(0 0 0 / 0.06), 0 1px 2px -1px rgb(0 0 0 / 0.04)',
        'panel': '0 4px 16px 0 rgb(13 77 139 / 0.08)',
        'modal': '0 20px 60px 0 rgb(13 77 139 / 0.15)',
      },
      spacing: {
        'sidebar': '260px',   // Ancho del sidebar desktop
        'topbar':  '64px',    // Alto del topbar
      },
      transitionDuration: {
        DEFAULT: '150ms',
      },
    },
  },
  plugins: [
    require('@tailwindcss/forms'),
  ],
};
