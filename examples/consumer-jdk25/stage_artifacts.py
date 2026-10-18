"""Stage generated publication files in a disposable repository for smoke tests."""

import hashlib
import json
import shutil
import xml.etree.ElementTree as ET
from pathlib import Path


PROJECT_ROOT = Path(__file__).resolve().parents[2]
REPOSITORY_ROOT = PROJECT_ROOT / "build" / "consumer-jdk25" / "repository"
MODULES = (
    "commons-message",
    "commons-object-api",
    "commons-object-impl",
    "commons-reflection",
    "commons-spi",
    "commons-thread",
)


def validate_coordinates(module_name: str, publication_root: Path, metadata: dict) -> None:
    component = metadata["component"]
    if component["module"] != module_name:
        raise ValueError(f"Metadados incompatíveis com {module_name}")

    pom = ET.parse(publication_root / "pom-default.xml").getroot()
    namespace = {"m": "http://maven.apache.org/POM/4.0.0"}
    for element, coordinate in (
        ("groupId", "group"),
        ("artifactId", "module"),
        ("version", "version"),
    ):
        if pom.findtext(f"m:{element}", namespaces=namespace) != component[coordinate]:
            raise ValueError(f"POM incompatível com {module_name}: {element}")


def validate_artifacts(module_root: Path, metadata: dict) -> set[str]:
    artifact_names = {
        artifact["name"]
        for variant in metadata["variants"]
        for artifact in variant.get("files", [])
    }
    component = metadata["component"]
    primary_artifact = f"{component['module']}-{component['version']}.jar"
    if primary_artifact not in artifact_names:
        raise ValueError(f"JAR principal ausente dos metadados: {primary_artifact}")
    for variant in metadata["variants"]:
        for artifact in variant.get("files", []):
            artifact_path = module_root / "build" / "libs" / artifact["name"]
            artifact_bytes = artifact_path.read_bytes()
            if len(artifact_bytes) != artifact["size"]:
                raise ValueError(f"Tamanho incompatível: {artifact_path}")
            if hashlib.sha256(artifact_bytes).hexdigest() != artifact["sha256"]:
                raise ValueError(f"SHA-256 incompatível: {artifact_path}")
    return artifact_names


def copy_publication(
    module_root: Path, publication_root: Path, metadata: dict, artifact_names: set[str]
) -> None:
    component = metadata["component"]
    module_name = component["module"]
    version = component["version"]
    relative_path = Path(component["group"].replace(".", "/")) / module_name / version

    for source in ("module", "pom"):
        destination = REPOSITORY_ROOT / source / relative_path
        destination.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(
            publication_root / "pom-default.xml",
            destination / f"{module_name}-{version}.pom",
        )
        if source == "module":
            shutil.copyfile(
                publication_root / "module.json",
                destination / f"{module_name}-{version}.module",
            )
        staged_artifacts = artifact_names if source == "module" else {
            f"{module_name}-{version}.jar"
        }
        for artifact_name in staged_artifacts:
            artifact_path = module_root / "build" / "libs" / artifact_name
            shutil.copyfile(artifact_path, destination / artifact_name)


def stage_module(module_name: str) -> None:
    module_root = PROJECT_ROOT / module_name
    publication_root = module_root / "build" / "publications" / "mavenJava"
    metadata = json.loads((publication_root / "module.json").read_text())
    validate_coordinates(module_name, publication_root, metadata)
    artifact_names = validate_artifacts(module_root, metadata)
    copy_publication(module_root, publication_root, metadata, artifact_names)


if __name__ == "__main__":
    for module in MODULES:
        stage_module(module)
    print(f"Artefatos de {len(MODULES)} módulos copiados para {REPOSITORY_ROOT}")
