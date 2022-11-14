const {webDev} = require("@fun-stack/fun-pack");

module.exports = webDev({
  indexHtml: "src/main/html/index.html",
  // assetsDir: "assets",
  extraStaticDirs: [
    "src" // for source maps
  ]
});
