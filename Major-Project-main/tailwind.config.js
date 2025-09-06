/** @type {import('tailwindcss').Config} */
module.exports = {
    content: [
        "./src/main/resources/templates/**/*.html",
        "./src/main/resources/static/**/*.css",
    ],
    theme: {
        extend: {
            colors: {
                'primary-neon': '#39FF14', // A vibrant neon green
                'secondary-neon': '#00FFFF', // Cyan
                'dark-bg': '#121212',
                'dark-card': '#1E1E1E',
            },
            fontFamily: {
                'sans': ['Inter', 'sans-serif'], // Use a clean, modern font
            },
        },
    },
    plugins: [],
}