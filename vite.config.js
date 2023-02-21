import basicSsl from '@vitejs/plugin-basic-ssl'

/** @type {import('vitest/config').UserConfig} */
export default {
	plugins: [
		basicSsl()
	],
	test: {
		environment: "jsdom",
		testTimeout: 300000,
		hookTimeout: 300000,
	},
	preview: {
		port: 5173,
	},
	clearScreen: false,
};
