#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$project_root"

python3 examples/consumer-jdk25/stage_artifacts.py
./gradlew -p examples/consumer-jdk25 writeApiScanClasspath --quiet
classpath="$(cat examples/consumer-jdk25/build/api-scan-classpath.txt)"

jar_count=0
for jar in commons-*/build/libs/*.jar; do
    case "$jar" in
        *-sources.jar|*-javadoc.jar|*-test-fixtures.jar) continue ;;
    esac
    for scanner in jdeps jdeprscan; do
        if [[ "$scanner" == "jdeps" ]]; then
            command=("$JAVA_HOME/bin/jdeps" --jdk-internals --multi-release 25 "$jar")
        else
            command=("$JAVA_HOME/bin/jdeprscan" --release 25 --class-path "$classpath" "$jar")
        fi
        if ! output="$("${command[@]}" 2>&1)"; then
            printf '%s: %s\n%s\n' "$scanner" "$jar" "$output" >&2
            exit 1
        fi
        if [[ "$scanner" == "jdeprscan" ]]; then
            output="${output#Jar file $jar:}"
        fi
        if [[ -n "$output" ]]; then
            printf '%s: %s\n%s\n' "$scanner" "$jar" "$output" >&2
            exit 1
        fi
    done
    jar_count=$((jar_count + 1))
done

if [[ "$jar_count" -ne 6 ]]; then
    printf 'Esperados 6 JARs de produção, encontrados %s\n' "$jar_count" >&2
    exit 1
fi
printf 'Análise de APIs JDK 25 aprovada para %s JARs de produção\n' "$jar_count"
