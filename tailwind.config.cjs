/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./index.html",
    "./js/target/scala-3.2.1/webapp-fastopt/**/*.js",
  ],
  theme: {
    extend: {},
  },
  plugins: [require("daisyui")],
}
