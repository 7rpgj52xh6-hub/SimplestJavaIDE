import { defineConfig } from "vite";

// base "./" keeps asset URLs relative so the built site works from any
// sub-path (e.g. GitHub/GitLab Pages project pages), not just the domain root.
export default defineConfig({
  base: "./",
  build: {
    target: "es2022",
    outDir: "dist",
  },
});
