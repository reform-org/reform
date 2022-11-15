import { webDev } from "@fun-stack/fun-pack";

export default webDev({
  indexHtml: "src/main/html/index.html",
  // assetsDir: "assets",
  extraStaticDirs: [
    "src" // for source maps
  ]
});
