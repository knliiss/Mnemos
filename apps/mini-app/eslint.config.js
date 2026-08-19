import js from "@eslint/js"
import globals from "globals"
import reactHooks from "eslint-plugin-react-hooks"
import reactRefresh from "eslint-plugin-react-refresh"
import tseslint from "typescript-eslint"

const fastRefreshMixedExportFiles = [
  "src/app/providers/AppProvider.tsx",
  "src/product-ui/components/ErrorNotifications.tsx",
  "src/product-ui/components/PersonalizationPreview.tsx",
  "src/product-ui/data/ProductDataProvider.tsx",
]

export default tseslint.config(
  { ignores: ["dist"] },
  {
    extends: [js.configs.recommended, ...tseslint.configs.recommended],
    files: ["**/*.{ts,tsx}"],
    languageOptions: {
      ecmaVersion: 2020,
      globals: globals.browser,
    },
    plugins: {
      "react-hooks": reactHooks,
      "react-refresh": reactRefresh,
    },
    rules: {
      ...reactHooks.configs.recommended.rules,
      "react-refresh/only-export-components": ["warn", { allowConstantExport: true }],
    },
  },
  {
    files: fastRefreshMixedExportFiles,
    rules: {
      "react-refresh/only-export-components": "off",
    },
  },
)
