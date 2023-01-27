/** @type {import('vitest/config').UserConfig} */
export default {
	test: {
		environment: "jsdom",
		testTimeout: 30000,
	},
	preview: {
		port: 5173,
	},
	clearScreen: false,
};
