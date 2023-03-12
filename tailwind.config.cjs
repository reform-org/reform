/** @type {import('tailwindcss').Config} */
module.exports = {
	darkMode: 'class',
	content: ["./index.html", "./js/target/webapp/**/*.js"],
	theme: {
		minHeight: {
			10: "2.5rem",
			9: "2.25rem",
			8: "2rem",
		},
		extend: {},
	},
	plugins: [require("daisyui")],
	daisyui: {
		themes: ["light", "dark"],
		darkTheme: false,
	}
};
