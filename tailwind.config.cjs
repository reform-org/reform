/** @type {import('tailwindcss').Config} */
module.exports = {
	content: ["./index.html", "./js/target/scala-*/webapp-fastopt/**/*.js"],
	theme: {
		minHeight: {
			10: "2.5rem",
			9: "2.25rem",
		},
		extend: {},
	},
	plugins: [require("daisyui")],
};
