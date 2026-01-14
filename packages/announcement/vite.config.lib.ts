import { svelte } from "@sveltejs/vite-plugin-svelte";
import { minify } from "terser";
import { fileURLToPath } from "url";
import { copyFileSync, mkdirSync, existsSync } from "fs";
import { join } from "path";
import { defineConfig, type Plugin } from "vite";

const minifyBundle = (): Plugin => ({
  name: "minify-bundle",
  async generateBundle(_, bundle) {
    for (const asset of Object.values(bundle)) {
      if (asset.type === "chunk") {
        const code = (await minify(asset.code, { sourceMap: false })).code;
        if (code) {
          asset.code = code;
        }
      }
    }
  },
});

function copyToStatic(): Plugin {
  return {
    name: "copy-to-static",
    closeBundle() {
      const distDir = join(process.cwd(), "dist");
      const staticDir = fileURLToPath(new URL("../../src/main/resources/static", import.meta.url));

      // 确保 static 目录存在
      if (!existsSync(staticDir)) {
        mkdirSync(staticDir, { recursive: true });
      }

      // 复制 JS 文件
      const jsSrc = join(distDir, "announcement.umd.cjs");
      const jsDest = join(staticDir, "announcement.umd.cjs");
      if (existsSync(jsSrc)) {
        copyFileSync(jsSrc, jsDest);
        console.log(`✓ Copied announcement.umd.cjs to ${staticDir}`);
      }

      // 复制 CSS 文件
      const cssSrc = join(distDir, "announcement.css");
      const cssDest = join(staticDir, "announcement.css");
      if (existsSync(cssSrc)) {
        copyFileSync(cssSrc, cssDest);
        console.log(`✓ Copied announcement.css to ${staticDir}`);
      }
    },
  };
}

export default defineConfig({
  experimental: {
    enableNativePlugin: true,
  },
  plugins: [
    svelte(),
    minifyBundle(),
    copyToStatic(),
  ],
  build: {
    lib: {
      entry: "src/index.ts",
      name: "Announcement",
      fileName: "announcement",
      formats: ["es", "umd"],
    },
    rollupOptions: {
      output: {
        extend: true,
        assetFileNames: "announcement.[ext]",
      },
    },
  },
});
