// Regenerates the OpenSpec tool files from .ignite/openspec-recipe.json, with no user or
// runner config leaking in. `--check` also fails when the result differs from what is committed.
import { spawnSync } from "node:child_process";
import { mkdirSync, mkdtempSync, readFileSync, rmSync, writeFileSync } from "node:fs";
import { tmpdir } from "node:os";
import { dirname, join, resolve } from "node:path";
import { fileURLToPath } from "node:url";

export const TOOL_PATHS = [
  ".claude/skills",
  ".claude/commands",
  ".agents/skills",
  ".github/skills",
  ".github/prompts/opsx-*",
  "openspec/config.yaml",
];

// Only what a child process needs to find node/npx and a temp dir. HOME and every XDG/APPDATA
// variable are deliberately absent so the caller's OpenSpec profile cannot reach the run.
const PASSTHROUGH = ["PATH", "Path", "PATHEXT", "SystemRoot", "SYSTEMROOT", "ComSpec", "TEMP", "TMP", "TMPDIR", "LANG", "CI"];

export function buildEnv(home, base = process.env) {
  const env = {};
  for (const key of PASSTHROUGH) {
    if (base[key] !== undefined) env[key] = base[key];
  }
  return {
    ...env,
    HOME: home,
    USERPROFILE: home,
    XDG_CONFIG_HOME: join(home, ".config"),
    XDG_DATA_HOME: join(home, ".local", "share"),
    APPDATA: join(home, "AppData", "Roaming"),
    LOCALAPPDATA: join(home, "AppData", "Local"),
    OPENSPEC_TELEMETRY: "0",
  };
}

export function buildCommand(recipe) {
  const cmd = ["npx", "-y", `@fission-ai/openspec@${recipe.openspecVersion}`, "init", "--tools", recipe.tools.join(",")];
  if (!recipe.copilotCloud) cmd.push("--no-copilot-cloud");
  cmd.push("--no-animation", ".");
  return cmd;
}

export function globalConfig(recipe) {
  return { profile: recipe.profile, delivery: recipe.delivery, workflows: recipe.workflows };
}

export function driftMessage(paths) {
  return [
    "OpenSpec tool files changed:",
    ...paths.map((p) => `  ${p}`),
    "Students: revert these files with `git checkout -- <paths>`. Never run `openspec init` or `openspec update` in a team repo.",
    "Mentors: regenerate with `node .ignite/openspec-regen.mjs` (recipe: .ignite/openspec-recipe.json) and commit the result.",
  ].join("\n");
}

export function parsePorcelain(output) {
  return output
    .split("\n")
    .filter((line) => line.length > 3)
    .map((line) => line.slice(3).replace(/^"|"$/g, ""));
}

function regenerate(root, recipe) {
  const home = mkdtempSync(join(tmpdir(), "openspec-regen-"));
  try {
    const env = buildEnv(home);
    const body = JSON.stringify(globalConfig(recipe));
    // OpenSpec reads $XDG_CONFIG_HOME/openspec on macOS/Linux and %APPDATA%/openspec on Windows.
    for (const dir of [env.XDG_CONFIG_HOME, env.APPDATA]) {
      mkdirSync(join(dir, "openspec"), { recursive: true });
      writeFileSync(join(dir, "openspec", "config.json"), body);
    }
    const [bin, ...args] = buildCommand(recipe);
    const result = spawnSync(bin, args, { cwd: root, env, stdio: "inherit", shell: process.platform === "win32" });
    return result.status ?? 1;
  } finally {
    rmSync(home, { recursive: true, force: true });
  }
}

function drift(root) {
  const result = spawnSync("git", ["status", "--porcelain", "--untracked-files=all", "--", ...TOOL_PATHS], {
    cwd: root,
    encoding: "utf8",
  });
  if (result.status !== 0) {
    console.error(`git status failed: ${result.stderr}`);
    return null;
  }
  return parsePorcelain(result.stdout);
}

function main() {
  const root = resolve(dirname(fileURLToPath(import.meta.url)), "..");
  const recipe = JSON.parse(readFileSync(join(root, ".ignite", "openspec-recipe.json"), "utf8"));
  const status = regenerate(root, recipe);
  if (status !== 0) {
    console.error(`openspec exited with status ${status}; tool files were not regenerated.`);
    process.exit(2);
  }
  if (!process.argv.includes("--check")) return;
  const changed = drift(root);
  if (changed === null) process.exit(2);
  if (changed.length > 0) {
    console.error(driftMessage(changed));
    process.exit(1);
  }
}

if (process.argv[1] && resolve(process.argv[1]) === fileURLToPath(import.meta.url)) main();
