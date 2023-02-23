import { VitePWA } from 'vite-plugin-pwa'

/** @type {import('vitest/config').UserConfig} */
export default {
	plugins: [
		VitePWA({ registerType: 'autoUpdate' })
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
