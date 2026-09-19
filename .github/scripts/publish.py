import json
import os
import subprocess
import urllib.error
import urllib.request
from pathlib import Path


def request(url, token=None):
    headers = {"User-Agent": "Wechirok-release-publisher"}
    if token:
        headers["Authorization"] = token
    with urllib.request.urlopen(urllib.request.Request(url, headers=headers), timeout=60) as response:
        return json.load(response)


def inventories(manifest, repository, tag):
    versions = request(
        f"https://api.modrinth.com/v2/project/{manifest['modrinth']}/version",
        os.environ["MODRINTH_TOKEN"],
    )
    modrinth = {file["filename"] for version in versions for file in version["files"]}
    curseforge = set()
    page = 0
    while True:
        response = request(
            f"https://www.curseforge.com/api/v1/mods/{manifest['curseforge']}/files"
            f"?pageIndex={page}&pageSize=50&sort=dateCreated&sortDescending=true"
        )
        curseforge.update(file["fileName"] for file in response["data"])
        if (page + 1) * 50 >= response["pagination"]["totalCount"]:
            break
        page += 1
    try:
        release = request(
            f"https://api.github.com/repos/{repository}/releases/tags/{tag}",
            f"Bearer {os.environ['GITHUB_TOKEN']}",
        )
    except urllib.error.HTTPError as error:
        if error.code != 404:
            raise
        release = None
    github = set()
    if release:
        page = 1
        while True:
            assets = request(
                f"https://api.github.com/repos/{repository}/releases/{release['id']}/assets?per_page=100&page={page}",
                f"Bearer {os.environ['GITHUB_TOKEN']}",
            )
            github.update(asset["name"] for asset in assets)
            if len(assets) < 100:
                break
            page += 1
    return {"modrinth": modrinth, "curseforge": curseforge, "github": github}, release


def main():
    gradle = ["./gradlew", "--no-daemon", "--console=plain"]
    if os.environ.get("TARGETS"):
        gradle.append("-Ptargets=" + os.environ["TARGETS"])
    subprocess.run(gradle + ["writeReleaseManifest"], check=True)
    manifest = json.loads(Path("build/release-manifest.json").read_text())
    repository = os.environ["GITHUB_REPOSITORY"]
    tag = "v" + manifest["version"]
    existing, release = inventories(manifest, repository, tag)
    plan = missing_targets(manifest, existing)
    for platform, names in plan.items():
        print(f"{platform}: {len(names)} missing files", flush=True)
    selected = sorted(set().union(*plan.values()))
    if not selected:
        return
    plan_file = Path("build/publish-plan.json")
    plan_file.write_text(json.dumps(plan))
    subprocess.run([
        "./gradlew", "--no-daemon", "--console=plain", "publishMods",
        "-Ptargets=" + ",".join(selected),
        "-Ppublish_plan=" + str(plan_file.resolve()),
        "-Prelease_type=" + os.environ.get("RELEASE_TYPE", "release"),
    ], check=True)
    if plan["github"]:
        if release is None:
            notes = Path("build/release-notes.md")
            notes.write_text(os.environ["CHANGELOG"])
            command = [
                "gh", "release", "create", tag, "--repo", repository, "--title", tag,
                "--target", os.environ["GITHUB_SHA"], "--notes-file", str(notes), "--draft",
            ]
            if os.environ.get("RELEASE_TYPE", "release") != "release":
                command.append("--prerelease")
            subprocess.run(command, check=True)
        for name in plan["github"]:
            artifact = Path("build/release") / manifest["targets"][name]["file"]
            subprocess.run(["gh", "release", "upload", tag, str(artifact), "--repo", repository], check=True)
        if release is None:
            subprocess.run(["gh", "release", "edit", tag, "--repo", repository, "--draft=false"], check=True)
    existing, _ = inventories(manifest, repository, tag)
    for platform, names in plan.items():
        missing = [name for name in names if manifest["targets"][name]["file"] not in existing[platform]]
        if missing:
            raise RuntimeError(f"{platform}: publication not yet confirmed for {missing}")


def missing_targets(manifest, existing):
    filenames = [target["file"] for target in manifest["targets"].values()]
    if len(filenames) != len(set(filenames)):
        raise ValueError("Release targets contain duplicate filenames")
    return {
        platform: [name for name, target in manifest["targets"].items() if target["file"] not in files]
        for platform, files in existing.items()
    }


if __name__ == "__main__":
    main()
