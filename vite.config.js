import {VitePWA} from 'vite-plugin-pwa'
import scalaJSPlugin from "@scala-js/vite-plugin-scalajs";

import basicSsl from '@vitejs/plugin-basic-ssl'
import {visualizer} from "rollup-plugin-visualizer";

/** @type {import('vitest/config').UserConfig} */


export default {
    plugins: [
        scalaJSPlugin({
            // path to the directory containing the sbt build
            // default: '.'
            cwd: '.',

            // sbt project ID from within the sbt build to get fast/fullLinkJS from
            // default: the root project of the sbt build
            // projectID: 'webapp',

            // URI prefix of imports that this plugin catches (without the trailing ':')
            // default: 'scalajs' (so the plugin recognizes URIs starting with 'scalajs:')
            uriPrefix: 'scalajs',
        }),
        VitePWA({
            registerType: 'autoUpdate',
            injectRegister: 'inline',
            workbox: {
                maximumFileSizeToCacheInBytes: 10 * 1000 * 1000,
                navigateFallbackDenylist: [
                    // TODO FIXME configure based on env variables
                    /^\/discovery-server-websocket/,
                    /^\/always-online-peer/,
                    /^\/api/,
                    /.*\.js\.map/,
                ],
                clientsClaim: true,
                skipWaiting: true,
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
                        src: '/android-chrome-512x512.png',
                        sizes: '512x512',
                        type: 'image/png',
                        purpose: 'any maskable'
                    }
                ],
                "theme_color": "#ffffff",
                "background_color": "#ffffff",
                "display": "standalone"
            }
        }),
        //basicSsl()
        visualizer()
    ],
    root: './webapp/js/src/main/js',
    envDir: process.cwd(),
    build: {
        sourcemap: true,
    },
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
    clearScreen: false,
}