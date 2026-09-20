import unittest
from unittest.mock import call, patch

from publish import add_curseforge_retries, inventories, missing_targets, replace_existing, selected_targets


class PublicationTests(unittest.TestCase):
    def test_existing_files_are_skipped_independently_on_each_platform(self):
        manifest = {"targets": {"fabric": {"file": "fabric.jar"}, "forge": {"file": "forge.jar"}}}
        existing = {"github": {"fabric.jar"}, "modrinth": {"fabric.jar", "forge.jar"}, "curseforge": set()}
        self.assertEqual(
            missing_targets(manifest, existing),
            {"github": ["forge"], "modrinth": [], "curseforge": ["forge"]},
        )

    def test_curseforge_processing_file_is_not_uploaded_twice(self):
        manifest = {"targets": {"fabric": {"file": "fabric.jar"}}}
        existing = {"github": {"fabric.jar"}, "modrinth": {"fabric.jar"}, "curseforge": set()}
        self.assertEqual(missing_targets(manifest, existing)["curseforge"], [])

    def test_curseforge_missing_file_is_retried_without_completion_marker(self):
        manifest = {"targets": {"fabric": {"file": "fabric.jar"}}}
        existing = {"github": set(), "modrinth": {"fabric.jar"}, "curseforge": set()}
        self.assertEqual(missing_targets(manifest, existing)["curseforge"], ["fabric"])

    def test_complete_release_has_no_uploads(self):
        manifest = {"targets": {"fabric": {"file": "fabric.jar"}}}
        existing = {platform: {"fabric.jar"} for platform in ["github", "modrinth", "curseforge"]}
        self.assertTrue(all(not pending for pending in missing_targets(manifest, existing).values()))

    def test_duplicate_target_filenames_are_rejected(self):
        manifest = {"targets": {"first": {"file": "same.jar"}, "second": {"file": "same.jar"}}}
        with self.assertRaises(ValueError):
            missing_targets(manifest, {"github": set()})

    @patch.dict("os.environ", {"MODRINTH_TOKEN": "test", "GITHUB_TOKEN": "test"})
    @patch("publish.request")
    def test_inventory_includes_moderating_files_and_all_pages(self, request):
        request.side_effect = [
            [{"status": "unlisted", "files": [{"filename": "moderating.jar"}]}],
            {"data": [{"fileName": "first.jar", "status": 1}], "pagination": {"totalCount": 51}},
            {"data": [{"fileName": "last.jar", "status": 4}], "pagination": {"totalCount": 51}},
            {"id": 123, "draft": False},
            [{"name": "github.jar"}],
        ]
        existing, release = inventories({"modrinth": "project", "curseforge": "123"}, "owner/repo", "v1")
        self.assertEqual(existing["modrinth"], {"moderating.jar"})
        self.assertEqual(existing["curseforge"], {"first.jar", "last.jar"})
        self.assertEqual(existing["github"], {"github.jar"})
        self.assertEqual(release["id"], 123)

    @patch.dict("os.environ", {"MODRINTH_TOKEN": "test", "GITHUB_TOKEN": "test"})
    @patch("publish.request", side_effect=PermissionError("unauthorized"))
    def test_failed_inventory_does_not_become_an_empty_release(self, request):
        with self.assertRaises(PermissionError):
            inventories({"modrinth": "project", "curseforge": "123"}, "owner/repo", "v1")

    def test_unknown_replacement_target_is_rejected(self):
        with self.assertRaises(ValueError):
            selected_targets({"targets": {"fabric": {}}}, "forge")

    def test_curseforge_retry_adds_only_missing_files(self):
        manifest = {
            "targets": {
                "missing": {"file": "missing.jar"},
                "present": {"file": "present.jar"},
            },
        }
        existing = {"curseforge": {"present.jar"}}
        plan = {"curseforge": []}
        add_curseforge_retries(manifest, existing, plan, {"missing", "present"})
        self.assertEqual(plan["curseforge"], ["missing"])

    def test_curseforge_file_blocks_replacement_before_deletion(self):
        manifest = {
            "modrinth": "project",
            "targets": {"forge": {"file": "forge.jar"}},
        }
        existing = {"github": {"forge.jar"}, "modrinth": {"forge.jar"}, "curseforge": {"forge.jar"}}
        with self.assertRaises(RuntimeError), patch("publish.request") as request:
            replace_existing(manifest, "owner/repo", {"id": 1}, {"forge"}, existing)
        request.assert_not_called()

    @patch.dict("os.environ", {"MODRINTH_TOKEN": "modrinth", "GITHUB_TOKEN": "github"})
    @patch("publish.request")
    def test_replacement_deletes_matching_modrinth_version_and_github_asset(self, request):
        manifest = {
            "modrinth": "project",
            "targets": {"forge": {"file": "forge.jar"}},
        }
        existing = {"github": {"forge.jar"}, "modrinth": {"forge.jar"}, "curseforge": set()}
        request.side_effect = [
            [{"id": "version", "files": [{"filename": "forge.jar"}]}],
            None,
            [{"id": 2, "name": "forge.jar"}],
            None,
        ]
        replace_existing(manifest, "owner/repo", {"id": 1}, {"forge"}, existing)
        self.assertEqual(
            request.call_args_list,
            [
                call("https://api.modrinth.com/v2/project/project/version", "modrinth"),
                call("https://api.modrinth.com/v2/version/version", "modrinth", "DELETE"),
                call("https://api.github.com/repos/owner/repo/releases/1/assets?per_page=100&page=1", "Bearer github"),
                call("https://api.github.com/repos/owner/repo/releases/assets/2", "Bearer github", "DELETE"),
            ],
        )


if __name__ == "__main__":
    unittest.main()
