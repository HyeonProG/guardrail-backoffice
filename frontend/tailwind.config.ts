import type { Config } from 'tailwindcss';

export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        border: '#d7dde5',
        surface: '#f6f8fb',
        ink: '#17202a'
      }
    }
  },
  plugins: []
} satisfies Config;
