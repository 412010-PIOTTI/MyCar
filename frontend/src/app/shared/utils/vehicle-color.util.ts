export interface VehicleColorTheme {
  bg: string;
  icon: string;
  text: string;
}

const DEFAULT_THEME: VehicleColorTheme = {
  bg: '#d9dee5',
  icon: '#b0bec5',
  text: '#546e7a',
};

/**
 * `color` is free text entered by the user (e.g. "Rojo", "Gris oscuro", "Azul metalizado"),
 * so matching is done by substring against common Spanish car color names rather than
 * an exact lookup. Longer/more specific keys are listed first to win over shorter overlaps.
 */
const COLOR_THEMES: Array<{ keys: string[]; theme: VehicleColorTheme }> = [
  { keys: ['blanco', 'white'], theme: { bg: '#eef1f4', icon: '#94a3b8', text: '#64748b' } },
  { keys: ['negro', 'black'], theme: { bg: '#3f4753', icon: '#cbd5e1', text: '#f1f5f9' } },
  { keys: ['plata', 'plateado', 'silver'], theme: { bg: '#e2e6ea', icon: '#8792a0', text: '#5b6570' } },
  { keys: ['gris', 'plomo', 'gray', 'grey'], theme: { bg: '#dbe1e8', icon: '#8792a0', text: '#55606b' } },
  { keys: ['bordo', 'bordó', 'bordeaux', 'vino', 'granate'], theme: { bg: '#ecd9de', icon: '#b3546a', text: '#8a2436' } },
  { keys: ['rojo', 'red'], theme: { bg: '#fde2e1', icon: '#e0665f', text: '#b3261e' } },
  { keys: ['naranja', 'orange'], theme: { bg: '#fde3cf', icon: '#e8934f', text: '#c4590d' } },
  { keys: ['amarillo', 'yellow'], theme: { bg: '#fdf3d0', icon: '#d1a638', text: '#a8790a' } },
  { keys: ['dorado', 'gold'], theme: { bg: '#f4ecd0', icon: '#c4a542', text: '#96741a' } },
  { keys: ['beige'], theme: { bg: '#f1ead9', icon: '#b89968', text: '#8a6d3b' } },
  { keys: ['marron', 'marrón', 'cafe', 'café', 'chocolate'], theme: { bg: '#ead9cd', icon: '#a37552', text: '#6b4226' } },
  { keys: ['verde', 'green'], theme: { bg: '#dcf3e3', icon: '#5cb583', text: '#1f8a4c' } },
  { keys: ['turquesa', 'turquoise'], theme: { bg: '#d7f2ef', icon: '#4fb3a9', text: '#157b70' } },
  { keys: ['celeste', 'sky'], theme: { bg: '#dbeefc', icon: '#5cabd6', text: '#1c7cbf' } },
  { keys: ['azul', 'blue'], theme: { bg: '#dce7fb', icon: '#6d8fd6', text: '#2554b8' } },
  { keys: ['violeta', 'purpura', 'púrpura', 'morado', 'purple'], theme: { bg: '#e8ddf5', icon: '#9b72c4', text: '#6b3fa0' } },
  { keys: ['rosa', 'fucsia', 'pink'], theme: { bg: '#fbdfe9', icon: '#dd6f9a', text: '#b3266b' } },
];

function normalize(value: string): string {
  return value
    .toLowerCase()
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '');
}

export function getVehicleColorTheme(color: string | null | undefined): VehicleColorTheme {
  if (!color) return DEFAULT_THEME;

  const normalized = normalize(color);
  const match = COLOR_THEMES.find(({ keys }) => keys.some((key) => normalized.includes(normalize(key))));
  return match?.theme ?? DEFAULT_THEME;
}
