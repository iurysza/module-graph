#!/usr/bin/env python3
from __future__ import annotations

import argparse
import importlib.util
import unittest
from pathlib import Path

SCRIPT_PATH = Path(__file__).with_name("generate_tts.py")
SPEC = importlib.util.spec_from_file_location("generate_tts", SCRIPT_PATH)
assert SPEC and SPEC.loader
GENERATE_TTS = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(GENERATE_TTS)


def args(**overrides: object) -> argparse.Namespace:
    values = {
        "template": None,
        "voice": None,
        "profile": None,
        "scene": None,
        "notes": None,
        "speed": None,
    }
    values.update(overrides)
    return argparse.Namespace(**values)


class ResolveArgsTest(unittest.TestCase):
    def setUp(self) -> None:
        self.catalog = GENERATE_TTS.load_catalog()

    def test_uses_natural_tech_conference_by_default(self) -> None:
        voice, profile, scene, notes, speed = GENERATE_TTS.resolve_args(
            args(), self.catalog
        )

        self.assertEqual(self.catalog["defaultTemplate"], "natural-tech-conference")
        self.assertEqual(voice, "Algenib")
        self.assertIn("San Francisco technical conference", profile)
        self.assertIn("conference breakout room", scene)
        self.assertIn("low emotional range", notes)
        self.assertEqual(speed, 1.3225)

    def test_explicit_template_replaces_catalog_default(self) -> None:
        voice, _, _, _, speed = GENERATE_TTS.resolve_args(
            args(template="promo-hype"), self.catalog
        )

        self.assertEqual(voice, "Fenrir")
        self.assertEqual(speed, 1.15)

    def test_explicit_flags_override_catalog_default(self) -> None:
        voice, profile, scene, notes, speed = GENERATE_TTS.resolve_args(
            args(
                voice="Kore",
                profile="Custom profile",
                scene="Custom scene",
                notes="Custom notes",
                speed=1.1,
            ),
            self.catalog,
        )

        self.assertEqual(
            (voice, profile, scene, notes, speed),
            ("Kore", "Custom profile", "Custom scene", "Custom notes", 1.1),
        )


if __name__ == "__main__":
    unittest.main()
