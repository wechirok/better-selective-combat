import json
import os
import subprocess
import urllib.error
import urllib.request
from pathlib import Path


def request(url, token=None, method="GET"):
    headers = {"User-Agent": "Wechirok-release-publisher"}
    if token:
        headers["Authorization"] = token
    with urllib.request.urlopen(urllib.request.Request(url, headers=headers, method=method), timeout=60) as response:
        body = response.read()
        return json.loads(body) if body else None


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
    replacements = selected_targets(manifest, os.environ.get("REPLACE_TARGETS", ""))
    if replacements:
        replace_existing(manifest, repository, release, replacements, existing)
        existing, release = inventories(manifest, repository, tag)
    plan = missing_targets(manifest, existing)
    retries = selected_targets(manifest, os.environ.get("RETRY_CURSEFORGE_TARGETS", ""))
    add_curseforge_retries(manifest, existing, plan, retries)
    for platform, names in plan.items():
        print(f"{platform}: {len(names)} missing files", flush=True)
    selected = sorted(set().union(*plan.values()))
    if not selected:
        return
    plan_file = Path("build/publish-plan.json")
    plan_file.write_text(json.dumps(plan))
    command = [
        "./gradlew", "--no-daemon", "--console=plain", "publishMods",
        "-Ptargets=" + ",".join(selected),
        "-Ppublish_plan=" + str(plan_file.resolve()),
        "-Prelease_type=" + os.environ.get("RELEASE_TYPE", "release"),
    ]
    if plan["curseforge"]:
        command.append("--max-workers=1")
    subprocess.run(command, check=True)
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
    verify_publication(manifest, existing, plan)


def verify_publication(manifest, existing, plan):
    for platform, names in plan.items():
        missing = [name for name in names if manifest["targets"][name]["file"] not in existing[platform]]
        if missing:
            if platform == "curseforge":
                print(f"curseforge: upload accepted, public listing pending for {missing}", flush=True)
            else:
                raise RuntimeError(f"{platform}: publication not yet confirmed for {missing}")


def missing_targets(manifest, existing):
    filenames = [target["file"] for target in manifest["targets"].values()]
    if len(filenames) != len(set(filenames)):
        raise ValueError("Release targets contain duplicate filenames")
    missing = {
        platform: [name for name, target in manifest["targets"].items() if target["file"] not in files]
        for platform, files in existing.items()
    }
    missing["curseforge"] = [
        name for name in missing["curseforge"]
        if manifest["targets"][name]["file"] not in existing["github"]
        or manifest["targets"][name]["file"] not in existing["modrinth"]
    ]
    return missing


def add_curseforge_retries(manifest, existing, plan, retries):
    missing = {
        name for name in retries
        if manifest["targets"][name]["file"] not in existing["curseforge"]
    }
    plan["curseforge"] = sorted(set(plan["curseforge"]) | missing)


def selected_targets(manifest, value):
    selected = {name for name in value.split(",") if name}
    unknown = selected - manifest["targets"].keys()
    if unknown:
        raise ValueError(f"Unknown replacement targets: {sorted(unknown)}")
    return selected


def replace_existing(manifest, repository, release, replacements, existing):
    filenames = {manifest["targets"][name]["file"] for name in replacements}
    blocked = sorted(filenames & existing["curseforge"])
    if blocked:
        raise RuntimeError(f"curseforge: remove files before replacement: {blocked}")
    versions = request(
        f"https://api.modrinth.com/v2/project/{manifest['modrinth']}/version",
        os.environ["MODRINTH_TOKEN"],
    )
    for version in versions:
        if any(file["filename"] in filenames for file in version["files"]):
            request(
                f"https://api.modrinth.com/v2/version/{version['id']}",
                os.environ["MODRINTH_TOKEN"],
                "DELETE",
            )
    if release:
        page = 1
        while True:
            assets = request(
                f"https://api.github.com/repos/{repository}/releases/{release['id']}/assets?per_page=100&page={page}",
                f"Bearer {os.environ['GITHUB_TOKEN']}",
            )
            for asset in assets:
                if asset["name"] in filenames:
                    request(
                        f"https://api.github.com/repos/{repository}/releases/assets/{asset['id']}",
                        f"Bearer {os.environ['GITHUB_TOKEN']}",
                        "DELETE",
                    )
            if len(assets) < 100:
                break
            page += 1


if __name__ == "__main__":
    main()
