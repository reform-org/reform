/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./index.html",
    "./webapp/target/scala-3.2.1/webapp-fastopt/**/*.js",
  ],
  theme: {
    extend: {},
  },
  plugins: [require("daisyui")],
}
