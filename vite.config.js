import basicSsl from '@vitejs/plugin-basic-ssl'
import { visualizer } from "rollup-plugin-visualizer";

/** @type {import('vitest/config').UserConfig} */
export default {
	plugins: [
		//basicSsl(),
		visualizer()
	],
	define: {
		APP_VERSION: JSON.stringify(process.env.npm_package_version),
	},
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
