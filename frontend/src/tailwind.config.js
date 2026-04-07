

export default {
  content: [
    './index.html',
    './**/*.{js,ts,jsx,tsx}',
  ],
  theme: {
    extend: {
      fontFamily: {
        sans: ['PP Neue Montreal', '-apple-system', 'BlinkMacSystemFont', 'Segoe UI', 'sans-serif'],
        mono: ['JetBrains Mono', 'monospace'],
        serif: ['Playfair Display', 'serif'],
      },
      colors: {
        primary: {
          dark: '#051A24',
        },
        dark: '#0D212C',
        muted: {
          dark: '#273C46',
          light: '#E0EBF0',
        },
        light: {
          text: '#F6FCFF',
        }
      },
      animation: {
        'marquee': 'marquee 30s linear infinite',
        'marquee-mobile': 'marquee 10s linear infinite',
        'fade-in-up': 'fadeInUp 0.8s ease-out forwards',
      },
      keyframes: {
        marquee: {
          '0%': { transform: 'translateX(0)' },
          '100%': { transform: 'translateX(-50%)' },
        },
        fadeInUp: {
          '0%': { opacity: '0', transform: 'translateY(30px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        }
      }
    },
  },
  plugins: [],
}

