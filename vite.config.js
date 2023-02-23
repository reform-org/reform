import { VitePWA } from 'vite-plugin-pwa'

/** @type {import('vitest/config').UserConfig} */
export default {
	plugins: [
		VitePWA({
			devOptions: {
				enabled: true
			},
			workbox: {
				maximumFileSizeToCacheInBytes: 10 * 1000 * 1000
			},
			includeAssets: ['favicon.ico', 'apple-touch-icon.png', 'safari-pinned-tab.svg', 'favicon-32x32.png', 'favicon-16x16.png'],
			manifest: {
				"name": "ReForm",
				"short_name": "ReForm",
				"icons": [
					{
						"src": "/android-chrome-192x192.png",
						"sizes": "192x192",
						"type": "image/png"
					},
					{
						"src": "/android-chrome-512x512.png",
						"sizes": "512x512",
						"type": "image/png"
					},
					{
						src: 'pwa-512x512.png',
						sizes: '512x512',
						type: 'image/png',
						purpose: 'any maskable'
					}
				],
				"theme_color": "#ffffff",
				"background_color": "#ffffff",
				"display": "standalone"
			}
		})
	],
	build: {
		sourcemap: true,
	},
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
