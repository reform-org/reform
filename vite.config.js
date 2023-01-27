/** @type {import('vitest/config').UserConfig} */
export default {
	test: {
		environment: "jsdom",
		testTimeout: 300000,
	},
	preview: {
		port: 5173,
	},
	clearScreen: false,
};
