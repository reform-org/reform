/** @type {import('tailwindcss').Config} */
module.exports = {
	content: ["./index.html", "./js/target/scala-*/webapp-fastopt/**/*.js"],
	theme: {
		extend: {},
	},
	plugins: [require("daisyui")],
};
