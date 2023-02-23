import basicSsl from '@vitejs/plugin-basic-ssl'
import { visualizer } from "rollup-plugin-visualizer";

/** @type {import('vitest/config').UserConfig} */
export default {
	plugins: [
		//basicSsl(),
		visualizer()
	],
	test: {
		environment: "jsdom",
		testTimeout: 300000,
		hookTimeout: 300000,
	},
	preview: {
		port: 5173,
	},
	build: {
		sourcemap: true
	},
	clearScreen: false,
};
