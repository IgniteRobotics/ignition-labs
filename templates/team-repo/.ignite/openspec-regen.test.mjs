import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import { tmpdir } from "node:os";
import { dirname, join } from "node:path";
import test from "node:test";
import { fileURLToPath } from "node:url";

import { buildCommand, buildEnv, driftMessage } from "./openspec-regen.mjs";

const here = dirname(fileURLToPath(import.meta.url));
const recipe = JSON.parse(readFileSync(join(here, "openspec-recipe.json"), "utf8"));

test("regen: builds an isolated env", () => {
  const saved = { HOME: process.env.HOME, XDG: process.env.XDG_CONFIG_HOME };
  process.env.HOME = "/home/student-with-core-profile";
  process.env.XDG_CONFIG_HOME = "/home/student-with-core-profile/.config";
  process.env.OPENSPEC_TELEMETRY = "1";
  try {
    const home = join(tmpdir(), "regen-home");
    const env = buildEnv(home);
    assert.equal(env.HOME, home);
    assert.equal(env.USERPROFILE, home);
    assert.equal(env.XDG_CONFIG_HOME, join(home, ".config"));
    assert.equal(env.APPDATA, join(home, "AppData", "Roaming"));
    assert.equal(env.OPENSPEC_TELEMETRY, "0");
    for (const value of Object.values(env)) {
      assert.ok(!String(value).includes("student-with-core-profile"), `leaked: ${value}`);
    }
  } finally {
    process.env.HOME = saved.HOME;
    if (saved.XDG === undefined) delete process.env.XDG_CONFIG_HOME;
    else process.env.XDG_CONFIG_HOME = saved.XDG;
    delete process.env.OPENSPEC_TELEMETRY;
  }
});

test("regen: command uses pinned version and recipe tools", () => {
  assert.deepEqual(buildCommand(recipe), [
    "npx",
    "-y",
    "@fission-ai/openspec@1.10.0",
    "init",
    "--tools",
    "claude,codex,github-copilot",
    "--no-copilot-cloud",
    "--no-animation",
    ".",
  ]);
  const withCloud = buildCommand({ ...recipe, copilotCloud: true });
  assert.ok(!withCloud.includes("--no-copilot-cloud"));
  assert.ok(buildCommand({ ...recipe, openspecVersion: "2.0.0" }).includes("@fission-ai/openspec@2.0.0"));
});

test("regen: drift message names files and both audiences", () => {
  const msg = driftMessage([".claude/skills/openspec-explore/SKILL.md", "openspec/config.yaml"]);
  assert.equal(
    msg,
    [
      "OpenSpec tool files changed:",
      "  .claude/skills/openspec-explore/SKILL.md",
      "  openspec/config.yaml",
      "Students: revert these files with `git checkout -- <paths>`. Never run `openspec init` or `openspec update` in a team repo.",
      "Mentors: regenerate with `node .ignite/openspec-regen.mjs` (recipe: .ignite/openspec-recipe.json) and commit the result.",
    ].join("\n"),
  );
});
