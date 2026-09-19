import unittest
from unittest.mock import patch

from publish import inventories, missing_targets


class PublicationTests(unittest.TestCase):
    def test_existing_files_are_skipped_independently_on_each_platform(self):
        manifest = {"targets": {"fabric": {"file": "fabric.jar"}, "forge": {"file": "forge.jar"}}}
        existing = {"github": {"fabric.jar"}, "modrinth": {"fabric.jar", "forge.jar"}, "curseforge": set()}
        self.assertEqual(
            missing_targets(manifest, existing),
            {"github": ["forge"], "modrinth": [], "curseforge": ["fabric", "forge"]},
        )

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


if __name__ == "__main__":
    unittest.main()
